# FINAL PROJECT ADVANCE SUBMISSION

## AgroConecta — Agricultural Connection Platform

---

**Course:** Software Engineering Final Project  
**Student:** Brayan Bareño  
**Institution:** SENA  
**Date:** June 2026  
**Repository:** https://github.com/Brayanbii/Agroconecta  
**Production URL:** https://agroconecta.farm

---

## Table of Contents

1. [Project Description](#1-project-description)
2. [Objectives](#2-objectives)
3. [Scope and Functional Requirements](#3-scope-and-functional-requirements)
4. [Technical Architecture](#4-technical-architecture)

---

## 1. Project Description

### 1.1 Executive Summary

AgroConecta is a web platform designed to connect Colombian farmers directly with consumers. The platform eliminates intermediaries in the agricultural supply chain, ensuring fair prices for both producers and buyers.

Farmers can publish their products with photos, descriptions, and prices. Consumers can browse a digital marketplace, add products to a shopping cart, and place orders. The system includes a payment gateway (MercadoPago sandbox), an identity verification system (KYC), and official price references from the Colombian government (DANE/SIPSA).

### 1.2 Problem Statement

In Colombia, small and medium farmers face significant challenges when selling their products:

- **Intermediaries take most of the profit.** A farmer who sells a kilo of potatoes for $2,500 COP often receives less than $1,000 COP after multiple resellers.
- **Limited access to markets.** Many farmers can only sell at local town markets because they lack the means to reach urban consumers.
- **No price transparency.** Farmers often do not know the real market price of their products and are forced to accept whatever intermediaries offer.
- **Consumers pay inflated prices.** By the time a product reaches a supermarket in Bogotá, it has passed through 3 to 5 intermediaries, each adding their margin.

### 1.3 Proposed Solution

AgroConecta solves these problems through a digital platform where:

1. **Farmers register their farm**, upload their identity documents for verification, and publish their products with photos, descriptions, and prices.
2. **Consumers browse the digital marketplace**, search by product name or category, compare prices, and read reviews from other buyers.
3. **Orders are placed through the platform**, processed with MercadoPago (currently in sandbox testing mode), and the farmer confirms the order.
4. **Price references from DANE/SIPSA** help farmers set competitive prices based on official Colombian market data.

### 1.4 Key Features

| Feature | Description |
|---|---|
| **User Roles** | 5 distinct roles: Admin, Support, Farmer, Client, Delivery Driver |
| **Digital Marketplace** | Search, filter by category, view product details with photos and reviews |
| **Shopping Cart** | Add products, adjust quantities, calculate totals |
| **Order Management** | Complete purchase flow: cart → checkout → payment → farmer confirmation |
| **KYC Verification** | Identity verification with document upload (ID card, farm photos, driver's license) |
| **Official Prices** | SIPSA/DANE integration for agricultural price references |
| **Analytics Dashboard** | Sales charts, product rankings, monthly income reports |
| **Support System** | Ticket-based help desk with real-time messaging |
| **Responsive Design** | Optimized for desktop, tablet, and mobile phone browsers |

### 1.5 Current Status

The web platform is **fully operational in production** at `https://agroconecta.farm`. It is deployed on Render cloud platform with MySQL (Aiven) for textual data and MongoDB Atlas for image storage via GridFS. Two companion Android applications (AgroConecta for clients/farmers and AgroConectaGo for delivery drivers) are under development as the next project phase.

---

## 2. Objectives

### 2.1 General Objective

To develop and deploy a web platform that connects Colombian farmers directly with consumers, enabling the commercialization of agricultural products through digital tools that ensure price transparency, delivery traceability, and operational efficiency, with REST APIs ready for future mobile application integration.

### 2.2 Specific Objectives

| # | Objective | Status |
|---|---|---|
| **SO-1** | Allow farmers to publish and manage their agricultural products through an intuitive web interface | ✅ Completed |
| **SO-2** | Integrate official DANE/SIPSA price references to support fair pricing | ✅ Completed |
| **SO-3** | Implement an identity verification system (KYC) for farmers and delivery drivers | ✅ Completed |
| **SO-4** | Process payments securely through MercadoPago (sandbox mode for testing) | ✅ Completed |
| **SO-5** | Expose documented REST APIs that can be consumed by external clients such as mobile applications | ✅ Completed |
| **SO-6** | Guarantee data persistence and availability through cloud infrastructure with separate databases for textual data and images | ✅ Completed |
| **SO-7** | Provide real-time analytics for administrators and farmers through interactive charts | ✅ Completed |
| **SO-8** | Deploy the platform to production with a custom domain, HTTPS security, and continuous integration | ✅ Completed |
| **SO-9** | Validate the system through automated testing (Postman, Selenium, integration tests, load tests) | ✅ Completed |
| **SO-10** | Document the platform with a technical document, user manual, and deployment guide | ✅ Completed |

---

## 3. Scope and Functional Requirements

### 3.1 Project Scope

**Included in this delivery:**

- Full-stack web platform with 5 user roles
- 13 database tables managed by Hibernate ORM
- 76 REST API endpoints documented and tested
- Authentication and authorization with Spring Security (BCrypt password encoding)
- Responsive user interface with 57 Thymeleaf templates
- Cloud deployment with Docker containerization
- External service integrations (MercadoPago, SIPSA/DANE, OSRM, Gemini AI)
- Automated test suite (Postman collection, Selenium UI tests, REST integration tests, load tests)
- Two databases: MySQL (Aiven) for structured data, MongoDB Atlas (GridFS) for image storage

**Out of scope (future phases):**

- Production payment processing (currently sandbox mode)
- Mobile application publication on Google Play Store
- Real-time push notifications
- Offline mode for rural areas without connectivity

### 3.2 Functional Requirements by Role

#### Client (Buyer)
| ID | Requirement | Description |
|---|---|---|
| FR-C01 | User Registration | Create account with email, password, and personal data |
| FR-C02 | User Login | Authenticate with email and password |
| FR-C03 | Browse Marketplace | View all available products with photos, prices, and ratings |
| FR-C04 | Search Products | Search by product name using keyword search bar |
| FR-C05 | Filter by Category | Filter products by category (Fruits, Vegetables, Tubers, Grains, etc.) |
| FR-C06 | View Product Details | See full description, farmer information, farm location, and reviews |
| FR-C07 | Add to Cart | Add products to shopping cart with custom quantities |
| FR-C08 | Manage Cart | View cart items, change quantities, remove products |
| FR-C09 | Place Order | Checkout with delivery address, shipping type, and payment |
| FR-C10 | View Order History | See all past orders with status and totals |
| FR-C11 | Save Favorites | Mark products as favorites for quick access |
| FR-C12 | Write Reviews | Rate purchased products (1-5 stars) with optional comments |
| FR-C13 | Manage Addresses | Save multiple delivery addresses |
| FR-C14 | Edit Profile | Update personal information and profile photo |

#### Farmer (Producer)
| ID | Requirement | Description |
|---|---|---|
| FR-F01 | Publish Product | Create new product with name, price, category, description, unit, stock, and images |
| FR-F02 | Edit Product | Modify existing product information |
| FR-F03 | Delete Product | Remove product from marketplace |
| FR-F04 | Manage Inventory | Quickly update stock quantities for all products |
| FR-F05 | View Orders | See incoming customer orders with status tracking |
| FR-F06 | Accept Orders | Confirm orders and mark them as prepared |
| FR-F07 | View Analytics | Access sales charts, product rankings, and monthly income reports |
| FR-F08 | View Finances | Check total earnings and payment history (AgroWallet) |
| FR-F09 | Verify Identity (KYC) | Upload identity documents for verification |
| FR-F10 | View Reputation | See average rating, delivery count, and individual reviews |
| FR-F11 | Consult Reference Prices | View SIPSA/DANE market prices when creating products |

#### Administrator
| ID | Requirement | Description |
|---|---|---|
| FR-A01 | View Global Dashboard | See platform statistics: users, products, orders, reviews |
| FR-A02 | View Sales Charts | Analyze product rankings and monthly sales graphs |
| FR-A03 | Manage Users | View, search, create, edit, and delete user accounts |
| FR-A04 | Approve KYC (Farmers) | Review and approve/reject farmer identity documents |
| FR-A05 | Approve KYC (Drivers) | Review and approve/reject delivery driver documents |

#### Delivery Driver
| ID | Requirement | Description |
|---|---|---|
| FR-D01 | KYC Registration | Upload documents (ID, driver's license, vehicle registration, SOAT, technical inspection) |
| FR-D02 | Vehicle Registration | Register vehicle data (plate, type, brand, model, year, capacity) |

#### Support
| ID | Requirement | Description |
|---|---|---|
| FR-S01 | View Tickets | See all support tickets organized by status |
| FR-S02 | Respond to Tickets | Reply to user messages within tickets |
| FR-S03 | Change Ticket Status | Update ticket status (Open, In Progress, Closed) |

### 3.3 Non-Functional Requirements

| ID | Requirement | Description |
|---|---|---|
| NFR-01 | Security | All passwords encrypted with BCrypt. All connections use HTTPS/TLS 1.3 |
| NFR-02 | Availability | Platform available 24/7 with automatic restart policies |
| NFR-03 | Performance | Response time under 3 seconds for up to 10 concurrent users on free tier |
| NFR-04 | Scalability | System scales to 20 simultaneous users at 98% success rate on free tier hardware |
| NFR-05 | Responsiveness | User interface adapts to desktop, tablet, and mobile phone screen sizes |
| NFR-06 | Data Persistence | Dual database architecture: MySQL for structured data, MongoDB GridFS for images |
| NFR-07 | Portability | Fully containerized with Docker for deployment on any cloud platform |
| NFR-08 | Maintainability | Source code organized in MVC layered architecture with dependency injection |

---

## 4. Technical Architecture

### 4.1 System Architecture Overview

AgroConecta follows a **three-tier web architecture** deployed on cloud infrastructure:

```
┌─────────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER                     │
│  ┌──────────────────────────────────────────────────┐   │
│  │  Web Browser (Desktop / Tablet / Mobile)          │   │
│  │  Thymeleaf Templates + Bootstrap 5 + JavaScript   │   │
│  │  57 HTML views server-rendered                    │   │
│  └──────────────────────┬───────────────────────────┘   │
└─────────────────────────┼───────────────────────────────┘
                          │ HTTPS (TLS 1.3)
┌─────────────────────────┼───────────────────────────────┐
│                   APPLICATION LAYER                      │
│  ┌──────────────────────┴───────────────────────────┐   │
│  │        Spring Boot 3.5.7 (Java 17)               │   │
│  │        Docker Container (Alpine Linux)            │   │
│  │        Embedded Tomcat on port 8080               │   │
│  │  ┌────────────────────────────────────────────┐  │   │
│  │  │  23 Controllers (MVC + REST)               │  │   │
│  │  │  13 Services (Business Logic)              │  │   │
│  │  │  13 Repositories (Data Access)             │  │   │
│  │  │  13 JPA Entities (Domain Model)            │  │   │
│  │  │  Spring Security (5 Roles + BCrypt)        │  │   │
│  │  └────────────────────────────────────────────┘  │   │
│  │  External Integrations:                           │   │
│  │  - MercadoPago SDK (payments)                     │   │
│  │  - SIPSA/DANE SOAP (price references)             │   │
│  │  - OSRM API (route calculation)                   │   │
│  │  - Gemini AI (product descriptions)               │   │
│  └──────────────────────┬───────────────────────────┘   │
└─────────────────────────┼───────────────────────────────┘
                          │
            ┌─────────────┴─────────────┐
            ▼                           ▼
┌───────────────────────┐   ┌───────────────────────────┐
│     DATA LAYER         │   │     IMAGE STORAGE          │
│  ┌───────────────────┐ │   │  ┌──────────────────────┐  │
│  │  MySQL (Aiven)    │ │   │  │ MongoDB Atlas        │  │
│  │  13 tables        │ │   │  │ GridFS               │  │
│  │  DigitalOcean SFO │ │   │  │ fs.files + fs.chunks │  │
│  │  MySQL 8.4.8      │ │   │  │ AWS us-east-1        │  │
│  │  TLS 1.3 + CA     │ │   │  │ Replica Set (3 nodes)│  │
│  └───────────────────┘ │   │  └──────────────────────┘  │
└───────────────────────┘   └───────────────────────────┘
```

### 4.2 Technology Stack

| Layer | Technology | Version | Purpose |
|---|---|---|---|
| **Backend Language** | Java | 17 (LTS) | Main programming language |
| **Framework** | Spring Boot | 3.5.7 | Application framework with embedded server |
| **Security** | Spring Security | 6.x | Authentication, authorization, BCrypt |
| **ORM** | Hibernate / JPA | 6.6.33 | Object-relational mapping for MySQL |
| **Template Engine** | Thymeleaf | 3.1 | Server-side HTML rendering |
| **CSS Framework** | Bootstrap 5 + Tailwind CSS | 5.x / CDN | Responsive user interface |
| **Charts** | ApexCharts | 3.x / CDN | Interactive data visualization |
| **PDF Generation** | OpenPDF | 1.3.30 | Order receipt generation |
| **Payment Gateway** | MercadoPago SDK | 2.1.27 | Payment processing (sandbox) |
| **Build Tool** | Maven | 3.9.11 | Dependency management and build |
| **Container** | Docker | Multi-stage | Application packaging |
| **OS (Container)** | Alpine Linux | 3.21 | Lightweight runtime environment |
| **MySQL Driver** | MySQL Connector/J | 9.4.0 | JDBC driver |
| **Connection Pool** | HikariCP | 6.3.3 | Database connection management |
| **MongoDB Driver** | MongoDB Java Driver | 5.5.2 | GridFS image storage |
| **Python Script** | Python 3 + Zeep | 3.12 / 4.3 | SIPSA/DANE SOAP client |

### 4.3 Database Design

#### MySQL — Structured Data (Aiven)

The database contains 13 tables managed by Hibernate with `ddl-auto=update` (automatic schema creation):

| Table | Purpose | Key Fields |
|---|---|---|
| `usuario` | All 5 user roles in one table | id, email, password (BCrypt), rol, nombreCompleto, KYC fields, vehicle fields, farm fields, reputation counters |
| `producto` | Agricultural products | id, nombre, precio, categoria, descripcion, stock, 4 image URLs, lat/long, FK→usuario |
| `orden` | Purchase orders | id, numeroOrden, estado, total, shipping address, coordinates, PIN codes, FK→usuario, FK→ruta |
| `detalle_orden` | Order line items | id, nombre, cantidad, precio, total, FK→orden, FK→producto |
| `ruta` | Delivery routes | id, codigoRuta, zona, pesoTotal, estado, FK→repartidor |
| `direccion` | Saved addresses | id, alias, direccionCompleta, lat/long, FK→usuario |
| `resena` | Product reviews | id, estrellas (1-5), comentario, FK→producto, FK→usuario |
| `favorito_producto` | Product favorites | id, FK→cliente, FK→producto (unique constraint) |
| `favorito_campesino` | Farmer favorites | id, FK→cliente, FK→campesino (unique constraint) |
| `ticket_soporte` | Support tickets | id, asunto, estado, FK→usuario |
| `mensaje_soporte` | Support messages | id, contenido, FK→ticket, FK→remitente |
| `notificacion` | Push notifications | id, usuarioId, titulo, mensaje, tipo |
| `contacto_horeca` | B2B contacts | id, nombre, empresa, tipoNegocio |

#### MongoDB — Image Storage (Atlas)

Images are stored in MongoDB using **GridFS**, which splits files into 255KB chunks:

| Collection | Purpose |
|---|---|
| `fs.files` | File metadata (filename, contentType, uploadDate) |
| `fs.chunks` | File binary data in chunks |

### 4.4 Cloud Infrastructure

| Component | Provider | Plan | Specifications |
|---|---|---|---|
| **Application Server** | Render | Free | 1 vCPU shared, 512 MB RAM, Docker container |
| **MySQL Database** | Aiven | Free | 1 vCPU, 1 GB RAM, 1 GB SSD, DigitalOcean SFO |
| **MongoDB (Images)** | MongoDB Atlas | M0 Free | Shared, 512 MB, AWS us-east-1, 3-node replica set |
| **Domain** | Spaceship | Purchased | `agroconecta.farm` |
| **SSL Certificate** | Render | Auto-provisioned | Let's Encrypt |
| **DNS** | Spaceship | Free | CNAME to Render |

### 4.5 JVM Optimization for 512 MB

The application is optimized to run within the 512 MB limit of the free Render plan:

| Parameter | Value | Reason |
|---|---|---|
| `-Xmx` | 128 MB | Maximum heap size |
| `-Xmn` | 32 MB | Young generation size |
| `-Xss` | 192 KB | Thread stack size |
| `-XX:+UseSerialGC` | Serial collector | Lowest memory overhead |
| HikariCP max pool | 3 connections | Minimize database connection memory |
| MongoDB max pool | 3 connections | Minimize image storage connection memory |
| Lazy initialization | Enabled | Beans created only when needed |
| SQL logging | Disabled | Reduce I/O overhead |
| Logging level | WARN | Only important messages |

### 4.6 Security Implementation

| Aspect | Implementation |
|---|---|
| **Authentication** | Spring Security with email + password |
| **Password Storage** | BCrypt hashing (no plain-text passwords) |
| **Authorization** | 5 role-based access controls (ADMIN, SOPORTE, CAMPESINO, CLIENTE, REPARTIDOR) |
| **CSRF Protection** | Enabled on all POST/PUT/DELETE endpoints |
| **Session Management** | HTTP sessions with 30-minute timeout |
| **Transport Security** | HTTPS with TLS 1.3 (Let's Encrypt auto-renewal) |
| **Database Encryption** | MySQL TLS connection with Aiven CA certificate |
| **Secret Management** | All API keys and passwords stored as environment variables (not in source code) |

### 4.7 Continuous Integration and Deployment

Every push to the `main` branch on GitHub triggers an automatic deployment on Render:

1. **GitHub** → Push to `main` branch
2. **Render** → Detects Dockerfile, starts build
3. **Docker** → Multi-stage build: Maven compilation → lightweight JRE image
4. **Deploy** → Container starts with environment variables from Render dashboard
5. **Health Check** → Render verifies port binding before routing traffic
6. **Live** → Application available at `https://agroconecta.farm`

---

## Appendix A: Testing Summary

The platform was validated through **4 testing methodologies** with **372 total test executions**:

| Method | Tool | Tests | Success Rate |
|---|---|---|---|
| **API Testing** | Postman Runner | 28 endpoints | 89.3% |
| **UI Testing** | Selenium WebDriver (Edge) | 10 browser tests | 100% |
| **Integration Testing** | RestTemplate + JUnit | 20 programmatic tests | 75% |
| **Load Testing** | Java HttpURLConnection (3 methods) | 321 requests across 5 concurrency levels | 92% |

## Appendix B: Key Project Metrics

| Metric | Value |
|---|---|
| Java source files | 69 |
| Lines of Java code | ~15,000 |
| Thymeleaf templates | 57 |
| REST API endpoints | 76 |
| Database tables | 13 |
| User roles | 5 |
| External integrations | 5 |
| Test cases | 58 |
| Load test requests | 321 |
| Documentation pages | 4 documents (1,500+ lines) |

---

**Submitted in partial fulfillment of the Software Engineering Final Project requirements.**

**June 2026**
