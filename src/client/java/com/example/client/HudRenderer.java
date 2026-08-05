package com.example.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HudRenderer {
    public static boolean musicEnabled = true;
    public static boolean targetEnabled = true;
    public static boolean watermarkEnabled = true;
    public static boolean fpsEnabled = true;
    public static boolean inventoryEnabled = false;
    public static boolean potionEnabled = true;
    public static boolean enemiesEnabled = true;
    private static String currentMusic = null;
    private static int musicTick = 0;
    private static int hudTick = 0;

    private static final int BTN_SIZE = 18;
    private static int panelX, panelY, panelW, panelH;
    private static int prevX, prevY, playX, playY, nextX, nextY;
    private static boolean hoverPrev = false;
    private static boolean hoverPlay = false;
    private static boolean hoverNext = false;
    private static boolean wasLeftDown = false;

    private static final class EnemyRow {
        final String name;
        final float hp;
        final float max;
        final double dist;
        final boolean mob;

        EnemyRow(String name, float hp, float max, double dist, boolean mob) {
            this.name = name;
            this.hp = hp;
            this.max = max;
            this.dist = dist;
            this.mob = mob;
        }
    }

    private static final List<EnemyRow> enemyCache = new ArrayList<>();
    private static int enemyTick = 0;

    public static void tick(Minecraft client) {
        hudTick++;
        if (musicEnabled) {
            musicTick++;
            if (musicTick >= 20) {
                musicTick = 0;
                currentMusic = WinMusicReader.getNowPlaying();
            }
        }
        if (enemiesEnabled) {
            enemyTick++;
            if (enemyTick >= 5) {
                enemyTick = 0;
                rebuildEnemies(client);
            }
        }
        PlayerSearch.tick(client);
    }

    private static void rebuildEnemies(Minecraft client) {
        enemyCache.clear();
        if (client.level == null || client.player == null) {
            return;
        }
        List<EnemyRow> tmp = new ArrayList<>();
        double rangeSq = 128.0 * 128.0;
        for (net.minecraft.world.entity.Entity e : client.level.entitiesForRendering()) {
            if (e == client.player || e.isRemoved()) {
                continue;
            }
            if (e.distanceToSqr(client.player) > rangeSq) {
                continue;
            }
            if (e instanceof Player p) {
                if (Friends.contains(p.getName().getString())) {
                    continue;
                }
                String nm = p.getName().getString();
                tmp.add(new EnemyRow(nm, p.getHealth(), Math.max(1, p.getMaxHealth()),
                    Math.sqrt(e.distanceToSqr(client.player)), false));
            } else if (e instanceof Monster mo) {
                tmp.add(new EnemyRow(mo.getType().getDescription().getString(), mo.getHealth(),
                    Math.max(1, mo.getMaxHealth()), Math.sqrt(e.distanceToSqr(client.player)), true));
            }
        }
        tmp.sort(Comparator.comparingDouble(r -> r.dist));
        int keep = Math.min(tmp.size(), 10);
        for (int i = 0; i < keep; i++) {
            enemyCache.add(tmp.get(i));
        }
    }

    public static void render(GuiGraphics gui, DeltaTracker delta, Minecraft client) {
        if (client.player == null) {
            return;
        }
        FreeCam.update(client, delta.getRealtimeDeltaTicks());
        HudDrag.beginFrame(client);
        if (musicEnabled) {
            renderMusic(gui, client);
        }
        if (targetEnabled) {
            renderTarget(gui, client);
        }
        if (watermarkEnabled) {
            renderWatermark(gui, client);
        }
        if (Minimap.enabled) {
            Minimap.render(gui, client);
        }
        if (inventoryEnabled) {
            InventoryHud.render(gui, client);
        }
        if (potionEnabled) {
            PotionHud.render(gui, client);
        }
        if (StaffHud.enabled) {
            StaffHud.render(gui, client);
        }
        if (CoordinatesHud.enabled) {
            CoordinatesHud.render(gui, client);
        }
        if (JojoHud.enabled) {
            JojoHud.render(gui, client);
        }
        if (fpsEnabled) {
            renderFps(gui, client);
        }
        if (enemiesEnabled) {
            renderEnemies(gui, client);
        }
        PlayerSearch.render(gui, client);
        if (StreamerMode.isOn()) {
            renderStreamerBadge(gui, client);
        }
        WorldVisuals.render(gui, client);
        HudDrag.endFrame(client);
    }

    private static void renderStreamerBadge(GuiGraphics gui, Minecraft client) {
        Font font = client.font;
        String label = "STREAMER MODE";
        int w = font.width(label) + 16;
        int[] pos = HudPos.get("streamer", gui.guiWidth() - w - 6, 4);
        int x = pos[0];
        int y = pos[1];
        Ui.panel(gui, x, y, w, 14, 0xB00B0F1A, 0xFFFF5555);
        HudDrag.setArea("streamer", x, y, w, 14);
        gui.drawString(font, Component.literal(label), x + 8, y + 3, 0xFFFF8888);
    }

    private static void renderEnemies(GuiGraphics gui, Minecraft client) {
        if (enemyCache.isEmpty()) {
            return;
        }
        Font font = client.font;
        int w = 140;
        int rowH = 16;
        int headH = 11;
        int n = enemyCache.size();
        int panelH = headH + n * rowH + 2;
        int[] pos = HudPos.get("enemies", gui.guiWidth() - w - 6, gui.guiHeight() / 2 - panelH / 2);
        int x = pos[0];
        int y = pos[1];
        Ui.panel(gui, x, y, w, panelH, Ui.PULSE_PANEL, Ui.PULSE_LINE);
        HudDrag.setArea("enemies", x, y, w, panelH);
        String head = "ВРАГИ (" + n + ")";
        int hw = font.width(head);
        gui.fill(x, y, x + w, y + 1, Ui.PULSE_ACCENT);
        gui.drawString(font, Component.literal(head), x + (w - hw) / 2, y + 2, Ui.PULSE_ACCENT);

        int ry = y + headH;
        for (EnemyRow r : enemyCache) {
            int nameCol = r.mob ? 0xFF9A6E6E : 0xFFFFD24A;
            gui.drawString(font, Component.literal(r.name), x + 5, ry, nameCol);
            String ds = String.format("%.0fm", r.dist);
            gui.drawString(font, Component.literal(ds), x + w - font.width(ds) - 4, ry, 0xFF9A9A9A);
            int barX = x + 5;
            int barY = ry + 8;
            int barW = w - 10;
            float frac = Math.min(1.0f, r.hp / r.max);
            gui.fill(barX, barY, barX + barW, barY + 1, 0x40202020);
            if (frac > 0) {
                int hc = frac > 0.5f ? Ui.GREEN : (frac > 0.2f ? 0xFFFFAA00 : Ui.RED);
                gui.fill(barX, barY, barX + (int) (barW * frac), barY + 1, hc);
            }
            ry += rowH;
        }
    }

    private static void renderFps(GuiGraphics gui, Minecraft client) {
        Font font = client.font;
        int fps = client.getFps();
        String label = "FPS";
        String val = String.valueOf(fps);
        int w = font.width(label) + font.width(val) + 16;
        int[] pos = HudPos.get("fps", 4, 4);
        int x = pos[0];
        int y = pos[1];
        Ui.panel(gui, x, y, w, 14, 0xA0121212, 0x33FFAA00);
        HudDrag.setArea("fps", x, y, w, 14);
        gui.drawString(font, Component.literal(label), x + 5, y + 3, 0xFF9A9A9A);
        int color = fps >= 60 ? Ui.GREEN : (fps >= 30 ? 0xFFFFAA00 : Ui.RED);
        gui.drawString(font, Component.literal(val), x + 5 + font.width(label) + 4, y + 3, color);
    }

    private static void renderWatermark(GuiGraphics gui, Minecraft client) {
        Font font = client.font;
        String name = "Flash Visual";
        String ver = "v1.1.0-pre1";
        int w = font.width(name) + font.width(ver) + 18;
        int x = 4;
        int y = 4;
        Ui.panel(gui, x, y, w, 14, 0xA0121212, 0x33FFAA00);
        Ui.gradientText(gui, font, name, x + 6, y + 3, 0xFFFF8800, 0xFFFFD24A);
        gui.drawString(font, Component.literal(ver), x + 6 + font.width(name) + 5, y + 3, 0xFF9A9A9A);
    }

    public static String getCurrentMusic() {
        return currentMusic;
    }

    public static int getLastFps() {
        return Minecraft.getInstance().getFps();
    }

    public static void updateInteraction(Minecraft client) {
        hoverPrev = false;
        hoverPlay = false;
        hoverNext = false;
        if (!musicEnabled || client.getWindow() == null || client.screen != null) {
            return;
        }
        long handle = client.getWindow().handle();
        double[] px = new double[1];
        double[] py = new double[1];
        GLFW.glfwGetCursorPos(handle, px, py);
        double scale = client.getWindow().getGuiScale();
        int mx = (int) (px[0] / scale);
        int my = (int) (py[0] / scale);
        hoverPrev = inBtn(mx, my, prevX, prevY);
        hoverPlay = inBtn(mx, my, playX, playY);
        hoverNext = inBtn(mx, my, nextX, nextY);
        boolean down = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        if (down && !wasLeftDown) {
            if (hoverPrev) {
                WinMusicReader.prev();
            } else if (hoverPlay) {
                WinMusicReader.playPause();
            } else if (hoverNext) {
                WinMusicReader.next();
            }
        }
        wasLeftDown = down;
    }

    private static boolean inBtn(int mx, int my, int bx, int by) {
        return mx >= bx && mx < bx + BTN_SIZE && my >= by && my < by + BTN_SIZE;
    }

    private static void renderMusic(GuiGraphics gui, Minecraft client) {
        SmtcReader.start();
        Font font = client.font;

        boolean smtcAlive = SmtcReader.alive();
        boolean smtcPlaying = smtcAlive && SmtcReader.isPlaying();
        long smtcPos = smtcAlive ? SmtcReader.position() : -1;
        long smtcDur = smtcAlive ? SmtcReader.duration() : -1;
        boolean smtcHasTitle = smtcAlive && SmtcReader.title() != null;

        String title;
        String artist;
        long pos = -1;
        long dur = -1;
        boolean playing = false;

        if (smtcHasTitle) {
            // SMTC даёт название даже когда длительность неизвестна (стримы, вкладки браузеров)
            playing = smtcPlaying;
            title = SmtcReader.title();
            artist = SmtcReader.artist() == null ? "" : SmtcReader.artist();
            dur = smtcDur;
            if (playing) {
                if (dur > 0) {
                    long poll = SmtcReader.lastPollMs();
                    if (poll > 0) {
                        long now = System.currentTimeMillis();
                        long elapsed = now - poll;
                        if (elapsed < 0) elapsed = 0;
                        if (elapsed < 2000) {
                            pos = Math.min(smtcPos + elapsed, dur);
                        } else {
                            pos = smtcPos;
                        }
                    } else {
                        pos = smtcPos;
                    }
                } else {
                    pos = smtcPos;
                }
            }
        } else {
            // фоллбек на парсинг заголовков окон (обновляется в tick())
            pos = WinMusicReader.realPosition();
            dur = WinMusicReader.realDuration();
            playing = pos >= 0 && dur > 0;
            if (currentMusic != null) {
                String[] parts = splitArtistTitle(currentMusic);
                title = parts[0];
                artist = parts[1];
            } else {
                title = "Музыка не играет";
                artist = "";
            }
        }

        // защита от overshoot (не гасим playing, если длительность неизвестна)
        if (dur > 0 && pos >= 0) {
            pos = Math.min(pos, dur);
        } else if (dur <= 0 && playing) {
            pos = pos >= 0 ? pos : -1;
        }

        int panelH = 46;
        int w = 264;
        int[] mpos = HudPos.get("music", client.getWindow().getGuiScaledWidth() - w, 0);
        int x = mpos[0];
        int y = mpos[1];

        int cov = 32;
        int covX = x + 7;
        int covY = y + 7;

        int btnW = 18;
        int btnGap = 2;
        int btnsW = btnW * 3 + btnGap * 2;
        prevX = x + w - 8 - btnsW;
        playX = prevX + btnW + btnGap;
        nextX = playX + btnW + btnGap;
        int btnY = y + 13;
        prevY = btnY;
        playY = btnY;
        nextY = btnY;

        int textX = covX + cov + 9;
        int textEnd = prevX - 6;
        int textMax = textEnd - textX;

        panelX = x;
        panelY = y;
        panelW = w;
        panelH = panelH;

        Ui.panel(gui, x, y, w, panelH, Ui.PANEL, Ui.PULSE_LINE);
        HudDrag.setArea("music", x, y, w, 11);
        if (playing) {
            Ui.roundRect(gui, x, y, 2, panelH, 1, Ui.PULSE_ACCENT);
        }

        drawCover(gui, covX, covY, cov, playing, smtcAlive ? SmtcReader.appId() : null);

        int textY = y + 7;
        if (textMax > 8 && font.width(title) > textMax) {
            int scroll = (hudTick / 4) % (font.width(title) + 30);
            gui.enableScissor(textX, y, textMax, panelH);
            gui.drawString(font, Component.literal(title), textX - scroll, textY, 0xFFFFFF);
            gui.disableScissor();
        } else {
            gui.drawString(font, Component.literal(title), textX, textY, 0xFFFFFF);
        }
        if (!artist.isEmpty() && textMax > 8) {
            gui.drawString(font, Component.literal(artist), textX, textY + 10, 0xFF9A9A9A);
        }

        drawMediaButton(gui, font, "⏮", prevX, prevY, hoverPrev);
        drawMediaButton(gui, font, playing ? "⏸" : "▶", playX, playY, hoverPlay);
        drawMediaButton(gui, font, "⏭", nextX, nextY, hoverNext);

        int bw = w - 14;
        int barY = y + panelH - 6;
        gui.fill(x + 7, barY, x + 7 + bw, barY + 3, 0x22FFFFFF);
        if (playing && dur > 0 && pos >= 0) {
            int fill = (int) (bw * Math.min(1.0, (double) pos / dur));
            Ui.roundRect(gui, x + 7, barY, Math.max(1, fill), 3, 1, Ui.PULSE_ACCENT);
            String t = fmt(pos) + " / " + fmt(dur);
            gui.drawString(font, Component.literal(t), x + 7, barY - 9, 0xFF9A9A9A);
        }
    }

    private static String fmt(long ms) {
        long total = Math.max(0, (ms + 500) / 1000);
        long m = total / 60;
        long s = total % 60;
        return m + ":" + (s < 10 ? "0" : "") + s;
    }

    private static void drawCover(GuiGraphics gui, int x, int y, int size, boolean playing, String appId) {
        if (playing) {
            gui.fillGradient(x, y, x + size, y + size, 0xFF2A2A2A, 0xFF080808);
        } else {
            gui.fill(x, y, x + size, y + size, 0x44000000);
        }
        gui.renderOutline(x, y, size, size, playing ? 0xFF9A9A9A : 0xFF3A3A3A);
        Font font = Minecraft.getInstance().font;
        int cy = y + size / 2 - 4;
        if (playing) {
            String app = (appId != null && !appId.isEmpty()) ? appId : WinMusicReader.lastApp();
            IconSpec spec = appSpec(app);
            int pad = 4;
            gui.fill(x + pad, y + pad, x + size - pad, y + size - pad, spec.color);
            gui.fillGradient(x + pad, y + size / 2, x + size - pad, y + size - pad, 0x00000000, 0x50000000);
            gui.drawCenteredString(font, spec.glyph, x + size / 2, cy, 0xFFFFFFFF);
        } else {
            gui.drawCenteredString(font, "♫", x + size / 2, cy, 0xFF666666);
        }
    }

    private static final class IconSpec {
        final int color;
        final String glyph;

        IconSpec(int color, String glyph) {
            this.color = color;
            this.glyph = glyph;
        }
    }

    private static IconSpec appSpec(String app) {
        if (app == null) {
            return new IconSpec(0xFF444444, "♪");
        }
        String a = app.toLowerCase();
        if (a.contains("spotify")) return new IconSpec(0xFF1DB954, "♪");
        if (a.contains("youtube")) return new IconSpec(0xFFFF0000, "▶");
        if (a.contains("яндекс") || a.contains("yandex")) return new IconSpec(0xFFFFCC00, "Я");
        if (a.contains("vk") || a.contains("вк")) return new IconSpec(0xFF4C75A3, "VK");
        if (a.contains("soundcloud")) return new IconSpec(0xFFFF5500, "☁");
        if (a.contains("deezer")) return new IconSpec(0xFFA238FF, "D");
        if (a.contains("vlc")) return new IconSpec(0xFFFF7A00, "V");
        if (a.contains("aimp")) return new IconSpec(0xFF3D7CC9, "A");
        if (a.contains("foobar")) return new IconSpec(0xFF2ECC40, "f");
        if (a.contains("winamp")) return new IconSpec(0xFFFF6B2C, "W");
        if (a.contains("tidal")) return new IconSpec(0xFF00FFFF, "T");
        return new IconSpec(0xFFFFAA00, "♪");
    }

    private static String[] splitArtistTitle(String raw) {
        String t = raw.trim();
        int idx = t.lastIndexOf(" - ");
        if (idx > 0) {
            String first = t.substring(0, idx).trim();
            String second = t.substring(idx + 3).trim();
            if (!first.isEmpty() && !second.isEmpty()) {
                return new String[]{first, second};
            }
        }
        return new String[]{t, ""};
    }

    private static void drawMediaButton(GuiGraphics gui, Font font, String glyph, int bx, int by, boolean hover) {
        int bg = hover ? 0x55FFFFFF : 0x22111111;
        Ui.roundRect(gui, bx, by, BTN_SIZE, BTN_SIZE, 4, bg);
        gui.drawCenteredString(font, glyph, bx + BTN_SIZE / 2, by + (BTN_SIZE - 8) / 2,
            hover ? 0xFFFFFFFF : 0xFFCCCCCC);
    }

    private static void renderTarget(GuiGraphics gui, Minecraft client) {
        if (!(client.hitResult instanceof EntityHitResult ehr)) {
            return;
        }
        if (!(ehr.getEntity() instanceof LivingEntity target) || target == client.player) {
            return;
        }
        int w = client.getWindow().getGuiScaledWidth();
        int h = client.getWindow().getGuiScaledHeight();
        int cx = w / 2;
        int cxLeft = w / 2 - 130;
        Font font = client.font;
        int[] toff = HudPos.get("target", 0, 0);

        String name = target.getName().getString();
        String own = StreamerMode.ownNick(client);
        if (own != null && own.equalsIgnoreCase(name)) {
            name = "Вы";
        }
        boolean friend = target instanceof Player && Friends.contains(name);
        int nameColor = friend ? Ui.GREEN : (target instanceof Player ? 0xFF2ECC40 : 0xFFFFFFFF);

        float hp = Math.max(0, target.getHealth());
        float maxHp = Math.max(1, target.getMaxHealth());
        double dist = Math.round(client.player.distanceTo(target) * 10.0) / 10.0;
        String typeName = target.getType().getDescription().getString();

        int hpW = 120;
        int x0 = cxLeft - hpW / 2 + toff[0];
        int barY = h / 2 + 26 + toff[1];

        Ui.panel(gui, x0 - 4, barY - 4, hpW + 8, 16, 0x90000000, 0xFF333333);
        int fillW = (int) (hpW * Math.min(1, hp / maxHp));
        int hpColor = hp / maxHp > 0.5 ? 0xFF2ECC40 : (hp / maxHp > 0.2 ? 0xFFFFAA00 : 0xFFFF4136);
        if (fillW > 0) {
            gui.fillGradient(x0, barY, x0 + fillW, barY + 6, 0xFF000000 | mixLight(hpColor), hpColor);
        }
        if (friend) {
            gui.fill(x0 - 2, barY, x0 - 1, barY + 6, Ui.GREEN);
        }

        int nameW = font.width(name);
        if (friend) {
            String tag = "★ ";
            int tagW = font.width(tag);
            int totalW = tagW + nameW;
            int nameX = cxLeft - totalW / 2 + toff[0];
            gui.drawString(font, Component.literal(tag), nameX, barY - 16, Ui.GREEN);
            gui.drawString(font, Component.literal(name), nameX + tagW, barY - 16, nameColor);
        } else {
            gui.drawString(font, Component.literal(name), cxLeft - nameW / 2 + toff[0], barY - 16, nameColor);
        }

        String info = typeName + "  " + (int) hp + "/" + (int) maxHp + "  " + dist + "m";
        int infoW = font.width(info);
        gui.drawString(font, Component.literal(info), cxLeft - infoW / 2 + toff[0], barY + 10, 0xFF9A9A9A);

        int armor = target.getArmorValue();
        if (armor > 0) {
            int armorW = 120;
            int aX0 = cxLeft - armorW / 2 + toff[0];
            int aY = barY + 14;
            int aFill = (int) (armorW * Math.min(1, armor / 20.0));
            gui.fill(aX0, aY, aX0 + armorW, aY + 4, 0x90000000);
            gui.fill(aX0, aY, aX0 + aFill, aY + 4, 0xFFAAAAAA);
        }

        renderEquipment(gui, font, target, cxLeft + toff[0], barY + 26);
    }

    private static void renderEquipment(GuiGraphics gui, Font font, LivingEntity target, int cx, int top) {
        EquipmentSlot[] slots = {
            EquipmentSlot.MAINHAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST,
            EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.OFFHAND
        };
        int n = slots.length;
        int tile = 18;
        int slot = 16;
        int eqW = n * tile;
        int x0 = cx - eqW / 2;
        Ui.panel(gui, x0 - 3, top - 2, eqW + 6, tile + 4, 0x90000000, 0xFF333333);
        HudDrag.setArea("target", x0 - 4, top - 6, eqW + 8, tile + 8);
        for (int i = 0; i < n; i++) {
            int sx = x0 + i * tile;
            gui.fill(sx, top, sx + slot, top + slot, 0x66000000);
            gui.renderOutline(sx, top, slot, slot, 0xFF444444);
            var stack = target.getItemBySlot(slots[i]);
            if (!stack.isEmpty()) {
                gui.renderItem(stack, sx + 1, top + 1);
            }
        }
    }

    private static int mixLight(int color) {
        int r = (color >> 16) & 0xFF, g = (color >> 8) & 0xFF, b = color & 0xFF;
        return (r * 2 + 255) / 3 << 16 | (g * 2 + 255) / 3 << 8 | (b * 2 + 255) / 3;
    }
}
