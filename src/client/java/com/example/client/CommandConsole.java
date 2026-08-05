package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class CommandConsole extends Screen {
    private static final int LOG_LIMIT = 200;

    private final Minecraft client = Minecraft.getInstance();
    private final List<String> log = new ArrayList<>();
    private final List<String> history = new ArrayList<>();
    private EditBox input;
    private int scroll = 0;
    private int historyIndex = -1;

    public CommandConsole() {
        super(Component.literal("Командная консоль"));
        log("Клиентская консоль · введи 'help' для списка команд");
    }

    private void log(String msg) {
        log.add(msg);
        if (log.size() > LOG_LIMIT) {
            log.remove(0);
        }
        scroll = 0;
    }

    @Override
    protected void init() {
        input = new EditBox(client.font, 12, height - 26, width - 24, 18, Component.literal("Команда"));
        input.setMaxLength(256);
        addRenderableWidget(input);
        setInitialFocus(input);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        gui.fill(0, 0, width, height, 0xFF0A0A0A);
        gui.fill(0, 0, width, 2, Ui.PULSE_ACCENT);
        Font font = client.font;

        gui.drawString(font, "Консоль команд", 12, 8, 0xFFFFFFFF);
        String hint = "Enter — выполнить · Уп/Вниз — история · колесо — скролл · Esc — назад";
        gui.drawString(font, hint, width - font.width(hint) - 12, 8, 0xFF7A7A7A);
        gui.fill(12, 22, width - 12, 23, Ui.PULSE_LINE);

        int top = 30;
        int bottom = height - 34;
        int lines = (bottom - top) / 11;
        int startIdx = log.size() - (lines - scroll);
        if (startIdx < 0) {
            startIdx = 0;
        }
        for (int i = startIdx; i < log.size(); i++) {
            int y = top + (i - startIdx) * 11;
            if (y > bottom) {
                break;
            }
            String line = log.get(i);
            int color = 0xFFC9D2E0;
            if (line.startsWith("> ")) {
                color = 0xFFFFAA00;
            } else if (line.startsWith("OK:") || line.startsWith("on:")) {
                color = Ui.GREEN;
            } else if (line.startsWith("err:") || line.startsWith("?:")) {
                color = 0xFFFF5A5A;
            }
            gui.drawString(font, Component.literal(line), 12, y, color);
        }

        input.render(gui, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_ENTER) {
            submit();
            return true;
        }
        if (event.key() == GLFW.GLFW_KEY_UP) {
            navigateHistory(-1);
            return true;
        }
        if (event.key() == GLFW.GLFW_KEY_DOWN) {
            navigateHistory(1);
            return true;
        }
        return super.keyPressed(event);
    }

    private void navigateHistory(int dir) {
        if (history.isEmpty()) {
            return;
        }
        historyIndex += dir;
        if (historyIndex < 0) {
            historyIndex = 0;
        }
        if (historyIndex >= history.size()) {
            historyIndex = history.size() - 1;
        }
        input.setValue(history.get(historyIndex));
    }

    private void submit() {
        String raw = input.getValue().trim();
        if (raw.isEmpty()) {
            return;
        }
        history.add(raw);
        historyIndex = history.size();
        input.setValue("");
        run(raw);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scroll += (int) verticalAmount;
        if (scroll < 0) {
            scroll = 0;
        }
        int max = Math.max(0, log.size() - (height - 64) / 11);
        if (scroll > max) {
            scroll = max;
        }
        return true;
    }

    private void run(String raw) {
        log("> " + raw);
        String cmd = raw;
        boolean slash = cmd.startsWith("/");
        if (slash) {
            cmd = cmd.substring(1);
        }
        String[] a = cmd.trim().split("\\s+");
        String name = a[0].toLowerCase();
        String arg = a.length > 1 ? raw.substring((slash ? 1 : 0) + a[0].length()).trim() : "";

        if (name.equals("help") || name.equals("?")) {
            log("Команды: tp <x y z|ник> · tptarget · ghost · freecam ·");
            log("killaura · scaffold · autototem · fullbright · time <day|night|off> ·");
            log("esp <player|mob|item|off> · house · rpc · suit · zhiguli · zvit ·");
            log("music · target · watermark · fps · invis · fognr · find <ник> ·");
            log("findoff · findmin <n> · streamer · cfgsave · cfgload · cls · help");
            return;
        }
        if (name.equals("cls") || name.equals("clear")) {
            log.clear();
            return;
        }

        switch (name) {
            case "tp" -> {
                if (client.player == null) {
                    log("err: нет игрока");
                    return;
                }
                if (arg.isEmpty()) {
                    DupeModClient.tpToTarget(client);
                    log("OK: телепорт к прицелу");
                } else if (arg.split("\\s+").length == 3) {
                    try {
                        String[] p = arg.split("\\s+");
                        double x = Double.parseDouble(p[0]);
                        double y = Double.parseDouble(p[1]);
                        double z = Double.parseDouble(p[2]);
                        DupeModClient.tpTo(client, x, y, z);
                        log("OK: телепорт на x y z");
                    } catch (NumberFormatException ex) {
                        log("err: неверные координаты");
                    }
                } else {
                    DupeModClient.tpByNick(client, arg);
                    log("OK: телепорт к " + arg);
                }
            }
            case "tptarget" -> {
                DupeModClient.tpToTarget(client);
                log("OK: телепорт к цели");
            }
            case "ghost" -> {
                DupeModClient.toggleGhostBlocks(client);
                log("on:gghost " + (DupeModClient.isGhostBlocksEnabled() ? "on" : "off"));
            }
            case "freecam" -> {
                FreeCam.toggle(client);
                log("on:freecam " + (FreeCam.isActive() ? "on" : "off"));
            }
            case "killaura" -> {
                KillAura.enabled = !KillAura.enabled;
                log("on:killaura " + (KillAura.enabled ? "on" : "off"));
            }
            case "scaffold" -> {
                Scaffold.enabled = !Scaffold.enabled;
                log("on:scaffold " + (Scaffold.enabled ? "on" : "off"));
            }
            case "autototem" -> {
                AutoTotem.enabled = !AutoTotem.enabled;
                log("on:autototem " + (AutoTotem.enabled ? "on" : "off"));
            }
            case "fullbright" -> {
                Fullbright.toggle(client);
                log("on:fullbright " + (Fullbright.enabled ? "on" : "off"));
            }
            case "time" -> {
                String v = arg.toLowerCase();
                TimeChanger.mode = v.startsWith("night") ? 2 : v.startsWith("day") ? 1 : 0;
                log("on:time " + TimeChanger.MODE_NAMES[TimeChanger.mode]);
            }
            case "esp" -> {
                String v = arg.toLowerCase();
                if (v.contains("mob")) {
                    EspRenderer.mobEsp = !EspRenderer.mobEsp;
                    log("on:mobEsp " + (EspRenderer.mobEsp ? "on" : "off"));
                } else if (v.contains("item")) {
                    EspRenderer.itemEsp = !EspRenderer.itemEsp;
                    log("on:itemEsp " + (EspRenderer.itemEsp ? "on" : "off"));
                } else if (v.startsWith("off")) {
                    EspRenderer.playerEsp = EspRenderer.mobEsp = EspRenderer.itemEsp = false;
                    log("OK: ESP выключен");
                } else {
                    EspRenderer.playerEsp = !EspRenderer.playerEsp;
                    log("on:playerEsp " + (EspRenderer.playerEsp ? "on" : "off"));
                }
            }
            case "house" -> {
                HouseBuilder.toggle(client);
                log("on:house " + (HouseBuilder.enabled ? "on" : "off"));
            }
            case "rpc" -> {
                DiscordRpc.enabled = !DiscordRpc.enabled;
                if (DiscordRpc.enabled) {
                    DiscordRpc.start();
                    log("OK: Discord RPC on");
                } else {
                    DiscordRpc.stop();
                    log("OK: Discord RPC off");
                }
            }
            case "suit" -> {
                WorldVisuals.majorSuit = !WorldVisuals.majorSuit;
                log("on:suit " + (WorldVisuals.majorSuit ? "on" : "off"));
            }
            case "zhiguli" -> {
                WorldVisuals.zhiguli = !WorldVisuals.zhiguli;
                log("on:zhiguli " + (WorldVisuals.zhiguli ? "on" : "off"));
            }
            case "zvit" -> {
                WorldVisuals.zhiguliView = !WorldVisuals.zhiguliView;
                log("on:zhiguliView " + (WorldVisuals.zhiguliView ? "on" : "off"));
            }
            case "music" -> {
                HudRenderer.musicEnabled = !HudRenderer.musicEnabled;
                log("on:music " + (HudRenderer.musicEnabled ? "on" : "off"));
            }
            case "target" -> {
                HudRenderer.targetEnabled = !HudRenderer.targetEnabled;
                log("on:targetHud " + (HudRenderer.targetEnabled ? "on" : "off"));
            }
            case "watermark" -> {
                HudRenderer.watermarkEnabled = !HudRenderer.watermarkEnabled;
                log("on:watermark " + (HudRenderer.watermarkEnabled ? "on" : "off"));
            }
            case "fps" -> {
                HudRenderer.fpsEnabled = !HudRenderer.fpsEnabled;
                log("on:fps " + (HudRenderer.fpsEnabled ? "on" : "off"));
            }
            case "invis" -> {
                Features.showInvis = !Features.showInvis;
                log("on:showInvis " + (Features.showInvis ? "on" : "off"));
            }
            case "fognr" -> {
                Features.noRender = !Features.noRender;
                log("on:noRender " + (Features.noRender ? "on" : "off"));
            }
            case "find" -> {
                if (arg.isEmpty()) {
                    log("err: укажи ник, например: find Notch");
                    return;
                }
                PlayerSearch.enabled = true;
                PlayerSearch.setQuery(arg);
                PlayerSearch.refresh(client);
                log("OK: поиск «" + arg + "» — " + PlayerSearch.resultCount() + " игроков");
            }
            case "findoff" -> {
                PlayerSearch.enabled = false;
                PlayerSearch.setQuery("");
                log("OK: поиск выключен");
            }
            case "findmin" -> {
                try {
                    int n = Integer.parseInt(arg.trim());
                    PlayerSearch.minLength = Math.max(1, Math.min(16, n));
                    log("OK: мин. длина ника = " + PlayerSearch.minLength);
                } catch (NumberFormatException ex) {
                    log("err: нужна цифра");
                }
            }
            case "streamer" -> {
                StreamerMode.enabled = !StreamerMode.enabled;
                log("OK: " + StreamerMode.statusLine());
            }
            case "cfgsave" -> {
                Config.save();
                log("OK: конфиг сохранён");
            }
            case "cfgload" -> {
                Config.load();
                log("OK: конфиг загружен");
            }
            default -> {
                if (client.getConnection() == null) {
                    log("err: нет соединения");
                    return;
                }
                if (slash) {
                    client.getConnection().sendCommand(name + (arg.isEmpty() ? "" : " " + arg));
                    log("→ отправлено (нужны права): /" + name);
                } else {
                    client.getConnection().sendChat(raw);
                    log("→ отправлено в чат");
                }
            }
        }
    }
}