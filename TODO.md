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

- [ ] Paso 8:  Agregar matplotlib a `requirements.txt` e instalar
- [ ] Paso 9:  Agregar endpoint `POST /api/v1/graficos` en `servidor.py`
- [ ] Paso 10: Agregar queries en `DetalleOrdenRepository.java`
- [ ] Paso 11: Agregar queries en `OrdenRepository.java`
- [ ] Paso 12: Agregar metodo `generarGraficos()` en `PythonService.java`
- [ ] Paso 13: Actualizar `DashboardController.java` para enviar datos a Python
- [ ] Paso 14: Actualizar `dashboard_admin.html` con los 3 graficos
- [ ] Paso 15: Actualizar `EXPLICACION_PROFESOR.txt` con la nueva funcionalidad
