# 🚜 PLAN DE MEJORA UX/UI: EXPERIENCIA PARA CAMPESINOS

El objetivo de este documento es detallar las estrategias y mejoras visuales/interactivas para lograr que **AgroConecta sea la plataforma más intuitiva del mercado**, diseñada específicamente para campesinos que quizás tienen poca o nula experiencia con smartphones y tecnología. 

La meta es que el usuario diga: *"¡Wow, esto es muy fácil, no puedo creer que ya estoy vendiendo en internet!"*

A medida que implementemos estas funciones en el código, marcaremos la casilla con una `[x]`.

---

## 1. 🎓 Onboarding Interactivo (La Guía Paso a Paso)
- [ ] **Tour de Bienvenida (Tooltips):** Cuando el campesino entra por primera vez, el fondo se oscurece y una luz resalta los botones importantes uno por uno con mensajes amigables:
  - *"👋 ¡Bienvenido! Toca aquí para ver tus ganancias."*
  - *"➕ Toca este botón verde gigante para publicar tu primer producto."*
- [ ] **Modo "Prueba" o Simulador:** Una opción de "Practicar" donde el campesino pueda crear un producto de prueba sin que nadie lo vea, para que pierda el miedo a equivocarse o borrar cosas.
- [ ] **Mensajes de Celebración:** Animaciones de confeti y mensajes positivos la primera vez que completan una acción ("¡Felicidades, subiste tu primer producto!").

## 2. 📱 Diseño "Anti-Frustración" (Mobile-First Extremo)
- [ ] **Botones Gigantes y Claros:** Olvidar los botones pequeños. Los botones de acción deben ocupar casi todo el ancho de la pantalla para que sea imposible fallar al tocarlos.
- [ ] **Lenguaje Cotidiano (Cero Tecnicismos):** Reemplazar palabras como "Submit", "Upload", "Analytics" o "Dashboard" por "Guardar", "Subir Foto", "Mis Ventas" o "Mi Finca".
- [ ] **Tipografía Grande y Legible:** Un tamaño de letra más grande del habitual y en negrita para asegurar que se pueda leer bien en exteriores y bajo el fuerte sol del campo.
- [ ] **Colores de Alto Contraste:** Uso de fondos blancos con textos oscuros. Botones primarios en verde fuerte, botones de peligro (borrar) en rojo claro, siempre acompañados de iconos intuitivos (ej. 🗑️ para borrar).

## 3. 📸 Subida de Productos Simplificada (Modo Asistente / Wizard)
En vez de mostrar un formulario largo que intimida, lo dividiremos en pantallas simples (una pregunta a la vez):
- [ ] **Paso 1: "¿Qué producto vas a vender hoy?"** (Con iconos grandes o búsqueda predictiva).
- [ ] **Paso 2: "Toma una foto"** (Botón gigante que abra directamente la cámara del celular, en vez de obligarlos a buscar en galerías complicadas).
- [ ] **Paso 3: "¿Cuánto cuesta y cuánto ofreces?"** (Aquí la integración del SIPSA brillará mostrando visualmente: *"El mercado hoy vende la papa a $2.000"*, seguido de botones de `[ + ]` y `[ - ]` para ajustar el precio sin usar el teclado pequeño).
- [ ] **Autocompletado Inteligente:** Si selecciona "Tomate", sugerir automáticamente la descripción ("Tomate fresco recién cosechado") para que solo presione "Aceptar".

## 4. 📊 "Mis Ventas": Claro y Alentador
- [ ] **Métricas Principales Inmensas:** Mostrar "Dinero Ganado Hoy" en el centro de la pantalla con números gigantes y un botón obvio que diga "Ver mis detalles".
- [ ] **Semáforo Visual en Gráficas:** Las gráficas de Python deben ser muy fáciles de entender a primera vista. Verde si las ventas suben, rojo/naranja si bajan, con frases al lado tipo *"Tus ventas están mejorando esta semana"*.
- [ ] **Lista de Tareas Diarias:** Una tarjeta simple arriba que diga: *"Tienes 2 pedidos por entregar hoy"*.

## 5. 🗣️ Soporte, Ayuda y Accesibilidad Integrada
- [ ] **El Botón Salvavidas:** Un botón flotante permanente (ícono de WhatsApp o audífonos) que diga "Ayuda". Al tocarlo, los contacta con alguien de soporte o lanza un mensaje preestablecido por WhatsApp.
- [ ] **Video-Tutoriales en la App:** Pequeños videos de 15 a 30 segundos (estilo TikTok) incrustados en partes clave de la app. Por ejemplo, al ir a editar producto, un video corto que diga *"Aprende a tomar la foto perfecta para vender más"*.
- [ ] **Feedback Táctil:** Hacer que el celular vibre ligeramente cuando tocan un botón de "Guardar" o cuando se realiza una venta con éxito, dando confirmación física de la acción.
- [ ] **(Futuro) Registro por Voz:** Que en un futuro puedan presionar un micrófono y decir *"Quiero vender 10 bultos de cebolla a 50 mil pesos"*, y que la app llene el formulario por ellos.

## 6. 👨‍👩‍👧‍👦 La Familia Granito (Mascotas y Guías de la App)
Para hacer la plataforma muchísimo más amigable y cercana, AgroConecta contará con un equipo de asistencia animado: **La Familia Granito** (unos adorables granitos de café). Ellos aparecerán en distintas pantallas para guiar, felicitar o ayudar al usuario:

- [ ] **Papá Granito (El Campesino):** Representa el trabajo duro. Aparecerá en el panel principal celebrando las ventas ("¡Buen trabajo en la cosecha de hoy!").
- [ ] **Mamá Granito (La Comunidad):** Es la figura protectora y familiar. Dará la bienvenida a la app, aparecerá en la sección de soporte/comunidad y dará tips de bienestar o ayuda general.
- [ ] **Granitín (El Hijo / Soporte Técnico):** Es el pequeño experto en tecnología. Aparecerá cuando haya un error técnico (Ej: Error 404 o si falla el servidor Python) diciendo: *"Uy, algo no funcionó, ¡pero ya lo estoy arreglando!"* y también dará mini-tutoriales sobre cómo usar la app.
- [ ] **Granitilla (La Hija / Guía de Clientes):** Guiará a los **clientes** en la tienda (Frontend comprador). Aparecerá sugiriendo las mejores ofertas, destacando las frutas más frescas y ayudando a los clientes a encontrar lo que necesitan con una sonrisa.

---
**¿Cómo avanzamos?**
Para lograr esto, empezaremos a modificar el Frontend (HTML/Thymeleaf) en la sección de `campesino_producto_form.html` y los menús, agregando las ilustraciones de la **Familia Granito** e integrando librerías de tours interactivos como `Intro.js` para darles vida en pantalla.
