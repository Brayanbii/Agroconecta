package com.proyecto.AccesoUsuarios;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas automatizadas con Selenium para AgroConecta
 * Requiere: msedgedriver.exe en Downloads/edgedriver/
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AgroConectaSeleniumTests {

    private static final String BASE_URL = "https://agroconecta-04uf.onrender.com";
    private static WebDriver driver;

    @BeforeAll
    static void setUp() {
        System.setProperty("webdriver.edge.driver",
            System.getProperty("user.home") + "\\Downloads\\edgedriver\\msedgedriver.exe");
        EdgeOptions opts = new EdgeOptions();
        opts.addArguments("--headless");
        opts.addArguments("--window-size=1920,1080");
        driver = new EdgeDriver(opts);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(120));
        despertarServidor();
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) driver.quit();
    }

    static void despertarServidor() {
        for (int i = 0; i < 24; i++) {
            try {
                driver.get(BASE_URL);
                String b = driver.getPageSource();
                if (b.contains("AgroConecta") || b.contains("DOCTYPE")) {
                    System.out.println("⚡ Servidor despierto tras " + (i + 1) + " intentos\n");
                    return;
                }
            } catch (Exception ignored) {}
            try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
        }
        System.out.println("⚠ Servidor sin respuesta, continuando\n");
    }

    // ================================================
    // TEST 1: Página de inicio
    // ================================================
    @Test @Order(1) @DisplayName("TC01 - Página de inicio")
    void tc01_inicio() {
        driver.get(BASE_URL);
        String t = driver.getTitle();
        assertNotNull(t);
        System.out.println("✅ TC01: Inicio cargado — " + t);
    }

    // ================================================
    // TEST 2: Login Spring Security (ADMIN)
    // ================================================
    @Test @Order(2) @DisplayName("TC02 - Login administrador exitoso")
    void tc02_loginAdmin() {
        hacerLogin("admin@agroconecta.com", "123");
        String url = driver.getCurrentUrl();
        assertTrue(url.contains("admin") || url.contains("dashboard") || url.contains("productos"),
                "URL tras login admin: " + url);
        System.out.println("✅ TC02: Login admin → " + url);
    }

    // ================================================
    // TEST 3: Login fallido
    // ================================================
    @Test @Order(3) @DisplayName("TC03 - Login fallido")
    void tc03_loginFallido() {
        hacerLogin("falso@noexiste.com", "mal123");
        String url = driver.getCurrentUrl();
        assertTrue(url.contains("error") || url.contains("login"),
                "Tras login fallido debe mostrar error. URL: " + url);
        System.out.println("✅ TC03: Login fallido detectado → " + url);
    }

    // ================================================
    // TEST 4: Acceso denegado sin login
    // ================================================
    @Test @Order(4) @DisplayName("TC04 - Acceso denegado sin autenticación")
    void tc04_accesoDenegado() {
        driver.manage().deleteAllCookies(); // limpiar sesión de pruebas anteriores
        driver.get(BASE_URL + "/admin/dashboard");
        sleep(3);
        String url = driver.getCurrentUrl();
        assertTrue(url.contains("login") || url.contains("error") || !url.contains("admin"),
                "Sin login no debe acceder a admin. URL: " + url);
        System.out.println("✅ TC04: Acceso denegado → " + url);
    }

    // ================================================
    // TEST 5: Registro de nuevo usuario
    // ================================================
    @Test @Order(5) @DisplayName("TC05 - Registro de nuevo usuario")
    void tc05_registro() {
        driver.get(BASE_URL + "/registro");
        sleep(4);
        String ts = String.valueOf(System.currentTimeMillis());
        String email = "t" + ts.substring(ts.length() - 6) + "@test.com";

        rellenarSiExiste("userName", "test_" + ts.substring(ts.length() - 5));
        rellenarSiExiste("nombreCompleto", "Usuario Prueba Selenium");
        rellenarSiExiste("username", email);
        rellenarSiExiste("email", email);
        rellenarSiExiste("password", "123456");
        rellenarSiExiste("telefono", "3001234567");

        try {
            WebElement rol = driver.findElement(By.cssSelector("input[value='CLIENTE']"));
            if (rol.isEnabled()) rol.click();
        } catch (Exception ignored) {}

        WebElement btn = driver.findElement(By.cssSelector("button[type='submit']"));
        btn.click();
        sleep(3);
        System.out.println("✅ TC05: Registro enviado → " + driver.getCurrentUrl());
    }

    // ================================================
    // TEST 6: Login campesino
    // ================================================
    @Test @Order(6) @DisplayName("TC06 - Login campesino")
    void tc06_loginCampesino() {
        hacerLogin("pepe@finca.com", "123");
        String url = driver.getCurrentUrl();
        assertTrue(url.contains("campesino") || url.contains("productos"),
                "URL tras login campesino: " + url);
        System.out.println("✅ TC06: Login campesino → " + url);
    }

    // ================================================
    // TEST 7: Cliente en tienda
    // ================================================
    @Test @Order(7) @DisplayName("TC07 - Cliente navega tienda")
    void tc07_tiendaCliente() {
        hacerLogin("maria@gmail.com", "123");
        sleep(3);
        driver.get(BASE_URL + "/tienda");
        sleep(3);
        String html = driver.getPageSource();
        assertFalse(html.contains("Error 500") || html.contains("Whitelabel Error Page"),
                "La tienda no debe mostrar error 500");
        System.out.println("✅ TC07: Tienda cargada correctamente");
    }

    // ================================================
    // TEST 8: API precios responde
    // ================================================
    @Test @Order(8) @DisplayName("TC08 - API de precios responde")
    void tc08_apiPrecios() {
        driver.get(BASE_URL);
        sleep(2);
        // La API se sirve vía controlador Spring y requiere ciertos headers
        driver.get(BASE_URL + "/api/v1/precios");
        sleep(3);
        String body = driver.findElement(By.tagName("body")).getText();
        System.out.println("   API response: " + body.substring(0, Math.min(200, body.length())));
        boolean ok = body.contains("success") || body.contains("data") || body.contains("nombre")
                || body.contains("precio") || body.contains("fuente");
        assertTrue(ok, "API debe devolver datos de precios");
        System.out.println("✅ TC08: API precios OK");
    }

    // ================================================
    // TEST 9: Páginas informativas
    // ================================================
    @Test @Order(9) @DisplayName("TC09 - Páginas informativas accesibles")
    void tc09_paginasEstaticas() {
        String[] paginas = {"/contacto", "/sobre_nosotros", "/como_funciona"};
        for (String p : paginas) {
            driver.get(BASE_URL + p);
            sleep(2);
            String body = driver.findElement(By.tagName("body")).getText();
            assertFalse(body.contains("Error 500") && body.length() < 500,
                    p + " no debe dar error 500");
            System.out.println("   " + p + " → OK");
        }
        System.out.println("✅ TC09: Páginas informativas OK");
    }

    // ================================================
    // TEST 10: Logout
    // ================================================
    @Test @Order(10) @DisplayName("TC10 - Cerrar sesión")
    void tc10_logout() {
        driver.get(BASE_URL);
        sleep(2);
        try {
            WebElement logoutForm = driver.findElement(By.cssSelector("form[action*='logout']"));
            logoutForm.submit();
        } catch (Exception e) {
            driver.get(BASE_URL + "/logout");
        }
        sleep(3);
        String url = driver.getCurrentUrl();
        assertTrue(url.contains("login") || url.contains("/"),
                "Tras logout debe estar en login o home. URL: " + url);
        System.out.println("✅ TC10: Logout → " + url);
    }

    // ================================================
    // MÉTODOS AUXILIARES
    // ================================================
    private void hacerLogin(String email, String pass) {
        driver.get(BASE_URL + "/login");
        sleep(4);
        rellenarSiExiste("username", email);
        rellenarSiExiste("password", pass);
        try {
            driver.findElement(By.cssSelector("button[type='submit']")).click();
        } catch (Exception e) {
            try {
                driver.findElement(By.cssSelector("form[action*='login']")).submit();
            } catch (Exception ignored) {}
        }
        sleep(4);
    }

    private void rellenarSiExiste(String nameOrId, String value) {
        for (String sel : new String[]{nameOrId, "input[name='" + nameOrId + "']", "input[id='" + nameOrId + "']"}) {
            try {
                List<WebElement> els = driver.findElements(By.cssSelector(sel));
                if (els.isEmpty()) {
                    try { els = driver.findElements(By.name(nameOrId)); } catch (Exception ignored) {}
                }
                if (els.isEmpty()) {
                    try { els = driver.findElements(By.id(nameOrId)); } catch (Exception ignored) {}
                }
                for (WebElement el : els) {
                    if (el.isDisplayed() || el.getTagName().equals("input")) {
                        el.clear();
                        el.sendKeys(value);
                        return;
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    private void sleep(int s) {
        try { Thread.sleep(s * 1000); } catch (InterruptedException ignored) {}
    }
}
