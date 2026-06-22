package com.proyecto.AccesoUsuarios;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Pruebas de carga y estrés para AgroConecta
 * Simula usuarios concurrentes y mide rendimiento
 */
public class LoadTestRunner {

    private static final String BASE_URL = "https://agroconecta-04uf.onrender.com";
    private static final List<String> CSV_ROWS = new ArrayList<>();
    private static final List<String> REPORT_LINES = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        log("");
        log("╔══════════════════════════════════════════════════════╗");
        log("║   PRUEBAS DE CARGA - AGROCONECTA                     ║");
        log("║   Fecha: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "                    ║");
        log("╚══════════════════════════════════════════════════════╝");
        log("");
        log("URL objetivo: " + BASE_URL);
        log("");

        // Calentar el servidor (cold start de Render)
        log("🔥 CALENTANDO SERVIDOR (cold start Render ~50s)...");
        for (int i = 0; i < 6; i++) {
            try {
                HttpURLConnection c = (HttpURLConnection) URI.create(BASE_URL).toURL().openConnection();
                c.setConnectTimeout(10000);
                c.setReadTimeout(15000);
                int code = c.getResponseCode();
                c.disconnect();
                if (code == 200) {
                    log("⚡ Servidor listo tras " + (i + 1) + " intentos");
                    break;
                }
            } catch (Exception ignored) {}
            Thread.sleep(8000);
        }

        log("");
        log("═══════════════════════════════════════════════════════");
        log("  INICIANDO PRUEBAS DE CARGA");
        log("═══════════════════════════════════════════════════════");
        log("");

        CSV_ROWS.add("Nivel,Usuarios,Requests,Éxitos,Fallos,TasaÉxito,TiempoTotal(s),TPS,RespuestaMin(ms),RespuestaMax(ms),RespuestaProm(ms)");

        // Niveles de carga
        int[] niveles = {1, 5, 10, 20, 50};
        for (int nivel : niveles) {
            runLoadTest(nivel);
        }

        // Guardar CSV
        String csvPath = "target/load-test-results.csv";
        Files.write(Path.of(csvPath), CSV_ROWS);
        log("");
        log("📊 CSV guardado en: " + csvPath);

        // Guardar reporte
        String reportPath = "target/load-test-report.md";
        Files.write(Path.of(reportPath), REPORT_LINES);
        log("📄 Reporte guardado en: " + reportPath);
        log("");
        log("✅ PRUEBAS DE CARGA COMPLETADAS");
    }

    static void runLoadTest(int concurrentUsers) throws Exception {
        log("───────────────────────────────────────────────────────");
        log("⚡ NIVEL: " + concurrentUsers + " usuarios concurrentes");
        log("───────────────────────────────────────────────────────");

        String[] endpoints = {
            "/",                                    // Página inicio
            "/login",                               // Página login
            "/tienda",                              // Tienda
            "/api/productos",                       // API productos
            "/api/sipsa/catalogo",                  // Catálogo precios
        };

        int totalRequests = endpoints.length * concurrentUsers;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(concurrentUsers, 20));
        Instant start = Instant.now();

        List<Future<?>> futures = new ArrayList<>();

        for (int u = 0; u < concurrentUsers; u++) {
            for (String endpoint : endpoints) {
                futures.add(executor.submit(() -> {
                    try {
                        long t0 = System.currentTimeMillis();
                        HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URL + endpoint).toURL().openConnection();
                        conn.setConnectTimeout(15000);
                        conn.setReadTimeout(30000);
                        conn.setRequestMethod("GET");
                        conn.setRequestProperty("User-Agent", "AgroConecta-LoadTest/1.0");
                        int code = conn.getResponseCode();
                        conn.getInputStream().close();
                        long t1 = System.currentTimeMillis();
                        responseTimes.add(t1 - t0);
                        if (code >= 200 && code < 400) {
                            successCount.incrementAndGet();
                        } else {
                            failCount.incrementAndGet();
                        }
                        conn.disconnect();
                    } catch (Exception e) {
                        failCount.incrementAndGet();
                        responseTimes.add(-1L);
                    }
                }));
            }
        }

        for (Future<?> f : futures) {
            try { f.get(60, TimeUnit.SECONDS); } catch (Exception ignored) {}
        }

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        Instant end = Instant.now();
        double totalSeconds = Duration.between(start, end).toMillis() / 1000.0;

        List<Long> validTimes = responseTimes.stream().filter(t -> t > 0).sorted().toList();
        long minMs = validTimes.isEmpty() ? 0 : validTimes.get(0);
        long maxMs = validTimes.isEmpty() ? 0 : validTimes.get(validTimes.size() - 1);
        double avgMs = validTimes.isEmpty() ? 0 : validTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        double tps = totalSeconds > 0 ? totalRequests / totalSeconds : 0;
        double successRate = totalRequests > 0 ? (successCount.get() * 100.0 / totalRequests) : 0;

        log(String.format("   ✅ Éxitos: %d  |  ❌ Fallos: %d  |  📊 Tasa: %.1f%%", successCount.get(), failCount.get(), successRate));
        log(String.format("   ⏱️  Total: %.1fs  |  🚀 TPS: %.1f  |  ⚡ Min: %dms  |  🐢 Max: %dms  |  📈 Prom: %.0fms", totalSeconds, tps, minMs, maxMs, avgMs));
        log("");

        CSV_ROWS.add(String.format(Locale.US, "%d,%d,%d,%d,%d,%.1f,%.1f,%.1f,%d,%d,%.0f",
            concurrentUsers, concurrentUsers, totalRequests, successCount.get(), failCount.get(),
            successRate, totalSeconds, tps, minMs, maxMs, avgMs));
    }

    static void log(String msg) {
        System.out.println(msg);
        REPORT_LINES.add(msg);
    }
}
