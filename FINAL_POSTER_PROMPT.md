# POSTER DESIGN PROMPT — AGROCONECTA

## Instructions for generating a professional academic poster

Use this prompt with any AI image generator (DALL-E, Midjourney, Stable Diffusion) or design tool (Canva, PowerPoint, Adobe Illustrator) to create the final project poster.

---

## PROMPT FOR AI IMAGE GENERATOR

```
Professional academic poster for a software engineering final project.
Title: "AgroConecta — Agricultural Connection Platform".
Subtitle: "A web platform connecting Colombian farmers directly with consumers".

Design style: Clean, modern, academic. Green and earth color palette 
(#16a34a, #15803d, #f0fdf4, #1a1a2e). White background with green accents.

Layout: Vertical A0 size (841mm × 1189mm), portrait orientation.

Sections from top to bottom:
1. Header bar: dark green (#15803d) with white text "AgroConecta" and leaf icon
2. Student name and institution (SENA, June 2026)
3. "The Problem" box: 3 columns — Intermediaries, Limited Access, No Price Info
4. "Our Solution" box: Farmer → AgroConecta → Consumer diagram
5. "5 User Roles" cards: Admin, Support, Farmer, Client, Driver
6. "Technical Architecture" diagram: Browser → Spring Boot → MySQL + MongoDB
7. "Technology Stack" icons: Java, Spring Boot, Docker, MySQL, MongoDB, Thymeleaf
8. "Testing Results" graph: 4 bars showing 89%, 100%, 75%, 92%
9. "Key Metrics" row: 69 files, 57 templates, 76 APIs, 379 tests, 10 docs
10. Footer bar: agroconecta.farm | github.com/Brayanbii/Agroconecta | SENA 2026

Visual style: Flat design with rounded corners, subtle shadows, sans-serif font.
Icons for each section. No photos, only illustrations and diagrams.
Professional and academic, not commercial.
```

---

## MANUAL CANVA / POWERPOINT INSTRUCTIONS

If generating with AI is not possible, follow these steps to build the poster manually:

### Template
- Size: A0 (84.1 cm × 118.9 cm) or Tabloid (28 cm × 43 cm for printing)
- Background: White `#FFFFFF`
- Primary color: Green `#16a34a`
- Secondary color: Dark green `#15803d`
- Accent: Light green `#f0fdf4`
- Text: Dark gray `#1a1a2e`
- Font: Inter, Plus Jakarta Sans, or Montserrat (bold for titles, regular for body)

### Layout (Portrait, 9 Rows)

**ROW 1 — HEADER (15% height)**
- Full-width dark green bar `#15803d`
- Left: Large leaf icon (Font Awesome `fa-leaf`)
- Center: "AGROCONECTA" in white, bold, 72pt
- Right: "Agricultural Connection Platform" in white, 28pt
- Below header (outside bar): "Brayan Bareño · SENA · June 2026" centered, 18pt

**ROW 2 — PROBLEM & SOLUTION (20% height)**
- Left half: "THE PROBLEM" with 3 columns
  - Column 1: 🏪 icon, "Intermediaries", "3-5 resellers between farmer and consumer. Farmer receives smallest share."
  - Column 2: 📍 icon, "Limited Access", "Farmers can only sell at local markets. No urban consumer reach."
  - Column 3: ❓ icon, "No Price Info", "Farmers don't know real market prices. Forced to accept unfair offers."
- Right half: "OUR SOLUTION" with diagram
  - Farmer emoji → AgroConecta logo → Consumer emoji
  - Text: "One platform. Direct connection. Fair prices for everyone."

**ROW 3 — USER ROLES (10% height)**
- 5 horizontal cards with icons, role name, and one-line description:
  - 🔧 Admin (supervision) | 💬 Support (help desk) | 🌱 Farmer (sell products) | 🛒 Client (buy products) | 🚚 Driver (delivery)

**ROW 4 — ARCHITECTURE (15% height)**
- Title: "TECHNICAL ARCHITECTURE"
- Three-layer diagram (left to right arrows):
  - Box 1: "Browser / Mobile" (Thymeleaf + Bootstrap 5 + Tailwind CSS)
  - Arrow → Box 2: "Spring Boot 3.5.7" (Java 17, Docker, Alpine Linux)
  - Two arrows down → Box 3a: "MySQL (Aiven)" + Box 3b: "MongoDB Atlas"

**ROW 5 — TECHNOLOGY STACK (10% height)**
- Title: "TECHNOLOGY STACK"
- Grid of 8 technology icons with labels:
  - Java 17 | Spring Boot | Docker | MySQL | MongoDB | Thymeleaf | Bootstrap | Git

**ROW 6 — TESTING RESULTS (15% height)**
- Title: "TESTING & VALIDATION — 379 EXECUTIONS"
- 4 horizontal bars (like a bar chart):
  - Postman API: 89.3% (25/28 passed) — green bar at 89%
  - Selenium UI: 100% (10/10 passed) — full green bar
  - Integration: 75% (15/20 passed) — green bar at 75%
  - Load Testing: 92% (295/321 passed) — green bar at 92%
- Total: "91% OVERALL SUCCESS RATE"

**ROW 7 — KEY METRICS (8% height)**
- 5 large numbers in a row:
  - "69" below: "Java files"
  - "57" below: "Templates"
  - "76" below: "API endpoints"
  - "379" below: "Tests"
  - "10" below: "Documents"

**ROW 8 — DATABASE (10% height)**
- Title: "DATABASE DESIGN"
- Left: MySQL table list (13 tables: usuario, producto, orden, detalle_orden, ruta, direccion, resena, favorito_producto, favorito_campesino, ticket_soporte, mensaje_soporte, notificacion, contacto_horeca)
- Right: MongoDB GridFS (fs.files + fs.chunks for image storage)

**ROW 9 — FOOTER (7% height)**
- Full-width dark green bar `#15803d`
- Left: "agroconecta.farm" (white)
- Center: "github.com/Brayanbii/Agroconecta" (white)
- Right: "SENA — 2026" (white)

---

## CANVA QUICK START

1. Go to canva.com → Search "Academic Poster A0"
2. Select a clean template (white background)
3. Replace colors with the green palette
4. Use "Elements" → "Charts" for the testing results bar chart
5. Use "Elements" → "Shapes" for boxes and diagrams
6. Use "Text" for titles (Inter or Montserrat font)
7. Export as PDF (Print quality)

---

## TIPS FOR A GOOD POSTER

- **Less text, more visuals.** The poster should be readable from 2 meters away
- **Use the green color consistently.** Headers, icons, and chart bars should all use the same green
- **Big numbers are eye-catching.** The metrics row (69, 57, 76, 379, 10) draws attention
- **The architecture diagram is the centerpiece.** Make it the largest visual element
- **Include the QR code.** Generate a QR code for `agroconecta.farm` and place it in the footer
- **Print in high quality.** A0 size needs 300 DPI. Use a professional printing service
- **Test readability.** Print a small version (A4) first. Can you read everything from 1 meter away?
