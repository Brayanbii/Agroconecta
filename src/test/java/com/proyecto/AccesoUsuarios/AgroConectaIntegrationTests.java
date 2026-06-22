package com.proyecto.AccesoUsuarios;

import org.junit.jupiter.api.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración contra la API REST de AgroConecta
 * Ejecuta contra el servidor en producción (Render)
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AgroConectaIntegrationTests {

    private static final String BASE_URL = "https://agroconecta-04uf.onrender.com";
    private static final RestTemplate rest = new RestTemplate();

    private static String adminCookie;
    private static String campesinoCookie;
    private static String clienteCookie;
    private static Long ordenId;

    // ============================================
    // 🔐 AUTENTICACIÓN
    // ============================================

    @Test @Order(1) @DisplayName("IT01 - Login Admin")
    void loginAdmin() {
        Map<String, String> body = Map.of("email", "admin@agroconecta.com", "password", "123");
        ResponseEntity<Map> resp = post("/api/usuarios/login", body, null);

        assertEquals(200, resp.getStatusCodeValue(), "Login admin debe retornar 200");
        assertTrue(((Boolean) resp.getBody().get("success")), "Login debe ser exitoso");
        adminCookie = extractCookie(resp);
        assertNotNull(adminCookie, "Debe devolver cookie de sesión");
        System.out.println("✅ IT01: Login admin exitoso — sesión capturada");
    }

    @Test @Order(2) @DisplayName("IT02 - Login Campesino")
    void loginCampesino() {
        Map<String, String> body = Map.of("email", "pepe@finca.com", "password", "123");
        ResponseEntity<Map> resp = post("/api/usuarios/login", body, null);

        assertEquals(200, resp.getStatusCodeValue());
        assertTrue(((Boolean) resp.getBody().get("success")));
        campesinoCookie = extractCookie(resp);
        assertNotNull(campesinoCookie);
        System.out.println("✅ IT02: Login campesino exitoso");
    }

    @Test @Order(3) @DisplayName("IT03 - Login Cliente")
    void loginCliente() {
        Map<String, String> body = Map.of("email", "maria@gmail.com", "password", "123");
        ResponseEntity<Map> resp = post("/api/usuarios/login", body, null);

        assertEquals(200, resp.getStatusCodeValue());
        assertTrue(((Boolean) resp.getBody().get("success")));
        clienteCookie = extractCookie(resp);
        assertNotNull(clienteCookie);
        System.out.println("✅ IT03: Login cliente exitoso");
    }

    @Test @Order(4) @DisplayName("IT04 - Login fallido")
    void loginFallido() {
        Map<String, String> body = Map.of("email", "noexiste@falso.com", "password", "xxx");
        ResponseEntity<Map> resp = post("/api/usuarios/login", body, null);

        assertEquals(200, resp.getStatusCodeValue());
        assertFalse(((Boolean) resp.getBody().get("success")), "Login con credenciales falsas debe fallar");
        System.out.println("✅ IT04: Login fallido detectado");
    }

    @Test @Order(5) @DisplayName("IT05 - Verificar email existente")
    void verificarEmail() {
        ResponseEntity<Map> resp = get("/api/usuarios/check-email?email=admin@agroconecta.com", null);

        assertEquals(200, resp.getStatusCodeValue());
        assertTrue(((Boolean) resp.getBody().get("existe")), "Email de admin debe existir");
        System.out.println("✅ IT05: Verificación de email correcta");
    }

    // ============================================
    // 📦 PRODUCTOS
    // ============================================

    @Test @Order(10) @DisplayName("IT06 - Listar productos")
    void listarProductos() {
        ResponseEntity<List> resp = getList("/api/productos", null);

        assertEquals(200, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
        assertFalse(resp.getBody().isEmpty(), "Debe haber al menos 2 productos de seed");
        System.out.println("✅ IT06: " + resp.getBody().size() + " productos listados");
    }

    @Test @Order(11) @DisplayName("IT07 - Productos de campesino")
    void productosCampesino() {
        ResponseEntity<List> resp = getList("/api/productos/campesino/2", null);

        assertEquals(200, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
        System.out.println("✅ IT07: " + resp.getBody().size() + " productos del campesino");
    }

    // ============================================
    // 🛒 CARRITO
    // ============================================

    @Test @Order(20) @DisplayName("IT08 - Ver carrito (vacío)")
    void verCarrito() {
        ResponseEntity<Map> resp = get("/api/carrito", clienteCookie);

        assertEquals(200, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
        System.out.println("✅ IT08: Carrito consultado — items: " + resp.getBody().getOrDefault("itemsCount", "0"));
    }

    @Test @Order(21) @DisplayName("IT09 - Agregar producto al carrito")
    void agregarCarrito() {
        Map<String, Object> body = new HashMap<>();
        body.put("id", 1);
        body.put("cantidad", 2);

        ResponseEntity<Map> resp = post("/api/carrito/agregar", body, clienteCookie);

        assertEquals(200, resp.getStatusCodeValue());
        System.out.println("✅ IT09: Producto agregado al carrito");
    }

    // ============================================
    // 📋 PEDIDOS — FLUJO COMPLETO
    // ============================================

    @Test @Order(30) @DisplayName("IT10 - Previsualizar costo de envío")
    void previewEnvio() {
        Map<String, Object> body = new HashMap<>();
        body.put("tipoEnvio", "ECONOMICO");
        body.put("latitud", 4.7110);
        body.put("longitud", -74.0721);

        ResponseEntity<Map> resp = post("/api/ordenes/preview-envio", body, clienteCookie);

        assertEquals(200, resp.getStatusCodeValue());
        assertTrue(resp.getBody().containsKey("costoEnvio"));
        System.out.println("✅ IT10: Costo envío calculado → $" + resp.getBody().get("costoEnvio"));
    }

    @Test @Order(31) @DisplayName("IT11 - Crear pedido")
    void crearPedido() {
        Map<String, Object> body = new HashMap<>();
        body.put("tipoEnvio", "ECONOMICO");
        body.put("direccionEnvio", "Carrera 10 #20-30, Bogotá");
        body.put("latitud", 4.7110);
        body.put("longitud", -74.0721);

        ResponseEntity<Map> resp = post("/api/ordenes/crear", body, clienteCookie);

        assertEquals(200, resp.getStatusCodeValue());
        if (resp.getBody().get("ordenId") != null) {
            ordenId = ((Number) resp.getBody().get("ordenId")).longValue();
            System.out.println("✅ IT11: Pedido creado — ID: " + ordenId);
        } else {
            System.out.println("✅ IT11: Pedido creado (respuesta: " + resp.getBody().get("message") + ")");
        }
    }

    @Test @Order(32) @DisplayName("IT12 - Mis compras")
    void misCompras() {
        ResponseEntity<List> resp = getList("/api/ordenes/mis-compras", clienteCookie);

        assertEquals(200, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
        System.out.println("✅ IT12: Compras listadas — " + resp.getBody().size() + " pedidos");
    }

    @Test @Order(33) @DisplayName("IT13 - Mis ventas (campesino)")
    void misVentas() {
        ResponseEntity<Map> resp = get("/api/pedidos/mis-ventas", campesinoCookie);

        assertEquals(200, resp.getStatusCodeValue());
        System.out.println("✅ IT13: Ventas del campesino consultadas");
    }

    @Test @Order(34) @DisplayName("IT14 - Campesino acepta pedido")
    void campesinoAceptaPedido() {
        if (ordenId != null) {
            ResponseEntity<Map> resp = post("/api/pedidos/orden/" + ordenId + "/aceptar", null, campesinoCookie);

            assertEquals(200, resp.getStatusCodeValue());
            assertTrue(((Boolean) resp.getBody().get("success")),
                    "Campesino debe poder aceptar pedido. Resp: " + resp.getBody());
            System.out.println("✅ IT14: Pedido aceptado por campesino → " + resp.getBody().get("nuevoEstado"));
        } else {
            System.out.println("⚠ IT14: Saltado — no hay ordenId");
        }
    }

    // ============================================
    // 📊 PRECIOS Y ANALÍTICAS
    // ============================================

    @Test @Order(40) @DisplayName("IT15 - API precios SIPSA")
    void apiPreciosSipsa() {
        ResponseEntity<Map> resp = get("/api/v1/precios", null);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals("success", resp.getBody().get("status"));
        assertNotNull(resp.getBody().get("data"));
        System.out.println("✅ IT15: Precios SIPSA obtenidos — fuente: " + resp.getBody().get("fuente"));
    }

    @Test @Order(41) @DisplayName("IT16 - Catálogo de precios")
    void catalogoPrecios() {
        ResponseEntity<Map> resp = get("/api/sipsa/catalogo", null);

        assertEquals(200, resp.getStatusCodeValue());
        System.out.println("✅ IT16: Catálogo de precios OK");
    }

    // ============================================
    // ⭐ RESEÑAS
    // ============================================

    @Test @Order(50) @DisplayName("IT17 - Reseñas de producto")
    void resenasProducto() {
        ResponseEntity<List> resp = getList("/api/resenas/producto/1", null);

        assertEquals(200, resp.getStatusCodeValue());
        System.out.println("✅ IT17: Reseñas consultadas — " + (resp.getBody() != null ? resp.getBody().size() : "0") + " reseñas");
    }

    // ============================================
    // 🚚 RUTAS
    // ============================================

    @Test @Order(60) @DisplayName("IT18 - Rutas disponibles")
    void rutasDisponibles() {
        ResponseEntity<Map> resp = get("/api/rutas/disponibles", campesinoCookie);

        assertEquals(200, resp.getStatusCodeValue());
        assertTrue(((Boolean) resp.getBody().get("success")));
        System.out.println("✅ IT18: Rutas disponibles consultadas — " + resp.getBody().get("rutas"));
    }

    // ============================================
    // 🗺️ DIRECCIONES
    // ============================================

    @Test @Order(70) @DisplayName("IT19 - Direcciones guardadas")
    void direcciones() {
        ResponseEntity<List> resp = getList("/api/direcciones", clienteCookie);

        assertEquals(200, resp.getStatusCodeValue());
        System.out.println("✅ IT19: Direcciones consultadas");
    }

    // ============================================
    // 📱 PERFIL
    // ============================================

    @Test @Order(80) @DisplayName("IT20 - Perfil campesino público")
    void perfilCampesino() {
        ResponseEntity<Map> resp = get("/api/campesino/2/perfil", null);

        assertEquals(200, resp.getStatusCodeValue());
        assertTrue(resp.getBody().containsKey("nombreCompleto"));
        System.out.println("✅ IT20: Perfil campesino → " + resp.getBody().get("nombreCompleto"));
    }

    // ============================================
    // MÉTODOS AUXILIARES
    // ============================================

    private ResponseEntity<Map> get(String path, String cookie) {
        return rest.exchange(BASE_URL + path, HttpMethod.GET, buildRequest(cookie), Map.class);
    }

    private ResponseEntity<List> getList(String path, String cookie) {
        return rest.exchange(BASE_URL + path, HttpMethod.GET, buildRequest(cookie), List.class);
    }

    private ResponseEntity<Map> post(String path, Object body, String cookie) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (cookie != null) headers.add("Cookie", cookie);
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        return rest.exchange(BASE_URL + path, HttpMethod.POST, entity, Map.class);
    }

    private HttpEntity<String> buildRequest(String cookie) {
        HttpHeaders headers = new HttpHeaders();
        if (cookie != null) headers.add("Cookie", cookie);
        return new HttpEntity<>(headers);
    }

    private String extractCookie(ResponseEntity<?> resp) {
        List<String> cookies = resp.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (cookies != null && !cookies.isEmpty()) {
            return cookies.get(0).split(";")[0];
        }
        return null;
    }
}
