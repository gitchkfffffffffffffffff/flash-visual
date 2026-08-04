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

import java.util.UUID;

public class WorldVisuals {
    public static boolean chinaHat = false;
    public static boolean jumpCircle = false;
    public static boolean tracers = false;
    public static boolean tracersMobs = false;
    public static boolean nameTag = false;
    public static boolean penis = false;
    public static boolean pumping = false;
    public static volatile long cumStartMs = -1;
    public static volatile UUID fuckTargetUuid = null;
    public static volatile boolean blowjob = false;
    public static volatile long actStartMs = -1;
    public static volatile boolean autoCumDone = false;
    private static final int HEAD_COLOR = 0xFFE8C9A0;
    private static final int GIVER_COLOR = 0xFF9FE0B0;
    private static final int GIVER_GLANS = 0xFF86C896;

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
        boolean isTarget = fuckTargetUuid != null && e.getUUID().equals(fuckTargetUuid);
        if (blowjob && isTarget && client.player != null) {
            renderBlowjob(gui, client, e, feet, camPos, fwd, w, h);
            return;
        }
        boolean pumpMe = pumping && (fuckTargetUuid == null || isTarget);

        float yaw = e.getYRot();
        double dx = -Math.sin(Math.toRadians(yaw));
        double dz = Math.cos(Math.toRadians(yaw));
        double px = -dz;
        double pz = dx;
        if (isTarget && client.player != null) {
            double toX = client.player.getX() - feet.x;
            double toZ = client.player.getZ() - feet.z;
            double len = Math.hypot(toX, toZ);
            if (len > 0.001) {
                dx = toX / len;
                dz = toZ / len;
            }
        }

        long now = System.currentTimeMillis();
        double stroke = 0;
        double forward = 0.40;
        if (pumpMe) {
            double ph = (now % 2000) / 1000.0 * Math.PI;
            stroke = Math.sin(ph) * 0.14;
            forward = 0.40 + Math.abs(Math.sin(ph)) * 0.12;
        }

        double hy = feet.y + 0.32 + stroke * 0.5;
        Vec3 base = new Vec3(feet.x + dx * 0.12, hy, feet.z + dz * 0.12);
        Vec3 tip = new Vec3(feet.x + dx * (0.12 + forward), hy + 0.04, feet.z + dz * (0.12 + forward));
        Vec3 ballL = new Vec3(feet.x + px * 0.07 - dx * 0.06, feet.y + 0.24 + stroke * 0.3, feet.z + pz * 0.07 - dz * 0.06);
        Vec3 ballR = new Vec3(feet.x - px * 0.07 - dx * 0.06, feet.y + 0.24 + stroke * 0.3, feet.z - pz * 0.07 - dz * 0.06);

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

        if (cumStartMs > 0 && (fuckTargetUuid == null || isTarget)) {
            long elapsed = now - cumStartMs;
            if (elapsed < 800) {
                renderCum(gui, t, elapsed, rScale);
            } else {
                cumStartMs = -1;
                if (isTarget) {
                    fuckTargetUuid = null;
                }
            }
        }
    }

    private static void renderBlowjob(GuiGraphics gui, Minecraft client, Entity e, Vec3 feet,
                                      Vec3 camPos, Vector3fc fwd, int w, int h) {
        double toX = client.player.getX() - feet.x;
        double toZ = client.player.getZ() - feet.z;
        double len = Math.hypot(toX, toZ);
        double dx = len > 0.001 ? toX / len : -Math.sin(Math.toRadians(e.getYRot()));
        double dz = len > 0.001 ? toZ / len : Math.cos(Math.toRadians(e.getYRot()));

        long now = System.currentTimeMillis();
        long elapsed = actStartMs > 0 ? now - actStartMs : now;

        if (actStartMs > 0 && !autoCumDone && elapsed >= 10000) {
            autoCumDone = true;
            cumStartMs = now;
        }

        double hyBase = feet.y + 0.85;
        Vec3 base = new Vec3(feet.x + dx * 0.1, hyBase, feet.z + dz * 0.1);
        Vec3 tip = new Vec3(feet.x + dx * 0.1, hyBase + 0.55, feet.z + dz * 0.1);
        Vec3 ballL = new Vec3(feet.x - dz * 0.05, feet.y + 0.24, feet.z + dx * 0.05);
        Vec3 ballR = new Vec3(feet.x + dz * 0.05, feet.y + 0.24, feet.z - dx * 0.05);

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

        double ogdx = -dx;
        double ogdz = -dz;
        double thrustPh = Math.sin((now % 500) / 500.0 * Math.PI * 2);
        double thrust = 0.28 + Math.max(0.0, thrustPh) * 0.14;
        Vec3 aBase = new Vec3(feet.x + ogdx * 0.36, feet.y + 0.9, feet.z + ogdz * 0.36);
        Vec3 aTip = new Vec3(feet.x + ogdx * (0.36 - thrust), feet.y + 1.02, feet.z + ogdz * (0.36 - thrust));
        double[] ab = project(gui, client, aBase, camPos, fwd, w, h);
        double[] at = project(gui, client, aTip, camPos, fwd, w, h);
        if (ab != null && at != null) {
            line(gui, ab[0], ab[1], at[0], at[1], 3.0f, GIVER_COLOR);
            fillCircle(gui, at[0], at[1], rScale * 0.7, GIVER_GLANS);
        }

        double mThrust = Math.max(0.0, Math.sin((now % 420) / 420.0 * Math.PI * 2));
        Vec3 mBase = new Vec3(feet.x + dx * 0.62, feet.y + 1.3, feet.z + dz * 0.62);
        Vec3 mTip = new Vec3(feet.x + dx * (0.12 + mThrust * 0.2), feet.y + 1.56, feet.z + dz * (0.12 + mThrust * 0.2));
        double[] mb = project(gui, client, mBase, camPos, fwd, w, h);
        double[] mt = project(gui, client, mTip, camPos, fwd, w, h);
        if (mb != null && mt != null) {
            line(gui, mb[0], mb[1], mt[0], mt[1], 3.0f, GIVER_COLOR);
            fillCircle(gui, mt[0], mt[1], rScale * 0.7, GIVER_GLANS);
        }

        if (cumStartMs > 0) {
            long cElapsed = now - cumStartMs;
            if (cElapsed < 800) {
                renderCum(gui, t, cElapsed, rScale);
            } else {
                cumStartMs = -1;
            }
        }
    }

    private static void renderCum(GuiGraphics gui, double[] tip, long elapsedMs, double rScale) {
        double p = Math.min(1.0, elapsedMs / 800.0);
        int n = 22;
        for (int i = 0; i < n; i++) {
            double a = (i / (double) n) * Math.PI;
            double spd = 0.35 + (i % 6) * 0.10;
            double x = tip[0] + Math.cos(a) * spd * p * 110.0 * rScale;
            double y = tip[1] - Math.abs(Math.sin(a)) * spd * p * 90.0 * rScale + p * p * 16.0;
            fillCircle(gui, x, y, 1.5 * rScale, 0xFFFAFAFA);
        }
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