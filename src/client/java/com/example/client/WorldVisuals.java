package com.example.client;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;

public class WorldVisuals {
    public static boolean chinaHat = false;
    public static boolean jumpCircle = false;
    public static boolean tracers = false;
    public static boolean tracersMobs = false;
    public static boolean nameTag = false;
    public static boolean penis = false;

    public static float hatScale = 1.0f;
    public static float circleRadius = 3.0f;

    private static final double RANGE = 64.0;
    private static final int HAT_COLOR = 0xDDFF5555;
    private static final int CIRCLE_COLOR = 0xCCFFAA00;
    private static final int TRACER_COLOR = 0x88FFFFFF;
    private static final int MOB_COLOR = 0xCCFF5555;
    private static final int PENIS_COLOR = 0xFFE9A0A5;
    private static final int GLANS_COLOR = 0xFFD9778A;
    private static final int BALL_COLOR = 0xFFD9A0A5;

    public static void render(GuiGraphics gui, Minecraft client) {
        if (client.level == null || client.player == null) {
            return;
        }
        Camera cam = client.gameRenderer.getMainCamera();
        if (!cam.isInitialized()) {
            return;
        }
        if (!(chinaHat || jumpCircle || tracers || tracersMobs || nameTag || penis)) {
            return;
        }

        int w = gui.guiWidth();
        int h = gui.guiHeight();
        Vector3fc fwd = cam.forwardVector();
        Vec3 camPos = cam.position();
        double rangeSq = RANGE * RANGE;
        int accent = Theme.current();

        for (Entity e : client.level.entitiesForRendering()) {
            if (e == client.player || e.isRemoved()) {
                continue;
            }
            if (e.distanceToSqr(client.player) > rangeSq) {
                continue;
            }
            boolean isPlayer = e instanceof Player;
            boolean isMob = e instanceof Monster;
            if (!isPlayer && !isMob) {
                continue;
            }

            double topY = e.getY() + e.getBbHeight();

            Vec3 head = new Vec3(e.getX(), topY, e.getZ());
            Vec3 feet = e.position();

            if (chinaHat && isPlayer) {
                renderHat(gui, client, e, head, camPos, fwd, w, h);
            }

            if (penis && isPlayer) {
                renderPenis(gui, client, e, feet, camPos, fwd, w, h);
            }

            if (jumpCircle && isPlayer) {
                renderCircle(gui, client, feet, circleRadius, camPos, fwd, w, h, accent);
            } else if (jumpCircle && isMob && tracersMobs) {
                renderCircle(gui, client, feet, circleRadius, camPos, fwd, w, h, MOB_COLOR);
            }

            if (nameTag && isPlayer) {
                renderNameTag(gui, client, (Player) e, head, camPos, fwd, w, h);
            }

            if ((tracers && isPlayer) || (tracersMobs && isMob)) {
                renderTracer(gui, client, feet, camPos, fwd, w, h, isPlayer ? accent : TRACER_COLOR);
            }
        }

        if (jumpCircle && client.player != null) {
            renderCircle(gui, client, client.player.position(), circleRadius, camPos, fwd, w, h, CIRCLE_COLOR);
        }
    }

    private static void renderTracer(GuiGraphics gui, Minecraft client, Vec3 world, Vec3 camPos, Vector3fc fwd,
                                     int w, int h, int color) {
        double[] p = project(gui, client, world, camPos, fwd, w, h);
        if (p == null) {
            return;
        }
        line(gui, w / 2.0, h, p[0], p[1], 1.0f, color);
    }

    private static void renderCircle(GuiGraphics gui, Minecraft client, Vec3 center, double radius,
                                     Vec3 camPos, Vector3fc fwd, int w, int h, int color) {
        int segments = 48;
        double[][] pts = new double[segments][2];
        for (int i = 0; i < segments; i++) {
            double a = Math.PI * 2.0 * i / segments;
            Vec3 wp = new Vec3(center.x + radius * Math.cos(a), center.y + 0.05, center.z + radius * Math.sin(a));
            double[] px = project(gui, client, wp, camPos, fwd, w, h);
            if (px == null) {
                return;
            }
            pts[i][0] = px[0];
            pts[i][1] = px[1];
        }
        polyline(gui, pts, 1.0f, color);
    }

    private static void renderHat(GuiGraphics gui, Minecraft client, Entity e, Vec3 head,
                                  Vec3 camPos, Vector3fc fwd, int w, int h) {
        double r = 0.32 * hatScale;
        double hgt = 0.45 * hatScale;
        Vec3 apex = new Vec3(head.x, head.y + hgt, head.z);
        Vec3 camToHead = head.subtract(camPos);
        if (camToHead.lengthSqr() < 1.0E-4) {
            return;
        }
        Vec3 toPlayer = camToHead.normalize();
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = toPlayer.cross(up);
        if (right.lengthSqr() < 1.0E-6) {
            return;
        }
        right = right.normalize();
        Vec3 leftPt = new Vec3(head.x - right.x * r, head.y, head.z - right.z * r);
        Vec3 rightPt = new Vec3(head.x + right.x * r, head.y, head.z + right.z * r);

        double[] a = project(gui, client, apex, camPos, fwd, w, h);
        double[] lt = project(gui, client, leftPt, camPos, fwd, w, h);
        double[] rt = project(gui, client, rightPt, camPos, fwd, w, h);
        if (a == null || lt == null || rt == null) {
            return;
        }
        line(gui, a[0], a[1], lt[0], lt[1], 2.0f, HAT_COLOR);
        line(gui, a[0], a[1], rt[0], rt[1], 2.0f, HAT_COLOR);
        line(gui, lt[0], lt[1], rt[0], rt[1], 2.0f, HAT_COLOR);
    }

    private static void renderPenis(GuiGraphics gui, Minecraft client, Entity e, Vec3 feet,
                                    Vec3 camPos, Vector3fc fwd, int w, int h) {
        float yaw = e.getYRot();
        double dx = -Math.sin(Math.toRadians(yaw));
        double dz = Math.cos(Math.toRadians(yaw));
        double px = -dz;
        double pz = dx;

        Vec3 base = new Vec3(feet.x + dx * 0.12, feet.y + 0.32, feet.z + dz * 0.12);
        Vec3 tip = new Vec3(feet.x + dx * 0.52, feet.y + 0.36, feet.z + dz * 0.52);
        Vec3 ballL = new Vec3(feet.x + px * 0.07 - dx * 0.06, feet.y + 0.24, feet.z + pz * 0.07 - dz * 0.06);
        Vec3 ballR = new Vec3(feet.x - px * 0.07 - dx * 0.06, feet.y + 0.24, feet.z - pz * 0.07 - dz * 0.06);

        double[] b = project(gui, client, base, camPos, fwd, w, h);
        double[] t = project(gui, client, tip, camPos, fwd, w, h);
        double[] bl = project(gui, client, ballL, camPos, fwd, w, h);
        double[] br = project(gui, client, ballR, camPos, fwd, w, h);
        if (b == null || t == null || bl == null || br == null) {
            return;
        }

        double dist = Math.max(1.0, feet.distanceTo(camPos));
        double rScale = Math.max(1.5, Math.min(6.0, 9.0 / dist));

        line(gui, b[0], b[1], t[0], t[1], 3.0f, PENIS_COLOR);
        fillCircle(gui, t[0], t[1], rScale * 0.8, GLANS_COLOR);
        fillCircle(gui, bl[0], bl[1], rScale * 0.6, BALL_COLOR);
        fillCircle(gui, br[0], br[1], rScale * 0.6, BALL_COLOR);
    }

    private static void fillCircle(GuiGraphics gui, double cx, double cy, double r, int color) {
        int ix = (int) Math.round(cx);
        int iy = (int) Math.round(cy);
        int ir = Math.max(1, (int) Math.ceil(r));
        for (int yy = -ir; yy <= ir; yy++) {
            for (int xx = -ir; xx <= ir; xx++) {
                if (xx * xx + yy * yy <= ir * ir) {
                    gui.fill(ix + xx, iy + yy, ix + xx + 1, iy + yy + 1, color);
                }
            }
        }
    }

    private static void renderNameTag(GuiGraphics gui, Minecraft client, Player player, Vec3 head,
                                      Vec3 camPos, Vector3fc fwd, int w, int h) {
        Vec3 labelPos = new Vec3(head.x, head.y + 0.3, head.z);
        double[] p = project(gui, client, labelPos, camPos, fwd, w, h);
        if (p == null) {
            return;
        }
        int x = (int) p[0];
        int y = (int) p[1];
        String name = player.getName().getString();
        int hp = (int) Math.ceil(player.getHealth());
        String text = name + " " + hp;
        int tw = client.font.width(text);
        gui.fill(x - tw / 2 - 3, y - 10, x + tw / 2 + 3, y + 2, 0xA0121212);
        gui.fill(x - tw / 2 - 3, y - 10, x + tw / 2 + 3, y - 8, Theme.current());
        gui.drawCenteredString(client.font, Component.literal(text), x, y - 8, 0xFFFFFFFF);
    }

    private static double[] project(GuiGraphics gui, Minecraft client, Vec3 world, Vec3 camPos,
                                    Vector3fc fwd, int w, int h) {
        Vec3 d = world.subtract(camPos);
        double f = d.x * fwd.x() + d.y * fwd.y() + d.z * fwd.z();
        if (f <= 0.05) {
            return null;
        }
        Vec3 ndc = client.gameRenderer.projectPointToScreen(world);
        return new double[] { (ndc.x * 0.5 + 0.5) * w, (0.5 - ndc.y * 0.5) * h };
    }

    private static void polyline(GuiGraphics gui, double[][] pts, float th, int color) {
        int n = pts.length;
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            line(gui, pts[i][0], pts[i][1], pts[j][0], pts[j][1], th, color);
        }
    }

    private static void line(GuiGraphics gui, double x0, double y0, double x1, double y1, float th, int color) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len < 0.5) {
            return;
        }
        gui.pose().pushMatrix();
        gui.pose().translate((float) x0, (float) y0);
        gui.pose().rotate((float) Math.atan2(dy, dx));
        int half = Math.max(1, (int) (th / 2f));
        gui.fill(0, -half, (int) len, half, color);
        gui.pose().popMatrix();
    }
}