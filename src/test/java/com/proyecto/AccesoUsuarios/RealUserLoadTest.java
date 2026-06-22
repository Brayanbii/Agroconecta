package com.proyecto.AccesoUsuarios;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * Simulación de usuarios REALES concurrentes en AgroConecta
 * Cada usuario: se registra → inicia sesión → navega tienda →
 *   agrega al carrito → crea pedido
 */
public class RealUserLoadTest {

    static final String BASE = "https://agroconecta-04uf.onrender.com";
    static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    static final List<String> LOG = new ArrayList<>();
    static final List<String> CSV = new ArrayList<>();
    static AtomicInteger totalReq = new AtomicInteger(0);
    static AtomicInteger totalOk = new AtomicInteger(0);
    static AtomicInteger totalFail = new AtomicInteger(0);
    static List<Long> allTimes = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) throws Exception {
        log("");
        log("╔══════════════════════════════════════════════════════════╗");
        log("║   PRUEBAS DE CARGA CON USUARIOS REALES - AGROCONECTA     ║");
        log("║   Cada usuario: registra → login → tienda → carrito → pedido");
        log("╚══════════════════════════════════════════════════════════╝");
        log("Fecha: " + LocalDateTime.now().format(FMT));
        log("URL:   " + BASE);
        log("");

        // Calentar servidor
        log("🔥 Calentando servidor...");
        for (int i = 0; i < 6; i++) {
            try {
                HttpURLConnection c = conn("/");
                if (c.getResponseCode() == 200) { c.disconnect(); log("⚡ Listo tras " + (i+1) + " intentos"); break; }
                c.disconnect();
            } catch (Exception ignored) {}
            Thread.sleep(8000);
        }

        CSV.add("Nivel,Usuarios,Peticiones,Éxitos,Fallos,Tasa,Total(s),Prom(ms),Min(ms),Max(ms)");
        int[] niveles = {1, 3, 5, 10};

        for (int n : niveles) {
            log("");
            log("══════════════════════════════════════════════════");
            log("⚡ " + n + " USUARIOS SIMULTÁNEOS (flujo completo)");
            log("══════════════════════════════════════════════════");
            runLevel(n);
        }

        // Guardar CSV
        Files.write(Path.of("target/load-user-results.csv"), CSV);
        Files.write(Path.of("target/load-user-log.txt"), LOG);
        log("");
        log("✅ PRUEBAS COMPLETADAS");
    }

    static void runLevel(int users) throws Exception {
        AtomicInteger req = new AtomicInteger(0);
        AtomicInteger ok = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);
        List<Long> times = Collections.synchronizedList(new ArrayList<>());
        Instant t0 = Instant.now();
        ExecutorService pool = Executors.newFixedThreadPool(users);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < users; i++) {
            final int uid = i + 1;
            futures.add(pool.submit(() -> flujoUsuario(uid, req, ok, fail, times)));
        }

        for (Future<?> f : futures) {
            try { f.get(120, TimeUnit.SECONDS); } catch (Exception ignored) {}
        }
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        double secs = Duration.between(t0, Instant.now()).toMillis() / 1000.0;
        List<Long> sorted = times.stream().filter(t -> t > 0).sorted().toList();
        long minMs = sorted.isEmpty() ? 0 : sorted.get(0);
        long maxMs = sorted.isEmpty() ? 0 : sorted.get(sorted.size()-1);
        double avgMs = sorted.isEmpty() ? 0 : sorted.stream().mapToLong(Long::longValue).average().orElse(0);
        double tasa = req.get() > 0 ? ok.get() * 100.0 / req.get() : 0;

        log(String.format("   ✅ %d  |  ❌ %d  |  Total: %d peticiones  |  Tasa: %.0f%%",
                ok.get(), fail.get(), req.get(), tasa));
        log(String.format("   ⏱ %.1fs  |  Promedio: %.0fms  |  Mín: %dms  |  Máx: %dms",
                secs, avgMs, minMs, maxMs));
        log("");

        CSV.add(String.format(Locale.US, "%d,%d,%d,%d,%d,%.0f,%.1f,%.0f,%d,%d",
                users, users, req.get(), ok.get(), fail.get(), tasa, secs, avgMs, minMs, maxMs));

        totalReq.addAndGet(req.get());
        totalOk.addAndGet(ok.get());
        totalFail.addAndGet(fail.get());
        allTimes.addAll(times);
    }

    static void flujoUsuario(int uid, AtomicInteger req, AtomicInteger ok, AtomicInteger fail, List<Long> times) {
        String ts = String.valueOf(System.currentTimeMillis()).substring(7);
        String name = "testuser" + uid + "_" + ts;
        String email = name + "@test.com";
        String pass = "123456";
        String sessionCookie = null;

        try {
            // 1. REGISTRO
            req.incrementAndGet();
            Pair r = post("/api/usuarios/registrar",
                    "{\"userName\":\""+name+"\",\"nombreCompleto\":\"Usuario Carga "+uid+"\","
                            + "\"email\":\""+email+"\",\"password\":\""+pass+"\","
                            + "\"telefono\":\"300000"+uid+"\",\"rol\":\"CLIENTE\"}",
                    null, times);
            if (r.code == 200) ok.incrementAndGet(); else fail.incrementAndGet();
            Thread.sleep(500 + (int)(Math.random() * 1000));

            // 2. LOGIN
            req.incrementAndGet();
            r = post("/api/usuarios/login",
                    "{\"email\":\""+email+"\",\"password\":\""+pass+"\"}", null, times);
            if (r.code == 200) {
                ok.incrementAndGet();
                sessionCookie = r.cookie;
            } else fail.incrementAndGet();
            Thread.sleep(500 + (int)(Math.random() * 500));

            // 3. TIENDA
            req.incrementAndGet();
            r = get("/tienda", sessionCookie, times);
            if (r.code == 200) ok.incrementAndGet(); else fail.incrementAndGet();

            // 4. API PRODUCTOS
            req.incrementAndGet();
            r = get("/api/productos", sessionCookie, times);
            if (r.code == 200) ok.incrementAndGet(); else fail.incrementAndGet();

            // 5. AGREGAR AL CARRITO
            req.incrementAndGet();
            r = post("/api/carrito/agregar", "{\"id\":1,\"cantidad\":2}", sessionCookie, times);
            if (r.code == 200) ok.incrementAndGet(); else fail.incrementAndGet();

            // 6. CREAR PEDIDO
            req.incrementAndGet();
            r = post("/api/ordenes/crear",
                    "{\"tipoEnvio\":\"ECONOMICO\","
                            + "\"direccionEnvio\":\"Calle "+uid+" #"+ (10+uid) +"-"+ (20+uid) +", Bogotá\","
                            + "\"latitud\":4.7110,\"longitud\":-74.0721}",
                    sessionCookie, times);
            if (r.code == 200) ok.incrementAndGet(); else fail.incrementAndGet();

            Thread.sleep(300);
        } catch (Exception e) {
            fail.incrementAndGet();
        }
    }

    // ═══ HELPERS HTTP ═══
    static Pair get(String path, String cookie, List<Long> times) throws Exception {
        long t0 = System.currentTimeMillis();
        HttpURLConnection c = conn(path);
        if (cookie != null) c.setRequestProperty("Cookie", cookie);
        int code = c.getResponseCode();
        c.getInputStream().close();
        c.disconnect();
        times.add(System.currentTimeMillis() - t0);
        return new Pair(code, null);
    }

    static Pair post(String path, String json, String cookie, List<Long> times) throws Exception {
        long t0 = System.currentTimeMillis();
        HttpURLConnection c = conn(path);
        c.setRequestMethod("POST");
        c.setDoOutput(true);
        c.setRequestProperty("Content-Type", "application/json");
        if (cookie != null) c.setRequestProperty("Cookie", cookie);
        c.getOutputStream().write(json.getBytes());
        int code = c.getResponseCode();
        c.getInputStream().close();
        String setCookie = c.getHeaderField("Set-Cookie");
        if (setCookie != null && setCookie.contains(";")) setCookie = setCookie.split(";")[0];
        c.disconnect();
        times.add(System.currentTimeMillis() - t0);
        return new Pair(code, setCookie);
    }

    static HttpURLConnection conn(String path) throws Exception {
        HttpURLConnection c = (HttpURLConnection) URI.create(BASE + path).toURL().openConnection();
        c.setConnectTimeout(15000);
        c.setReadTimeout(30000);
        c.setRequestProperty("User-Agent", "AgroConecta-UserLoad/1.0");
        return c;
    }

    static class Pair { int code; String cookie; Pair(int c, String co) { this.code = c; this.cookie = co; } }
    static void log(String s) { System.out.println(s); LOG.add(s); }
}
