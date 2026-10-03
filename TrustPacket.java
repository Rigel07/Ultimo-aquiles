package com.achilles.network;

import com.achilles.block.StatueBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Cliente -> servidor: dar o quitar permiso a un jugador. */
public class TrustPacket {
    private final BlockPos pos;
    private final String name;
    private final boolean add;

    public TrustPacket(BlockPos pos, String name, boolean add) {
        this.pos = pos;
        this.name = name;
        this.add = add;
    }

    public static void encode(TrustPacket p, FriendlyByteBuf buf) {
        buf.writeBlockPos(p.pos);
        buf.writeUtf(p.name, 32);
        buf.writeBoolean(p.add);
    }

    public static TrustPacket decode(FriendlyByteBuf buf) {
        return new TrustPacket(buf.readBlockPos(), buf.readUtf(32), buf.readBoolean());
    }

    public static void handle(TrustPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || p.name.isBlank()) return;
            if (player.distanceToSqr(Vec3.atCenterOf(p.pos)) > 100.0D) return;
            if (player.level().getBlockEntity(p.pos) instanceof StatueBlockEntity statue) {
                statue.setTrusted(player, p.name.trim(), p.add);
                statue.sendOpen(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
