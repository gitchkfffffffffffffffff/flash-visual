package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class DupeGuiScreen extends Screen {
    private static final int SIDEBAR_W = 150;
    private static final String[] CATS = {"ГЛАВНОЕ", "ВИЗУАЛ", "МИР", "ЗВУК", "МЕДИА"};
    private static final double[] RANGES = {3, 4, 5, 6, 8};
    private static final int[] DELAYS = {1, 2, 3, 5, 8};

    private static final String[][] COMMANDS = {
        {".dupe", "Дюп предметов"},
        {"/dupe", "Дюп на сервере"}
    };

    private final Minecraft client = Minecraft.getInstance();
    private int category = 0;
    private QuickCommandBox quickCommandBox;

    public DupeGuiScreen() {
        super(Component.literal("Flash Visual"));
    }

    @Override
    protected void init() {
        rebuild();
    }

    private void rebuild() {
        clearWidgets();
        int y = 66;
        for (int i = 0; i < CATS.length; i++) {
            int idx = i;
            addRenderableWidget(new Ui.PulseCategory(18, y, SIDEBAR_W - 36, 30, CATS[i], category == i,
                b -> {
                    category = idx;
                    rebuild();
                }));
            y += 36;
        }
        int cx = SIDEBAR_W + 20;
        int cw = width - cx - 16;
        switch (category) {
            case 0 -> buildMain(cx, cw);
            case 1 -> buildVisual(cx, cw);
            case 2 -> buildWorld(cx, cw);
            case 3 -> buildSound(cx, cw);
            default -> buildMedia(cx, cw);
        }
    }

    private void buildMain(int cx, int cw) {
        int x2 = cx + 250;
        int w2 = Math.max(150, cw - 250);
        int y = 64;
        addAction(x2, y, w2, "D U P E", () -> {
            DupeModClient.performDupe();
            client.setScreen(null);
        });
        y += 32;
        addToggle(x2, y, w2, "KillAura", () -> KillAura.enabled, () -> KillAura.enabled = !KillAura.enabled);
        y += 32;
        addCycle(x2, y, w2, "Дальность", () -> (int) KillAura.range + " блоков", () -> KillAura.range = nextRange());
        y += 32;
        addCycle(x2, y, w2, "Скорость", () -> "каждые " + KillAura.delay + " тик", () -> KillAura.delay = nextDelay());
        y += 32;
        addAction(x2, y, w2, "Выдача предметов", () -> client.setScreen(new ItemGiveScreen()));
        y += 32;
        addToggle(x2, y, w2, "Auto Totem", () -> AutoTotem.enabled, () -> AutoTotem.enabled = !AutoTotem.enabled);
        y += 32;
        addAction(x2, y, w2, "Список друзей", () -> client.setScreen(new FriendListScreen()));
        y += 32;
        addAction(x2, y, w2, "Бинды (клавиши)", () -> client.setScreen(new BindChangerScreen()));

        quickCommandBox = new QuickCommandBox(client.font, cx + 2, 372, 238, 18);
        addRenderableWidget(quickCommandBox);
    }

    private void buildVisual(int cx, int cw) {
        int colW = (cw - 12) / 2;
        int xa = cx, xb = cx + colW + 12;
        int ya = 64, yb = 64;
        ya = addVisToggle(xa, ya, colW, "FreeCam", () -> FreeCam.isActive(), () -> FreeCam.toggle(client));
        yb = addVisToggle(xb, yb, colW, "Custom Fog", () -> Features.customFog, () -> Features.customFog = !Features.customFog);
        ya = addVisCycle(xa, ya, colW, "Цвет тумана", () -> Features.FOG_COLOR_NAMES[Features.fogColorIndex],
            () -> Features.fogColorIndex = (Features.fogColorIndex + 1) % Features.FOG_COLORS.length);
        yb = addVisCycle(xb, yb, colW, "Туман", () -> Features.FOG_DISTANCE_NAMES[Features.fogDistanceIndex],
            () -> Features.fogDistanceIndex = (Features.fogDistanceIndex + 1) % Features.FOG_DISTANCES.length);
        ya = addVisToggle(xa, ya, colW, "Fullbright", () -> Fullbright.enabled, () -> Fullbright.toggle(client));
        yb = addVisToggle(xb, yb, colW, "NoRender (варден)", () -> Features.noRender, () -> Features.noRender = !Features.noRender);
        ya = addVisToggle(xa, ya, colW, "Music HUD", () -> HudRenderer.musicEnabled, () -> HudRenderer.musicEnabled = !HudRenderer.musicEnabled);
        yb = addVisToggle(xb, yb, colW, "Target HUD", () -> HudRenderer.targetEnabled, () -> HudRenderer.targetEnabled = !HudRenderer.targetEnabled);
        ya = addVisToggle(xa, ya, colW, "Player ESP", () -> EspRenderer.playerEsp, () -> EspRenderer.playerEsp = !EspRenderer.playerEsp);
        yb = addVisToggle(xb, yb, colW, "Mob ESP", () -> EspRenderer.mobEsp, () -> EspRenderer.mobEsp = !EspRenderer.mobEsp);
        ya = addVisToggle(xa, ya, colW, "Item ESP", () -> EspRenderer.itemEsp, () -> EspRenderer.itemEsp = !EspRenderer.itemEsp);
        yb = addVisToggle(xb, yb, colW, "Watermark", () -> HudRenderer.watermarkEnabled, () -> HudRenderer.watermarkEnabled = !HudRenderer.watermarkEnabled);
        ya = addVisToggle(xa, ya, colW, "Показ невидимок", () -> Features.showInvis, () -> Features.showInvis = !Features.showInvis);
        yb = addVisToggle(xb, yb, colW, "Тёмные меню", () -> Features.darkMenu, () -> Features.darkMenu = !Features.darkMenu);
        ya = addVisToggle(xa, ya, colW, "Минимап", () -> Minimap.enabled, () -> Minimap.enabled = !Minimap.enabled);
        yb = addVisToggle(xb, yb, colW, "FPS счётчик", () -> HudRenderer.fpsEnabled, () -> HudRenderer.fpsEnabled = !HudRenderer.fpsEnabled);
        ya = addVisToggle(xa, ya, colW, "Курсор", () -> CursorOverlay.enabled, () -> {
            CursorOverlay.enabled = !CursorOverlay.enabled;
            if (!CursorOverlay.enabled) {
                CursorOverlay.restore();
            }
        });
        yb = addVisToggle(xb, yb, colW, "Красивое меню", () -> Features.beautifulMenu, () -> Features.beautifulMenu = !Features.beautifulMenu);
        ya = addVisToggle(xa, ya, colW, "Inventory HUD", () -> HudRenderer.inventoryEnabled, () -> HudRenderer.inventoryEnabled = !HudRenderer.inventoryEnabled);
        yb = addVisToggle(xb, yb, colW, "Potion GUI", () -> HudRenderer.potionEnabled, () -> HudRenderer.potionEnabled = !HudRenderer.potionEnabled);
        ya = addVisToggle(xa, ya, colW, "Staff List", () -> StaffHud.enabled, () -> StaffHud.enabled = !StaffHud.enabled);
        yb = addVisToggle(xb, yb, colW, "Координаты", () -> CoordinatesHud.enabled, () -> CoordinatesHud.enabled = !CoordinatesHud.enabled);
        ya = addVisToggle(xa, ya, colW, "Джоджо", () -> JojoHud.enabled, () -> JojoHud.enabled = !JojoHud.enabled);
        yb = addVisToggle(xb, yb, colW, "View Model", () -> ViewModel.enabled, () -> ViewModel.enabled = !ViewModel.enabled);
        ya = addVisToggle(xa, ya, colW, "Кит. шляпа", () -> WorldVisuals.chinaHat, () -> WorldVisuals.chinaHat = !WorldVisuals.chinaHat);
        yb = addVisToggle(xb, yb, colW, "Круг прыжка", () -> WorldVisuals.jumpCircle, () -> WorldVisuals.jumpCircle = !WorldVisuals.jumpCircle);
        ya = addVisToggle(xa, ya, colW, "Трассеры", () -> WorldVisuals.tracers, () -> WorldVisuals.tracers = !WorldVisuals.tracers);
        yb = addVisToggle(xb, yb, colW, "Трассеры мобы", () -> WorldVisuals.tracersMobs, () -> WorldVisuals.tracersMobs = !WorldVisuals.tracersMobs);
        ya = addVisToggle(xa, ya, colW, "Ники над головой", () -> WorldVisuals.nameTag, () -> WorldVisuals.nameTag = !WorldVisuals.nameTag);
        ya = addVisSlider(xa, ya, colW, "Шляпа размер", 0.5f, 2.0f, () -> WorldVisuals.hatScale, v -> WorldVisuals.hatScale = (float) v, f -> String.format("%.1f", f));
        yb = addVisSlider(xb, yb, colW, "Круг радиус", 1.0f, 6.0f, () -> WorldVisuals.circleRadius, v -> WorldVisuals.circleRadius = (float) v, f -> String.format("%.1f", f));
        ya = addVisSlider(xa, ya, colW, "VM X", -1.0f, 1.0f, () -> ViewModel.posX, v -> ViewModel.posX = (float) v, f -> String.format("%.2f", f));
        yb = addVisSlider(xb, yb, colW, "VM Y", -1.0f, 1.0f, () -> ViewModel.posY, v -> ViewModel.posY = (float) v, f -> String.format("%.2f", f));
        ya = addVisSlider(xa, ya, colW, "VM Z", -1.0f, 1.0f, () -> ViewModel.posZ, v -> ViewModel.posZ = (float) v, f -> String.format("%.2f", f));
        yb = addVisSlider(xb, yb, colW, "VM Размер", 0.2f, 2.0f, () -> ViewModel.scale, v -> ViewModel.scale = (float) v, f -> String.format("%.2f", f));
        ya = addVisSlider(xa, ya, colW, "VM Пов. X", -180.0f, 180.0f, () -> ViewModel.rotX, v -> ViewModel.rotX = (float) v, f -> (int) (float) f + "°");
        yb = addVisSlider(xb, yb, colW, "VM Пов. Y", -180.0f, 180.0f, () -> ViewModel.rotY, v -> ViewModel.rotY = (float) v, f -> (int) (float) f + "°");
        ya = addVisSlider(xa, ya, colW, "VM Пов. Z", -180.0f, 180.0f, () -> ViewModel.rotZ, v -> ViewModel.rotZ = (float) v, f -> (int) (float) f + "°");
        ya = addVisCycle(xa, ya, colW, "Тема", Theme::name, Theme::next);
        ya = addVisToggle(xa, ya, colW, "Аниме-тема", Theme::isAnime, () -> {
            Theme.toggleAnime();
            client.setScreen(new DupeGuiScreen());
        });
        if (ya < yb) {
            ya = yb;
        }
        addAction(xa, ya, colW, "Добавить админа (цель)", () -> addTargetAsStaff());
        addAction(xa, ya + 28, colW, "Очистить админов", () -> StaffHud.STAFF.clear());
        addAction(xa, ya + 56, colW, "Сброс позиций HUD", HudPos::reset);
    }

    private void addTargetAsStaff() {
        if (client.hitResult instanceof EntityHitResult ehr && ehr.getEntity() instanceof Player p) {
            StaffHud.STAFF.add(p.getName().getString().toLowerCase());
            if (client.player != null) {
                client.player.displayClientMessage(Component.literal("Админ: " + p.getName().getString()), false);
            }
        } else if (client.player != null) {
            client.player.displayClientMessage(Component.literal("Наведи прицел на игрока"), false);
        }
    }

    private int addVisToggle(int x, int y, int w, String name, BooleanSupplier state, Runnable toggle) {
        addToggle(x, y, w, name, state, toggle);
        return y + 26;
    }

    private int addVisCycle(int x, int y, int w, String name, Supplier<String> value, Runnable cycle) {
        addCycle(x, y, w, name, value, cycle);
        return y + 26;
    }

    private int addVisSlider(int x, int y, int w, String name, float min, float max,
                             Supplier<Float> get, DoubleConsumer set, Function<Float, String> fmt) {
        addRenderableWidget(new Ui.PulseSlider(x, y, w, 20, name, min, max, get, set, fmt));
        return y + 22;
    }

    private void buildWorld(int cx, int cw) {
        int y = 64;
        addToggle(cx, y, cw, "Ghost Blocks", DupeModClient::isGhostBlocksEnabled, () -> DupeModClient.toggleGhostBlocks(client));
        y += 32;
        addToggle(cx, y, cw, "Scaffold", () -> Scaffold.enabled, () -> Scaffold.enabled = !Scaffold.enabled);
        y += 32;
        addCycle(cx, y, cw, "Время", () -> TimeChanger.MODE_NAMES[TimeChanger.mode],
            () -> TimeChanger.mode = (TimeChanger.mode + 1) % TimeChanger.MODE_NAMES.length);
    }

    private void buildSound(int cx, int cw) {
        int y = 64;
        addToggle(cx, y, cw, "Усиление звука", () -> Features.soundBoost, () -> Features.soundBoost = !Features.soundBoost);
        y += 32;
        addToggle(cx, y, cw, "Тихий варден", () -> Features.quietWarden, () -> Features.quietWarden = !Features.quietWarden);
    }

    private void buildMedia(int cx, int cw) {
        int y = 64;
        addAction(cx, y, cw, "Альт менеджер", () -> client.setScreen(new AltManagerScreen()));
        y += 32;
        addAction(cx, y, cw, "◀  Перемотка назад", () -> WinMusicReader.prev());
        y += 32;
        addAction(cx, y, cw, "⏯  Пауза / Плей", () -> WinMusicReader.playPause());
        y += 32;
        addAction(cx, y, cw, "▶  Вперёд", () -> WinMusicReader.next());
        y += 32;
        addToggle(cx, y, cw, "Discord RPC", () -> DiscordRpc.enabled, () -> {
            DiscordRpc.enabled = !DiscordRpc.enabled;
            if (DiscordRpc.enabled) {
                DiscordRpc.start();
            } else {
                DiscordRpc.stop();
            }
        });
    }

    private void addToggle(int x, int y, int w, String name, BooleanSupplier state, Runnable toggle) {
        addRenderableWidget(new Ui.PulseRow(x, y, w, 26, name,
            state::getAsBoolean,
            () -> state.getAsBoolean() ? "ON" : "OFF",
            toggle));
    }

    private void addAction(int x, int y, int w, String name, Runnable action) {
        addRenderableWidget(new Ui.PulseRow(x, y, w, 26, name, () -> true, () -> "▶", action));
    }

    private void addCycle(int x, int y, int w, String name, Supplier<String> value, Runnable cycle) {
        addRenderableWidget(new Ui.PulseRow(x, y, w, 26, name, () -> false, value, cycle));
    }

    private double nextRange() {
        int i = 0;
        for (; i < RANGES.length; i++) {
            if (RANGES[i] == KillAura.range) {
                break;
            }
        }
        return RANGES[(i + 1) % RANGES.length];
    }

    private int nextDelay() {
        int i = 0;
        for (; i < DELAYS.length; i++) {
            if (DELAYS[i] == KillAura.delay) {
                break;
            }
        }
        return DELAYS[(i + 1) % DELAYS.length];
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        gui.fill(0, 0, width, height, Ui.PULSE_BG);
        gui.fill(0, 0, width, 2, Ui.PULSE_ACCENT);
        super.render(gui, mouseX, mouseY, partialTick);

        Font font = client.font;
        gui.drawString(font, "Flash Visual", 18, 16, 0xFFFFFFFF);
        String ver = "v1.1.0-pre1";
        gui.drawString(font, ver, width - font.width(ver) - 18, 16, 0xFF9AA4B2);
        gui.drawString(font, CATS[category], 18, 31, Ui.PULSE_ACCENT);
        gui.fill(18, 46, width - 18, 47, Ui.PULSE_LINE);
        gui.fill(SIDEBAR_W, 47, SIDEBAR_W + 1, height, Ui.PULSE_LINE);
        if (category == 0) {
            renderKeybinds(gui, font);
        }
    }

    private void renderKeybinds(GuiGraphics gui, Font font) {
        int panelX = 18;
        int panelW = SIDEBAR_W - 36;
        Ui.section(gui, font, "КЛАВИШИ", panelX + 2, 244, panelX + panelW - 6);
        int panelY = 258;
        int rowH = 11;
        java.util.List<String> actions = Binds.actions();
        int panelH = actions.size() * rowH + 10;
        Ui.panel(gui, panelX, panelY, panelW, panelH, 0xC00B0F1A, Ui.PULSE_LINE);
        int kx = panelX + 6;
        for (int i = 0; i < actions.size(); i++) {
            int ky = panelY + 5 + i * rowH;
            String key = Binds.keyName(actions.get(i));
            String desc = Binds.label(actions.get(i));
            int capW = Math.max(30, font.width(key) + 8);
            String d = font.plainSubstrByWidth(desc, panelW - 12 - capW - 6);
            Ui.roundRect(gui, kx, ky, capW, 11, 4, 0xFF101C30);
            Ui.roundRect(gui, kx, ky, capW, 11, 4, Ui.PULSE_ACCENT);
            gui.drawCenteredString(font, key, kx + capW / 2, ky + 1, Ui.PULSE_ACCENT);
            gui.drawString(font, Component.literal(d), kx + capW + 6, ky + 2, 0xFFB6BDC9);
        }

        int cx = SIDEBAR_W + 20;
        int cy = 300;
        Ui.section(gui, font, "КОМАНДЫ · быстрый ввод", cx + 2, cy, cx + 244 - 6);
        gui.drawString(font, Component.literal("Tab — автодополнение · Enter — отправить"),
            cx + 2, cy + 32, 0xFF6A6A6A);
        int ly = cy + 44;
        for (String[] c : COMMANDS) {
            gui.fill(cx + 2, ly + 1, cx + 3, ly + 9, Ui.PULSE_ACCENT);
            gui.drawString(font, Component.literal(c[0]), cx + 10, ly, Ui.PULSE_ACCENT);
            gui.drawString(font, Component.literal("— " + c[1]),
                cx + 10 + font.width(c[0]) + 8, ly, 0xFF9AA4B2);
            ly += 12;
        }
    }

    private static class QuickCommandBox extends EditBox {
        private int tabIndex = 0;

        QuickCommandBox(Font font, int x, int y, int w, int h) {
            super(font, x, y, w, h, Component.literal("Быстрая команда"));
            setMaxLength(64);
        }

        @Override
        public boolean keyPressed(KeyEvent event) {
            if (event.key() == GLFW.GLFW_KEY_TAB) {
                String cur = getValue().trim().toLowerCase();
                if (cur.isEmpty()) {
                    setValue(COMMANDS[0][0]);
                    tabIndex = 0;
                    return true;
                }
                List<String> matches = new ArrayList<>();
                for (String[] c : COMMANDS) {
                    if (c[0].toLowerCase().startsWith(cur)) {
                        matches.add(c[0]);
                    }
                }
                if (matches.isEmpty()) {
                    setValue(COMMANDS[0][0]);
                    tabIndex = 0;
                } else {
                    setValue(matches.get(tabIndex % matches.size()));
                    tabIndex++;
                }
                return true;
            }
            if (event.key() == GLFW.GLFW_KEY_ENTER) {
                String cmd = getValue().trim();
                Minecraft client = Minecraft.getInstance();
                if (client.getConnection() != null) {
                    if (cmd.startsWith("/")) {
                        client.getConnection().sendCommand(cmd.substring(1));
                    } else if (!cmd.isEmpty()) {
                        client.getConnection().sendChat(cmd);
                    }
                }
                setValue("");
                return true;
            }
            tabIndex = 0;
            return super.keyPressed(event);
        }
    }
}
