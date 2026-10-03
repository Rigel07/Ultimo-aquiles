package com.achilles.events;

import com.achilles.block.StatueBlockEntity;
import com.achilles.data.StatueRegistry;
import com.achilles.registry.ModBlocks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ModEvents {

    /** Nadie puede romper una estatua (ni siquiera en creativo). */
    @SubscribeEvent
    public void onBreak(BlockEvent.BreakEvent event) {
        if (event.getState().is(ModBlocks.STATUE.get())) {
            event.setCanceled(true);
        }
    }

    /** Dentro de la zona los jugadores no reciben daño (salvo vacío y /kill). */
    @SubscribeEvent
    public void onAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            StatueBlockEntity statue = StatueRegistry.findCovering(player.level(), player.position());
            if (statue != null && statue.isAllowed(player)) {
                event.setCanceled(true);
            }
        }
    }

    /** Los mobs hostiles no pueden aparecer dentro de una zona (los jefes sí, pero son repelidos). */
    @SubscribeEvent
    public void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || event.loadedFromDisk()) return;
        Entity entity = event.getEntity();
        if (entity instanceof Enemy && !entity.getType().is(Tags.EntityTypes.BOSSES)
                && StatueRegistry.findCovering(event.getLevel(), entity.position()) != null) {
            event.setCanceled(true);
        }
    }
}
