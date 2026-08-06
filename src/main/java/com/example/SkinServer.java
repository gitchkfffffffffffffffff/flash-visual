package com.example;

import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SkinServer {
    private static final Logger LOGGER = LoggerFactory.getLogger("flash-visual-skins");

    private static final Map<UUID, byte[]> SKINS = new ConcurrentHashMap<>();
    private static HttpServer http;
    private static int port = 0;

    public static void setSkin(UUID uuid, byte[] png) {
        SKINS.put(uuid, png);
        ensureRunning();
    }

    public static boolean hasSkin(UUID uuid) {
        return SKINS.containsKey(uuid);
    }

    public static String skinUrl(UUID uuid) {
        if (port == 0) {
            return null;
        }
        return "http://127.0.0.1:" + port + "/skin/" + uuid;
    }

    private static synchronized void ensureRunning() {
        if (http != null) {
            return;
        }
        try {
            ServerSocket probe = new ServerSocket(0);
            port = probe.getLocalPort();
            probe.close();
            http = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
            http.createContext("/skin/", exchange -> {
                String path = exchange.getRequestURI().getPath();
                String id = path.substring(path.lastIndexOf('/') + 1);
                try {
                    byte[] data = SKINS.get(UUID.fromString(id));
                    if (data == null) {
                        exchange.sendResponseHeaders(404, -1);
                    } else {
                        exchange.getResponseHeaders().set("Content-Type", "image/png");
                        exchange.sendResponseHeaders(200, data.length);
                        try (OutputStream out = exchange.getResponseBody()) {
                            out.write(data);
                        }
                    }
                } catch (Exception e) {
                    try {
                        exchange.sendResponseHeaders(500, -1);
                    } catch (IOException ignored) {
                    }
                } finally {
                    exchange.close();
                }
            });
            http.start();
            LOGGER.info("Skin HTTP server started on port {}", port);
        } catch (IOException e) {
            LOGGER.error("Failed to start skin HTTP server", e);
            port = 0;
        }
    }
}
