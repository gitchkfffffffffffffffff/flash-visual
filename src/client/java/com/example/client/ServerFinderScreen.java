package com.example.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerFinderScreen extends Screen {
    private static final int STATE_CHECKING = 0;
    private static final int STATE_ONLINE = 1;
    private static final int STATE_ONLINE_EMPTY = 2;
    private static final int STATE_OFFLINE = 3;

    private static final String[] AC_KEYWORDS = {
        "anticheat", "anti-cheat", "anti cheat", "nocheatplus", "no cheat plus",
        "matrix", "grim", "vulcan", "aac", "negativity", "spartan", "verus",
        "zeus", "anticom", "antibot", "geyser", "oreo", "wraith", "lynx",
        "intave", "ncp", "anti-aura", "antiaura"
    };

    private final Minecraft client = Minecraft.getInstance();
    private final List<ServerEntry> entries = new ArrayList<>();
    private final AtomicBoolean checking = new AtomicBoolean(false);
    private EditBox ipBox;
    private boolean onlyEmpty = false;
    private int scroll = 0;
    private int selected = -1;

    public ServerFinderScreen() {
        super(Component.literal("Поиск серверов"));
    }

    private static final class ServerEntry {
        volatile String ip;
        volatile int state = STATE_OFFLINE;
        volatile int players = 0;
        volatile int max = 0;
        volatile String info = "";
        volatile String motd = "";
        volatile boolean antiCheat = false;

        ServerEntry(String ip) {
            this.ip = ip.trim();
        }
    }

    @Override
    public void renderBackground(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    protected void init() {
        ipBox = new EditBox(client.font, 20, 32, 300, 18, Component.literal("IP:port или список"));
        ipBox.setMaxLength(2000);
        ipBox.setValue("");
        addRenderableWidget(ipBox);

        addRenderableWidget(new Ui.StyledButton(330, 30, 70, 20, Component.literal("Добавить"), Ui.CYAN, b -> addIps()));
        addRenderableWidget(new Ui.StyledButton(410, 30, 110, 20, Component.literal("Проверить все"), Ui.ACCENT, b -> checkAll()));
        addRenderableWidget(new Ui.StyledButton(530, 30, 120, 20, Component.literal("Только пустые"), onlyEmpty ? Ui.GREEN : 0xFF444444,
            b -> {
                onlyEmpty = !onlyEmpty;
                ((Ui.StyledButton) b).setAccent(onlyEmpty ? Ui.GREEN : 0xFF444444);
            }));
        addRenderableWidget(new Ui.StyledButton(660, 30, 90, 20, Component.literal("Копировать"), 0xFF555555,
            b -> copySelected()));
        addRenderableWidget(new Ui.StyledButton(760, 30, 60, 20, Component.literal("Удалить"), 0xFF444444,
            b -> removeSelected()));
        addRenderableWidget(new Ui.StyledButton(16, height - 34, 120, 20, Component.literal("Подключиться"), Ui.GREEN,
            b -> connectSelected()));
        addRenderableWidget(new Ui.StyledButton(client.getWindow().getGuiScaledWidth() - 80, height - 34, 60, 20,
            Component.literal("Назад"), 0xFF444444, b -> client.setScreen(new DupeGuiScreen())));
    }

    private void addIps() {
        String raw = ipBox.getValue();
        for (String part : raw.split("[\n,;\\s]+")) {
            String ip = part.trim();
            if (ip.isEmpty()) {
                continue;
            }
            if (entries.stream().noneMatch(e -> e.ip.equalsIgnoreCase(ip))) {
                entries.add(new ServerEntry(ip));
            }
        }
        ipBox.setValue("");
        scroll = 0;
        checkAll();
    }

    private void checkAll() {
        if (checking.get() || entries.isEmpty()) {
            return;
        }
        checking.set(true);
        Thread worker = new Thread(() -> {
            try {
                for (ServerEntry e : entries) {
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    e.state = STATE_CHECKING;
                    e.info = "";
                    try {
                        fetch(e);
                    } catch (Throwable ignored) {
                        e.state = STATE_OFFLINE;
                        e.info = "ошибка";
                    }
                }
            } finally {
                checking.set(false);
            }
        }, "flash-server-check");
        worker.setDaemon(true);
        worker.start();
    }

    private void fetch(ServerEntry e) throws Exception {
        String host = e.ip.replaceFirst("^.*://", "");
        HttpURLConnection conn = (HttpURLConnection) new URL("https://api.mcsrvstat.us/3/" + host).openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "FlashVisual/1.1.0");
        conn.setRequestProperty("Accept", "application/json");
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } finally {
            conn.disconnect();
        }
        JsonObject root = JsonParser.parseString(sb.toString()).getAsJsonObject();
        if (!root.has("online") || !root.get("online").getAsBoolean()) {
            e.state = STATE_OFFLINE;
            e.info = "оффлайн";
            return;
        }
        int players = 0;
        int max = 0;
        if (root.has("players")) {
            JsonObject pl = root.getAsJsonObject("players");
            players = pl.has("online") ? pl.get("online").getAsInt() : 0;
            max = pl.has("max") ? pl.get("max").getAsInt() : 0;
        }
        String ver = "";
        if (root.has("version")) {
            JsonElement v = root.get("version");
            if (v != null && !v.isJsonNull()) {
                ver = v.getAsString();
            }
        }
        String motd = "";
        if (root.has("motd")) {
            JsonElement m = root.get("motd");
            if (m != null && m.isJsonObject()) {
                JsonObject mo = m.getAsJsonObject();
                JsonElement clean = mo.get("clean");
                if (clean != null && !clean.isJsonNull()) {
                    if (clean.isJsonArray()) {
                        List<String> lines = new ArrayList<>();
                        for (JsonElement le : clean.getAsJsonArray()) {
                            if (le != null && !le.isJsonNull()) {
                                lines.add(le.getAsString().trim());
                            }
                        }
                        motd = String.join(" · ", lines);
                    } else {
                        motd = clean.getAsString();
                    }
                }
            }
        }
        e.players = players;
        e.max = max;
        e.motd = motd;
        e.antiCheat = hasAntiCheat(motd);
        e.info = ver;
        e.state = players == 0 ? STATE_ONLINE_EMPTY : STATE_ONLINE;
    }

    private static boolean hasAntiCheat(String motd) {
        String low = motd.toLowerCase(Locale.ROOT);
        for (String kw : AC_KEYWORDS) {
            if (low.contains(kw)) {
                return true;
            }
        }
        return false;
    }

    private void connectSelected() {
        if (selected < 0 || selected >= entries.size()) {
            message("Сначала выбери сервер");
            return;
        }
        ServerEntry e = entries.get(selected);
        ServerData data = new ServerData(e.ip, e.ip, ServerData.Type.OTHER);
        ServerAddress addr = ServerAddress.parseString(e.ip);
        ConnectScreen.startConnecting(this, client, addr, data, false, new TransferState(Map.of(), Map.of(), false));
    }

    private void copySelected() {
        if (selected < 0 || selected >= entries.size()) {
            message("Сначала выбери сервер");
            return;
        }
        client.keyboardHandler.setClipboard(entries.get(selected).ip);
        message("IP скопирован: " + entries.get(selected).ip);
    }

    private void removeSelected() {
        if (selected < 0 || selected >= entries.size()) {
            return;
        }
        entries.remove(selected);
        selected = -1;
        scroll = 0;
    }

    private void message(String text) {
        if (client.player != null) {
            client.player.displayClientMessage(Component.literal(text), false);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        if (event.buttonInfo().button() == 0) {
            int idx = rowAt((int) event.x(), (int) event.y());
            if (idx >= 0) {
                selected = idx;
                return true;
            }
        }
        return super.mouseClicked(event, bl);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxScroll = Math.max(0, visibleCount() - visibleRows());
        scroll = Math.max(0, Math.min(maxScroll, scroll + (verticalAmount > 0 ? -1 : 1)));
        return true;
    }

    private List<ServerEntry> visible() {
        if (!onlyEmpty) {
            return entries;
        }
        List<ServerEntry> out = new ArrayList<>();
        for (ServerEntry e : entries) {
            if (e.state == STATE_ONLINE_EMPTY) {
                out.add(e);
            }
        }
        return out;
    }

    private int visibleCount() {
        return visible().size();
    }

    private int visibleRows() {
        return Math.max(1, (height - 170) / 20);
    }

    private int rowAt(int mouseX, int mouseY) {
        int listX = 16;
        int listY = 72;
        int panelW = width - 32;
        if (mouseX < listX || mouseX >= listX + panelW) {
            return -1;
        }
        List<ServerEntry> vis = visible();
        int rows = visibleRows();
        int startIdx = scroll;
        for (int i = 0; i < rows; i++) {
            int idx = startIdx + i;
            if (idx >= vis.size()) {
                break;
            }
            int y0 = listY + 8 + i * 20;
            if (mouseY >= y0 && mouseY < y0 + 20) {
                return idx;
            }
        }
        return -1;
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.render(gui, mouseX, mouseY, partialTick);

        int w = client.getWindow().getGuiScaledWidth();
        int h = client.getWindow().getGuiScaledHeight();
        Ui.gradientText(gui, client.font, "Поиск серверов", 20, 6, 0xFF00A8FF, 0xFF9DFFE0);
        gui.drawString(client.font, Component.literal("(пустые подсвечены зелёным)"), 250, 10, 0xFF6A6A6A);

        int listX = 16;
        int listY = 72;
        int listW = w - 32;
        int listH = h - 150;
        Ui.panel(gui, listX, listY, listW, listH, Ui.PULSE_PANEL, 0xFF26304A);

        int rows = visibleRows();
        List<ServerEntry> vis = visible();
        for (int i = 0; i < rows; i++) {
            int idx = scroll + i;
            if (idx >= vis.size()) {
                break;
            }
            ServerEntry e = vis.get(idx);
            int y0 = listY + 8 + i * 20;
            int color = rowColor(e.state);
            gui.fill(listX + 6, y0, listX + listW - 6, y0 + 18, 0x22000000);
            gui.fill(listX + 6, y0, listX + 10, y0 + 18, color);
            gui.drawString(client.font, Component.literal(e.ip), listX + 16, y0 + 5, 0xFFFFFFFF);
            String right = e.state == STATE_CHECKING ? "проверка..." : (e.state == STATE_OFFLINE ? e.info : (e.info + " · " + e.players + "/" + e.max + (e.players == 0 ? " · ПУСТО" : "")));
            int rightColor = e.state == STATE_OFFLINE ? 0xFF777777 : (e.antiCheat ? 0xFFFF5555 : 0xFF9A9A9A);
            if (e.antiCheat && e.state != STATE_CHECKING && e.state != STATE_OFFLINE) {
                right += " · ⛔AC";
            }
            gui.drawString(client.font, Component.literal(right), listX + listW - 16 - client.font.width(right), y0 + 5,
                rightColor);
            if (selected == idx) {
                gui.renderOutline(listX + 6, y0, listW - 12, 18, 0xFFFFFFFF);
            }
        }

        int emptyCount = 0;
        for (ServerEntry e : entries) {
            if (e.state == STATE_ONLINE_EMPTY) {
                emptyCount++;
            }
        }
        String stat = "Серверов: " + entries.size() + " · Пустых: " + emptyCount + (checking.get() ? " · проверка..." : "");
        gui.drawString(client.font, Component.literal(stat), listX, h - 60, 0xFF9A9A9A);
        gui.drawString(client.font, Component.literal("⛔AC = на сервере упомянут античит · клик по строке — выбор, Подключиться — вход"),
            listX, h - 48, 0xFF6A6A6A);
    }

    private static int rowColor(int state) {
        return switch (state) {
            case STATE_ONLINE -> 0xFFFFAA00;
            case STATE_ONLINE_EMPTY -> Ui.GREEN;
            case STATE_OFFLINE -> 0xFF444444;
            default -> 0xFF00CFFF;
        };
    }
}
