# Plan de Integración Python → Java

## Tareas — Fase 1: Integración Java ↔ Python

- [x] Paso 1: Verificar `PythonService.java` — Ya existe y está correcto.
- [x] Paso 2: Actualizar `CampesinoController.java`
  - [x] Método `nuevoProducto()` → llamar a Python y pasar precios al modelo
  - [x] Método `editarProducto()` → llamar a Python y pasar precios al modelo
- [x] Paso 3: Actualizar `campesino_producto_form.html`
  - [x] Insertar bloque de precios de referencia entre el header verde y el `<form>`

## Tareas — Fase 2: Precios Reales de Colombia

- [x] Paso 4: Actualizar `servidor.py` con datos reales del SIPSA (datos.gov.co)
- [x] Paso 5: Crear `requirements.txt`
- [x] Paso 6: Instalar dependencias
- [x] Paso 7: Crear documentación `README.md`

## Tareas — Fase 3: Graficos Estadisticos para el Admin

- [x] Paso 8:  Agregar matplotlib a `requirements.txt` e instalar
- [x] Paso 9:  Agregar endpoint `POST /api/v1/graficos` en `servidor.py`
- [x] Paso 10: Agregar queries en `DetalleOrdenRepository.java`
- [x] Paso 11: Agregar queries en `OrdenRepository.java`
- [x] Paso 12: Agregar metodo `generarGraficos()` en `PythonService.java`
- [x] Paso 13: Actualizar `DashboardController.java` para enviar datos a Python
- [x] Paso 14: Actualizar `dashboard_admin.html` con los 3 graficos
- [x] Paso 15: Actualizar `EXPLICACION_PROFESOR.txt` con la nueva funcionalidad

## Tareas — Fase 4: Fix Borrar Producto + Informe Campesino

- [x] Paso 16: Fix `eliminarProducto()` → `@PostMapping` + `@Transactional` + desvincula FK
- [x] Paso 17: Agregar endpoint `POST /api/v1/informe-campesino` en `servidor.py` (4 gráficas)
- [x] Paso 18: Agregar método `generarInformeCampesino()` en `PythonService.java`
- [x] Paso 19: Agregar endpoint `GET /informe` en `CampesinoController.java`
  - [x] Agrupa ventas por producto y por mes
  - [x] Calcula resumen estadístico (ingresos, unidades, producto estrella, mejor mes)
  - [x] Envía datos a Python y recibe 4 gráficas base64
- [x] Paso 20: Crear `campesino_informe.html` con diseño premium
  - [x] Hero con KPIs en tiempo real
  - [x] 4 gráficas Matplotlib embebidas como base64
  - [x] Resumen estadístico detallado
  - [x] Aviso elegante si Python está apagado
- [x] Paso 21: Agregar "⚡ Botón Python" en navbar de `mis_productos.html`

## ✅ TODAS LAS FASES COMPLETADAS

## Tareas — Fase 5: Mejora Landing Main (Index)

- [x] Paso 22: Mejorar `index.html` manteniendo estética AgroConecta
- [x] Paso 23: Cambiar imagen de verduras/brócoli por imagen de papa
- [x] Paso 24: Subir nivel visual (hero, cards, CTA, microinteracciones)
- [x] Paso 25: Actualizar footer a: `© 2025 AgroConecta. Hecho con ❤️ en Bogotá, 🇨🇴.`
