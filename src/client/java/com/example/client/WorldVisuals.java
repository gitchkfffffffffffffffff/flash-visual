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
    public static boolean enemyLabels = true;
    public static boolean zhiguli = false;
    public static boolean zhiguliView = false;
    public static boolean majorSuit = false;
    public static boolean skinOverride = true;
    public static boolean wings = false;
    public static boolean near = false;
    public static double nearRange = 90.0;
    public static String skinTargetName = "iamknow";

    public static float hatScale = 1.0f;
    public static float circleRadius = 3.0f;

    private static final double RANGE = 64.0;
    private static final int HAT_COLOR = 0xFFFF5555;
    private static final int HAT_FILL = 0xCCFF7A4D;
    private static final int HAT_BRIM = 0xFFFF8A5A;
    private static final int CIRCLE_COLOR = 0xCCFFAA00;
    private static final int TRACER_COLOR = 0x88FFFFFF;
    private static final int MOB_COLOR = 0xCCFF5555;
    private static final int ZHIGULI_BODY = 0xFFE8C8A0;
    private static final int ZHIGULI_DARK = 0xFF2A333D;
    private static final int ZHIGULI_GLASS = 0xFF9FC6E8;
    private static final int SUIT_GREEN = 0xFF4F6A2F;
    private static final int SUIT_DARK = 0xFF2E3D1A;
    private static final int SUIT_GOLD = 0xFFFFD24A;
    private static final int SUIT_BELT = 0xFF3B2A1A;
    private static final int SUIT_STAR = 0xFFFF3B30;
    private static final int WING_FILL = 0xD014161C;
    private static final int WING_LINE = 0xFF565B66;

    public static void render(GuiGraphics gui, Minecraft client) {
        if (client.level == null || client.player == null) {
            return;
        }
        Camera cam = client.gameRenderer.getMainCamera();
        if (!cam.isInitialized()) {
            return;
        }
        if (!(chinaHat || jumpCircle || tracers || tracersMobs || nameTag || zhiguli || majorSuit || wings || near)) {
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
            boolean isFriend = isPlayer && Friends.contains(((Player) e).getName().getString());

            double topY = e.getY() + e.getBbHeight();

            Vec3 head = new Vec3(e.getX(), topY, e.getZ());
            Vec3 feet = e.position();

            if (nameTag && isPlayer) {
                renderNameTag(gui, client, e, head, camPos, fwd, w, h);
            }

            if (enemyLabels && (isMob || (isPlayer && !isFriend))) {
                renderEnemyLabel(gui, client, e, head, camPos, fwd, w, h);
            }

            if (chinaHat && isPlayer) {
                renderHat(gui, client, e, head, camPos, fwd, w, h);
            }

            if (majorSuit && isPlayer) {
                renderMajorSuit(gui, client, e, feet, head, camPos, fwd, w, h);
            }

            if (wings && isPlayer && client.player != null && e == client.player) {
                renderWings(gui, client, e, feet, camPos, fwd, w, h);
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

        if (zhiguli && client.player != null) {
            renderZhiguli(gui, client, client.player.position(), camPos, fwd, w, h);
        }

        if (wings && client.player != null) {
            renderWings(gui, client, client.player, client.player.position(), camPos, fwd, w, h);
        }

        if (near) {
            renderNear(gui, client, camPos, fwd, w, h);
        }
    }

    private static void renderNear(GuiGraphics gui, Minecraft client, Vec3 camPos, Vector3fc fwd, int w, int h) {
        double rangeSq = nearRange * nearRange;
        for (Entity e : client.level.entitiesForRendering()) {
            if (e == client.player || !(e instanceof Player) || e.isRemoved()) {
                continue;
            }
            if (e.distanceToSqr(client.player) > rangeSq) {
                continue;
            }
            Vec3 pos = e.position();
            double[] p = project(gui, client,
                new Vec3(pos.x, pos.y + e.getBbHeight() * 0.8, pos.z), camPos, fwd, w, h);
            if (p == null) {
                continue;
            }
            double dx = p[0] - (double) w / 2.0;
            double dy = p[1] - (double) h / 2.0;
            double d = Math.sqrt(dx * dx + dy * dy);
            if (d < 40.0) {
                continue;
            }
            double nx = dx / d;
            double ny = dy / d;
            double scX = (w / 2.0 - 26.0) / Math.max(1.0, Math.abs(dx) * 0.9);
            double scY = (h / 2.0 - 26.0) / Math.max(1.0, Math.abs(dy) * 0.9);
            double scale = Math.max(0.2, Math.min(scX, scY));
            double ax = (double) w / 2.0 + dx * scale;
            double ay = (double) h / 2.0 + dy * scale;
            int accent = 0xFFFF5A5A;
            drawArrow(gui, (float) ax, (float) ay, (float) Math.atan2(dy, dx), accent);
            String name = ((Player) e).getName().getString();
            int tw = client.font.width(name);
            int lx = (int) (ax + nx * 16);
            int ly = (int) (ay + ny * 16 - 3);
            gui.drawString(client.font, Component.literal(name), lx, ly, accent);
        }
    }

    private static void drawArrow(GuiGraphics gui, float x, float y, float angle, int color) {
        gui.pose().pushMatrix();
        gui.pose().translate(x, y);
        gui.pose().rotate(angle);
        double[][] tri = { { 11, 0 }, { -7, 5.5 }, { -7, -5.5 } };
        fillPolygon(gui, tri, color);
        line(gui, -7, 5.5, 11, 0, 1.0f, 0xFF000000);
        line(gui, -7, -5.5, 11, 0, 1.0f, 0xFF000000);
        gui.pose().popMatrix();
    }

    private static void renderWings(GuiGraphics gui, Minecraft client, Entity e, Vec3 feet,
                                    Vec3 camPos, Vector3fc fwd, int w, int h) {
        double hgt = Math.max(0.5, e.getBbHeight());
        double yawRad = Math.toRadians(e.getYRot());
        double bx = -Math.sin(yawRad);
        double bz = Math.cos(yawRad);
        double sx = -Math.cos(yawRad);
        double sz = -Math.sin(yawRad);
        double baseY = feet.y + hgt * 0.7;
        double cbx = feet.x - bx * 0.08;
        double cbz = feet.z - bz * 0.08;
        long t = System.currentTimeMillis();
        double flap = Math.sin(t / 260.0) * 0.22;
        int accent = Theme.current();
        for (int side = 1; side >= -1; side -= 2) {
            double wx = sx * side;
            double wz = sz * side;
            double[] A = project(gui, client, new Vec3(cbx + wx * 0.16, baseY, cbz + wz * 0.16), camPos, fwd, w, h);
            double[] C = project(gui, client, new Vec3(cbx + wx * 0.95, baseY + 0.20 + flap, cbz + wz * 0.95), camPos, fwd, w, h);
            double[] F = project(gui, client, new Vec3(cbx + wx * 0.70 - bx * 0.20, baseY + 0.40 + flap * 0.9, cbz + wz * 0.70 - bz * 0.20), camPos, fwd, w, h);
            double[] E = project(gui, client, new Vec3(cbx + wx * 0.40 - bx * 0.46, baseY + 0.66 + flap * 1.35, cbz + wz * 0.40 - bz * 0.46), camPos, fwd, w, h);
            if (A == null || C == null || F == null || E == null) {
                continue;
            }
            fillPolygon(gui, new double[][]{A, C, F}, WING_FILL);
            fillPolygon(gui, new double[][]{A, C, E}, WING_FILL);
            line(gui, A[0], A[1], C[0], C[1], 1.4f, WING_LINE);
            line(gui, C[0], C[1], E[0], E[1], 1.4f, WING_LINE);
            line(gui, E[0], E[1], A[0], A[1], 1.4f, WING_LINE);
            line(gui, A[0], A[1], E[0], E[1], 1.0f, accent);
        }
    }

    private static void fillPolygon(GuiGraphics gui, double[][] p, int color) {
        int n = p.length;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (double[] pt : p) {
            if (pt[1] < minY) {
                minY = (int) Math.floor(pt[1]);
            }
            if (pt[1] > maxY) {
                maxY = (int) Math.ceil(pt[1]);
            }
        }
        for (int y = minY; y <= maxY; y++) {
            double yy = y + 0.5;
            java.util.ArrayList<Double> xs = new java.util.ArrayList<>();
            for (int i = 0; i < n; i++) {
                int j = (i + 1) % n;
                double y0 = p[i][1];
                double y1 = p[j][1];
                if ((y0 <= yy && y1 > yy) || (y1 <= yy && y0 > yy)) {
                    double x = p[i][0] + (yy - y0) * (p[j][0] - p[i][0]) / (y1 - y0);
                    xs.add(x);
                }
            }
            java.util.Collections.sort(xs);
            for (int k = 0; k + 1 < xs.size(); k += 2) {
                int x0 = (int) Math.ceil(xs.get(k));
                int x1 = (int) Math.floor(xs.get(k + 1));
                if (x1 >= x0) {
                    gui.fill(x0, y, x1 + 1, y + 1, color);
                }
            }
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
        double r = 0.34 * hatScale;
        double hgt = 0.42 * hatScale;
        double brim = 0.52 * hatScale;
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
        Vec3 blPt = new Vec3(head.x - right.x * brim, head.y + 0.02, head.z - right.z * brim);
        Vec3 brPt = new Vec3(head.x + right.x * brim, head.y + 0.02, head.z + right.z * brim);

        double[] a = project(gui, client, apex, camPos, fwd, w, h);
        double[] lt = project(gui, client, leftPt, camPos, fwd, w, h);
        double[] rt = project(gui, client, rightPt, camPos, fwd, w, h);
        double[] bl = project(gui, client, blPt, camPos, fwd, w, h);
        double[] br = project(gui, client, brPt, camPos, fwd, w, h);
        if (a == null || lt == null || rt == null || bl == null || br == null) {
            return;
        }
        fillTriangle(gui, a, lt, rt, HAT_FILL);
        line(gui, a[0], a[1], lt[0], lt[1], 2.0f, HAT_COLOR);
        line(gui, a[0], a[1], rt[0], rt[1], 2.0f, HAT_COLOR);
        fillQuad(gui, lt, bl, br, rt, HAT_FILL);
        line(gui, bl[0], bl[1], br[0], br[1], 3.0f, HAT_BRIM);
        line(gui, lt[0], lt[1], rt[0], rt[1], 1.5f, HAT_COLOR);
    }

    private static void fillTriangle(GuiGraphics gui, double[] p0, double[] p1, double[] p2, int color) {
        double[][] pts = { p0, p1, p2 };
        java.util.Arrays.sort(pts, (u, v) -> Double.compare(u[1], v[1]));
        double y0 = pts[0][1], y1 = pts[1][1], y2 = pts[2][1];
        double[] x0 = { pts[0][0] }, x1 = { pts[2][0] };
        double top = Math.ceil(y0), bot = Math.floor(y2);
        if (top > bot) {
            return;
        }
        int ty = Math.max(0, (int) top);
        int by = (int) bot;
        for (int y = ty; y <= by; y++) {
            double yy = y + 0.5;
            double xl = Double.MAX_VALUE, xr = -Double.MAX_VALUE;
            double[][] edges = { { pts[0][0], pts[0][1], pts[1][0], pts[1][1] },
                { pts[1][0], pts[1][1], pts[2][0], pts[2][1] },
                { pts[0][0], pts[0][1], pts[2][0], pts[2][1] } };
            for (double[] ed : edges) {
                double e0y = ed[1], e1y = ed[3];
                if (yy < Math.min(e0y, e1y) || yy > Math.max(e0y, e1y)) {
                    continue;
                }
                if (Math.abs(e1y - e0y) < 1.0E-4) {
                    continue;
                }
                double t = (yy - e0y) / (e1y - e0y);
                double ex = ed[0] + (ed[2] - ed[0]) * t;
                xl = Math.min(xl, ex);
                xr = Math.max(xr, ex);
            }
            if (xr >= xl) {
                gui.fill((int) Math.max(0, Math.ceil(xl)), y, (int) Math.max((int) xl, Math.floor(xr)) + 1, y + 1, color);
            }
        }
    }

    private static void fillQuad(GuiGraphics gui, double[] a, double[] b, double[] c, double[] d, int color) {
        fillTriangle(gui, a, b, c, color);
        fillTriangle(gui, a, c, d, color);
    }

    private static void renderMajorSuit(GuiGraphics gui, Minecraft client, Entity e, Vec3 feet, Vec3 head,
                                        Vec3 camPos, Vector3fc fwd, int w, int h) {
        double hgt = Math.max(0.5, e.getBbHeight());
        Vec3 torso = new Vec3(feet.x, feet.y + hgt * 0.62, feet.z);
        Vec3 toBody = torso.subtract(camPos);
        if (toBody.lengthSqr() < 1.0E-4) {
            return;
        }
        Vec3 right = toBody.normalize().cross(new Vec3(0, 1, 0));
        if (right.lengthSqr() < 1.0E-6) {
            return;
        }
        right = right.normalize();
        double rx = right.x;
        double rz = right.z;

        double shY = feet.y + hgt * 0.8;
        double waY = feet.y + hgt * 0.42;
        double hiY = feet.y + hgt * 0.27;
        double vx = torso.x;
        double vz = torso.z;

        double shW = 0.34;
        double waW = 0.28;
        double hiW = 0.26;

        double[] shL = project(gui, client, new Vec3(vx - rx * shW, shY, vz - rz * shW), camPos, fwd, w, h);
        double[] shR = project(gui, client, new Vec3(vx + rx * shW, shY, vz + rz * shW), camPos, fwd, w, h);
        double[] waL = project(gui, client, new Vec3(vx - rx * waW, waY, vz - rz * waW), camPos, fwd, w, h);
        double[] waR = project(gui, client, new Vec3(vx + rx * waW, waY, vz + rz * waW), camPos, fwd, w, h);
        double[] hiL = project(gui, client, new Vec3(vx - rx * hiW, hiY, vz - rz * hiW), camPos, fwd, w, h);
        double[] hiR = project(gui, client, new Vec3(vx + rx * hiW, hiY, vz + rz * hiW), camPos, fwd, w, h);
        if (shL == null || shR == null || waL == null || waR == null || hiL == null || hiR == null) {
            return;
        }

        double x0 = Math.min(Math.min(shL[0], waL[0]), hiL[0]);
        double x1 = Math.max(Math.max(shR[0], waR[0]), hiR[0]);
        double yTop = Math.min(shL[1], shR[1]);
        double yBot = Math.max(hiL[1], hiR[1]);
        gui.fill((int) x0, (int) yTop, (int) x1 + 1, (int) yBot + 1, SUIT_GREEN);
        gui.renderOutline((int) x0, (int) yTop, (int) (x1 - x0) + 1, (int) (yBot - yTop) + 1, SUIT_DARK);

        double dist = Math.max(1.0, feet.distanceTo(camPos));
        double rScale = Math.max(1.2, Math.min(7.0, 10.0 / dist));

        double neckX = (shL[0] + shR[0]) / 2.0;
        double neckY = (shL[1] + shR[1]) / 2.0;
        double chestY = (waL[1] + waR[1]) / 2.0;
        double placketX = (waL[0] + waR[0]) / 2.0;
        line(gui, neckX, neckY, placketX, chestY, 2.0f, SUIT_DARK);
        fillCircle(gui, neckX, neckY, 1.4 * rScale, SUIT_DARK);
        fillCircle(gui, placketX, chestY, 1.2 * rScale, SUIT_GOLD);
        fillCircle(gui, placketX, (waL[1] + waR[1]) / 2.0 + (hiL[1] - waL[1]) * 0.4, 1.2 * rScale, SUIT_GOLD);

        fillCircle(gui, shL[0], shL[1], 2.5 * rScale, SUIT_GOLD);
        fillCircle(gui, shR[0], shR[1], 2.5 * rScale, SUIT_GOLD);
        fillCircle(gui, shL[0], shL[1], 1.0 * rScale, SUIT_DARK);
        fillCircle(gui, shR[0], shR[1], 1.0 * rScale, SUIT_DARK);

        double beltY = (waL[1] + waR[1]) / 2.0 + (hiL[1] - waL[1]) * 0.5;
        double bL = Math.min(waL[0], hiL[0]);
        double bR = Math.max(waR[0], hiR[0]);
        gui.fill((int) bL, (int) beltY, (int) bR + 1, (int) beltY + 4, SUIT_BELT);

        double capY = head.y + hgt * 0.16;
        double[] capC = project(gui, client, new Vec3(head.x, capY, head.z), camPos, fwd, w, h);
        if (capC != null) {
            double cr = 0.30 * rScale;
            fillCircle(gui, capC[0], capC[1], 3.8 * rScale, SUIT_GREEN);
            double[] bLp = project(gui, client, new Vec3(head.x - rx * 0.30, capY - 0.06, head.z - rz * 0.30), camPos, fwd, w, h);
            double[] bRp = project(gui, client, new Vec3(head.x + rx * 0.30, capY - 0.06, head.z + rz * 0.30), camPos, fwd, w, h);
            if (bLp != null && bRp != null) {
                line(gui, bLp[0], bLp[1], bRp[0], bRp[1], 3.0f, SUIT_BELT);
            }
            line(gui, capC[0] - cr, capC[1], capC[0] + cr, capC[1], 1.6f, SUIT_GOLD);
            fillCircle(gui, capC[0], capC[1] - 0.5, 1.0 * rScale, SUIT_STAR);
        }
    }

    private static void renderZhiguli(GuiGraphics gui, Minecraft client, Vec3 feet, Vec3 camPos,
                                      Vector3fc fwd, int w, int h) {
        float yaw = client.player.getYRot();
        double dx = -Math.sin(Math.toRadians(yaw));
        double dz = Math.cos(Math.toRadians(yaw));
        double px = -dz;
        double pz = dx;

        Vec3[] wheels = {
            new Vec3(feet.x + dx * 0.85 + px * 0.55, feet.y + 0.12, feet.z + dz * 0.85 + pz * 0.55),
            new Vec3(feet.x + dx * 0.85 - px * 0.55, feet.y + 0.12, feet.z + dz * 0.85 - pz * 0.55),
            new Vec3(feet.x - dx * 0.85 + px * 0.55, feet.y + 0.12, feet.z - dz * 0.85 + pz * 0.55),
            new Vec3(feet.x - dx * 0.85 - px * 0.55, feet.y + 0.12, feet.z - dz * 0.85 - pz * 0.55)
        };
        double[][] wheelsP = new double[4][];
        for (int i = 0; i < 4; i++) {
            wheelsP[i] = project(gui, client, wheels[i], camPos, fwd, w, h);
        }
        if (wheelsP[0] == null || wheelsP[1] == null || wheelsP[2] == null || wheelsP[3] == null) {
            return;
        }

        Vec3 cFL = new Vec3(feet.x + dx * 1.15 + px * 0.6, feet.y + 0.5, feet.z + dz * 1.15 + pz * 0.6);
        Vec3 cFR = new Vec3(feet.x + dx * 1.15 - px * 0.6, feet.y + 0.5, feet.z + dz * 1.15 - pz * 0.6);
        Vec3 cRL = new Vec3(feet.x - dx * 1.15 + px * 0.6, feet.y + 0.5, feet.z - dz * 1.15 + pz * 0.6);
        Vec3 cRR = new Vec3(feet.x - dx * 1.15 - px * 0.6, feet.y + 0.5, feet.z - dz * 1.15 - pz * 0.6);
        double[][] body = new double[4][2];
        int bi = 0;
        for (Vec3 v : new Vec3[] { cFL, cFR, cRR, cRL }) {
            double[] p = project(gui, client, v, camPos, fwd, w, h);
            if (p == null) {
                return;
            }
            body[bi++] = p;
        }

        Vec3 aFL = new Vec3(feet.x + dx * 0.6 + px * 0.5, feet.y + 0.95, feet.z + dz * 0.6 + pz * 0.5);
        Vec3 aFR = new Vec3(feet.x + dx * 0.6 - px * 0.5, feet.y + 0.95, feet.z + dz * 0.6 - pz * 0.5);
        Vec3 aRR = new Vec3(feet.x - dx * 0.75 + px * 0.5, feet.y + 0.95, feet.z - dz * 0.75 + pz * 0.5);
        Vec3 aRL = new Vec3(feet.x - dx * 0.75 - px * 0.5, feet.y + 0.95, feet.z - dz * 0.75 - pz * 0.5);
        double[][] roof = new double[4][2];
        int ri = 0;
        for (Vec3 v : new Vec3[] { aFL, aFR, aRR, aRL }) {
            double[] p = project(gui, client, v, camPos, fwd, w, h);
            if (p == null) {
                return;
            }
            roof[ri++] = p;
        }

        double dist = Math.max(1.0, feet.distanceTo(camPos));
        double rScale = Math.max(1.5, Math.min(8.0, 12.0 / dist));

        for (double[] wh : wheelsP) {
            fillCircle(gui, wh[0], wh[1], 2.6 * rScale, ZHIGULI_DARK);
            fillCircle(gui, wh[0], wh[1], 1.0 * rScale, 0xFFB9C0C8);
        }
        polyline(gui, body, 3.0f, ZHIGULI_BODY);
        polyline(gui, roof, 2.0f, ZHIGULI_GLASS);
        fillCircle(gui, body[0][0], body[0][1], 1.4 * rScale, 0xFFFFE08A);
        fillCircle(gui, body[1][0], body[1][1], 1.4 * rScale, 0xFFFFE08A);
        fillCircle(gui, body[2][0], body[2][1], 1.4 * rScale, 0xFFFF5A5A);
        fillCircle(gui, body[3][0], body[3][1], 1.4 * rScale, 0xFFFF5A5A);
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

    private static void renderNameTag(GuiGraphics gui, Minecraft client, Entity e, Vec3 head,
                                      Vec3 camPos, Vector3fc fwd, int w, int h) {
        Vec3 labelPos = new Vec3(head.x, head.y + 0.3, head.z);
        double[] p = project(gui, client, labelPos, camPos, fwd, w, h);
        if (p == null) {
            return;
        }
        int x = (int) p[0];
        int y = (int) p[1];
        String name = e.getName().getString();
        int hp = (int) Math.ceil(((LivingEntity) e).getHealth());
        String text = name + " " + hp;
        int tw = client.font.width(text);
        gui.fill(x - tw / 2 - 3, y - 10, x + tw / 2 + 3, y + 2, 0xA0121212);
        gui.fill(x - tw / 2 - 3, y - 10, x + tw / 2 + 3, y - 8, Theme.current());
        gui.drawCenteredString(client.font, Component.literal(text), x, y - 8, 0xFFFFFFFF);
    }

    private static void renderEnemyLabel(GuiGraphics gui, Minecraft client, Entity e, Vec3 head,
                                         Vec3 camPos, Vector3fc fwd, int w, int h) {
        Vec3 labelPos = new Vec3(head.x, head.y + 0.35, head.z);
        double[] p = project(gui, client, labelPos, camPos, fwd, w, h);
        if (p == null) {
            return;
        }
        int x = (int) p[0];
        int y = (int) p[1];
        boolean mob = e instanceof Monster;
        String name = mob ? e.getType().getDescription().getString() : ((Player) e).getName().getString();
        double dist = Math.sqrt(e.distanceToSqr(client.player));
        float hp = ((LivingEntity) e).getHealth();
        float maxHp = Math.max(1.0f, ((LivingEntity) e).getMaxHealth());

        int accent = mob ? 0xFFFF5555 : 0xFFB44AFF;
        int tw = client.font.width(name);
        int barW = Math.max(28, tw + 6);
        int bw = barW + 12;
        int hgt = 14;
        int px = x - bw / 2;
        int py = y - hgt;
        if (px < 0) px = 0;
        if (py < 0) py = 0;

        gui.fill(px, py, px + bw, py + hgt, 0xB0121212);
        gui.fill(px, py, px + bw, py + 1, accent);
        gui.drawString(client.font, Component.literal(name), px + 4, py + 3, 0xFFFFFFFF);
        String ds = String.format("%.0fm", dist);
        gui.drawString(client.font, Component.literal(ds), px + bw - client.font.width(ds) - 4, py + 3, 0xFF7A7A7A);

        int barX = px + 3;
        int barY = py + hgt - 3;
        int fillW = (int) (barW * Math.min(1.0, hp / maxHp));
        gui.fill(barX, barY, barX + barW, barY + 1, 0xFF3A3A3A);
        int hc = hp / maxHp > 0.5 ? Ui.GREEN : (hp / maxHp > 0.2 ? 0xFFFFAA00 : Ui.RED);
        if (fillW > 0) {
            gui.fill(barX, barY, barX + fillW, barY + 1, hc);
        }
    }

    private static double[] project(GuiGraphics gui, Minecraft client, Vec3 world, Vec3 camPos,
                                    Vector3fc fwd, int w, int h) {
        double f = (world.x - camPos.x) * fwd.x() + (world.y - camPos.y) * fwd.y() + (world.z - camPos.z) * fwd.z();
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
