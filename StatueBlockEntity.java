package com.achilles.block;

import com.achilles.data.PointsData;
import com.achilles.data.StatueRegistry;
import com.achilles.data.StatueStats;
import com.achilles.network.NetworkHandler;
import com.achilles.network.OpenStatuePacket;
import com.achilles.registry.ModBlockEntities;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Cerebro de la estatua: propietario, niveles, jugadores con permiso, y la zona:
 * repele/elimina mobs hostiles, expulsa a jugadores sin permiso y cura a los que están dentro.
 */
public class StatueBlockEntity extends BlockEntity {
    private UUID owner;
    private String ownerName = "";
    private int radiusLevel;
    private int healLevel;
    private final Map<UUID, String> trusted = new LinkedHashMap<>();
    private int ticks;

    public StatueBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STATUE.get(), pos, state);
    }

    // ------------------------------------------------------------ ciclo de vida

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level != null && !this.level.isClientSide) {
            StatueRegistry.add(this);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        StatueRegistry.remove(this);
    }

    // ------------------------------------------------------------ datos

    public boolean hasOwner() {
        return owner != null;
    }

    public boolean isOwner(Player player) {
        return owner != null && owner.equals(player.getUUID());
    }

    public double getRadius() {
        return StatueStats.radius(radiusLevel);
    }

    private Vec3 center() {
        return Vec3.atCenterOf(this.worldPosition).add(0.0D, 0.5D, 0.0D);
    }

    public boolean contains(Vec3 pos) {
        double r = getRadius();
        return center().distanceToSqr(pos) <= r * r;
    }

    /** Sin propietario: todos. Con propietario: él, los de la lista y administradores en creativo. */
    public boolean isAllowed(Player player) {
        if (owner == null) return true;
        if (owner.equals(player.getUUID()) || trusted.containsKey(player.getUUID())) return true;
        return player.isCreative() && player.hasPermissions(2);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (owner != null) tag.putUUID("Owner", owner);
        tag.putString("OwnerName", ownerName);
        tag.putInt("RadiusLevel", radiusLevel);
        tag.putInt("HealLevel", healLevel);
        ListTag list = new ListTag();
        for (Map.Entry<UUID, String> e : trusted.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Id", e.getKey());
            entry.putString("Name", e.getValue());
            list.add(entry);
        }
        tag.put("Trusted", list);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        ownerName = tag.getString("OwnerName");
        radiusLevel = Mth.clamp(tag.getInt("RadiusLevel"), 0, StatueStats.MAX_LEVEL);
        healLevel = Mth.clamp(tag.getInt("HealLevel"), 0, StatueStats.MAX_LEVEL);
        trusted.clear();
        ListTag list = tag.getList("Trusted", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            trusted.put(entry.getUUID("Id"), entry.getString("Name"));
        }
    }

    // ------------------------------------------------------------ interacción

    public void interact(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        if (owner == null) {
            owner = player.getUUID();
            ownerName = player.getGameProfile().getName();
            PointsData.get(level.getServer()).addPoint(owner);
            setChanged();
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, worldPosition.getX() + 0.5D, worldPosition.getY() + 1.5D,
                    worldPosition.getZ() + 0.5D, 40, 0.6D, 1.0D, 0.6D, 0.3D);
            level.playSound(null, worldPosition, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.BLOCKS, 1.0F, 1.0F);
            player.displayClientMessage(Component.translatable("message.achilles.claimed"), false);
            sendOpen(player);
        } else if (isOwner(player)) {
            sendOpen(player);
        } else if (trusted.containsKey(player.getUUID())) {
            player.displayClientMessage(Component.translatable("message.achilles.trusted", ownerName), true);
        } else {
            player.displayClientMessage(Component.translatable("message.achilles.owned_by", ownerName), true);
        }
    }

    public void sendOpen(ServerPlayer player) {
        if (owner == null) return;
        int points = PointsData.get(player.server).available(owner);
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new OpenStatuePacket(worldPosition, ownerName, radiusLevel, healLevel, points,
                        new ArrayList<>(trusted.values())));
    }

    /** type 0 = radio, 1 = curación. */
    public void upgrade(ServerPlayer player, int type, int amount) {
        if (!isOwner(player)) return;
        PointsData data = PointsData.get(player.server);
        int current = type == 0 ? radiusLevel : healLevel;
        int spend = Math.min(Math.min(amount, StatueStats.MAX_LEVEL - current), data.available(owner));
        if (spend <= 0) return;
        data.spend(owner, spend);
        if (type == 0) radiusLevel += spend; else healLevel += spend;
        setChanged();
        player.level().playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 1.2F);
    }

    public void setTrusted(ServerPlayer player, String name, boolean add) {
        if (!isOwner(player)) return;
        if (add) {
            if (trusted.size() >= 20) return;
            Optional<GameProfile> profile = lookup(player.server, name);
            if (profile.isEmpty()) {
                player.displayClientMessage(Component.translatable("message.achilles.player_not_found", name), true);
                return;
            }
            if (!profile.get().getId().equals(owner)) {
                trusted.put(profile.get().getId(), profile.get().getName());
            }
        } else {
            trusted.entrySet().removeIf(e -> e.getValue().equalsIgnoreCase(name));
        }
        setChanged();
    }

    private static Optional<GameProfile> lookup(MinecraftServer server, String name) {
        ServerPlayer online = server.getPlayerList().getPlayerByName(name);
        if (online != null) return Optional.of(online.getGameProfile());
        return server.getProfileCache() == null ? Optional.empty() : server.getProfileCache().get(name);
    }

    // ------------------------------------------------------------ la zona

    public static void serverTick(Level level, BlockPos pos, BlockState state, StatueBlockEntity statue) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        statue.ticks++;
        if (statue.ticks % 4 == 0) statue.guard(serverLevel);
        if (statue.ticks % 20 == 0) statue.healPlayers(serverLevel);
        if (statue.ticks % 40 == 0) {
            serverLevel.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5D, pos.getY() + 2.2D, pos.getZ() + 0.5D,
                    2, 0.3D, 0.2D, 0.3D, 0.01D);
        }
    }

    private void guard(ServerLevel level) {
        double r = getRadius();
        Vec3 c = center();
        AABB box = new AABB(c, c).inflate(r + 6.0D);

        for (Mob mob : level.getEntitiesOfClass(Mob.class, box, m -> m instanceof Enemy)) {
            LivingEntity target = mob.getTarget();
            if (target != null && c.distanceToSqr(target.position()) < r * r) {
                mob.setTarget(null);
            }
            double d = Math.sqrt(mob.distanceToSqr(c));
            if (d >= r) continue;

            boolean boss = mob.getType().is(Tags.EntityTypes.BOSSES);
            if (!boss && !mob.hasCustomName() && d < r - 3.0D) {
                level.sendParticles(ParticleTypes.POOF, mob.getX(), mob.getY(0.5D), mob.getZ(), 8, 0.2D, 0.2D, 0.2D, 0.02D);
                mob.discard();
                continue;
            }
            Vec3 away = new Vec3(mob.getX() - c.x, 0.0D, mob.getZ() - c.z);
            if (away.lengthSqr() < 1.0E-4D) away = new Vec3(1.0D, 0.0D, 0.0D);
            away = away.normalize();
            mob.setDeltaMovement(away.x * 0.8D, 0.35D, away.z * 0.8D);
            mob.hurtMarked = true;
            mob.getNavigation().stop();
        }

        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, box)) {
            if (c.distanceToSqr(player.position()) < r * r && !isAllowed(player)) {
                eject(level, player, c, r);
            }
        }
    }

    private void eject(ServerLevel level, ServerPlayer player, Vec3 c, double r) {
        Vec3 dir = new Vec3(player.getX() - c.x, 0.0D, player.getZ() - c.z);
        if (dir.lengthSqr() < 1.0E-4D) dir = new Vec3(1.0D, 0.0D, 0.0D);
        dir = dir.normalize();
        double tx = c.x + dir.x * (r + 2.0D);
        double tz = c.z + dir.z * (r + 2.0D);
        int ty = level.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(tx), Mth.floor(tz));
        player.connection.teleport(tx, ty, tz, player.getYRot(), player.getXRot());
        player.displayClientMessage(Component.translatable("message.achilles.restricted", ownerName), true);
    }

    private void healPlayers(ServerLevel level) {
        double r = getRadius();
        Vec3 c = center();
        float amount = StatueStats.healPerSecond(healLevel);
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, new AABB(c, c).inflate(r))) {
            if (player.isSpectator() || c.distanceToSqr(player.position()) > r * r || !isAllowed(player)) continue;

            if (player.getHealth() < player.getMaxHealth()) {
                player.heal(amount);
                level.sendParticles(ParticleTypes.HEART, player.getX(), player.getY(1.0D), player.getZ(), 1, 0.3D, 0.3D, 0.3D, 0.0D);
            }
            // Mejoras de curación altas: limpia efectos negativos (50+) y sacia el hambre (100)
            if (healLevel >= 50 && ticks % 100 == 0) {
                List<net.minecraft.world.effect.MobEffect> harmful = new ArrayList<>();
                for (MobEffectInstance effect : player.getActiveEffects()) {
                    if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) harmful.add(effect.getEffect());
                }
                harmful.forEach(player::removeEffect);
            }
            if (healLevel >= StatueStats.MAX_LEVEL && player.getFoodData().needsFood()) {
                player.getFoodData().eat(1, 0.5F);
            }
        }
    }
}
