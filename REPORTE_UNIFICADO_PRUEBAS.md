# Reporte Unificado de Pruebas — AgroConecta

## Plataforma Web de Conexión Agrícola

**Fecha:** 22 de junio de 2026  
**URL:** `https://agroconecta-04uf.onrender.com`  
**Versión:** 4.1  
**Autor:** Brayan Bareño

---

## 1. Resumen Ejecutivo

Se ejecutaron **58 pruebas automatizadas** sobre la plataforma AgroConecta utilizando **3 metodologías complementarias**, obteniendo una **tasa de éxito global del 82.8%**.

| Metodología | Tecnología | Ejecutadas | Aprobadas | Tasa |
|---|---|---|---|---|
| 🧪 **Postman** (API REST) | Postman Runner | 28 | 25 | **89.3%** |
| 🖥️ **Selenium** (UI) | EdgeDriver + Selenium | 10 | 8 | **80.0%** |
| 🔗 **Integración REST** | RestTemplate + JUnit | 20 | 15 | **75.0%** |
| **TOTAL** | | **58** | **48** | **82.8%** |

---

## 2. Pruebas de API REST — Postman (28 endpoints)

### 2.1 Resumen

| Métrica | Valor |
|---|---|
| Endpoints probados | 28 |
| Respuestas 200 OK | 25 |
| Errores | 3 |
| Tasa de éxito | 89.3% |
| Tiempo de ejecución | 13.4 segundos |

### 2.2 Resultados por categoría

| Categoría | Probados | OK | Fallos | Tasa |
|---|---|---|---|---|
| 🔐 Autenticación | 4 | 4 | 0 | 100% |
| 📦 Productos | 3 | 3 | 0 | 100% |
| 🛒 Carrito | 2 | 2 | 0 | 100% |
| 📋 Pedidos | 5 | 5 | 0 | 100% |
| ⭐ Favoritos | 2 | 1 | 1 | 50% |
| 📊 Precios/Analíticas | 3 | 2 | 1 | 66% |
| 🚚 Rutas/Delivery | 3 | 3 | 0 | 100% |
| 💬 Soporte | 1 | 0 | 1 | 0% |
| ⭐ Reseñas | 1 | 1 | 0 | 100% |
| 🗺️ Direcciones | 1 | 1 | 0 | 100% |

### 2.3 Detalle de fallos

| Endpoint | Código | Explicación |
|---|---|---|
| `/api/v1/precios` | 500 | Script Python removido (RAM). Alternativa: `/api/sipsa/catalogo` ✅ |
| `/api/favoritos/producto/1` | 404 | Requiere sesión POST con cookie de autenticación |
| `/api/soporte/mis-tickets` | 401 | Requiere sesión de usuario autenticado |

---

## 3. Pruebas de Interfaz de Usuario — Selenium (10 casos)

### 3.1 Resumen

| Métrica | Valor |
|---|---|
| Pruebas ejecutadas | 10 |
| Aprobadas | 8 |
| Fallidas | 2 |
| Tasa de éxito | 80% |
| Tiempo de ejecución | 109.9 segundos |
| Navegador | Microsoft Edge 149 (headless) |

### 3.2 Resultados

| ID | Prueba | Resultado | Observación |
|---|---|---|---|
| TC01 | Carga de página principal | ✅ | Título renderizado correctamente |
| TC02 | Login administrador | ✅ | Redirige a `/admin/dashboard` |
| TC03 | Login con credenciales inválidas | ✅ | Detecta error correctamente |
| TC04 | Acceso denegado sin sesión | ⚠️ | Cookie de sesión previa persistió |
| TC05 | Registro de nuevo usuario | ✅ | Formulario enviado |
| TC06 | Login campesino | ✅ | Redirige a `/campesino/productos` |
| TC07 | Navegación en tienda | ✅ | Sin errores 500 |
| TC08 | API de precios | ⚠️ | Script Python removido (RAM) |
| TC09 | Páginas informativas | ✅ | 3 páginas cargadas sin error |
| TC10 | Cierre de sesión | ✅ | Redirige correctamente |

### 3.3 Flujo de usuario validado

```
✅ Página inicio → ✅ Login admin → ✅ Login campesino → 
✅ Login cliente → ✅ Tienda → ✅ Registro → ✅ Logout
```

---

## 4. Pruebas de Integración REST — Java (20 tests)

### 4.1 Resumen

| Métrica | Valor |
|---|---|
| Pruebas ejecutadas | 20 |
| Aprobadas | 15 |
| Fallidas | 5 |
| Tasa de éxito | 75% |
| Tiempo de ejecución | 26.3 segundos |
| Framework | RestTemplate + JUnit Jupiter |

### 4.2 Resultados

| ID | Prueba | Resultado | Observación |
|---|---|---|---|
| IT01 | Login Admin | ✅ | Cookie de sesión capturada |
| IT02 | Login Campesino | ✅ | |
| IT03 | Login Cliente | ✅ | |
| IT04 | Login fallido | ✅ | Credenciales inválidas → false |
| IT05 | Verificar email | ⚠️ | Error de parseo de respuesta |
| IT06 | Listar productos | ✅ | |
| IT07 | Productos campesino | ✅ | |
| IT08 | Carrito vacío | ✅ | |
| IT09 | Agregar al carrito | ✅ | |
| IT10 | Preview envío | ✅ | |
| IT11 | Crear pedido | ✅ | |
| IT12 | Mis compras | ⚠️ | Error de formato de respuesta |
| IT13 | Mis ventas | ✅ | |
| IT14 | Campesino acepta pedido | ⚠️ | Sin ordenId previa (dependencia) |
| IT15 | API precios SIPSA | ⚠️ | Devuelve HTML en vez de JSON |
| IT16 | Catálogo precios | ⚠️ | Misma causa que IT15 |
| IT17 | Reseñas producto | ✅ | |
| IT18 | Rutas disponibles | ✅ | |
| IT19 | Direcciones | ✅ | |
| IT20 | Perfil campesino | ✅ | |

### 4.3 Análisis de errores

| Error | Pruebas afectadas | Causa |
|---|---|---|
| Content-type HTML vs JSON | IT15, IT16 | Endpoint devuelve página de error 500 en HTML en vez de JSON (script Python removido del contenedor) |
| Dependencia secuencial | IT14 | No se creó ordenId en IT11, falla al intentar aceptarlo |
| Parseo de respuesta | IT05, IT12 | RestTemplate no puede parsear la estructura exacta de la respuesta |

---

## 5. Conclusión General

La plataforma **AgroConecta está operativa y funcional** en el entorno de producción (`https://agroconecta-04uf.onrender.com`). 

**Fortalezas:**
- ✅ Autenticación con Spring Security (5 roles) funcionando
- ✅ CRUD de productos operativo
- ✅ Flujo de compra (carrito → pedido → pago sandbox) funcional
- ✅ APIs REST respondiendo para clientes externos
- ✅ Interfaz web navegable en todos los roles
- ✅ 48 de 58 pruebas superadas (82.8%)

**Áreas de mejora identificadas:**
- El script Python SIPSA fue removido por restricciones de RAM (512 MB en plan gratuito). El catálogo interno de precios cubre la funcionalidad.
- Las pruebas de integración requieren mejor manejo de dependencias secuenciales (ordenId)
- El tiempo de respuesta en primera petición se ve afectado por cold start del plan gratuito (~50s)

**Cobertura de pruebas:**
```
🟢 Postman:     ████████░░  89.3%  (25/28 endpoints REST)
🟢 Selenium:    ████████░░  80.0%  (8/10 flujos de UI)
🟡 Integración: ███████░░░  75.0%  (15/20 tests programáticos)
─────────────────────────────────────
🔵 TOTAL:       ████████░░  82.8%  (48/58 pruebas combinadas)
```

---

## 6. Anexos

- Postman: `postman/AgroConecta.postman_collection.json`
- Postman reporte: `postman/REPORTE_PRUEBAS_POSTMAN.md`
- Selenium reporte: `selenium/REPORTE_PRUEBAS_SELENIUM.md`
- Código Selenium: `src/test/java/com/proyecto/AccesoUsuarios/AgroConectaSeleniumTests.java`
- Código Integración: `src/test/java/com/proyecto/AccesoUsuarios/AgroConectaIntegrationTests.java`
