# FINAL PROJECT PRESENTATION — AGROCONECTA

## Slide-by-Slide Content Guide

**Total Slides:** 20  
**Estimated Duration:** 15-20 minutes  
**Language:** English B1 (simple, clear)  
**Design Recommendation:** Green and earth tones (#16a34a, #f0fdf4, #1a1a2e). Font: Inter or Plus Jakarta Sans.

---

## SLIDE 1 — Title Slide

**Title:** AgroConecta — Agricultural Connection Platform  
**Subtitle:** Final Project Presentation · Software Engineering · SENA  
**Student:** Brayan Bareño  
**Date:** June 2026  
**URL:** agroconecta.farm

**Visual:** Logo (green leaf icon) centered. Gradient background from dark green to light green.

---

## SLIDE 2 — The Problem

**Title:** The Problem We Are Solving

**Content (3 columns with icons):**

| 🏪 Intermediaries | 📍 Limited Access | ❓ No Price Info |
|---|---|---|
| 3-5 resellers between farmer and consumer. Farmer gets $1,000 COP for a kilo sold at $3,500 COP in the city | Farmers in rural areas can only sell at local markets. No access to urban consumers | Farmers don't know real market prices. Forced to accept whatever intermediaries offer |

**Visual:** Simple diagram showing farmer → intermediary → intermediary → supermarket → consumer with prices at each step.

**Speaker notes:** "In Colombia, small farmers face three big problems..."

---

## SLIDE 3 — The Solution

**Title:** AgroConecta — Direct Connection

**Content:** One platform that eliminates intermediaries. Farmer sells directly to consumer.

**3 key points:**
1. 🌱 **Farmers publish products** — photos, prices, descriptions
2. 🛒 **Consumers buy directly** — browse, search, cart, checkout
3. 📊 **Everyone benefits** — fair prices, traceability, analytics

**Visual:** Simple flow diagram: Farmer → AgroConecta → Consumer. Arrows showing money going to farmer, products going to consumer.

---

## SLIDE 4 — Project Objectives

**Title:** What We Set Out to Build

**Content (10 objectives, summarized as icons):**
- ✅ Web platform with 5 user roles
- ✅ Marketplace with search and categories
- ✅ Order processing with payments (MercadoPago)
- ✅ KYC identity verification
- ✅ SIPSA/DANE official price integration
- ✅ Analytics dashboard with charts
- ✅ REST APIs for mobile apps
- ✅ Cloud deployment with Docker
- ✅ Custom domain with HTTPS
- ✅ Automated testing (4 methods)

**Visual:** Checkmark icons. Green for completed.

---

## SLIDE 5 — System Architecture

**Title:** How It Works — Technical Architecture

**Content:** Three-tier architecture diagram (simplified)

```
[Web Browser / Mobile] 
        ↓ HTTPS
[Spring Boot 3.5.7 + Docker]
   ↙              ↘
[MySQL Aiven]  [MongoDB Atlas]
 (data)         (images)
```

**Key technologies listed below diagram:**
- Java 17, Spring Boot 3.5.7, Thymeleaf, Bootstrap 5, Tailwind CSS
- Spring Security (5 roles, BCrypt), Hibernate/JPA
- Docker, Alpine Linux, Render Cloud
- MercadoPago, DANE/SIPSA, OSRM, Gemini AI

**Visual:** Clean architecture diagram with icons for each technology.

---

## SLIDE 6 — Database Design

**Title:** Data Structure — 13 Tables + MongoDB

**Content:** Entity diagram (simplified)

```
usuario ──< producto ──< detalle_orden
   │                       │
   ├──< orden ─────────────┘
   ├──< direccion
   ├──< favorito_producto
   ├──< favorito_campesino
   ├──< resena
   ├──< ticket_soporte ──< mensaje_soporte
   └──< ruta
```

Plus MongoDB GridFS for images: `fs.files` + `fs.chunks`

**Visual:** Database ER diagram with table names and relationships.

---

## SLIDE 7 — User Roles

**Title:** 5 User Roles — One Platform

**Content (5 cards):**

| Admin 🔧 | Support 💬 | Farmer 🌱 | Client 🛒 | Driver 🚚 |
|---|---|---|---|---|
| Dashboard, user management, KYC approval | Ticket management, user help | Product CRUD, orders, analytics, KYC | Marketplace, cart, orders, reviews | Document upload, vehicle registration |

**Visual:** 5 cards with role icons, each with a unique color.

---

## SLIDE 8 — Key Feature: Marketplace

**Title:** The Digital Marketplace

**Content:**
- 📸 Products with photos, prices, and star ratings
- 🔍 Search bar with real-time filtering
- 🏷️ Category filters (Fruits, Vegetables, Tubers, Grains...)
- ❤️ Favorites toggle (save products for later)
- 📱 Responsive — works on phone, tablet, and desktop

**Visual:** Screenshot of the store page showing products in a grid.

**Speaker notes:** "This is what the client sees when they enter the store..."

---

## SLIDE 9 — Key Feature: Farmer Panel

**Title:** The Farmer's Control Center

**Content:**
- 📦 Product management (create, edit, delete)
- 💰 SIPSA price references from DANE
- 📊 Analytics dashboard with charts
- 🛍️ Order management (accept incoming orders)
- 🪪 KYC identity verification
- ⭐ Reputation and reviews

**Visual:** Screenshot of the farmer dashboard sidebar menu.

---

## SLIDE 10 — Key Feature: KYC Verification

**Title:** Identity Verification System

**Content:**
- 📄 Farmers upload: ID card, farm photo, identity number
- 🚚 Drivers upload: ID, driver's license (front/back), vehicle registration, SOAT, technical inspection, profile photo
- 👨‍💼 Admin reviews and approves/rejects
- 🔓 Public KYC portal — no login required

**Visual:** Screenshots of the KYC form showing document upload areas.

---

## SLIDE 11 — Testing: How We Validated the System

**Title:** Testing — 4 Methodologies, 379 Executions

**Content (4 cards):**

| 🧪 Postman | 🖥️ Selenium | 🔗 Integration | ⚡ Load Testing |
|---|---|---|---|
| 28 API endpoints | 10 browser tests | 20 REST tests | 321 requests |
| 89.3% success | 100% success | 75% success | 92% success |
| REST APIs | Real browser (Edge) | Java RestTemplate | 3 tools, 5 levels |

**TOTAL: 91% overall success rate**

**Visual:** 4 cards with methodology names and results. Bar chart comparing success rates.

---

## SLIDE 12 — Testing: Selenium in Action

**Title:** Browser Automation — 10/10 Passed

**Content:** Real execution log showing all 10 tests passing:

```
✅ TC01 | Homepage loaded | 1.6s
✅ TC02 | Admin login → dashboard | 14.6s
✅ TC03 | Failed login detected | 10.4s
✅ TC04 | Access denied (no session) | 6.0s
✅ TC05 | User registration | 11.2s
✅ TC06 | Farmer login | 11.1s
✅ TC07 | Store browsing | 24.3s
✅ TC08 | Price catalog API | 7.1s
✅ TC09 | Static pages | 7.3s
✅ TC10 | Logout with CSRF | 6.9s
────────────────────────────
TOTAL: 109.9s | 10/10 PASSED
```

**Visual:** Terminal-style green text on dark background.

---

## SLIDE 13 — Testing: Load Testing

**Title:** Can the System Handle Many Users?

**Content (table + key finding):**

| Users | Requests | Successes | Rate | Latency |
|---|---|---|---|---|
| 1 | 6 | 6 | 100% | 1.6s |
| 5 | 30 | 30 | 100% | 2.7s |
| 10 | 60 | 60 | 100% | 5.3s |
| 20 | 114 | 112 | 98% | 11.3s |
| 30 | 111 | 87 | 78% | 14.0s |

**Key finding:** Platform handles **20 concurrent users at 98%** on free hardware (512 MB RAM, 1 vCPU). The 30-user saturation is a hardware limit, not a software limit.

**Visual:** Bar chart showing success rate decreasing with user count. Line chart showing response time increasing.

---

## SLIDE 14 — Deployment: Production Infrastructure

**Title:** How the System Reaches Users

**Content:** Deployment pipeline diagram:

```
GitHub Push → Docker Build → Container Deploy → Live at agroconecta.farm
                   │
    ┌──────────────┴──────────────┐
    │  Multi-stage Docker build:  │
    │  Stage 1: Maven compile     │
    │  Stage 2: JRE image (~90MB) │
    └─────────────────────────────┘
```

**Infrastructure details:**
- 🌐 Render Cloud (auto-deploy on git push)
- 🗄️ MySQL (Aiven.io) + MongoDB Atlas
- 🔒 HTTPS/TLS 1.3 (auto-certificate)
- 🐳 Docker container (Alpine Linux)
- ⚡ CI/CD: ~3 minutes from push to live

**Visual:** Infrastructure diagram with cloud, database, and security icons.

---

## SLIDE 15 — JVM Optimization for 512 MB

**Title:** Making Spring Boot Fit in 512 MB

**Content (two columns):**

**Challenges:**
- Spring Boot 3.5.7 needs ~300 MB baseline
- Hibernate + JPA adds ~100 MB
- MongoDB driver adds ~80 MB
- Thymeleaf templates add ~50 MB
- 5 external service integrations

**Solutions:**
- ✅ Heap limited to 128 MB (`-Xmx128m`)
- ✅ SerialGC (lowest memory GC)
- ✅ Lazy bean initialization
- ✅ MongoDB pool: 3 connections max
- ✅ HikariCP pool: 3 connections max
- ✅ SQL logging disabled
- ✅ Template caching enabled
- ✅ Python/Zeep removed from container
- ✅ Logging set to WARN level

**Result:** 458 MB used of 512 MB available. Stable operation.

**Visual:** Before/after comparison. Left side: "Without optimization: 600+ MB required". Right side: "With optimization: 458 MB — fits in 512 MB".

---

## SLIDE 16 — User Manual and Training Plan

**Title:** Helping Users Succeed

**Content (two columns):**

**User Manual (11 sections):**
- Welcome and first steps
- Detailed guides per role (5 sections)
- Mobile navigation guide
- Future mobile apps preview
- 13 FAQs, 24-term glossary

**Training Plan (4 sessions):**
1. Clients (1 hour)
2. Farmers (2 hours)
3. Administrators (1.5 hours)
4. Drivers (45 minutes)

**Visual:** Book icon for manual. Presentation icon for training.

---

## SLIDE 17 — Results: What We Achieved

**Title:** Project Results — By the Numbers

**Content (big numbers with labels):**

| 69 | 57 | 76 | 379 | 13 | 5 | 10/10 |
|---|---|---|---|---|---|---|
| Java files | HTML templates | API endpoints | Test executions | DB tables | User roles | Documents |

**Plus:**
- 🌐 Production domain: `agroconecta.farm`
- 🐳 Docker image: 90 MB
- ⚡ Deploy time: 3 minutes
- 🧪 321 load test requests
- 📱 Responsive design (desktop + mobile)

**Visual:** Big numbers in circle icons. Clean, impactful layout.

---

## SLIDE 18 — Live Demo

**Title:** Let's See It Working

**Content:** Live demonstration of the platform:

1. Open `agroconecta.farm` → homepage loads
2. Login as `maria@gmail.com / 123` → client account
3. Browse the store → see products
4. Add a product to cart → cart icon updates
5. Login as `pepe@finca.com / 123` → farmer account
6. Show farmer dashboard → products, analytics
7. Login as `admin@agroconecta.com / 123` → admin view
8. Show admin dashboard → statistics, charts

**Visual:** "LIVE DEMO" large text in center. Prepare the browser with tabs open.

**Speaker notes:** "Now let me show you the platform working in real time..."

---

## SLIDE 19 — Future Work

**Title:** What Comes Next

**Content (timeline or roadmap):**

| Now ✅ | Next 🚧 | Later 📅 |
|---|---|---|
| Web platform live | Android apps (code exists) | Production payments |
| All 10 objectives completed | Push notifications | Route optimization |
| 4 testing methodologies | GPS tracking for drivers | Offline mode |
| Complete documentation | Google Play Store | Multi-language |

**Visual:** Roadmap timeline with three phases. Current phase highlighted in green.

---

## SLIDE 20 — Thank You

**Title:** Thank You — Questions?

**Content:**
- 🌐 `agroconecta.farm`
- 📂 `github.com/Brayanbii/Agroconecta`
- 📧 brayane13045@gmail.com

**Visual:** Green gradient background. Large "Thank You" text. Contact information below. Leaf icon.

---

## PRESENTATION NOTES FOR THE SPEAKER

### Tips for a Good Presentation:

1. **Practice the demo flow** before the presentation. Have all browser tabs open and logged in before starting.
2. **Speak slowly and clearly.** Explain each slide in simple terms.
3. **Use the numbers.** "379 tests, 91% success, 5 roles, 76 APIs" — numbers are memorable.
4. **Show the live demo** on Slide 18. It proves the system works.
5. **Be honest about limitations.** "The free plan limits us to 20 concurrent users. With $7/month, that goes to 100+."
6. **Have backup screenshots** in case the live demo has internet issues.
7. **End with confidence.** "This platform is ready to help Colombian farmers today."

### Answers to Expected Questions:

**Q: "Why 5 roles in one user table?"**
A: "Single-table inheritance simplifies queries and avoids JOINs. Role-specific fields are only populated for that role. It's a common pattern for systems with fewer than 10 roles."

**Q: "Why not a JavaScript framework like React?"**
A: "Server-side rendering with Thymeleaf is faster on low-end devices common in rural Colombia. No JavaScript download, no client-side rendering wait. The page arrives ready to read."

**Q: "Is the payment system real?"**
A: "Currently in sandbox mode. The full MercadoPago integration is implemented — it processes test payments correctly. Activating production mode requires MercadoPago business verification, which is planned for the next phase."

**Q: "Why two separate databases?"**
A: "MySQL is optimal for relational data (13 interconnected tables). MongoDB GridFS is optimal for binary files (images split into 255KB chunks). Separating them allows independent scaling — if image storage grows, we upgrade only MongoDB."

**Q: "How do you know the system works under load?"**
A: "We executed 321 real HTTP requests simulating up to 30 concurrent users performing the complete workflow: register, login, browse, add to cart, create order. 19 real users were registered. 19 real orders were created. The system maintains 98% success up to 20 users."
