# 📘 Arquitectura Híbrida: Integración Java y Python en AgroConecta

**AgroConecta** se caracteriza por tener una estructura orientada a microservicios. En lugar de forzar a Java a hacer todo el trabajo, delegamos las labores pesadas de minería de datos, analítica y conexiones externas de logística geoespacial a un servidor rápido en **Python**. Java se encarga de la seguridad, el E-commerce y la persistencia de datos.

A continuación, se detalla a fondo cómo estas dos tecnologías "dialogan" entre ellas:

---

## 1. El Puente de Comunicación (API REST)
La columna vertebral de la comunicación es `PythonService.java` que actúa como nuestro "traductor". Utiliza la librería **RestTemplate** de Spring Boot para hacer peticiones HTTP transparentes y síncronas al servidor de Python (`servidor.py`) corriendo en formato **Flask** en el puerto `5000`.

Todo el tráfico de comunicación entre los servidores se empaca y transfiere de forma veloz usando el estándar universal **JSON** (JavaScript Object Notation).

---

## 2. Motor de Precios Reales (SIPSA - DANE)
* **¿Qué es?** Sistema de Información de Precios del Sector Agropecuario (SIPSA), administrado por el DANE.
* **Fuente Oficial de Datos (Para sustentar tu proyecto):** 
  * Portal oficial: Portal de Datos Abiertos de Colombia.
  * Algoritmo Socrata / URL Exacta del Endpoint JSON usado por nuestro sistema: [`https://www.datos.gov.co/resource/ch4u-f3i5.json`](https://www.datos.gov.co/resource/ch4u-f3i5.json). Esta URL es el punto de enlace de datos agrónomos históricos recopilados gubernamentalmente.
* **Flujo Práctico:**
  1. El Backend de Python (`servidor.py`) hace una petición `GET` a la API oficial de datos abiertos del gobierno colombiano mencionada en el link.
  2. Al recibir múltiples filas de datos puros y desordenados, Python "filtra" iterativamente usando un diccionario de interés (Ej: "Papa Sabanera", "Tomate Chonto") para consolidar un precio promedio limpio por categoría.
  3. Python también es capaz de calcular **tendencias** automáticas (Alta / Baja / Estable) con un margen de +/- 4% comparado a un precio base duro.
  4. Desde Java (`CampesinoController`), se invoca al servicio `obtenerPreciosDesdePython()` y Spring Boot inyecta esos datos procesados en la vista (Thymeleaf) para que el campesino reciba recomendaciones dinámicas del mercado gubernamental a la hora de publicar productos nuevos.

---

## 3. Analítica y Gráficos Visuales (ApexCharts vs Matplotlib)
Anteriormente existían gráficas estáticas e inflexibles; ahora el sistema transcurre a gráficas fluidas interactuando entre ambas plataformas.

* **Flujo Práctico:**
  1. Cuando el Campesino (o Admin) examina un reporte, Java realiza el conteo matemático grueso conectándose a MongoDB/MySQL.
  2. Java empaca la contabilidad (Ventas totales, Órdenes, Cantidades) en un robusto `Map` de llave-valor y envía un `POST` a Python (Ej: endpoint `/api/v1/informe-campesino`).
  3. **El Motor Analítico de Python:** Toma toda esa data cruda, evalúa los top performers (productos más vendidos, ingresos más altos del mes), confronta precios contra la estadística nacional (SIPSA) y estructura una plantilla de JSON que `ApexCharts` sea capaz de entender.
  4. Los datos modelados (ahora segmentados como `labels` y `series`) regresan por HTTP a Java.
  5. Java los introduce inteligentemente en el Frontend (`<script th:inline="javascript">`). ApexCharts atrapa ese JSON y lo dibuja interactivamente en la pantalla como gráficas de barras, anillos (donuts) o áreas sombreadas de alta definición.

---

## 4. Ecosistema de Mapas e Inteligencia Geoespacial
El manejo geográfico abarca dos capas bien diferenciadas: Pura matemática rápida en Java, y trazado logístico experto en Python.

### A. Buscador Reactivo "A X km de Ti" (Lógica en Java)
* Para que la **Tienda Cliente** organice cientos de productos dictando qué tan cerca (km) te quedan sin consumir cuotas de API pagas, usamos mecánica pura.
* En `DashboardController.java`, la función inyectada usa la famosa **Fórmula de Haversine**, aplicando trigonometría esférica combinada con el radio de la tierra (6371 Km) sobre las latitudes/longitudes para sacar una línea recta precisa del campesino a tu pantalla en milisegundos.

### B. Rutas de Conducción Logísticas (Lógica en Python)
* El campesino requiere visualizar de verdad **qué vías debe tomar** en un entorno geográfico y cuánto se demora.
* Java extrae de su base de datos las coordenadas GPS preestablecidas por el cultivo (Origen) y la dirección digitada por el cliente final (Destino), logrando una petición `POST` al Middleware.
* **El Servidor Python entra a la acción:** Su ruta `/api/v1/logistica-rutas` intercepta las coordenadas, las formatea, contacta en nanosegundos al proyecto web abierto **OSRM** (_Open Source Routing Machine_).
* OSRM computa la red de calles completa y le devuelve a Python las coordenadas poligonales. Python extrae sólo la geometría crítica y retorna a Java un objeto estandarizado. 
* El frontend, cargado con `Leaflet.js`, pinta el trazado azul sobre un mapa de calles como si fuera un Google Maps nativo.

---

> **Resumen de Filosófía Técnica:**
> Python se comporta como un "Cerebro Algorítmico". Procesa rutas, desglosa bases de datos públicas para referenciar precios, y organiza las estructuras estadísticas. Por el otro carril, Java es el "Guardián Sólido": controla que las compras funcionen, administra cada milímetro de la base de datos segura y presenta la información de manera elegante y limpia al usuario sin sobrecargarse de transformaciones matemáticas pesadas.
