package com.achilles.network;

import com.achilles.client.ClientHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Servidor -> cliente: abre (o refresca) la pantalla de la estatua. */
public class OpenStatuePacket {
    public final BlockPos pos;
    public final String ownerName;
    public final int radiusLevel;
    public final int healLevel;
    public final int points;
    public final List<String> trusted;

    public OpenStatuePacket(BlockPos pos, String ownerName, int radiusLevel, int healLevel, int points, List<String> trusted) {
        this.pos = pos;
        this.ownerName = ownerName;
        this.radiusLevel = radiusLevel;
        this.healLevel = healLevel;
        this.points = points;
        this.trusted = trusted;
    }

    public static void encode(OpenStatuePacket p, FriendlyByteBuf buf) {
        buf.writeBlockPos(p.pos);
        buf.writeUtf(p.ownerName);
        buf.writeVarInt(p.radiusLevel);
        buf.writeVarInt(p.healLevel);
        buf.writeVarInt(p.points);
        buf.writeVarInt(p.trusted.size());
        for (String name : p.trusted) buf.writeUtf(name);
    }

    public static OpenStatuePacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        String owner = buf.readUtf();
        int radius = buf.readVarInt();
        int heal = buf.readVarInt();
        int points = buf.readVarInt();
        int count = buf.readVarInt();
        List<String> trusted = new ArrayList<>();
        for (int i = 0; i < count; i++) trusted.add(buf.readUtf());
        return new OpenStatuePacket(pos, owner, radius, heal, points, trusted);
    }

    public static void handle(OpenStatuePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.openStatue(packet)));
        ctx.get().setPacketHandled(true);
    }
}
