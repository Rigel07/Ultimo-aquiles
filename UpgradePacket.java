package com.achilles.network;

import com.achilles.block.StatueBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Cliente -> servidor: gastar puntos (type 0 = radio, 1 = curación). */
public class UpgradePacket {
    private final BlockPos pos;
    private final int type;
    private final int amount;

    public UpgradePacket(BlockPos pos, int type, int amount) {
        this.pos = pos;
        this.type = type;
        this.amount = amount;
    }

    public static void encode(UpgradePacket p, FriendlyByteBuf buf) {
        buf.writeBlockPos(p.pos);
        buf.writeVarInt(p.type);
        buf.writeVarInt(p.amount);
    }

    public static UpgradePacket decode(FriendlyByteBuf buf) {
        return new UpgradePacket(buf.readBlockPos(), buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(UpgradePacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || p.type < 0 || p.type > 1) return;
            if (player.distanceToSqr(Vec3.atCenterOf(p.pos)) > 100.0D) return;
            if (player.level().getBlockEntity(p.pos) instanceof StatueBlockEntity statue) {
                statue.upgrade(player, p.type, Mth.clamp(p.amount, 1, 100));
                statue.sendOpen(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
