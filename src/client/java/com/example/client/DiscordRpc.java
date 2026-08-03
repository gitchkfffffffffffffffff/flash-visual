package com.example.client;

import com.google.gson.JsonObject;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DiscordRpc {
    public static volatile boolean enabled = true;

    private static final String CLIENT_ID = "1533739399555321866";

    private static final int OP_HANDSHAKE = 0;
    private static final int OP_FRAME = 1;
    private static final int OP_CLOSE = 2;
    private static final int OP_PING = 3;
    private static final int OP_PONG = 4;

    private static final int GENERIC_READ = 0x80000000;
    private static final int GENERIC_WRITE = 0x40000000;
    private static final int OPEN_EXISTING = 3;

    private interface K32 extends StdCallLibrary {
        K32 INSTANCE = Native.load("kernel32", K32.class);

        Pointer CreateFileW(WString lpFileName, int dwDesiredAccess, int dwShareMode, Pointer lpSecurityAttributes,
                            int dwCreationDisposition, int dwFlagsAndAttributes, Pointer hTemplateFile);
        boolean WriteFile(Pointer hFile, byte[] lpBuffer, int nNumberOfBytesToWrite,
                          IntByReference lpNumberOfBytesWritten, Pointer lpOverlapped);
        boolean ReadFile(Pointer hFile, byte[] lpBuffer, int nNumberOfBytesToRead,
                         IntByReference lpNumberOfBytesRead, Pointer lpOverlapped);
        boolean PeekNamedPipe(Pointer hNamedPipe, byte[] lpBuffer, int nBufferSize,
                              IntByReference lpBytesRead, IntByReference lpTotalBytesAvail,
                              IntByReference lpBytesLeftThisMessage);
        boolean CloseHandle(Pointer hObject);
    }

    private static final List<Byte> frameBuf = new ArrayList<>();

    private static volatile boolean running = false;
    private static volatile Pointer pipe = null;
    private static volatile String wantDetails = "Flash Visual";
    private static volatile String wantState = "Запуск...";
    private static long sessionStart = 0;

    public static void init() {
        if (enabled) {
            start();
        }
    }

    public static void start() {
        if (running) {
            return;
        }
        running = true;
        sessionStart = System.currentTimeMillis();
        wantDetails = "Flash Visual";
        wantState = "В игре";
        Thread thread = new Thread(DiscordRpc::loop, "flash-discord-rpc");
        thread.setDaemon(true);
        thread.start();
    }

    public static void stop() {
        running = false;
        closePipe();
    }

    public static void update(String details, String state) {
        if (!enabled) {
            return;
        }
        wantDetails = details == null ? "" : details;
        wantState = state == null ? "" : state;
    }

    private static void loop() {
        while (running) {
            try {
                if (!connect()) {
                    sleepQuiet(3000);
                    continue;
                }
                sendFrame(OP_HANDSHAKE, "{\"v\":1,\"client_id\":\"" + CLIENT_ID + "\"}");
                boolean ok = drain();
                String lastD = null;
                String lastS = null;
                long lastSent = 0;
                while (running && ok && pipeOpen()) {
                    ok = drain();
                    if (!ok) {
                        break;
                    }
                    String d = wantDetails;
                    String s = wantState;
                    long now = System.currentTimeMillis();
                    if (now - lastSent > 15000 || !d.equals(lastD) || !s.equals(lastS)) {
                        if (sendActivity(d, s)) {
                            lastD = d;
                            lastS = s;
                            lastSent = now;
                        } else {
                            ok = false;
                        }
                    }
                    sleepQuiet(1000);
                }
            } catch (Throwable ignored) {
            }
            closePipe();
            sleepQuiet(3000);
        }
    }

    private static boolean connect() {
        for (int i = 0; i < 10; i++) {
            try {
                Pointer h = K32.INSTANCE.CreateFileW(new WString("\\\\.\\pipe\\discord-ipc-" + i),
                    GENERIC_READ | GENERIC_WRITE, 0, null, OPEN_EXISTING, 0, null);
                if (h != null) {
                    long v = Pointer.nativeValue(h);
                    if (v != 0 && v != -1) {
                        pipe = h;
                        return true;
                    }
                }
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    private static boolean sendActivity(String details, String state) {
        JsonObject activity = new JsonObject();
        activity.addProperty("type", 0);
        activity.addProperty("details", details);
        activity.addProperty("state", state);

        JsonObject timestamps = new JsonObject();
        timestamps.addProperty("start", sessionStart);
        activity.add("timestamps", timestamps);

        JsonObject assets = new JsonObject();
        assets.addProperty("large_image", "flash");
        assets.addProperty("large_text", "Flash Visual");
        assets.addProperty("small_image", "flash");
        assets.addProperty("small_text", "v1.1.0-pre1");
        activity.add("assets", assets);

        JsonObject args = new JsonObject();
        args.addProperty("pid", ProcessHandle.current().pid());
        args.add("activity", activity);

        JsonObject payload = new JsonObject();
        payload.addProperty("cmd", "SET_ACTIVITY");
        payload.addProperty("nonce", Long.toString(System.nanoTime()));
        payload.add("args", args);

        return sendFrame(OP_FRAME, payload.toString());
    }

    private static boolean sendFrame(int op, String json) {
        try {
            if (!pipeOpen()) {
                return false;
            }
            byte[] body = json.getBytes(StandardCharsets.UTF_8);
            ByteBuffer bb = ByteBuffer.allocate(8 + body.length).order(ByteOrder.LITTLE_ENDIAN);
            bb.putInt(op);
            bb.putInt(body.length);
            bb.put(body);
            byte[] data = bb.array();
            IntByReference written = new IntByReference();
            return K32.INSTANCE.WriteFile(pipe, data, data.length, written, null);
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean drain() {
        try {
            IntByReference read = new IntByReference();
            IntByReference avail = new IntByReference();
            IntByReference left = new IntByReference();
            if (!K32.INSTANCE.PeekNamedPipe(pipe, new byte[0], 0, read, avail, left)) {
                return false;
            }
            int n = avail.getValue();
            if (n <= 0) {
                return true;
            }
            byte[] data = new byte[n];
            IntByReference rd = new IntByReference();
            if (!K32.INSTANCE.ReadFile(pipe, data, n, rd, null)) {
                return false;
            }
            int got = rd.getValue();
            for (int i = 0; i < got; i++) {
                frameBuf.add(data[i]);
            }
            while (frameBuf.size() >= 8) {
                int op = intLE(frameBuf, 0);
                int len = intLE(frameBuf, 4);
                if (len < 0 || len > 65536) {
                    frameBuf.clear();
                    return false;
                }
                if (frameBuf.size() < 8 + len) {
                    break;
                }
                byte[] body = new byte[len];
                for (int i = 0; i < len; i++) {
                    body[i] = frameBuf.get(8 + i);
                }
                for (int i = 0; i < 8 + len; i++) {
                    frameBuf.remove(0);
                }
                if (op == OP_PING) {
                    sendFrame(OP_PONG, new String(body, StandardCharsets.UTF_8));
                } else if (op == OP_CLOSE) {
                    return false;
                }
            }
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static int intLE(List<Byte> buf, int off) {
        return (buf.get(off) & 0xFF)
            | ((buf.get(off + 1) & 0xFF) << 8)
            | ((buf.get(off + 2) & 0xFF) << 16)
            | ((buf.get(off + 3) & 0xFF) << 24);
    }

    private static boolean pipeOpen() {
        return pipe != null && Pointer.nativeValue(pipe) != 0 && Pointer.nativeValue(pipe) != -1;
    }

    private static void closePipe() {
        try {
            if (pipeOpen()) {
                K32.INSTANCE.CloseHandle(pipe);
            }
        } catch (Throwable ignored) {
        }
        pipe = null;
        frameBuf.clear();
    }

    private static void sleepQuiet(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }
}
