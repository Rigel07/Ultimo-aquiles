package com.achilles.client;

import com.achilles.network.OpenStatuePacket;
import net.minecraft.client.Minecraft;

/** Puente servidor -> cliente (solo se carga en el cliente). */
public class ClientHooks {
    public static void openStatue(OpenStatuePacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof StatueScreen screen && screen.getPos().equals(packet.pos)) {
            screen.refresh(packet);
        } else {
            mc.setScreen(new StatueScreen(packet));
        }
    }
}
