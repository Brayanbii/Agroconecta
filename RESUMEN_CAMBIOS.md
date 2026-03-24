# 📋 Resumen de Cambios Implementados — AgroConecta

Este documento resume todos los cambios aplicados en el proyecto **AccesoUsuarios / AgroConecta** (plan 10/10 completado).

---

## ✅ Estado general

- **Plan completado:** 10 de 10 pasos
- **Archivo de seguimiento:** `TODO.md` actualizado con todos los pasos en `[x]`
- **Cobertura de cambios:** Seguridad, validaciones, CSRF, roles, flujo de carrito/orden, manejo de errores, subida de imágenes y buscador en tienda.

---

## 1) Inyectar `PasswordEncoder` (sin instanciación manual)

### Archivos modificados
- `src/main/java/com/proyecto/AccesoUsuarios/controller/UsuarioController.java`
- `src/main/java/com/proyecto/AccesoUsuarios/controller/AdminUserController.java`
- `src/main/java/com/proyecto/AccesoUsuarios/config/DataInitializer.java`

### Qué se hizo
- Se reemplazó el uso manual de encoder (`new BCryptPasswordEncoder()`) por inyección de dependencia de `PasswordEncoder`.
- Se unificó la forma de encriptar contraseñas con el bean de Spring Security.

### Beneficio
- Código más limpio, consistente y alineado con buenas prácticas de Spring.

---

## 2) Activar validaciones con `@Valid` y mostrar errores

### Archivos modificados
- `src/main/java/com/proyecto/AccesoUsuarios/controller/UsuarioController.java`
- `src/main/java/com/proyecto/AccesoUsuarios/controller/AdminUserController.java`
- `src/main/resources/templates/registro.html`
- `src/main/resources/templates/admin_usuario_form.html`

### Qué se hizo
- Se agregó `@Valid` y `BindingResult` en métodos de guardado.
- Se implementó manejo de errores de validación en backend.
- Se añadieron bloques en frontend (Thymeleaf) para mostrar mensajes de error al usuario.
- En edición de usuario admin, se mantuvo lógica para no forzar cambio de password al editar.

### Beneficio
- Evita guardar datos inválidos.
- Mejora UX con retroalimentación clara al usuario.

---

## 3) Proteger endpoints de carrito y orden por rol

### Archivo modificado
- `src/main/java/com/proyecto/AccesoUsuarios/security/SecurityConfig.java`

### Qué se hizo
- Se agregaron rutas:
  - `/carrito/**`
  - `/orden/**`
  junto con `/tienda/**` bajo `.hasRole("CLIENTE")`.

### Beneficio
- Solo clientes pueden usar flujo de compra.
- Mejor control de autorización por rol.

---

## 4) Habilitar CSRF correctamente

### Archivos modificados
- `src/main/java/com/proyecto/AccesoUsuarios/security/SecurityConfig.java`
- `src/main/resources/templates/admin_pedidos.html`
- `src/main/resources/templates/admin_usuarios.html`

### Qué se hizo
- Se eliminó `.csrf(csrf -> csrf.disable())` para dejar CSRF activo.
- Se reemplazaron acciones sensibles tipo `<a href="/logout">` por `<form method="post">` compatibles con CSRF.

### Beneficio
- Protección real contra ataques CSRF.
- Cumplimiento de prácticas de seguridad web.

---

## 5) Cambiar eliminaciones de GET a POST

### Archivos modificados (backend)
- `src/main/java/com/proyecto/AccesoUsuarios/controller/AdminUserController.java`
- `src/main/java/com/proyecto/AccesoUsuarios/controller/UsuarioController.java`
- `src/main/java/com/proyecto/AccesoUsuarios/controller/CampesinoController.java`
- `src/main/java/com/proyecto/AccesoUsuarios/controller/CarritoController.java`

### Archivos modificados (frontend)
- `src/main/resources/templates/admin_usuarios.html`
- `src/main/resources/templates/usuarios.html`
- `src/main/resources/templates/mis_productos.html`
- `src/main/resources/templates/carrito.html`

### Qué se hizo
- Endpoints de eliminación pasaron de `@GetMapping` a `@PostMapping`.
- En vistas, se cambió `<a href=".../eliminar/...">` por `<form method="post">`.
- Se añadieron confirmaciones donde aplicaba.

### Beneficio
- Evita borrados por navegación accidental.
- Más seguro y correcto semánticamente.

---

## 6) Filtrar productos del campesino (solo los suyos)

### Archivos modificados
- `src/main/java/com/proyecto/AccesoUsuarios/repository/ProductoRepository.java`
- `src/main/java/com/proyecto/AccesoUsuarios/controller/DashboardController.java`
- `src/main/java/com/proyecto/AccesoUsuarios/controller/CampesinoController.java`

### Qué se hizo
- Se agregó método:
  - `findByUsuario(Usuario usuario)`
- `dashboardCampesino()` ahora carga productos del usuario autenticado, no `findAll()`.
- Se agregó validación de propiedad en editar/eliminar producto (el producto debe pertenecer al campesino logueado).

### Beneficio
- Aislamiento de datos por usuario campesino.
- Cierra huecos de autorización horizontal.

---

## 7) Validar stock antes de comprar

### Archivos modificados
- `src/main/java/com/proyecto/AccesoUsuarios/controller/OrdenController.java`
- `src/main/java/com/proyecto/AccesoUsuarios/service/CarritoService.java`
- `src/main/resources/templates/carrito.html`

### Qué se hizo
- En `OrdenController.pagarOrden()`:
  - Se valida stock de cada producto antes de confirmar compra.
  - Si no hay stock suficiente, redirige a carrito con parámetros de error.
- En `CarritoService`:
  - `agregarProducto()` limita cantidad al stock disponible.
  - `actualizarCantidad()` también limita con stock actualizado desde BD.
- En `carrito.html`:
  - Se añadieron alertas visuales para:
    - `stock_insuficiente`
    - `producto_no_existe`

### Beneficio
- Evita stock negativo y ventas inválidas.
- Flujo de compra más robusto y claro para el cliente.

---

## 8) Corregir doble slash en `UploadFileService`

### Archivo modificado
- `src/main/java/com/proyecto/AccesoUsuarios/service/UploadFileService.java`

### Qué se hizo
- Se corrigió ruta:
  - `"images//"` ➜ `"images/"`
- `folder` pasó a `final`.
- `deleteImage()` ahora usa `Files.deleteIfExists()` con `Path`.
- `saveImage()` crea directorio si no existe con `Files.createDirectories()`.

### Beneficio
- Manejo de rutas consistente.
- Menos errores de filesystem en guardado/borrado.

---

## 9) Manejo global de errores + vista personalizada

### Archivos creados
- `src/main/java/com/proyecto/AccesoUsuarios/config/GlobalExceptionHandler.java`
- `src/main/resources/templates/error.html`

### Qué se hizo
- Se creó `@ControllerAdvice` global con handlers para:
  - `NoSuchElementException` ➜ 404
  - `NoHandlerFoundException` ➜ 404
  - `AccessDeniedException` ➜ 403
  - `Exception` ➜ 500
- Se creó vista `error.html` estilizada (Tailwind), con:
  - código
  - título
  - mensaje
  - icono dinámico
  - botones de retorno/inicio

### Beneficio
- Manejo centralizado y elegante de fallos.
- Mejor experiencia de usuario en errores.

---

## 10) Buscador funcional en la tienda

### Archivos modificados
- `src/main/java/com/proyecto/AccesoUsuarios/repository/ProductoRepository.java`
- `src/main/java/com/proyecto/AccesoUsuarios/controller/DashboardController.java`
- `src/main/resources/templates/tienda.html`

### Qué se hizo
- Repositorio:
  - `findByNombreContainingIgnoreCase(String nombre)`
- Controller `/tienda`:
  - recibe `@RequestParam("buscar")` opcional
  - filtra si hay texto; si no, muestra todos
  - expone variable `busqueda` a la vista
- `tienda.html`:
  - buscador convertido a `<form method="get">`
  - input conectado a parámetro `buscar`
  - botón para limpiar búsqueda (X) cuando hay filtro activo

### Beneficio
- Búsqueda real por nombre de producto.
- Navegación más útil para cliente.

---

## 🗂️ Archivos nuevos creados

1. `AccesoUsuarios/TODO.md`
2. `AccesoUsuarios/src/main/java/com/proyecto/AccesoUsuarios/config/GlobalExceptionHandler.java`
3. `AccesoUsuarios/src/main/resources/templates/error.html`
4. `AccesoUsuarios/RESUMEN_CAMBIOS.md` (este documento)

---

## 🧪 Nota de testing

- Se intentó iniciar pruebas completas.
- Hubo bloqueo por entorno (herramienta de browser deshabilitada durante sesión).
- Se inició ejecución del proyecto con Maven para validación de arranque en terminal.
- Recomendado ejecutar validación funcional final manual de flujos críticos:
  - registro/login
  - tienda + buscador
  - carrito + stock
  - compra
  - panel campesino (propiedad de productos)
  - manejo de errores (404/403/500)

---

## 💾 Estado de guardado

Todos los cambios fueron aplicados y guardados en el proyecto.
