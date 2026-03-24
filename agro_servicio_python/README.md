# 🌱 AgroConecta — Servicio Python (Precios + Gráficos)

Microservicio en Python (Flask) v3.0 que provee dos funcionalidades al sistema Java (Spring Boot) de AgroConecta:

1. **Precios de referencia** de productos agrícolas colombianos (SIPSA-DANE) para el formulario del campesino.
2. **Generación de gráficas estadísticas** con Matplotlib para el panel del administrador.

---

## 📋 ¿Qué hace este servicio?

1. **Precios agrícolas:** Intenta obtener datos reales del SIPSA (Sistema de Información de Precios del Sector Agropecuario) publicados por el DANE en [datos.gov.co](https://www.datos.gov.co). Si falla, activa un **modo de simulación dinámica** que cambia cada hora.
2. **Gráficas estadísticas:** Recibe datos de ventas desde Java, genera 3 gráficas con Matplotlib y las devuelve como imágenes Base64 dentro de un JSON.

---

## 🗂️ Estructura del servicio

```
agro_servicio_python/
├── servidor.py        ← Código principal del servidor Flask (v3.0)
├── requirements.txt   ← Dependencias Python (flask, requests, matplotlib)
└── README.md          ← Esta documentación
```

---

## ⚙️ Instalación

### Requisitos
- Python 3.8 o superior
- pip

### Instalar dependencias
```bash
pip install -r requirements.txt
```

O manualmente:
```bash
pip install flask requests matplotlib
```

> **Nota Windows:** Si tienes múltiples versiones de Python, usa el ejecutable completo:
> ```bash
> C:\Python313\python.exe -m pip install -r requirements.txt
> ```

---

## 🚀 Cómo ejecutar

```bash
cd AccesoUsuarios/agro_servicio_python
python servidor.py
```

Verás en consola:
```
============================================================
  Servidor Python de AgroConecta v3.0
  Fuente precios: SIPSA - DANE Colombia (datos.gov.co)
  Graficos: Matplotlib
  GET  http://localhost:5000/api/v1/precios
  GET  http://localhost:5000/api/v1/precios/estado
  POST http://localhost:5000/api/v1/graficos
============================================================
 * Running on http://127.0.0.1:5000
 * Running on http://0.0.0.0:5000
```

> **Primera vez:** Matplotlib construye su caché de fuentes al arrancar. Espera ~30 segundos hasta ver el mensaje `* Running on`.

---

## 🔗 Endpoints disponibles

### `GET /api/v1/precios`
Devuelve la lista de precios de productos agrícolas colombianos.
**Usado por:** `CampesinoController.java` → formulario de nuevo/editar producto.

**Respuesta exitosa:**
```json
{
  "status": "success",
  "data": [
    { "id": 1, "nombre": "Papa Sabanera",  "precio": 2500, "tendencia": "baja"    },
    { "id": 2, "nombre": "Yuca",           "precio": 1800, "tendencia": "estable" },
    { "id": 3, "nombre": "Tomate Chonto",  "precio": 3200, "tendencia": "alta"    },
    { "id": 4, "nombre": "Cebolla Junca",  "precio": 1500, "tendencia": "estable" },
    { "id": 5, "nombre": "Zanahoria",      "precio": 1200, "tendencia": "baja"    },
    { "id": 6, "nombre": "Plátano Hartón", "precio": 2000, "tendencia": "alta"    },
    { "id": 7, "nombre": "Arroz Blanco",   "precio": 3500, "tendencia": "estable" },
    { "id": 8, "nombre": "Maíz Amarillo",  "precio": 1100, "tendencia": "baja"    }
  ],
  "fuente": "SIPSA - DANE Colombia | 15/07/2025",
  "timestamp": "2025-07-15T10:30:00.000000"
}
```

**Campo `tendencia`:**
| Valor      | Significado                          |
|------------|--------------------------------------|
| `"alta"`   | Precio subió más del 4% vs. base     |
| `"baja"`   | Precio bajó más del 4% vs. base      |
| `"estable"`| Variación dentro del ±4%             |

---

### `GET /api/v1/precios/estado`
Verifica que el servicio Python está activo.
**Usado por:** Health check / diagnóstico.

**Respuesta:**
```json
{
  "status": "online",
  "servicio": "AgroConecta Python Service",
  "version": "3.0",
  "timestamp": "2025-07-15T10:30:00.000000"
}
```

---

### `POST /api/v1/graficos`
Recibe datos de ventas desde Java y devuelve 3 gráficas estadísticas en Base64.
**Usado por:** `DashboardController.java` → panel del administrador.

**Body (JSON enviado por Java):**
```json
{
  "productos": [
    { "nombre": "Tomate", "cantidad": 50, "total": 150000 },
    { "nombre": "Papa",   "cantidad": 30, "total":  90000 }
  ],
  "ventas_mes": [
    { "mes": "Ene", "total": 200000 },
    { "mes": "Feb", "total": 320000 }
  ],
  "estados": [
    { "estado": "PENDIENTE",  "cantidad": 5  },
    { "estado": "ENTREGADO",  "cantidad": 10 },
    { "estado": "CANCELADO",  "cantidad": 2  }
  ]
}
```

**Respuesta:**
```json
{
  "status": "success",
  "grafico_productos": "iVBORw0KGgoAAAANSUhEUgAA...",
  "grafico_meses":     "iVBORw0KGgoAAAANSUhEUgAA...",
  "grafico_estados":   "iVBORw0KGgoAAAANSUhEUgAA..."
}
```

Cada valor es una imagen PNG codificada en Base64. En el HTML se usa así:
```html
<img th:src="'data:image/png;base64,' + ${graficoProductos}" />
```

**Gráficas generadas:**
| Campo               | Tipo              | Contenido                        |
|---------------------|-------------------|----------------------------------|
| `grafico_productos` | Barras horizontales | Top productos más vendidos      |
| `grafico_meses`     | Barras verticales   | Ventas por mes en COP           |
| `grafico_estados`   | Torta (pie)         | Distribución de estados de pedidos |

---

## 🔄 Lógica de datos

### Precios (GET)
```
Solicitud de Java
       │
       ▼
┌─────────────────────────────┐
│  Intentar SIPSA (datos.gov) │  ← Timeout: 8 segundos
└─────────────────────────────┘
       │
   ¿Éxito?
   /       \
 SÍ         NO
  │          │
  ▼          ▼
Datos      Simulación
reales     dinámica
SIPSA      (cambia c/hora)
  │          │
  └────┬─────┘
       ▼
  JSON → Java → Thymeleaf → Campesino ve precios
```

### Gráficas (POST)
```
Java consulta BD (3 queries JPA)
       │
       ▼
Java hace POST con datos JSON
a http://localhost:5000/api/v1/graficos
       │
       ▼
Python genera 3 gráficas con Matplotlib
       │
       ▼
Python convierte cada figura:
  fig.savefig(buf, format='png')
  base64.b64encode(buf.read())
       │
       ▼
Python devuelve JSON con 3 strings Base64
       │
       ▼
Java pasa strings al modelo Thymeleaf
       │
       ▼
HTML: <img src="data:image/png;base64,...">
       │
       ▼
Admin ve las 3 gráficas en el dashboard
```

---

## 🏛️ Fuente de datos real

- **Nombre:** SIPSA — Sistema de Información de Precios del Sector Agropecuario
- **Entidad:** DANE (Departamento Administrativo Nacional de Estadística)
- **Portal:** [datos.gov.co](https://www.datos.gov.co)
- **API:** Socrata Open Data API (SODA) — acceso público sin autenticación
- **Endpoint:** `https://www.datos.gov.co/resource/ha6j-pa2r.json`

---

## 🔧 Integración con Java (Spring Boot)

### `PythonService.java` — Dos métodos
```java
@Service
public class PythonService {

    private static final String BASE_URL     = "http://localhost:5000";
    private static final String PRECIOS_URL  = BASE_URL + "/api/v1/precios";
    private static final String GRAFICOS_URL = BASE_URL + "/api/v1/graficos";

    // GET precios → para el formulario del campesino
    public Map<String, Object> obtenerPreciosDesdePython() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            return restTemplate.getForObject(PRECIOS_URL, Map.class);
        } catch (Exception e) {
            return null;
        }
    }

    // POST datos → recibe gráficas en Base64 → para el dashboard admin
    public Map<String, Object> generarGraficos(Map<String, Object> datos) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(datos, headers);
            return restTemplate.postForObject(GRAFICOS_URL, request, Map.class);
        } catch (Exception e) {
            return null;
        }
    }
}
```

### Controladores afectados
| Controlador             | Ruta                              | Función Python usada          |
|-------------------------|-----------------------------------|-------------------------------|
| `CampesinoController`   | `GET /campesino/productos/nuevo`  | `obtenerPreciosDesdePython()` |
| `CampesinoController`   | `GET /campesino/productos/editar/{id}` | `obtenerPreciosDesdePython()` |
| `DashboardController`   | `GET /admin/dashboard`            | `generarGraficos()`           |

### Degradación elegante (Graceful Degradation)
Si Python está apagado, **ningún método lanza excepción** — simplemente devuelven `null`.
- En el formulario del campesino: el bloque de precios no aparece (`th:if="${preciosReferencia}"`).
- En el dashboard del admin: las gráficas no aparecen y se muestra un aviso amarillo.

---

## ⚠️ Notas importantes

- **Orden de arranque:** Primero Python, luego Spring Boot (o al revés — Spring Boot tolera que Python esté apagado).
- **Puerto:** El servidor Python usa el puerto `5000`. Asegúrate de que no esté ocupado.
- **CORS:** No configurado (no necesario porque Java llama a Python desde el **backend**, no desde el navegador).
- **Primera vez con Matplotlib:** Al arrancar por primera vez, construye el caché de fuentes (~30 seg). Las siguientes veces arranca inmediatamente.
- **Producción:** Para producción, usar `gunicorn` en lugar de `app.run()`.
- **matplotlib.use('Agg'):** Obligatorio en servidores sin pantalla. Sin esto, Matplotlib intenta abrir una ventana gráfica y falla.

---

## 👨‍💻 Desarrollado por

**Brayan** — Proyecto AgroConecta  
Sistema de conexión entre campesinos y compradores en Colombia 🇨🇴
