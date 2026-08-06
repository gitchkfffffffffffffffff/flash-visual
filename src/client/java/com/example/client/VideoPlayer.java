package com.example.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class VideoPlayer {
    private static final Logger LOGGER = LoggerFactory.getLogger("flash-visual-video");
    public static final Identifier TEX_ID = Identifier.fromNamespaceAndPath("flash-visual", "video_frame");

    public static volatile boolean enabled = false;
    public static volatile boolean playing = false;
    public static volatile String path = "";
    public static volatile int captureX = 0;
    public static volatile int captureY = 0;
    public static volatile int captureW = 960;
    public static volatile int captureH = 540;

    private static volatile NativeImage frame;
    private static DynamicTexture texture;
    private static Thread captureThread;
    private static volatile boolean running = false;
    private static Process externalPlayer;

    private VideoPlayer() {
    }

    public static void play(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }
        File f = new File(filePath);
        if (!f.isFile()) {
            return;
        }
        path = filePath;
        try {
            if (externalPlayer != null) {
                externalPlayer.destroy();
            }
            externalPlayer = new ProcessBuilder("cmd", "/c", "start", "", filePath)
                .redirectErrorStream(true).start();
            playing = true;
        } catch (IOException e) {
            LOGGER.warn("Failed to launch player for {}", filePath, e);
            playing = false;
        }
        startCapture();
    }

    public static void stop() {
        playing = false;
        stopCapture();
        if (externalPlayer != null) {
            externalPlayer.destroy();
            externalPlayer = null;
        }
        synchronized (VideoPlayer.class) {
            if (frame != null) {
                frame.close();
                frame = null;
            }
        }
    }

    public static synchronized void startCapture() {
        if (running) {
            return;
        }
        running = true;
        captureThread = new Thread(() -> {
            Robot robot = null;
            try {
                robot = new Robot();
            } catch (AWTException e) {
                running = false;
                return;
            }
            while (running) {
                try {
                    BufferedImage img = robot.createScreenCapture(new Rectangle(captureX, captureY, captureW, captureH));
                    NativeImage ni = toNativeImage(img);
                    synchronized (VideoPlayer.class) {
                        NativeImage prev = frame;
                        frame = ni;
                        if (prev != null) {
                            prev.close();
                        }
                    }
                } catch (Exception ignored) {
                }
                try {
                    Thread.sleep(33L);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "flash-visual-video-capture");
        captureThread.setDaemon(true);
        captureThread.start();
    }

    public static synchronized void stopCapture() {
        running = false;
        Thread t = captureThread;
        if (t != null) {
            t.interrupt();
        }
        captureThread = null;
    }

    private static NativeImage toNativeImage(BufferedImage img) {
        NativeImage out = new NativeImage(img.getWidth(), img.getHeight(), false);
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int argb = img.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                out.setPixelABGR(x, y, (a << 24) | (b << 16) | (g << 8) | r);
            }
        }
        return out;
    }

    public static void render(GuiGraphics gui, int x, int y, int w, int h) {
        if (!enabled || !playing) {
            return;
        }
        NativeImage cur;
        synchronized (VideoPlayer.class) {
            if (frame == null) {
                return;
            }
            cur = frame;
        }
        Minecraft mc = Minecraft.getInstance();
        if (texture == null || texture.getPixels() != cur) {
            texture = new DynamicTexture(() -> "flash-visual video", cur);
            mc.getTextureManager().register(TEX_ID, texture);
        }
        gui.blit(TEX_ID, x, y, w, h, 0.0f, 0.0f, cur.getWidth(), cur.getHeight());
    }

    public static void setRegion(int x, int y, int w, int h) {
        captureX = x;
        captureY = y;
        captureW = Math.max(8, w);
        captureH = Math.max(8, h);
    }
}
