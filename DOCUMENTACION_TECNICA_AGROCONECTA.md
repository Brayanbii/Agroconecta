# Documento Técnico e Implantación del Sistema
# AgroConecta — Plataforma Web de Conexión Agrícola

**Versión:** 4.1  
**Fecha:** Junio 2026  
**Autor:** Brayan Bareño  
**Repositorio:** `https://github.com/Brayanbii/Agroconecta`  
**URL Producción:** `https://agroconecta.farm`

---

## 1. Documento Técnico e Implantación del Sistema

### 1.1. Introducción

AgroConecta es una plataforma web diseñada para conectar directamente a campesinos colombianos con consumidores finales, eliminando intermediarios y garantizando precios justos para ambas partes. El sistema permite a los productores agrícolas publicar sus productos, gestionar pedidos y coordinar entregas, mientras que los clientes pueden descubrir productos frescos del campo, realizar compras y dar seguimiento a sus pedidos desde cualquier navegador.

La plataforma integra datos oficiales del SIPSA (Sistema de Información de Precios del Sector Agropecuario) del DANE para ofrecer precios de referencia del mercado colombiano, utiliza MercadoPago como pasarela de pagos, y está respaldada por una arquitectura en la nube con bases de datos separadas para datos textuales e imágenes, garantizando disponibilidad y escalabilidad.

### 1.2. Propósito del documento

Este documento describe la arquitectura técnica, los componentes de software, las integraciones externas, las tecnologías utilizadas y los procedimientos de implantación del sistema AgroConecta. Está dirigido a desarrolladores, administradores de sistemas y evaluadores del proyecto.

### 1.3. Alcance del proyecto

**Alcance actual (entregable):**

- Plataforma web con panel de administración, dashboard para campesinos y tienda para clientes
- APIs REST documentadas para integración con clientes externos
- Integración con pasarela de pagos MercadoPago (modo sandbox)
- Sistema de geolocalización y cálculo de rutas (OSRM)
- Integración con datos oficiales de precios del DANE/SIPSA
- Generación de reportes y analíticas en tiempo real
- Sistema de verificación KYC para campesinos y repartidores
- Infraestructura desplegada en Render con bases de datos en Aiven y MongoDB Atlas

**Proyección a futuro:**

- Aplicaciones móviles nativas Android para clientes y repartidores (código base existente en el repositorio, en fase de integración con las APIs REST)
- Notificaciones push en tiempo real para dispositivos móviles
- Modo offline con sincronización para zonas rurales sin conectividad

### 1.4. Listado de módulos

| Módulo | Descripción |
|---|---|
| **Autenticación y Roles** | Login, registro, 5 roles (ADMIN, SOPORTE, CAMPESINO, CLIENTE, REPARTIDOR) |
| **Tienda y Productos** | Catálogo, búsqueda, filtros, favoritos, carrito de compras |
| **Gestión de Pedidos** | Ciclo de vida del pedido, estados, seguimiento |
| **Dashboard Administrador** | Analíticas globales, gráficos, gestión de usuarios |
| **Panel Campesino** | CRUD de productos, finanzas, reputación, verificación |
| **Perfil Cliente** | Historial de compras, favoritos, direcciones, reseñas |
| **Soporte Técnico** | Sistema de tickets con mensajería en tiempo real |
| **SIPSA Explorer** | Consulta de precios oficiales del agro colombiano |
| **Verificación de Identidad** | KYC con subida de documentos (cédula, finca, licencia) |
| **APIs REST** | Endpoints JSON para consumo desde clientes externos (app móvil, terceros) |

> **Nota sobre aplicaciones móviles:** El repositorio incluye el código fuente de dos aplicaciones Android (`AgroConecta/` y `AgroconectaGo/`) desarrolladas en Kotlin con Jetpack Compose como proyección a futuro. Estas apps están diseñadas para consumir las mismas APIs REST del backend y representan la siguiente fase del proyecto, permitiendo que campesinos y repartidores accedan a la plataforma desde dispositivos móviles con funcionalidades como GPS tracking, notificaciones push y modo offline.

### 1.5. Objetivo general

Desarrollar e implantar una plataforma web integral que conecte a campesinos colombianos con consumidores, facilitando la comercialización directa de productos agrícolas mediante herramientas digitales que garanticen transparencia en precios, trazabilidad en entregas y eficiencia operativa, con APIs REST que permitan la futura integración de aplicaciones móviles nativas.

### 1.6. Objetivos específicos

- Permitir a los campesinos publicar y gestionar sus productos desde una interfaz web intuitiva
- Integrar precios de referencia del SIPSA/DANE como apoyo a la fijación de precios
- Implementar un sistema de verificación documental para validar la identidad de vendedores y repartidores
- Procesar pagos de forma segura mediante MercadoPago (modo sandbox para pruebas)
- Exponer APIs REST documentadas que permitan el consumo desde clientes externos como aplicaciones móviles
- Garantizar la persistencia y disponibilidad de la información mediante infraestructura cloud con bases de datos separadas para datos textuales e imágenes
- Ofrecer analíticas en tiempo real para administradores y campesinos mediante gráficos interactivos

---

## 2. Arquitectura de la Solución

### 2.1. Arquitectura general

```
┌──────────────────────────────────────────────────────┐
│                     CLIENTES                         │
│  ┌────────────────────────────────────────────┐     │
│  │        Navegador Web (Thymeleaf + JS)       │     │
│  │        agroconecta.farm                     │     │
│  └────────────────────┬───────────────────────┘     │
│                       │                              │
│  ┌────────────────────┴───────────────────────┐     │
│  │   PROYECCIÓN FUTURA                        │     │
│  │   ┌──────────────┐  ┌────────────────────┐ │     │
│  │   │ AgroConecta  │  │  AgroconectaGo     │ │     │
│  │   │ (Android)    │  │  (Android Delivery) │ │     │
│  │   └──────────────┘  └────────────────────┘ │     │
│  └─────────────────────────────────────────────┘     │
└─────────────────────────┬───────────────────────────┘
                          │
                          ▼
┌──────────────────────────────────────────────────────┐
│                  HTTPS (TLS 1.3)                     │
│               agroconecta.farm:443                   │
└────────────────────────┬─────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────┐
│              RENDER (Cloud Platform)                 │
│  ┌────────────────────────────────────────────────┐  │
│  │        Spring Boot 3.5.7 (Java 17)             │  │
│  │        Docker Container (Alpine Linux)         │  │
│  │        Embedded Tomcat :10000 (HTTP)            │  │
│  │  ┌──────────────────────────────────────────┐  │  │
│  │  │  MVC Controllers  │  REST Controllers    │  │  │
│  │  │  (Thymeleaf Views)│  (JSON APIs)         │  │  │
│  │  ├──────────────────────────────────────────┤  │  │
│  │  │  Spring Security  │  JPA/Hibernate       │  │  │
│  │  │  (5 Roles)        │  MongoDB GridFS      │  │  │
│  │  ├──────────────────────────────────────────┤  │  │
│  │  │  Services: MercadoPago, SIPSA, OSRM,     │  │  │
│  │  │  PDF, Notificaciones, Analíticas         │  │  │
│  │  └──────────────────────────────────────────┘  │  │
│  └────────────────────┬───────────────────────────┘  │
└────────────────────────┼─────────────────────────────┘
                         │
           ┌─────────────┴─────────────┐
           ▼                           ▼
┌─────────────────────┐   ┌──────────────────────────┐
│    AIVEN MySQL       │   │   MONGODB ATLAS           │
│  (Datos textuales)   │   │  (Imágenes vía GridFS)    │
│                      │   │                           │
│  13 tablas           │   │  fs.files / fs.chunks     │
│  DigitalOcean SFO    │   │  AWS us-east-1            │
│  MySQL 8.4.8         │   │  Replica Set (3 nodos)    │
└─────────────────────┘   └──────────────────────────┘
```

### 2.2. Componentes de Software

| Componente | Tecnología | Rol |
|---|---|---|
| **Backend principal** | Spring Boot 3.5.7 | Lógica de negocio, APIs REST, MVC con Thymeleaf |
| **Seguridad** | Spring Security 6 | Autenticación, autorización basada en roles, BCrypt |
| **ORM** | Hibernate 6 / JPA | Mapeo objeto-relacional para MySQL |
| **Templates** | Thymeleaf + Bootstrap | Vistas web renderizadas en servidor |
| **PDFs** | OpenPDF 1.3.30 | Generación de comprobantes de pedido |
| **Script SIPSA** | Python 3 + Zeep (SOAP) | Consumo de web service DANE para precios agrícolas |
| **Contenedor** | Docker + Alpine Linux | Empaquetado y despliegue en Render |

### 2.3. Integraciones externas

| Servicio | Propósito | Endpoint / SDK |
|---|---|---|
| **MercadoPago** | Pasarela de pagos | SDK Java v2.1.27 (sandbox mode) |
| **SIPSA - DANE** | Precios oficiales del agro colombiano | SOAP vía Zeep + API REST datos.gov.co |
| **OSRM** | Cálculo de rutas de conducción | `router.project-osrm.org` (API pública) |
| **Gemini AI** | Generación de descripciones de productos | `generativelanguage.googleapis.com` (Gemini Flash 3.5) |
| **Google Maps** | Navegación externa | Deep link `maps.google.com/maps` |

---

## 3. Tecnologías Utilizadas

### 3.1. Frontend

| Tecnología | Versión | Uso |
|---|---|---|
| **Thymeleaf** | 3.1 (Spring Boot 3.5.7) | Motor de plantillas HTML server-side |
| **Thymeleaf Extras Spring Security** | 6 | Integración de seguridad en vistas |
| **Bootstrap 5** | 5.x (CDN) | Framework CSS responsivo |
| **ApexCharts** | 3.x (CDN) | Gráficos interactivos en dashboards |
| **Font Awesome** | 6.x (CDN) | Iconografía |

**Proyección a futuro — Clientes Móviles:**

El backend expone APIs REST documentadas que pueden ser consumidas desde cualquier cliente HTTP. En el repositorio se incluye el código base de dos aplicaciones Android (Kotlin + Jetpack Compose + Retrofit 2 + OkHttp 4) como punto de partida para la siguiente fase del proyecto. Estas apps heredan la sesión del backend mediante cookies y están diseñadas para replicar la experiencia completa de la plataforma en dispositivos móviles.

### 3.2. Backend

| Tecnología | Versión | Uso |
|---|---|---|
| **Java** | 17 (LTS) | Lenguaje principal del backend |
| **Spring Boot** | 3.5.7 | Framework de aplicación |
| **Spring Security** | 6.x | Autenticación y autorización |
| **Spring Data JPA** | 3.x | Acceso a datos relacionales |
| **Spring Data MongoDB** | 4.5.5 | Acceso a MongoDB GridFS |
| **Hibernate** | 6.6.33 | ORM para MySQL |
| **MySQL Connector/J** | 9.4.0 | Driver JDBC |
| **HikariCP** | 6.3.3 | Pool de conexiones |
| **Maven** | 3.9.11 | Gestor de dependencias y build |
| **Docker** | 27.x | Contenedorización |
| **Alpine Linux** | 3.21 | Sistema operativo del contenedor |
| **Python** | 3.12 | Script SIPSA ETL |
| **Zeep** | 4.3.3 | Cliente SOAP para DANE |
| **OpenPDF** | 1.3.30 | Generación de PDFs |
| **MercadoPago SDK** | 2.1.27 | Integración de pagos |

### 3.3. Patrón de desarrollo

El proyecto sigue el patrón **MVC (Model-View-Controller)** con una arquitectura por capas:

```
Controller (MVC + REST)
    ↓
Service (Lógica de negocio)
    ↓
Repository (Spring Data JPA / MongoDB GridFs)
    ↓
Model (Entidades JPA / POJOs)
```

**Principios aplicados:**
- Separación de responsabilidades: controladores manejan HTTP, servicios manejan lógica
- Inyección de dependencias con Spring `@Autowired`
- Controllers: 23 archivos (MVC web + REST APIs)
- Services: 13 archivos (carrito, envíos, pagos, PDFs, notificaciones, analíticas)
- Repositories: 13 interfaces JPA + MongoDB GridFsOperations
- Models: 13 entidades JPA + ItemCarrito (POJO)

### 3.4. Bases de datos

#### MySQL (Aiven) — Datos textuales

| Parámetro | Valor |
|---|---|
| **Host** | `agroconecta-mysql-brayanebareno1304-47f1.a.aivencloud.com` |
| **Puerto** | `28963` |
| **Base de datos** | `defaultdb` |
| **Motor** | MySQL 8.4.8 |
| **Proveedor** | Aiven.io (Free Tier) |
| **Cloud** | DigitalOcean — San Francisco |
| **SSL** | TLS 1.3 con CA personalizada |
| **Tablas** | 13 tablas principales |

**Esquema de tablas:**

| Tabla | Registros (semilla) | Propósito |
|---|---|---|
| `usuario` | 4 | Usuarios con 5 roles (ADMIN, SOPORTE, CAMPESINO, CLIENTE, REPARTIDOR) |
| `producto` | 2 | Productos agrícolas (4 URLs de imagen, categoría, stock) |
| `orden` | - | Pedidos (estado, envío, PINs, coordenadas) |
| `detalle_orden` | - | Líneas de pedido (producto, cantidad, precio) |
| `ruta` | - | Rutas de entrega (código, zona, repartidor asignado) |
| `direccion` | - | Direcciones guardadas del usuario |
| `resena` | - | Reseñas de productos (1-5 estrellas) |
| `favorito_producto` | - | Productos favoritos del cliente |
| `favorito_campesino` | - | Campesinos favoritos del cliente |
| `ticket_soporte` | - | Tickets de soporte técnico |
| `mensaje_soporte` | - | Mensajes dentro de tickets |
| `notificacion` | - | Notificaciones push |
| `contacto_horeca` | - | Leads B2B (hoteles, restaurantes) |

#### MongoDB Atlas — Imágenes (GridFS)

| Parámetro | Valor |
|---|---|
| **Host** | `agroconecta-imagenes.vwbx8hb.mongodb.net` |
| **Base de datos** | `agroconecta_imagenes` |
| **Colecciones** | `fs.files`, `fs.chunks` |
| **Proveedor** | MongoDB Atlas (M0 Free Tier) |
| **Cloud** | AWS us-east-1 |
| **Réplicas** | 3 nodos (Replica Set) |
| **Almacenamiento** | 512 MB |

### 3.5. Especificaciones técnicas de los servidores

| Parámetro | Backend (Render) | MySQL (Aiven) | MongoDB (Atlas) |
|---|---|---|---|
| **Plan** | Free | Free | M0 Free |
| **vCPU** | 1 | 1 | Compartido |
| **RAM** | 512 MB | 1 GB | Compartido |
| **Almacenamiento** | Efímero (Docker) | 1 GB SSD | 512 MB |
| **Sistema operativo** | Alpine Linux (Docker) | MySQL 8.4 | MongoDB 7.x |
| **Región** | Oregon, USA | San Francisco, USA | Virginia, USA |
| **SSL/TLS** | Render auto-provisiona | Certificado CA Aiven | TLS nativo Atlas |
| **Cold start** | ~50s (plan free) | N/A | N/A |

---

## 4. Servidores e Implantación

### 4.1. Servidores y configuración

El sistema se despliega en **Render** mediante un pipeline de integración continua conectado al repositorio GitHub. Render detecta automáticamente el `Dockerfile` y construye la imagen en cada push a la rama `main`.

**URLs del sistema:**

| Entorno | URL |
|---|---|
| **Producción (dominio)** | `https://agroconecta.farm` |
| **Producción (Render directo)** | `https://agroconecta-04uf.onrender.com` |

**Configuración DNS (Spaceship):**

| Tipo | Nombre | Valor |
|---|---|---|
| `CNAME` | `@` | `agroconecta-04uf.onrender.com` |
| `CNAME` | `www` | `agroconecta-04uf.onrender.com` |

### 4.2. Dependencias necesarias para despliegue

#### Dockerfile (multi-etapa)

| Etapa | Imagen base | Propósito |
|---|---|---|
| **Build** | `eclipse-temurin:17-jdk-alpine` | Compilación Maven |
| **Runtime** | `eclipse-temurin:17-jre-alpine` | Ejecución de la aplicación |

El contenedor runtime incluye:
- Java 17 JRE
- Python 3.12 + Zeep (consumo SOAP DANE)
- Certificado CA de Aiven importado en truststore Java
- Script `sipsa_etl.py` para consulta de precios
- Usuario no-root por seguridad
- Health check de proceso

**JVM optimizada para 512 MB:**
```
-Xmx256m -Xss512k -XX:+UseSerialGC -XX:+ExitOnOutOfMemoryError
```

#### Variables de entorno (Render)

| Variable | Valor (referencia) |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://[AIVEN_HOST]:[PUERTO]/defaultdb?sslMode=VERIFY_CA&connectTimeout=30000` |
| `SPRING_DATASOURCE_USERNAME` | Usuario Aiven |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña Aiven |
| `MONGODB_URI` | `mongodb+srv://[USER]:[PASS]@[CLUSTER]/agroconecta_imagenes?appName=agroconecta-imagenes` |
| `GEMINI_API_KEY` | API Key de Google Gemini |
| `MERCADOPAGO_ACCESS_TOKEN` | Token sandbox de MercadoPago |

### 4.3. Verificación de la implantación

### 4.4. Lista de chequeo de implantación

#### Credenciales de acceso

| Rol | Email | Contraseña | Acceso |
|---|---|---|---|
| **ADMIN** | `admin@agroconecta.com` | `123` | Dashboard, gestión de usuarios, analíticas |
| **CAMPESINO** | `pepe@finca.com` | `123` | Publicar productos, gestionar pedidos |
| **CLIENTE** | `maria@gmail.com` | `123` | Comprar, favoritos, reseñas |
| **REPARTIDOR** | `repartidor@agroconecta.com` | `123` | Ver rutas, tracking GPS |

#### Dominio y DNS

| Elemento | Valor |
|---|---|
| **Dominio** | `agroconecta.farm` |
| **Proveedor dominio** | Spaceship.com |
| **Registro DNS (CNAME `@`)** | `agroconecta-04uf.onrender.com` |
| **Registro DNS (CNAME `www`)** | `agroconecta-04uf.onrender.com` |
| **SSL/TLS** | Auto-provisionado por Render (Let's Encrypt) |

#### Bases de datos

| Elemento | Valor |
|---|---|
| **MySQL — Proveedor** | Aiven.io (Free Tier) |
| **MySQL — Host** | `agroconecta-mysql-brayanebareno1304-47f1.a.aivencloud.com:28963` |
| **MySQL — Base de datos** | `defaultdb` |
| **MySQL — Usuario** | `avnadmin` |
| **MySQL — SSL** | CA personalizada (archivo `certs/aiven-ca.pem`) |
| **MongoDB — Proveedor** | MongoDB Atlas (M0 Free Tier) |
| **MongoDB — Cluster** | `agroconecta-imagenes.vwbx8hb.mongodb.net` |
| **MongoDB — Base de datos** | `agroconecta_imagenes` |
| **MongoDB — Usuario** | `brayanebareno1304_db_user` (rol Atlas Admin) |

#### Integraciones externas

| Servicio | Modo | Token / API Key |
|---|---|---|
| **MercadoPago** | Sandbox | `TEST-858847077141020-041322-...-741531223` |
| **Gemini AI** | Producción | `AQ.Ab8RN6IGsEBsn0JCeYaneYHacrm0QZyMH...` |
| **SIPSA - DANE** | Producción | Sin autenticación (API pública) |
| **OSRM** | Producción | Sin autenticación (API pública) |

#### Variables de entorno en Render

| Variable | Valor de referencia |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://agroconecta-mysql-brayanebareno1304-47f1.a.aivencloud.com:28963/defaultdb?sslMode=VERIFY_CA&connectTimeout=30000` |
| `SPRING_DATASOURCE_USERNAME` | `avnadmin` |
| `SPRING_DATASOURCE_PASSWORD` | `********` |
| `MONGODB_URI` | `mongodb+srv://brayanebareno1304_db_user:********@agroconecta-imagenes.vwbx8hb.mongodb.net/agroconecta_imagenes?appName=agroconecta-imagenes` |
| `GEMINI_API_KEY` | `********` |
| `MERCADOPAGO_ACCESS_TOKEN` | `********` |

> **Nota de seguridad:** Las contraseñas y tokens están ofuscados (`********`) en este documento. Los valores reales se encuentran en el dashboard de Render → Environment Variables y no deben compartirse en texto plano.

#### Verificación de despliegue

| Verificación | Cómo comprobarlo | Resultado esperado |
|---|---|---|
| Backend responde | Abrir `https://agroconecta.farm` | Carga la página de inicio |
| MySQL conectado | Logs Render: `HikariPool-1 - Start completed` | Pool activo |
| MongoDB conectado | Logs Render: `Monitor thread successfully connected` | 3 nodos Atlas visibles |
| Seeds creados | Logs Render: `Usuario ADMIN creado` ... `REPARTIDOR creado` | 4 usuarios base |
| Login funciona | Iniciar sesión con credenciales de arriba | Redirige al dashboard |
| Productos visibles | Tienda muestra productos de prueba | Productos del campesino |
| Imágenes cargan | Subir foto de perfil y verificar | Se guarda en MongoDB GridFS |
| Pedido completo | Cliente compra → Campesino acepta → ENTREGADO | Flujo funcional |
| Dominio HTTPS | `https://agroconecta.farm` carga con candado verde | SSL activo |
| Postman Runner | Ejecutar colección con 28 endpoints | 25/28 pasan (89.3%) |
| Selenium UI | `mvnw test -Dtest=AgroConectaSeleniumTests` | 10 pruebas de navegador |
| Integración REST | `mvnw test -Dtest=AgroConectaIntegrationTests` | 20 pruebas automatizadas |

---

### 4.5. Pruebas del sistema

#### Pruebas de API con Postman

Se creó una colección de Postman con **28 endpoints REST** que cubren todas las funcionalidades del sistema. La colección se encuentra en `postman/AgroConecta.postman_collection.json`.

**Resultados de la ejecución automatizada (Runner):**

| Métrica | Valor |
|---|---|
| Endpoints ejecutados | 28 |
| Respuestas 200 OK | 25 |
| Errores | 3 (esperados: autenticación requerida o servicio externo no disponible) |
| Tasa de éxito | **89.3%** |
| Tiempo total | 13.4 segundos |

El reporte detallado se encuentra en `postman/REPORTE_PRUEBAS_POSTMAN.md`.

#### Pruebas de interfaz con Selenium

Se implementaron **10 pruebas de navegador automatizadas** usando Selenium WebDriver con Microsoft Edge en modo headless. Las pruebas simulan un usuario real navegando por la plataforma:

| ID | Prueba | Acción |
|---|---|---|
| TC01 | Página de inicio | Verificar que carga el título |
| TC02 | Login administrador | Iniciar sesión con credenciales válidas |
| TC03 | Login fallido | Credenciales inválidas → error |
| TC04 | Acceso denegado | Intentar acceder a /admin sin login |
| TC05 | Registro de usuario | Llenar formulario y enviar |
| TC06 | Login campesino | Acceder al panel de productos |
| TC07 | Tienda como cliente | Navegar catálogo de productos |
| TC08 | API de precios | Verificar respuesta JSON |
| TC09 | Páginas informativas | Contacto, nosotros, cómo funciona |
| TC10 | Cerrar sesión | Verificar redirección a login |

**Ejecución:** `mvnw test -Dtest="AgroConectaSeleniumTests"`

**Requisito:** Microsoft Edge instalado + `msedgedriver.exe` en `Downloads/edgedriver/`

#### Pruebas de integración REST (Java)

Se implementaron **20 pruebas de integración** usando `RestTemplate` de Spring que validan el flujo completo de la API:

| ID | Prueba |
|---|---|
| IT01-IT03 | Login con los 3 roles (admin, campesino, cliente) |
| IT04 | Login con credenciales inválidas |
| IT05 | Verificación de email existente |
| IT06-IT07 | Listado de productos y filtro por campesino |
| IT08-IT09 | Operaciones del carrito de compras |
| IT10-IT11 | Previsualización de envío y creación de pedido |
| IT12-IT13 | Consulta de compras y ventas |
| IT14 | **Flujo completo:** campesino acepta pedido → ENTREGADO |
| IT15-IT16 | Consulta de precios SIPSA y catálogo |
| IT17 | Reseñas de productos |
| IT18 | Rutas disponibles |
| IT19-IT20 | Direcciones guardadas y perfil público |

**Ejecución:** `mvnw test -Dtest="AgroConectaIntegrationTests"`

---

## 5. Anexos

### Anexo A: Usuarios de prueba (seeds)

| Rol | Email | Contraseña | Nombre |
|---|---|---|---|
| **ADMIN** | `admin@agroconecta.com` | `123` | Admin Sistema |
| **CAMPESINO** | `pepe@finca.com` | `123` | Pepe Grillo |
| **CLIENTE** | `maria@gmail.com` | `123` | María Cliente |
| **REPARTIDOR** | `repartidor@agroconecta.com` | `123` | Carlos Moto |

### Anexo B: Estructura del proyecto

```
AccesoUsuarios/
├── Dockerfile                    # Build multi-etapa
├── .dockerignore                 # Exclusiones del contexto Docker
├── pom.xml                       # Maven: dependencias y plugins
├── mvnw / mvnw.cmd               # Maven Wrapper
├── .mvn/wrapper/                 # Config Maven Wrapper
├── src/main/
│   ├── java/com/proyecto/AccesoUsuarios/
│   │   ├── AccesoUsuariosApplication.java
│   │   ├── config/               # Security, Exceptions, Resources, Seeds
│   │   ├── controller/           # 23 controladores MVC + REST
│   │   ├── model/                # 13 entidades JPA
│   │   ├── repository/           # 13 repositorios Spring Data
│   │   ├── security/             # UserDetailsService, SecurityConfig
│   │   └── service/              # 13 servicios de negocio
│   └── resources/
│       ├── application.properties # Configuración Spring Boot
│       ├── certs/aiven-ca.pem     # Certificado SSL Aiven
│       ├── python/sipsa_etl.py    # Script DANE/SIPSA
│       ├── static/                # Archivos estáticos
│       └── templates/             # 57 plantillas Thymeleaf
├── AgroConecta/                   # App Android Cliente (proyección futura)
├── AgroconectaGo/                 # App Android Repartidor (proyección futura)
└── agro_servicio_python/          # Microservicio Python (deprecado — reemplazado por Java)
```

### Anexo C: Flujo simplificado de pedido

```
1. CLIENTE compra en la tienda
   ↓ (estado: PENDIENTE)
2. CAMPESINO ve el pedido y da clic en PREPARADO
   ↓ (estado: ENTREGADO)
3. CLIENTE ve el pedido como ENTREGADO
```

---

## 6. Definiciones de Acrónimos y Glosario

| Término | Definición |
|---|---|
| **SIPSA** | Sistema de Información de Precios del Sector Agropecuario — fuente oficial del DANE Colombia |
| **DANE** | Departamento Administrativo Nacional de Estadística de Colombia |
| **KYC** | Know Your Customer — proceso de verificación de identidad |
| **OSRM** | Open Source Routing Machine — motor de cálculo de rutas de conducción |
| **GridFS** | Sistema de archivos de MongoDB para almacenar archivos grandes (imágenes) |
| **ORM** | Object-Relational Mapping — mapeo entre objetos Java y tablas SQL |
| **JPA** | Jakarta Persistence API — especificación ORM estándar de Java |
| **MVC** | Model-View-Controller — patrón de diseño de software |
| **BCrypt** | Algoritmo de hashing seguro para contraseñas |
| **JDBC** | Java Database Connectivity — API de conexión a bases de datos |
| **JVM** | Java Virtual Machine — máquina virtual que ejecuta bytecode Java |
| **CRLF / LF** | Formatos de salto de línea (Windows vs Linux) |
| **CNAME** | Canonical Name — registro DNS que apunta un dominio a otro |
| **TLS** | Transport Layer Security — protocolo de cifrado para comunicaciones |
| **Cold Start** | Tiempo que tarda un servicio en iniciar tras un período de inactividad (típico en planes gratuitos) |
| **Replica Set** | Conjunto de nodos MongoDB que mantienen copias sincronizadas de los datos |
| **B2B** | Business to Business — modelo de negocio entre empresas (ej. HORECA) |
| **HORECA** | Hoteles, Restaurantes, Cafeterías — sector de clientes empresariales |
