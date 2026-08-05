package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
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
    private static final String[] CATS = {"ГЛАВНОЕ", "ВИЗУАЛ", "МИР", "ЗВУК", "МЕДИА", "МЕТКИ", "СОЗДАТЕЛИ", "КОНСОЛЬ"};
    private static final double[] RANGES = {3, 4, 5, 6, 8};
    private static final int[] DELAYS = {1, 2, 3, 5, 8};

    private static final String[][] COMMANDS = {
        {".dupe", "Дюп предметов"},
        {"/dupe", "Дюп на сервере"}
    };

    private final Minecraft client = Minecraft.getInstance();
    private int category = 0;
    private int contentScroll = 0;
    private int contentMax = 0;
    private final List<AbstractWidget> contentWidgets = new ArrayList<>();
    private final List<Integer> contentYs = new ArrayList<>();
    private QuickCommandBox quickCommandBox;
    private EditBox tpX;
    private EditBox tpY;
    private EditBox tpZ;
    private EditBox tpName;

    public DupeGuiScreen() {
        super(Component.literal("Flash Visual"));
    }

    @Override
    protected void init() {
        rebuild();
    }

    private void rebuild() {
        clearWidgets();
        contentWidgets.clear();
        contentYs.clear();
        contentMax = 0;
        int y = 66;
        for (int i = 0; i < CATS.length; i++) {
            int idx = i;
            addRenderableWidget(new Ui.PulseCategory(18, y, SIDEBAR_W - 36, 30, CATS[i], category == i,
                b -> {
                    category = idx;
                    contentScroll = 0;
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
            case 4 -> buildMedia(cx, cw);
            case 5 -> buildMarks(cx, cw);
            case 6 -> buildCreators(cx, cw);
            default -> buildConsole(cx, cw);
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
        addAction(x2, y, w2, "Эксплоит предметов", () -> client.setScreen(new ItemExploitScreen()));
        y += 32;
        addAction(x2, y, w2, "Поиск серверов", () -> client.setScreen(new ServerFinderScreen()));
        y += 32;
        addToggle(x2, y, w2, "Поиск игроков (HUD)", () -> PlayerSearch.enabled, () -> {
            PlayerSearch.enabled = !PlayerSearch.enabled;
            if (!PlayerSearch.enabled) {
                PlayerSearch.setQuery("");
            }
        });
        y += 32;
        addCycle(x2, y, w2, "Мин. длина ника", () -> PlayerSearch.minLength + " симв.", () -> PlayerSearch.minLength = nextMinLen());
        y += 32;
        addToggle(x2, y, w2, "Streamer Mode", () -> StreamerMode.enabled, () -> StreamerMode.enabled = !StreamerMode.enabled);
        y += 32;
        addToggle(x2, y, w2, "Auto Totem", () -> AutoTotem.enabled, () -> AutoTotem.enabled = !AutoTotem.enabled);
        y += 32;
        addToggle(x2, y, w2, "Fly", () -> CheatModules.Fly.enabled, () -> {
            CheatModules.Fly.enabled = !CheatModules.Fly.enabled;
            if (!CheatModules.Fly.enabled) {
                CheatModules.Fly.disable(client);
            }
        });
        y += 32;
        addToggle(x2, y, w2, "Speed", () -> CheatModules.Speed.enabled, () -> CheatModules.Speed.enabled = !CheatModules.Speed.enabled);
        y += 32;
        addToggle(x2, y, w2, "Spider", () -> CheatModules.Spider.enabled, () -> CheatModules.Spider.enabled = !CheatModules.Spider.enabled);
        y += 32;
        addToggle(x2, y, w2, "AirJump", () -> CheatModules.AirJump.enabled, () -> CheatModules.AirJump.enabled = !CheatModules.AirJump.enabled);
        y += 32;
        addToggle(x2, y, w2, "NoFall", () -> CheatModules.NoFall.enabled, () -> CheatModules.NoFall.enabled = !CheatModules.NoFall.enabled);
        y += 32;
        addToggle(x2, y, w2, "Noclip", () -> CheatModules.Noclip.enabled, () -> {
            CheatModules.Noclip.enabled = !CheatModules.Noclip.enabled;
            if (!CheatModules.Noclip.enabled) {
                CheatModules.Noclip.disable(client);
            }
        });
        y += 32;
        addToggle(x2, y, w2, "ChestStealer", () -> CheatModules.ChestStealer.enabled, () -> CheatModules.ChestStealer.enabled = !CheatModules.ChestStealer.enabled);
        y += 32;
        addAction(x2, y, w2, "Список друзей", () -> client.setScreen(new FriendListScreen()));
        y += 32;
        addAction(x2, y, w2, "Список ивентов", () -> client.setScreen(new EventsScreen()));
        y += 32;
        addAction(x2, y, w2, "Бинды (клавиши)", () -> client.setScreen(new BindChangerScreen()));

        quickCommandBox = new QuickCommandBox(client.font, cx + 2, 372 - contentScroll, 238, 18);
        registerContent(quickCommandBox, 372);
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
        ya = addVisToggle(xa, ya, colW, "Scoreboard тема", () -> Features.scoreboardTheme, () -> Features.scoreboardTheme = !Features.scoreboardTheme);
        ya = addVisToggle(xa, ya, colW, "Inventory HUD", () -> HudRenderer.inventoryEnabled, () -> HudRenderer.inventoryEnabled = !HudRenderer.inventoryEnabled);
        yb = addVisToggle(xb, yb, colW, "Potion GUI", () -> HudRenderer.potionEnabled, () -> HudRenderer.potionEnabled = !HudRenderer.potionEnabled);
        ya = addVisToggle(xa, ya, colW, "Staff List", () -> StaffHud.enabled, () -> StaffHud.enabled = !StaffHud.enabled);
        yb = addVisToggle(xb, yb, colW, "Координаты", () -> CoordinatesHud.enabled, () -> CoordinatesHud.enabled = !CoordinatesHud.enabled);
        ya = addVisToggle(xa, ya, colW, "Джоджо", () -> JojoHud.enabled, () -> JojoHud.enabled = !JojoHud.enabled);
        yb = addVisToggle(xb, yb, colW, "View Model", () -> ViewModel.enabled, () -> ViewModel.enabled = !ViewModel.enabled);
        ya = addVisToggle(xa, ya, colW, "Крылья", () -> WorldVisuals.wings, () -> WorldVisuals.wings = !WorldVisuals.wings);
        ya = addVisToggle(xa, ya, colW, "Near (стрелки)", () -> WorldVisuals.near, () -> WorldVisuals.near = !WorldVisuals.near);
        ya = addVisToggle(xa, ya, colW, "Кит. шляпа", () -> WorldVisuals.chinaHat, () -> WorldVisuals.chinaHat = !WorldVisuals.chinaHat);
        ya = addVisToggle(xa, ya, colW, "Жигули", () -> WorldVisuals.zhiguli, () -> WorldVisuals.zhiguli = !WorldVisuals.zhiguli);
        yb = addVisToggle(xb, yb, colW, "Вид из салона", () -> WorldVisuals.zhiguliView, () -> WorldVisuals.zhiguliView = !WorldVisuals.zhiguliView);
        ya = addVisToggle(xa, ya, colW, "Костюм майёра", () -> WorldVisuals.majorSuit, () -> WorldVisuals.majorSuit = !WorldVisuals.majorSuit);
        yb = addVisToggle(xb, yb, colW, "Чёрный HUD", () -> Features.darkHud, () -> Features.darkHud = !Features.darkHud);
        ya = addVisToggle(xa, ya, colW, "Item Physics", () -> Features.itemPhysics, () -> Features.itemPhysics = !Features.itemPhysics);
        yb = addVisToggle(xb, yb, colW, "Zoom", () -> Zoom.enabled, () -> Zoom.enabled = !Zoom.enabled);
        addAction(xa, 150, colW, "  Fake Player (P)", () -> FakePlayer.toggle(Minecraft.getInstance()));
        yb = addVisToggle(xb, yb, colW, "Скин iAMKNOW", () -> WorldVisuals.skinOverride, () -> WorldVisuals.skinOverride = !WorldVisuals.skinOverride);
        yb = addVisToggle(xb, yb, colW, "Круг прыжка", () -> WorldVisuals.jumpCircle, () -> WorldVisuals.jumpCircle = !WorldVisuals.jumpCircle);
        ya = addVisToggle(xa, ya, colW, "Трассеры", () -> WorldVisuals.tracers, () -> WorldVisuals.tracers = !WorldVisuals.tracers);
        yb = addVisToggle(xb, yb, colW, "Трассеры мобы", () -> WorldVisuals.tracersMobs, () -> WorldVisuals.tracersMobs = !WorldVisuals.tracersMobs);
        ya = addVisToggle(xa, ya, colW, "Ники над головой", () -> WorldVisuals.nameTag, () -> WorldVisuals.nameTag = !WorldVisuals.nameTag);
        yb = addVisToggle(xb, yb, colW, "Метки врагов", () -> WorldVisuals.enemyLabels, () -> WorldVisuals.enemyLabels = !WorldVisuals.enemyLabels);
        ya = addVisToggle(xa, ya, colW, "Враги (HUD)", () -> HudRenderer.enemiesEnabled, () -> HudRenderer.enemiesEnabled = !HudRenderer.enemiesEnabled);
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
        registerContent(new Ui.PulseSlider(x, y - contentScroll, w, 20, name, min, max, get, set, fmt), y);
        return y + 22;
    }

    private void buildWorld(int cx, int cw) {
        int y = 64;
        addToggle(cx, y, cw, "Ghost Blocks", DupeModClient::isGhostBlocksEnabled, () -> DupeModClient.toggleGhostBlocks(client));
        y += 32;
        addToggle(cx, y, cw, "Scaffold", () -> Scaffold.enabled, () -> Scaffold.enabled = !Scaffold.enabled);
        y += 32;
        addToggle(cx, y, cw, "Автостройка дома", () -> HouseBuilder.enabled, () -> HouseBuilder.toggle(client));
        y += 32;
        addCycle(cx, y, cw, "Время", () -> TimeChanger.MODE_NAMES[TimeChanger.mode],
            () -> TimeChanger.mode = (TimeChanger.mode + 1) % TimeChanger.MODE_NAMES.length);
        y += 38;

        int fw = (cw - 16) / 3;
        tpX = new EditBox(client.font, cx, y - contentScroll, fw, 18, Component.literal("X"));
        tpY = new EditBox(client.font, cx + fw + 8, y - contentScroll, fw, 18, Component.literal("Y"));
        tpZ = new EditBox(client.font, cx + (fw + 8) * 2, y - contentScroll, fw, 18, Component.literal("Z"));
        for (EditBox box : new EditBox[] { tpX, tpY, tpZ }) {
            box.setMaxLength(14);
            registerContent(box, y);
        }
        if (client.player != null) {
            tpX.setValue(String.valueOf((int) Math.floor(client.player.getX())));
            tpY.setValue(String.valueOf((int) Math.floor(client.player.getY())));
            tpZ.setValue(String.valueOf((int) Math.floor(client.player.getZ())));
        }
        y += 24;
        addAction(cx, y, cw, "Телепорт по координатам", () -> {
            try {
                double x = Double.parseDouble(tpX.getValue().trim());
                double yv = Double.parseDouble(tpY.getValue().trim());
                double z = Double.parseDouble(tpZ.getValue().trim());
                DupeModClient.tpTo(client, x, yv, z);
            } catch (NumberFormatException ex) {
                if (client.player != null) {
                    client.player.displayClientMessage(Component.literal("Неверные координаты"), false);
                }
            }
        });
        y += 28;
        addAction(cx, y, cw, "Телепорт к цели (прицел)", () -> DupeModClient.tpToTarget(client));
        y += 34;
        tpName = new EditBox(client.font, cx, y - contentScroll, cw, 18, Component.literal("Ник игрока"));
        tpName.setMaxLength(16);
        registerContent(tpName, y);
        y += 24;
        addAction(cx, y, cw, "Телепорт к игроку по нику", () -> DupeModClient.tpByNick(client, tpName.getValue()));
    }

    private void buildMarks(int cx, int cw) {
        int colW = (cw - 12) / 2;
        int xa = cx, xb = cx + colW + 12;
        int ya = 64, yb = 64;
        ya = addVisToggle(xa, ya, colW, "Метки врагов", () -> WorldVisuals.enemyLabels, () -> WorldVisuals.enemyLabels = !WorldVisuals.enemyLabels);
        yb = addVisToggle(xb, yb, colW, "Ники над головой", () -> WorldVisuals.nameTag, () -> WorldVisuals.nameTag = !WorldVisuals.nameTag);
        ya = addVisToggle(xa, ya, colW, "Трассеры", () -> WorldVisuals.tracers, () -> WorldVisuals.tracers = !WorldVisuals.tracers);
        yb = addVisToggle(xb, yb, colW, "Трассеры мобы", () -> WorldVisuals.tracersMobs, () -> WorldVisuals.tracersMobs = !WorldVisuals.tracersMobs);
        ya = addVisToggle(xa, ya, colW, "Near (стрелки)", () -> WorldVisuals.near, () -> WorldVisuals.near = !WorldVisuals.near);
        yb = addVisToggle(xb, yb, colW, "Круг прыжка", () -> WorldVisuals.jumpCircle, () -> WorldVisuals.jumpCircle = !WorldVisuals.jumpCircle);
        ya = addVisToggle(xa, ya, colW, "Показ невидимок", () -> Features.showInvis, () -> Features.showInvis = !Features.showInvis);
        yb = addVisToggle(xb, yb, colW, "Враги (HUD)", () -> HudRenderer.enemiesEnabled, () -> HudRenderer.enemiesEnabled = !HudRenderer.enemiesEnabled);
        ya = addVisSlider(xa, ya, colW, "Круг радиус", 1.0f, 6.0f, () -> WorldVisuals.circleRadius, v -> WorldVisuals.circleRadius = (float) v, f -> String.format("%.1f", f));
        if (ya < yb) {
            ya = yb;
        }
        addAction(xa, ya, colW, "  Список ивентов", () -> client.setScreen(new EventsScreen()));
    }

    private void buildSound(int cx, int cw) {
        int y = 64;
        addToggle(cx, y, cw, "Усиление звука", () -> Features.soundBoost, () -> Features.soundBoost = !Features.soundBoost);
        y += 32;
        addToggle(cx, y, cw, "Тихий варден", () -> Features.quietWarden, () -> Features.quietWarden = !Features.quietWarden);
    }

    private void buildCreators(int cx, int cw) {
        int y = 64;
        addAction(cx, y, cw, "✚ Забрать всё", () -> {
            boolean done = DupeModClient.transferAll(client, true);
            message(client, done ? "Забрал всё из контейнера" : "Открой сундук/контейнер");
        });
        y += 32;
        addAction(cx, y, cw, "⇩ Выложить всё", () -> {
            boolean done = DupeModClient.transferAll(client, false);
            message(client, done ? "Выложил всё в контейнер" : "Открой сундук/контейнер");
        });
    }

    private static void message(Minecraft client, String text) {
        if (client.player != null) {
            client.player.displayClientMessage(Component.literal(text), false);
        }
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
        y += 32;
        addToggle(cx, y, cw, "RPC детали (коорд.·FPS)", () -> DiscordRpc.showDetails, () -> DiscordRpc.showDetails = !DiscordRpc.showDetails);
    }

    private void buildConsole(int cx, int cw) {
        int y = 64;
        addAction(cx, y, cw, "Открыть командную консоль", () -> client.setScreen(new CommandConsole()));
    }

    private void addToggle(int x, int y, int w, String name, BooleanSupplier state, Runnable toggle) {
        registerContent(new Ui.PulseRow(x, y - contentScroll, w, 26, name,
            state::getAsBoolean,
            () -> state.getAsBoolean() ? "ON" : "OFF",
            toggle), y);
    }

    private void addAction(int x, int y, int w, String name, Runnable action) {
        registerContent(new Ui.PulseRow(x, y - contentScroll, w, 26, name, () -> true, () -> "▶", action), y);
    }

    private void addCycle(int x, int y, int w, String name, Supplier<String> value, Runnable cycle) {
        registerContent(new Ui.PulseRow(x, y - contentScroll, w, 26, name, () -> false, value, cycle), y);
    }

    private void registerContent(AbstractWidget w, int baseY) {
        addRenderableWidget(w);
        contentWidgets.add(w);
        contentYs.add(baseY);
        contentMax = Math.max(contentMax, baseY + w.getHeight());
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

    private int nextMinLen() {
        int[] lens = {1, 2, 3, 4, 5, 6};
        int i = 0;
        for (; i < lens.length; i++) {
            if (lens[i] == PlayerSearch.minLength) {
                break;
            }
        }
        return lens[(i + 1) % lens.length];
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        gui.fillGradient(0, 0, width, height, 0xE6000000, 0xA6000000);
        Font font = client.font;

        gui.fill(0, 0, width, height, 0x14FFFFFF);
        gui.fill(0, height - 1, width, height, Ui.PULSE_LINE);
        gui.fill(0, 0, width, 1, Ui.PULSE_ACCENT);

        String title = "Flash Visual";
        Ui.gradientText(gui, font, title, 18, 14, 0xFFCFCFCF, 0xFF7A7A7A);
        String ver = "v1.1.0-pre1";
        gui.drawString(font, ver, width - font.width(ver) - 18, 16, 0xFF6A6A6A);
        gui.fill(18, 47, width - 18, 48, 0x22FFFFFF);
        gui.fill(SIDEBAR_W, 47, SIDEBAR_W + 1, height, 0x22FFFFFF);

        gui.drawString(font, Component.literal(CATS[category]), 18, 60, 0xFF8A8A8A);

        super.render(gui, mouseX, mouseY, partialTick);

        if (category == 0) {
            renderKeybinds(gui, font);
        }
        if (category == 7) {
            renderConsoleHelp(gui, font);
        }
        if (category == 6) {
            int cx = SIDEBAR_W + 20;
            gui.drawString(font, "Создатели: deviaeostye_41139 · sasha21111", cx, 47, 0xFF8A8A8A);
        }
        renderScrollBar(gui);
    }

    private void renderScrollBar(GuiGraphics gui) {
        int usable = height - 64 - 30;
        if (contentMax <= usable) {
            return;
        }
        int trackX = width - 6;
        int trackTop = 64;
        int trackH = usable;
        int maxScroll = contentMax - usable;
        int thumbH = Math.max(20, trackH * usable / contentMax);
        int thumbY = trackTop + (trackH - thumbH) * contentScroll / maxScroll;
        gui.fill(trackX, trackTop, trackX + 2, trackTop + trackH, 0x22FFFFFF);
        gui.fill(trackX, thumbY, trackX + 2, thumbY + thumbH, 0xFF9A9A9A);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX <= SIDEBAR_W + 16) {
            return false;
        }
        int usable = height - 64 - 30;
        int maxScroll = Math.max(0, contentMax - usable);
        int target = Math.max(0, Math.min(maxScroll, contentScroll - (int) Math.round(verticalAmount * 12)));
        if (target == contentScroll) {
            return true;
        }
        contentScroll = target;
        for (int i = 0; i < contentWidgets.size(); i++) {
            contentWidgets.get(i).setY(contentYs.get(i) - contentScroll);
        }
        return true;
    }

    private void renderConsoleHelp(GuiGraphics gui, Font font) {
        int cx = SIDEBAR_W + 20;
        int y = 120;
        String[] lines = {
            "Клиентские команды (без прав сервера):",
            "  tp <x y z>  — телепорт по координатам",
            "  tp <ник>    — телепорт к игроку",
            "  tptarget    — телепорт к прицелу",
            "  ghost · freecam · killaura · scaffold · autototem · fullbright",
            "  time <day|night|off> · esp <player|mob|item|off>",
            "  house · rpc · suit · zhiguli · zvit · music · target · watermark · fps",
            "  invis · fognr · cls · help",
            "Нужные права сервера не требуются — всё выполняет клиент."
        };
        for (String line : lines) {
            gui.drawString(font, Component.literal(line), cx, y, 0xFF9A9A9A);
            y += 12;
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
        Ui.panel(gui, panelX, panelY, panelW, panelH, Ui.PULSE_PANEL, Ui.PULSE_LINE);
        int kx = panelX + 6;
        for (int i = 0; i < actions.size(); i++) {
            int ky = panelY + 5 + i * rowH;
            String key = Binds.keyName(actions.get(i));
            String desc = Binds.label(actions.get(i));
            int capW = Math.max(30, font.width(key) + 8);
            String d = font.plainSubstrByWidth(desc, panelW - 12 - capW - 6);
            Ui.roundRect(gui, kx, ky, capW, 11, 4, 0xFF101010);
            Ui.roundRect(gui, kx, ky, capW, 11, 4, Ui.PULSE_ACCENT);
            gui.drawCenteredString(font, key, kx + capW / 2, ky + 1, Ui.PULSE_ACCENT);
            gui.drawString(font, Component.literal(d), kx + capW + 6, ky + 2, 0xFF9A9A9A);
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
                cx + 10 + font.width(c[0]) + 8, ly, 0xFF7A7A7A);
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
