package com.achilles.network;

import com.achilles.Achilles;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Achilles.MODID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    public static void register() {
        CHANNEL.registerMessage(0, OpenStatuePacket.class,
                OpenStatuePacket::encode, OpenStatuePacket::decode, OpenStatuePacket::handle);
        CHANNEL.registerMessage(1, UpgradePacket.class,
                UpgradePacket::encode, UpgradePacket::decode, UpgradePacket::handle);
        CHANNEL.registerMessage(2, TrustPacket.class,
                TrustPacket::encode, TrustPacket::decode, TrustPacket::handle);
    }
}
