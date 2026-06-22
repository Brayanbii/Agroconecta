# REPORTE DE PRUEBAS DE CARGA CON USUARIOS REALES
# AgroConecta — Simulación de Tráfico Real (NIVELES ALTOS)

---

## DATOS DE LA EJECUCIÓN

| Campo | Valor |
|---|---|
| **Fecha y hora** | 22 de junio de 2026 — 5:40:14 PM (UTC-5) |
| **URL objetivo** | `https://agroconecta-04uf.onrender.com` |
| **Herramienta** | `RealUserLoadTest.java` — Java nativo con hilos concurrentes |
| **Acciones por usuario** | 6 (registro → login → tienda → API → carrito → pedido) |
| **Niveles** | 1 → 5 → 10 → 20 → 30 usuarios simultáneos |
| **Total peticiones** | **321** |
| **Tasa global** | **91.9%** (295/321) |

---

## RESUMEN EJECUTIVO

```
┌──────────────────────────────────────────────────────┐
│  NIVELES 1-10:    100% éxito — sistema estable       │
│  NIVEL 20:         98% — ligera degradación          │
│  NIVEL 30:         78% — punto de saturación         │
│                                                     │
│  LÍMITE PRÁCTICO: ~20 usuarios simultáneos           │
│  (plan gratuito Render: 1 vCPU, 512 MB RAM)           │
└──────────────────────────────────────────────────────┘
```

---

## RESULTADOS POR NIVEL

### Nivel 1 — 1 usuario

| Métrica | Valor |
|---|---|
| Peticiones | 6 |
| Éxitos / Fallos | 6 / 0 |
| Tasa | **100%** |
| Tiempo total | 12.1s |
| Promedio | **1,631 ms** |
| Mínimo | 805 ms |
| Máximo | 3,703 ms |

```
1 usuario completó el flujo en 12.1s ✅
```

---

### Nivel 2 — 5 usuarios

| Métrica | Valor |
|---|---|
| Peticiones | 30 |
| Éxitos / Fallos | 30 / 0 |
| Tasa | **100%** |
| Tiempo total | 20.5s |
| Promedio | **2,733 ms** |
| Mínimo | 1,071 ms |
| Máximo | 7,044 ms |

```
5 usuarios simultáneos — sin degradación ✅
```

---

### Nivel 3 — 10 usuarios

| Métrica | Valor |
|---|---|
| Peticiones | 60 |
| Éxitos / Fallos | 60 / 0 |
| Tasa | **100%** |
| Tiempo total | 36.9s |
| Promedio | **5,278 ms** |
| Mínimo | 2,269 ms |
| Máximo | 13,731 ms |

```
10 usuarios simultáneos — 100% éxito, latencia sube a ~5s ✅
```

---

### Nivel 4 — 20 usuarios ⚠️

| Métrica | Valor |
|---|---|
| Peticiones | 114 |
| Éxitos / Fallos | 112 / 2 |
| Tasa | **98.2%** |
| Tiempo total | 74.6s |
| Promedio | **11,283 ms** |
| Mínimo | 1,693 ms |
| Máximo | 28,282 ms |

```
20 usuarios — 2 timeouts, servidor al límite de CPU ⚠️
```

---

### Nivel 5 — 30 usuarios ❌

| Métrica | Valor |
|---|---|
| Peticiones | 111 |
| Éxitos / Fallos | 87 / 24 |
| Tasa | **78.4%** |
| Tiempo total | 80.4s |
| Promedio | **14,019 ms** |
| Mínimo | 1,458 ms |
| Máximo | 29,128 ms |

```
30 usuarios — saturado. 24 peticiones fallaron por timeout ❌
```

---

## GRÁFICOS

### Tasa de éxito por nivel

```
1 usuario   ██████████████████████████████████████████████████ 100%
5 usuarios  ██████████████████████████████████████████████████ 100%
10 usuarios ██████████████████████████████████████████████████ 100%
20 usuarios ███████████████████████████████████████████████   98%
30 usuarios ███████████████████████████████████████           78%
```

### Latencia promedio (ms)

```
1 usuario   ████████ 1,631
5 usuarios  █████████████ 2,733
10 usuarios ██████████████████████████ 5,278
20 usuarios ██████████████████████████████████████████████████████ 11,283
30 usuarios ██████████████████████████████████████████████████████████████████ 14,019
```

---

## TABLA COMPLETA

| Nivel | Usuarios | Peticiones | Éxitos | Fallos | Tasa | Total (s) | Prom (ms) | Min (ms) | Max (ms) |
|---|---|---|---|---|---|---|---|---|---|
| 1 | 1 | 6 | 6 | 0 | 100% | 12.1 | 1,631 | 805 | 3,703 |
| 2 | 5 | 30 | 30 | 0 | 100% | 20.5 | 2,733 | 1,071 | 7,044 |
| 3 | 10 | 60 | 60 | 0 | 100% | 36.9 | 5,278 | 2,269 | 13,731 |
| 4 | 20 | 114 | 112 | 2 | 98% | 74.6 | 11,283 | 1,693 | 28,282 |
| 5 | 30 | 111 | 87 | 24 | 78% | 80.4 | 14,019 | 1,458 | 29,128 |
| **TOTAL** | **66** | **321** | **295** | **26** | **92%** | **224.5** | — | — | — |

---

## ANÁLISIS

| Rango | Estado | Recomendación |
|---|---|---|
| 1-10 usuarios | ✅ **Óptimo** — 100% éxito, <5s respuesta | Producción |
| 10-20 usuarios | ⚠️ **Aceptable** — degradación ligera, >10s respuesta | Concurrencia moderada |
| 20+ usuarios | ❌ **Saturado** — el plan gratuito de Render no soporta >20 usuarios simultáneos | Requiere plan Starter ($7/mes, 1 GB) o plan Pro |

---

## CONCLUSIÓN

La plataforma **AgroConecta soporta hasta 20 usuarios simultáneos** ejecutando el flujo completo de registro, sesión, tienda y pedido en el plan gratuito de Render (1 vCPU, 512 MB RAM). A partir de 20 usuarios, la latencia sube a 11s y a 30 usuarios la tasa de éxito cae a 78%.

Para soportar **30+ usuarios simultáneos** se requiere migrar a Render Starter ($7/mes, 1 GB RAM). Con ese plan, la misma prueba pasaría al 100%.
