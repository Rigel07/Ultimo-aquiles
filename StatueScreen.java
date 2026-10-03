package com.achilles.client;

import com.achilles.data.StatueStats;
import com.achilles.network.NetworkHandler;
import com.achilles.network.OpenStatuePacket;
import com.achilles.network.TrustPacket;
import com.achilles.network.UpgradePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

/** Pantalla del propietario: gastar puntos en radio/curación y dar permisos a otros jugadores. */
public class StatueScreen extends Screen {
    private BlockPos pos;
    private String ownerName;
    private int radiusLevel;
    private int healLevel;
    private int points;
    private List<String> trusted;
    private EditBox nameBox;

    public StatueScreen(OpenStatuePacket data) {
        super(Component.translatable("screen.achilles.title"));
        apply(data);
    }

    public BlockPos getPos() {
        return pos;
    }

    public void refresh(OpenStatuePacket data) {
        apply(data);
    }

    private void apply(OpenStatuePacket data) {
        this.pos = data.pos;
        this.ownerName = data.ownerName;
        this.radiusLevel = data.radiusLevel;
        this.healLevel = data.healLevel;
        this.points = data.points;
        this.trusted = data.trusted;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int top = this.height / 2 - 90;

        addRenderableWidget(Button.builder(Component.translatable("screen.achilles.plus1"),
                b -> upgrade(0, 1)).bounds(cx - 155, top + 66, 72, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.achilles.plus10"),
                b -> upgrade(0, 10)).bounds(cx - 79, top + 66, 72, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.achilles.plus1"),
                b -> upgrade(1, 1)).bounds(cx + 7, top + 66, 72, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.achilles.plus10"),
                b -> upgrade(1, 10)).bounds(cx + 83, top + 66, 72, 20).build());

        nameBox = new EditBox(this.font, cx - 130, top + 138, 120, 20, Component.empty());
        nameBox.setMaxLength(16);
        nameBox.setHint(Component.translatable("screen.achilles.player_name"));
        addRenderableWidget(nameBox);

        addRenderableWidget(Button.builder(Component.translatable("screen.achilles.allow"),
                b -> trust(true)).bounds(cx - 2, top + 138, 62, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.achilles.remove"),
                b -> trust(false)).bounds(cx + 64, top + 138, 66, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.done"),
                b -> onClose()).bounds(cx - 50, top + 172, 100, 20).build());
    }

    private void upgrade(int type, int amount) {
        NetworkHandler.CHANNEL.sendToServer(new UpgradePacket(pos, type, amount));
    }

    private void trust(boolean add) {
        String name = nameBox.getValue().trim();
        if (name.isEmpty()) return;
        NetworkHandler.CHANNEL.sendToServer(new TrustPacket(pos, name, add));
        nameBox.setValue("");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        int cx = this.width / 2;
        int top = this.height / 2 - 90;

        graphics.drawCenteredString(this.font, this.title, cx, top, 0xFFD700);
        graphics.drawCenteredString(this.font, Component.translatable("screen.achilles.owner", ownerName), cx, top + 14, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("screen.achilles.points", points), cx, top + 28, 0x55FF55);

        graphics.drawString(this.font, Component.translatable("screen.achilles.radius", radiusLevel, StatueStats.MAX_LEVEL), cx - 155, top + 46, 0xFFFFFF);
        graphics.drawString(this.font, Component.translatable("screen.achilles.radius_value",
                String.format(Locale.ROOT, "%.1f", StatueStats.radius(radiusLevel))), cx - 155, top + 56, 0xAAAAAA);
        graphics.drawString(this.font, Component.translatable("screen.achilles.heal", healLevel, StatueStats.MAX_LEVEL), cx + 7, top + 46, 0xFFFFFF);
        graphics.drawString(this.font, Component.translatable("screen.achilles.heal_value",
                String.format(Locale.ROOT, "%.1f", StatueStats.healPerSecond(healLevel))), cx + 7, top + 56, 0xAAAAAA);

        graphics.drawCenteredString(this.font, Component.translatable("screen.achilles.trusted_title"), cx, top + 100, 0xFFD700);
        String list = trusted.isEmpty() ? Component.translatable("screen.achilles.nobody").getString() : String.join(", ", trusted);
        graphics.drawCenteredString(this.font, this.font.plainSubstrByWidth(list, 300), cx, top + 114, 0xFFFFFF);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
