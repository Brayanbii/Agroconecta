# Plan de Implementación: Sistema Integral de Perfil de Usuario ("AgroConecta Profile")

## Descripción del Problema y Visión
Has propuesto una evolución masiva. La falta actual de un panel donde los usuarios puedan gestionar de verdad su cuenta limita la confianza y credibilidad de la plataforma. Tu visión de implementar un sistema similar al "Account Layout" de Rappi es extremadamente acertada para dotar a la plataforma de "Startup-Grade Power". 

Agregaremos capacidades para gestionar la **Información del Perfil** (foto, DNI, género), manejar **AgroCréditos**, visualizar **Métodos de Pago**, y **Últimas Órdenes**, unificado y estructurado en una nueva vista espectacular que detectará mágicamente si es "Campesino" (enviándolo también a su dashboard de ventas) o si es "Cliente" normal.

## ⚠️ User Review Required
> [!IMPORTANT]
> Este plan afectará la Base de Datos (`Usuario` model). Asegúrate de validar que los atributos adicionales que agregaremos son los correctos antes de que construya la estructura.

## Cambios Propuestos en la Arquitectura

### 1. Extensión del Modelo (Base de Datos)
El modelo actual (`Usuario.java`) es muy modesto. Necesita un rediseño de atributos para soportar la lógica real:
#### [MODIFY] `Usuario.java`
- `fotoPerfil` (String, para URL o Path).
- `numeroIdentidad` (String, para cédula).
- `fechaNacimiento` (LocalDate).
- `genero` (String: Hombre, Mujer, Otro).
- `creditos` (BigDecimal: "AgroCréditos").

### 2. Controlador de Perfil Centralizado
Crearemos un nuevo controlador dedicado a interceptar todas las peticiones a la ruta estelar `/perfil`.
#### [NEW] `PerfilController.java`
Manejará las subrutas inmersivas:
- `@GetMapping("/perfil/ajustes")`
- `@GetMapping("/perfil/creditos")`
- `@GetMapping("/perfil/pagos")`
- `@GetMapping("/perfil/notificaciones")`
- `@GetMapping("/perfil/ordenes")`
- `@PostMapping("/perfil/actualizar")` (Para guardar foto y DNI por el momento).

### 3. Vistas de Perfil y Layout (Thymeleaf + Tailwind)
#### [NEW] `perfil.html`
Replica exacta de la estructura Rappi en la foto pero tematizada para AgroConecta.
- **Sidebar de Navegación Lateral (Izquierda):** Con la Foto de perfil, Nombre del usuario y las pestañas exactas (Ajustes de cuenta, Créditos, Pagos, Centro de notificaciones, Últimas órdenes, y para los Campesinos, el link a "Mi Cosecha / Ventas").
- **Sección de Contenido (Derecha):** Dinámica. Mostrando formularios limpios (Account Settings), tarjetas verdes (AgroCréditos) con un historial falso para simular regalos, y la UI Beta de "Métodos de Pago" con el diseño de tarjeta de Crédito (Visa/Efectivo) para que quede listo en el futuro.

### 4. Modificación de Acoples Existentes
#### [MODIFY] `tienda.html` y barra de navegación (`Header`)
- En el Navbar actual, conectaremos el "Hola, Brayan" o "User icon" directamente hacia el nuevo `/perfil/ajustes`.

---

## Open Questions
> [!WARNING]
> ¿Estás de acuerdo con añadir los atributos de DNI, fecha de nacimiento, género y créditos a la base de datos? ¿O quisieras modificar algo de los datos solicitados en el diseño de "Información de cuenta"?

> [!CAUTION]
> Para la subida de foto de perfil, en esta beta lo manejaremos permitiendo al usuario pegar un Link Público (URL estilo gravatar/unsplash) para simplificar la funcionalidad, ¿O necesitas estrictamente que sea subir archivo a servidor mediante `multipart`?

## Plan de Verificación
- Compilaremos Spring Boot.
- Accederemos a `/perfil/ajustes` autenticados como usuario real.
- Validaremos que el layout coincida estructuralmente con la meta tipo Rappi (sidebar vs grid profile).
- Evaluaremos que al cambiar de pestaña a "Créditos" el diseño inmersivo cambie a la zona de Wallet.
