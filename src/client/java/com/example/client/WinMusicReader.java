package com.example.client;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WinMusicReader {
    private interface FlashUser32 extends StdCallLibrary {
        FlashUser32 INSTANCE = Native.load("user32", FlashUser32.class);

        boolean EnumWindows(WndEnumProc lpEnumFunc, Pointer lParam);
        boolean IsWindowVisible(Pointer hWnd);
        int GetWindowTextW(Pointer hWnd, char[] lpString, int nMaxCount);
        int GetWindowThreadProcessId(Pointer hWnd, IntByReference processId);
        Pointer GetForegroundWindow();
        void keybd_event(byte bVk, byte bScan, int dwFlags, long dwExtraInfo);
    }

    private interface WndEnumProc extends StdCallLibrary.StdCallCallback {
        boolean callback(Pointer hWnd, Pointer lParam);
    }

    private interface Kernel32 extends StdCallLibrary {
        Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class);

        Pointer OpenProcess(int dwDesiredAccess, boolean bInheritHandle, int dwProcessId);
        boolean CloseHandle(Pointer hObject);
        boolean QueryFullProcessImageNameW(Pointer hProcess, int dwFlags, char[] lpExeName, IntByReference lpBufferSize);
    }

    private static final int PROCESS_QUERY_INFORMATION = 0x0400;
    private static final int PROCESS_QUERY_LIMITED_INFORMATION = 0x1000;

    private static final int VK_MEDIA_NEXT_TRACK = 0xB0;
    private static final int VK_MEDIA_PREV_TRACK = 0xB1;
    private static final int VK_MEDIA_PLAY_PAUSE = 0xB3;
    private static final int KEYEVENTF_KEYUP = 0x0002;

    private static final String[] MEDIA_EXES = {
        "spotify", "aimp", "musicbee", "foobar2000", "winamp", "vlc",
        "wmplayer", "itunes", "deezer", "soundcloud", "cloudmusic",
        "yandexmusic", "yandexmusictray", "mpv", "mpv.net", "vkmusic",
        "tidal", "resonic", "potplayer", "kmplayer", "media player",
        "windows media"
    };

    private static final String[] BROWSER_EXES = {
        "chrome", "msedge", "firefox", "yandexbrowser", "opera", "brave",
        "vivaldi", "chromium", "waterfox"
    };

    private static final String[][] MEDIA_MARKERS = {
        {"youtube music", "YouTube Music"},
        {"яндекс музыка", "Яндекс Музыка"},
        {"yandex music", "Yandex Music"},
        {"vk music", "VK Music"},
        {"музыка вк", "VK Music"},
        {"soundcloud", "SoundCloud"},
        {"deezer", "Deezer"},
        {"zvuk", "Звук"},
        {"звук", "Звук"},
        {"youtube", "YouTube"},
        {"яндекс", "Яндекс"},
        {"музыка", "Музыка"},
        {"music", "Music"}
    };

    private static final String[] BROWSER_SUFFIXES = {
        " - Google Chrome", " - Microsoft Edge", " - Mozilla Firefox",
        " - Yandex Browser", " - Opera GX", " - Opera", " - Brave"
    };

    private static volatile String lastApp = null;
    private static volatile long lastPosMs = -1;
    private static volatile long lastTotalMs = -1;

    public static String lastApp() {
        return lastApp;
    }

    public static long realPosition() {
        return lastPosMs;
    }

    public static long realDuration() {
        return lastTotalMs;
    }

    public static String getNowPlaying() {
        try {
            String foreground = checkWindow(FlashUser32.INSTANCE.GetForegroundWindow());
            if (foreground != null) {
                return foreground;
            }

            List<Pointer> handles = new ArrayList<>();
            WndEnumProc proc = new WndEnumProc() {
                @Override
                public boolean callback(Pointer hWnd, Pointer lParam) {
                    if (FlashUser32.INSTANCE.IsWindowVisible(hWnd)) {
                        handles.add(hWnd);
                    }
                    return true;
                }
            };
            FlashUser32.INSTANCE.EnumWindows(proc, null);

            for (Pointer h : handles) {
                String title = windowTitle(h);
                if (title == null || title.isEmpty() || title.startsWith("Minecraft")) {
                    continue;
                }
                String procName = processName(h);
                if (procName == null) {
                    continue;
                }
                if (isMediaExe(procName)) {
                    lastApp = mediaAppName(procName);
                    parseTime(title);
                    String clean = cleanTitle(title, null, false);
                    if (clean != null && !clean.isEmpty()) {
                        return clean;
                    }
                }
                if (isBrowserExe(procName)) {
                    String app = matchMarker(title);
                    if (app != null) {
                        lastApp = app;
                        parseTime(title);
                        String clean = cleanTitle(title, app, true);
                        if (clean != null && !clean.isEmpty()) {
                            return clean;
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
            // Music HUD should never break the game
        }
        lastPosMs = -1;
        lastTotalMs = -1;
        return null;
    }

    private static String checkWindow(Pointer h) {
        if (h == null || Pointer.nativeValue(h) == 0) {
            return null;
        }
        String title = windowTitle(h);
        if (title == null || title.isEmpty() || title.startsWith("Minecraft")) {
            return null;
        }
        String procName = processName(h);
        if (procName == null) {
            return null;
        }
        if (isMediaExe(procName)) {
            lastApp = mediaAppName(procName);
            parseTime(title);
            return cleanTitle(title, null, false);
        }
        if (isBrowserExe(procName)) {
            String app = matchMarker(title);
            if (app != null) {
                lastApp = app;
                parseTime(title);
                return cleanTitle(title, app, true);
            }
        }
        return null;
    }

    private static final Pattern TIME_PATTERN =
        Pattern.compile("(\\d{1,3}):(\\d{2})\\s*[/|·•-]\\s*(\\d{1,3}):(\\d{2})");

    private static void parseTime(String raw) {
        lastPosMs = -1;
        lastTotalMs = -1;
        if (raw == null) {
            return;
        }
        try {
            Matcher m = TIME_PATTERN.matcher(raw);
            if (m.find()) {
                long p = Long.parseLong(m.group(1)) * 60 + Long.parseLong(m.group(2));
                long t = Long.parseLong(m.group(3)) * 60 + Long.parseLong(m.group(4));
                if (t > 0 && p >= 0 && p <= t + 60) {
                    lastPosMs = p * 1000L;
                    lastTotalMs = t * 1000L;
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private static String mediaAppName(String procName) {
        String[][] map = {
            {"spotify", "Spotify"}, {"aimp", "AIMP"}, {"musicbee", "MusicBee"},
            {"foobar2000", "foobar2000"}, {"winamp", "Winamp"}, {"vlc", "VLC"},
            {"wmplayer", "Windows Media"}, {"windows media", "Windows Media"},
            {"itunes", "iTunes"}, {"deezer", "Deezer"}, {"soundcloud", "SoundCloud"},
            {"cloudmusic", "Netease"}, {"yandexmusic", "Яндекс Музыка"},
            {"mpv", "mpv"}, {"vkmusic", "VK Music"}, {"tidal", "TIDAL"},
            {"potplayer", "PotPlayer"}, {"kmplayer", "KMPlayer"},
            {"media player", "Media Player"}
        };
        for (String[] m : map) {
            if (procName.contains(m[0])) {
                return m[1];
            }
        }
        return "Музыка";
    }

    private static boolean isMediaExe(String procName) {
        for (String exe : MEDIA_EXES) {
            if (procName.contains(exe)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBrowserExe(String procName) {
        for (String exe : BROWSER_EXES) {
            if (procName.contains(exe)) {
                return true;
            }
        }
        return false;
    }

    private static String matchMarker(String title) {
        String lower = title.toLowerCase();
        for (String[] marker : MEDIA_MARKERS) {
            if (lower.contains(marker[0])) {
                return marker[1];
            }
        }
        return null;
    }

    private static String cleanTitle(String title, String app, boolean discardIfUnchanged) {
        String t = title.trim();
        for (String sfx : BROWSER_SUFFIXES) {
            if (t.endsWith(sfx)) {
                t = t.substring(0, t.length() - sfx.length()).trim();
                break;
            }
        }
        if (app != null) {
            int idx = t.toLowerCase().lastIndexOf(app.toLowerCase());
            if (idx >= 0) {
                t = t.substring(0, idx).trim();
                int sep = t.lastIndexOf(" - ");
                if (sep >= 0) {
                    t = t.substring(0, sep).trim();
                }
            }
        }
        if (t.isEmpty() || (discardIfUnchanged && t.equalsIgnoreCase(title.trim()))) {
            return null;
        }
        return t;
    }

    public static void prev() {
        press(VK_MEDIA_PREV_TRACK);
    }

    public static void playPause() {
        press(VK_MEDIA_PLAY_PAUSE);
    }

    public static void next() {
        press(VK_MEDIA_NEXT_TRACK);
    }

    private static void press(int vk) {
        if (!sendInput(vk, false)) {
            try {
                FlashUser32.INSTANCE.keybd_event((byte) vk, (byte) 0, 0, 0);
            } catch (Throwable ignored) {
            }
        }
        if (!sendInput(vk, true)) {
            try {
                FlashUser32.INSTANCE.keybd_event((byte) vk, (byte) 0, KEYEVENTF_KEYUP, 0);
            } catch (Throwable ignored) {
            }
        }
    }

    private static boolean sendInput(int vk, boolean up) {
        try {
            WinUser.INPUT input = new WinUser.INPUT();
            input.type = new WinDef.DWORD(WinUser.INPUT.INPUT_KEYBOARD);
            input.input.setType(WinUser.KEYBDINPUT.class);
            input.input.ki.wVk = new WinDef.WORD(vk);
            input.input.ki.wScan = new WinDef.WORD(0);
            input.input.ki.dwFlags = new WinDef.DWORD(up ? KEYEVENTF_KEYUP : 0);
            input.input.ki.time = new WinDef.DWORD(0);
            input.input.ki.dwExtraInfo = null;
            return User32.INSTANCE.SendInput(new WinDef.DWORD(1), new WinUser.INPUT[]{input}, input.size()).intValue() == 1;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static String windowTitle(Pointer hWnd) {
        char[] buf = new char[1024];
        int len = FlashUser32.INSTANCE.GetWindowTextW(hWnd, buf, buf.length);
        return len > 0 ? new String(buf, 0, len) : null;
    }

    private static String processName(Pointer hWnd) {
        IntByReference pid = new IntByReference();
        FlashUser32.INSTANCE.GetWindowThreadProcessId(hWnd, pid);
        if (pid.getValue() == 0) {
            return null;
        }
        Pointer handle = Kernel32.INSTANCE.OpenProcess(
            PROCESS_QUERY_INFORMATION | PROCESS_QUERY_LIMITED_INFORMATION, false, pid.getValue());
        if (handle == null || Pointer.nativeValue(handle) == 0) {
            return null;
        }
        try {
            char[] buf = new char[1024];
            IntByReference size = new IntByReference(buf.length);
            if (Kernel32.INSTANCE.QueryFullProcessImageNameW(handle, 0, buf, size)) {
                String path = new String(buf, 0, size.getValue());
                int idx = Math.max(path.lastIndexOf('\\'), path.lastIndexOf('/'));
                return (idx >= 0 ? path.substring(idx + 1) : path).toLowerCase();
            }
        } finally {
            Kernel32.INSTANCE.CloseHandle(handle);
        }
        return null;
    }
}
