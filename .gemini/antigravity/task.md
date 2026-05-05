# Perfil de Usuario Sistema Integral (Startup-Style)

- `[x]` 1. **Extensión del Modelo Base DB**: Modificar `Usuario.java` agregando `fotoPerfil` (url), `numeroIdentidad`, `fechaNacimiento`, `genero`, y `creditos`. 
- `[x]` 2. **Creación del Controlador de Perfil**: Programar `PerfilController.java` para gestionar las rutas (`/perfil/ajustes`, `/perfil/creditos`, `/perfil/pagos`, y rutas POST de actualización).
- `[x]` 3. **Diseño de Vista Principal (`perfil.html`)**: Crear la maqueta con Tailwind replicando la estructura del Layout Account (Sidebar izquierdo y Contenido dinámico a la derecha) acorde al diseño corporativo.
- `[x]` 4. **Enlace desde Navbar Global**: Modificar los headers de las páginas principales (`tienda.html` y posiblemente el dashboard principal) para que el ícono/botón de perfil realmente envíe a esta nueva arquitectura.
- `[x]` 5. **Pruebas y Verificación**: Validar con el cliente la interacción con la BDD y la nueva vista.
