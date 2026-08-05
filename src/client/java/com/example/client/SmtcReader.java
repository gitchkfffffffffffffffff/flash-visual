package com.example.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class SmtcReader {
    private static final String SCRIPT = """
        $ErrorActionPreference = 'Stop'
        Add-Type -AssemblyName System.Runtime.WindowsRuntime
        $null = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager, Windows.Media.Control, ContentType = WindowsRuntime]
        $asTaskGeneric = ([System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object { $_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1' })[0]
        function Await($WinRtTask, $ResultType) {
          $asTask = $asTaskGeneric.MakeGenericMethod($ResultType)
          $netTask = $asTask.Invoke($null, @($WinRtTask))
          $netTask.Wait(-1) | Out-Null
          return $netTask.Result
        }
        while ($true) {
          $cmd = [Console]::In.ReadLine()
          if ($null -eq $cmd) { break }
          if ($cmd -eq 'QUIT') { break }
          if ($cmd -ne 'GET') { continue }
          try {
            $mgr = Await ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager]::RequestAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager])
            $sess = $mgr.GetCurrentSession()
            if ($null -eq $sess) { [Console]::Out.WriteLine(''); [Console]::Out.Flush(); continue }
            $tl = Await ($sess.GetTimelinePropertiesAsync()) ([Windows.Media.Control.SystemMediaTransportControlsTimelineProperties])
            $info = Await ($sess.TryGetMediaPropertiesAsync()) ([Windows.Media.Control.SystemMediaTransportControlsMediaProperties])
            $pos = [long]$tl.Position.TotalMilliseconds
            $end = [long]$tl.EndTime.TotalMilliseconds
            $isPlaying = if ($sess.PlaybackState -eq 'Playing') { 1 } else { 0 }
            $t = [string]$info.Title
            $a = [string]$info.Artist
            $app = [string]$sess.SourceAppUserModelId
            [Console]::Out.WriteLine($pos.ToString() + '|' + $end.ToString() + '|' + $isPlaying.ToString() + '|' + $t + '|' + $a + '|' + $app)
          } catch {
            [Console]::Out.WriteLine('')
          }
          [Console]::Out.Flush()
        }
        """;

    private static volatile String curTitle = null;
    private static volatile String curArtist = null;
    private static volatile String curAppId = null;
    private static volatile long posMs = -1;
    private static volatile long durMs = -1;
    private static volatile long lastPollMs = 0;
    private static volatile boolean alive = false;
    private static volatile boolean isPlaying = false;
    private static final Object lock = new Object();
    private static Thread thread = null;

    public static String title() {
        return curTitle;
    }

    public static String artist() {
        return curArtist;
    }

    public static String appId() {
        return curAppId;
    }

    public static long position() {
        return posMs;
    }

    public static long duration() {
        return durMs;
    }

    public static long lastPollMs() {
        return lastPollMs;
    }

    public static boolean alive() {
        return alive;
    }

    public static boolean isPlaying() {
        return isPlaying;
    }

    public static void start() {
        synchronized (lock) {
            if (thread != null) {
                return;
            }
            thread = new Thread(SmtcReader::run, "Flash-SMTC");
            thread.setDaemon(true);
            thread.start();
        }
    }

    private static void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                if (!runOnce()) {
                    Thread.sleep(2000);
                }
            } catch (Throwable ignored) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
        alive = false;
    }

    private static boolean runOnce() {
        Process p = null;
        try {
            p = new ProcessBuilder("powershell.exe", "-NoLogo", "-NoProfile", "-NonInteractive",
                "-ExecutionPolicy", "Bypass", "-Command", SCRIPT)
                .redirectErrorStream(true)
                .start();
            OutputStreamWriter out = new OutputStreamWriter(p.getOutputStream(), StandardCharsets.UTF_8);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
            while (!p.isAlive() || in.ready()) {
                try {
                    out.write("GET\n");
                    out.flush();
                    String line = in.readLine();
                    if (line == null) {
                        break;
                    }
                    parse(line);
                    alive = true;
                    Thread.sleep(500);
                } catch (Exception e) {
                    break;
                }
            }
            alive = false;
            return false;
        } catch (Throwable ignored) {
            alive = false;
            return false;
        } finally {
            if (p != null) {
                p.destroy();
            }
        }
    }

    private static void parse(String line) {
        if (line == null || line.isEmpty()) {
            return;
        }
        String[] parts = line.split("\\|", -1);
        if (parts.length < 6) {
            return;
        }
        try {
            long pos = Long.parseLong(parts[0].trim());
            long end = Long.parseLong(parts[1].trim());
            isPlaying = "1".equals(parts[2].trim());
            if (end > 0) {
                durMs = end;
                posMs = Math.max(0, Math.min(pos, end));
            } else {
                posMs = -1;
                durMs = -1;
            }
            String title = parts[3].trim();
            curTitle = title.isEmpty() ? null : title;
            String artist = parts[4].trim();
            curArtist = artist.isEmpty() ? null : artist;
            String app = parts[5].trim();
            curAppId = app.isEmpty() ? null : app;
            lastPollMs = System.currentTimeMillis();
        } catch (Throwable ignored) {
        }
    }
}
