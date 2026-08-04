package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class EventsScreen extends Screen {
    private static final int ROW_H = 24;
    private static final int COLS = 2;

    private final Minecraft client = Minecraft.getInstance();
    private final List<Entry> entries = new ArrayList<>();
    private int scroll = 0;

    private static final class Entry {
        final String name;
        final BooleanSupplier on;
        final Runnable toggle;

        Entry(String name, BooleanSupplier on, Runnable toggle) {
            this.name = name;
            this.on = on;
            this.toggle = toggle;
        }
    }

    public EventsScreen() {
        super(Component.literal("Список ивентов"));
        add(new Entry("KillAura", () -> KillAura.enabled, () -> KillAura.enabled = !KillAura.enabled));
        add(new Entry("Scaffold", () -> Scaffold.enabled, () -> Scaffold.enabled = !Scaffold.enabled));
        add(new Entry("Auto Totem", () -> AutoTotem.enabled, () -> AutoTotem.enabled = !AutoTotem.enabled));
        add(new Entry("Ghost Blocks", () -> DupeModClient.isGhostBlocksEnabled(), () -> DupeModClient.toggleGhostBlocks(client)));
        add(new Entry("FreeCam", () -> FreeCam.isActive(), () -> FreeCam.toggle(client)));
        add(new Entry("Fullbright", () -> Fullbright.enabled, () -> Fullbright.toggle(client)));
        add(new Entry("Показ невидимок", () -> Features.showInvis, () -> Features.showInvis = !Features.showInvis));
        add(new Entry("Custom Fog", () -> Features.customFog, () -> Features.customFog = !Features.customFog));
        add(new Entry("NoRender (варден)", () -> Features.noRender, () -> Features.noRender = !Features.noRender));
        add(new Entry("Усиление звука", () -> Features.soundBoost, () -> Features.soundBoost = !Features.soundBoost));
        add(new Entry("Тихий варден", () -> Features.quietWarden, () -> Features.quietWarden = !Features.quietWarden));
        add(new Entry("Music HUD", () -> HudRenderer.musicEnabled, () -> HudRenderer.musicEnabled = !HudRenderer.musicEnabled));
        add(new Entry("Target HUD", () -> HudRenderer.targetEnabled, () -> HudRenderer.targetEnabled = !HudRenderer.targetEnabled));
        add(new Entry("Watermark", () -> HudRenderer.watermarkEnabled, () -> HudRenderer.watermarkEnabled = !HudRenderer.watermarkEnabled));
        add(new Entry("FPS", () -> HudRenderer.fpsEnabled, () -> HudRenderer.fpsEnabled = !HudRenderer.fpsEnabled));
        add(new Entry("Player ESP", () -> EspRenderer.playerEsp, () -> EspRenderer.playerEsp = !EspRenderer.playerEsp));
        add(new Entry("Mob ESP", () -> EspRenderer.mobEsp, () -> EspRenderer.mobEsp = !EspRenderer.mobEsp));
        add(new Entry("Item ESP", () -> EspRenderer.itemEsp, () -> EspRenderer.itemEsp = !EspRenderer.itemEsp));
        add(new Entry("Автостройка дома", () -> HouseBuilder.enabled, () -> HouseBuilder.toggle(client)));
        add(new Entry("Discord RPC", () -> DiscordRpc.enabled, () -> DiscordRpc.enabled = !DiscordRpc.enabled));
        add(new Entry("Вид из салона", () -> WorldVisuals.zhiguliView, () -> WorldVisuals.zhiguliView = !WorldVisuals.zhiguliView));
        add(new Entry("Жигули", () -> WorldVisuals.zhiguli, () -> WorldVisuals.zhiguli = !WorldVisuals.zhiguli));
        add(new Entry("Костюм майёра", () -> WorldVisuals.majorSuit, () -> WorldVisuals.majorSuit = !WorldVisuals.majorSuit));
        add(new Entry("Китайская шапка", () -> WorldVisuals.chinaHat, () -> WorldVisuals.chinaHat = !WorldVisuals.chinaHat));
        add(new Entry("Парашюты (круги)", () -> WorldVisuals.jumpCircle, () -> WorldVisuals.jumpCircle = !WorldVisuals.jumpCircle));
        add(new Entry("Трейсеры", () -> WorldVisuals.tracers, () -> WorldVisuals.tracers = !WorldVisuals.tracers));
        add(new Entry("Трейсеры (мобы)", () -> WorldVisuals.tracersMobs, () -> WorldVisuals.tracersMobs = !WorldVisuals.tracersMobs));
        add(new Entry("Ник над головой", () -> WorldVisuals.nameTag, () -> WorldVisuals.nameTag = !WorldVisuals.nameTag));
    }

    private void add(Entry e) {
        entries.add(e);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        gui.fill(0, 0, width, height, Ui.PULSE_BG);
        gui.fill(0, 0, width, 2, Ui.PULSE_ACCENT);
        Font font = client.font;
        gui.drawString(font, "Список ивентов", 18, 16, 0xFFFFFFFF);
        String hint = "клик — вкл/выкл · колесо — скролл · Esc — назад";
        gui.drawString(font, hint, width - font.width(hint) - 18, 16, 0xFF9AA4B2);
        gui.fill(18, 30, width - 18, 31, Ui.PULSE_LINE);

        int colW = (width - 48) / COLS;
        int startY = 40;
        int maxVisible = (height - startY) / ROW_H;
        int totalRows = (entries.size() + COLS - 1) / COLS;
        if (scroll > Math.max(0, totalRows - maxVisible)) {
            scroll = Math.max(0, totalRows - maxVisible);
        }
        if (scroll < 0) {
            scroll = 0;
        }

        for (int i = 0; i < entries.size(); i++) {
            Entry e = entries.get(i);
            int col = i % COLS;
            int row = i / COLS;
            int visRow = row - scroll;
            if (visRow < 0 || visRow >= maxVisible) {
                continue;
            }
            int x = 18 + col * (colW + 12);
            int y = startY + visRow * ROW_H;
            boolean on = e.on.getAsBoolean();
            Ui.panel(gui, x, y, colW, ROW_H - 4, on ? Ui.PANEL : 0x80111111, on ? Ui.ACCENT : 0xFF333333);
            gui.drawString(font, Component.literal(e.name), x + 6, y + 3, 0xFFF0F0F0);
            int sw = font.width(on ? "ON" : "OFF");
            int sc = on ? Ui.GREEN : 0xFF666666;
            gui.drawString(font, Component.literal(on ? "ON" : "OFF"), x + colW - sw - 6, y + 3, sc);
        }

        gui.drawString(font, entries.size() + " ивентов · колёсико для скролла",
            width / 2 - font.width(entries.size() + " ивентов · колёсико для скролла") / 2,
            height - 18, 0xFF9AA4B2);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        if (event.buttonInfo().button() != 0) {
            return super.mouseClicked(event, bl);
        }
        double mouseX = event.x();
        double mouseY = event.y();
        int colW = (width - 48) / COLS;
        int startY = 40;
        int maxVisible = (height - startY) / ROW_H;
        int col = (int) (mouseX - 18) / (colW + 12);
        int row = (int) (mouseY - startY) / ROW_H + scroll;
        if (col < 0 || col >= COLS || row < 0) {
            return super.mouseClicked(event, bl);
        }
        int i = row * COLS + col;
        if (i >= 0 && i < entries.size()) {
            entries.get(i).toggle.run();
            return true;
        }
        return super.mouseClicked(event, bl);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scroll -= (int) verticalAmount;
        return true;
    }
}