from flask import Flask, jsonify, request
import requests
import random
import io
import base64
import matplotlib
matplotlib.use('Agg')  # Backend sin pantalla (para servidor)
import matplotlib.pyplot as plt
from datetime import datetime

app = Flask(__name__)

# ============================================================
# FUENTE REAL: SIPSA - Sistema de Información de Precios
# del Sector Agropecuario — DANE Colombia
# API pública en: https://www.datos.gov.co
# ============================================================
SIPSA_API_URL = "https://www.datos.gov.co/resource/ha6j-pa2r.json"

# Palabras clave para filtrar productos del SIPSA
PRODUCTOS_INTERES = {
    "papa":      "Papa Sabanera",
    "yuca":      "Yuca",
    "tomate":    "Tomate Chonto",
    "cebolla":   "Cebolla Junca",
    "zanahoria": "Zanahoria",
    "platano":   "Plátano Hartón",
    "arroz":     "Arroz Blanco",
    "maiz":      "Maíz Amarillo",
}

# Precios base de referencia (para el fallback dinámico)
PRODUCTOS_BASE = [
    {"nombre": "Papa Sabanera",  "precio_base": 2500, "variacion": 400},
    {"nombre": "Yuca",           "precio_base": 1800, "variacion": 300},
    {"nombre": "Tomate Chonto",  "precio_base": 3200, "variacion": 600},
    {"nombre": "Cebolla Junca",  "precio_base": 1500, "variacion": 250},
    {"nombre": "Zanahoria",      "precio_base": 1200, "variacion": 200},
    {"nombre": "Plátano Hartón", "precio_base": 2000, "variacion": 300},
    {"nombre": "Arroz Blanco",   "precio_base": 3500, "variacion": 250},
    {"nombre": "Maíz Amarillo",  "precio_base": 1100, "variacion": 150},
]


def calcular_tendencia(precio_actual, precio_base):
    """Calcula si el precio está subiendo, bajando o estable respecto al base."""
    diff_pct = (precio_actual - precio_base) / precio_base * 100
    if diff_pct > 4:
        return "alta"
    elif diff_pct < -4:
        return "baja"
    else:
        return "estable"


def obtener_datos_sipsa():
    """
    Intenta obtener precios reales del SIPSA desde datos.gov.co.
    Retorna lista de productos o None si falla.
    """
    try:
        params = {
            "$limit": 300,
            "$order": "fecha DESC",
        }
        response = requests.get(SIPSA_API_URL, params=params, timeout=8)

        if response.status_code != 200:
            print(f"[!] SIPSA respondio con codigo {response.status_code}")
            return None

        datos_raw = response.json()
        if not datos_raw:
            return None

        # Agrupar precios por producto de interés
        precios_agrupados = {}
        for item in datos_raw:
            # El campo del nombre puede variar según el dataset
            nombre_raw = (
                item.get("articulo") or
                item.get("producto") or
                item.get("nombre_articulo") or
                item.get("nombre") or ""
            ).lower()

            # El campo del precio puede variar
            precio_raw = (
                item.get("precio") or
                item.get("precio_kg") or
                item.get("precio_promedio") or
                item.get("precio_minorista") or "0"
            )

            if not nombre_raw:
                continue

            try:
                precio = float(str(precio_raw).replace(",", "."))
                if precio <= 0:
                    continue
            except (ValueError, TypeError):
                continue

            # Buscar si este artículo corresponde a algún producto de interés
            for clave, nombre_display in PRODUCTOS_INTERES.items():
                if clave in nombre_raw:
                    if nombre_display not in precios_agrupados:
                        precios_agrupados[nombre_display] = []
                    precios_agrupados[nombre_display].append(precio)
                    break

        if not precios_agrupados:
            print("[!] SIPSA no devolvio productos reconocibles.")
            return None

        # Construir resultado final con promedios
        resultado = []
        for i, (nombre, precios) in enumerate(precios_agrupados.items()):
            precio_promedio = sum(precios) / len(precios)
            precio_redondeado = max(500, round(precio_promedio / 50) * 50)

            # Buscar precio base para calcular tendencia
            precio_base = next(
                (p["precio_base"] for p in PRODUCTOS_BASE
                 if p["nombre"].lower()[:4] == nombre.lower()[:4]),
                precio_redondeado
            )

            resultado.append({
                "id": i + 1,
                "nombre": nombre,
                "precio": int(precio_redondeado),
                "tendencia": calcular_tendencia(precio_redondeado, precio_base)
            })

        print(f"[OK] SIPSA: {len(resultado)} productos obtenidos correctamente.")
        return resultado

    except requests.exceptions.Timeout:
        print("[!] SIPSA: Tiempo de espera agotado.")
        return None
    except requests.exceptions.ConnectionError:
        print("[!] SIPSA: Sin conexion a internet.")
        return None
    except Exception as e:
        print(f"[!] SIPSA: Error inesperado - {e}")
        return None


def obtener_datos_dinamicos():
    """
    Genera precios dinámicos que cambian cada hora.
    Usa la hora actual como semilla → reproducibles pero variables.
    Simula el comportamiento real del mercado colombiano.
    """
    # Semilla = año + mes + día + hora → cambia cada hora
    seed = int(datetime.now().strftime("%Y%m%d%H"))
    random.seed(seed)

    resultado = []
    for i, producto in enumerate(PRODUCTOS_BASE):
        variacion = random.randint(-producto["variacion"], producto["variacion"])
        precio_actual = producto["precio_base"] + variacion
        # Redondear al múltiplo de 50 más cercano (como en mercados reales)
        precio_actual = max(500, round(precio_actual / 50) * 50)

        resultado.append({
            "id": i + 1,
            "nombre": producto["nombre"],
            "precio": precio_actual,
            "tendencia": calcular_tendencia(precio_actual, producto["precio_base"])
        })

    print(f"[~] Simulacion dinamica: {len(resultado)} productos generados.")
    return resultado


# ============================================================
# ENDPOINTS
# ============================================================

@app.route('/api/v1/precios', methods=['GET'])
def get_precios():
    """
    Endpoint principal. Devuelve precios de productos agrícolas colombianos.
    Intenta datos reales del SIPSA; si falla, usa simulación dinámica.
    """
    print(f"\n>>> [{datetime.now().strftime('%d/%m/%Y %H:%M:%S')}] Solicitud de precios recibida desde AgroConecta")

    # 1. Intentar datos reales del SIPSA (datos.gov.co)
    datos = obtener_datos_sipsa()

    if datos:
        fuente = f"SIPSA - DANE Colombia | {datetime.now().strftime('%d/%m/%Y')}"
        print(f"[OK] Usando datos reales del SIPSA.")
    else:
        # 2. Fallback: simulacion dinamica (cambia cada hora)
        datos = obtener_datos_dinamicos()
        fuente = f"Mercado AgroConecta | Actualizado: {datetime.now().strftime('%d/%m/%Y %H:%M')}"
        print(f"[~] Usando simulacion dinamica.")

    return jsonify({
        "status": "success",
        "data": datos,
        "fuente": fuente,
        "timestamp": datetime.now().isoformat()
    })


@app.route('/api/v1/precios/estado', methods=['GET'])
def estado():
    """Endpoint de salud — verifica que el servicio Python esta activo."""
    return jsonify({
        "status": "online",
        "servicio": "AgroConecta Python Service",
        "version": "3.0",
        "timestamp": datetime.now().isoformat()
    })


# ============================================================
# GRAFICOS ESTADISTICOS — Matplotlib
# ============================================================

def _fig_a_base64(fig):
    """Convierte una figura Matplotlib a string base64 PNG."""
    buf = io.BytesIO()
    fig.savefig(buf, format='png', bbox_inches='tight', dpi=100, facecolor='white')
    buf.seek(0)
    img_b64 = base64.b64encode(buf.read()).decode('utf-8')
    buf.close()
    plt.close(fig)
    return img_b64


@app.route('/api/v1/graficos', methods=['POST'])
def generar_graficos():
    """
    Recibe datos de ventas desde Java (JSON) y genera 3 graficos:
      1. Barras horizontales: Top productos mas vendidos
      2. Barras verticales:   Ventas totales por mes
      3. Torta:               Distribucion de pedidos por estado
    Devuelve las imagenes codificadas en base64.
    """
    datos = request.get_json(silent=True)
    if not datos:
        return jsonify({"error": "No se recibieron datos"}), 400

    productos  = datos.get("productos",  [])
    ventas_mes = datos.get("ventas_mes", [])
    estados    = datos.get("estados",    [])

    # Paleta de colores AgroConecta
    VERDE      = '#16a34a'
    VERDE_CLARO= '#86efac'
    AZUL       = '#3b82f6'
    AMARILLO   = '#f59e0b'
    ROJO       = '#ef4444'
    MORADO     = '#8b5cf6'
    GRIS_TEXTO = '#374151'
    GRIS_EJE   = '#6b7280'
    GRIS_BORDE = '#e5e7eb'

    resultado = {}

    # ----------------------------------------------------------
    # GRAFICO 1: Top productos mas vendidos (barras horizontales)
    # ----------------------------------------------------------
    if productos:
        top = productos[:8]
        nombres   = [str(p.get("nombre", "?")) for p in top]
        cantidades = [int(p.get("cantidad", 0)) for p in top]

        fig, ax = plt.subplots(figsize=(7, max(3, len(nombres) * 0.55)))
        fig.patch.set_facecolor('white')

        colores = [VERDE if i == 0 else VERDE_CLARO for i in range(len(nombres))]
        bars = ax.barh(nombres, cantidades, color=colores, edgecolor='white', height=0.6)

        max_val = max(cantidades) if cantidades else 1
        for bar, val in zip(bars, cantidades):
            ax.text(bar.get_width() + max_val * 0.02,
                    bar.get_y() + bar.get_height() / 2,
                    str(val), va='center', ha='left',
                    fontsize=9, fontweight='bold', color=GRIS_TEXTO)

        ax.set_xlabel('Unidades vendidas', fontsize=9, color=GRIS_EJE)
        ax.set_title('Productos Mas Vendidos', fontsize=12,
                     fontweight='bold', color='#111827', pad=12)
        ax.set_xlim(0, max_val * 1.25)
        for spine in ['top', 'right']:
            ax.spines[spine].set_visible(False)
        for spine in ['left', 'bottom']:
            ax.spines[spine].set_color(GRIS_BORDE)
        ax.tick_params(colors=GRIS_EJE, labelsize=8)
        plt.tight_layout()
        resultado["grafico_productos"] = _fig_a_base64(fig)
    else:
        resultado["grafico_productos"] = None

    # ----------------------------------------------------------
    # GRAFICO 2: Ventas por mes (barras verticales)
    # ----------------------------------------------------------
    if ventas_mes:
        meses   = [str(v.get("mes", "?")) for v in ventas_mes]
        totales = [float(v.get("total", 0)) for v in ventas_mes]

        fig, ax = plt.subplots(figsize=(7, 4))
        fig.patch.set_facecolor('white')

        bars = ax.bar(meses, totales, color=VERDE, edgecolor='white', width=0.6, alpha=0.9)

        max_val = max(totales) if totales else 1
        for bar, val in zip(bars, totales):
            ax.text(bar.get_x() + bar.get_width() / 2,
                    bar.get_height() + max_val * 0.01,
                    f'${val:,.0f}',
                    ha='center', va='bottom',
                    fontsize=8, fontweight='bold', color=GRIS_TEXTO)

        ax.set_ylabel('Total ($COP)', fontsize=9, color=GRIS_EJE)
        ax.set_title('Ventas por Mes', fontsize=12,
                     fontweight='bold', color='#111827', pad=12)
        ax.yaxis.set_major_formatter(
            plt.FuncFormatter(lambda x, _: f'${x:,.0f}'))
        for spine in ['top', 'right']:
            ax.spines[spine].set_visible(False)
        for spine in ['left', 'bottom']:
            ax.spines[spine].set_color(GRIS_BORDE)
        ax.tick_params(colors=GRIS_EJE, labelsize=8)
        plt.tight_layout()
        resultado["grafico_meses"] = _fig_a_base64(fig)
    else:
        resultado["grafico_meses"] = None

    # ----------------------------------------------------------
    # GRAFICO 3: Estado de pedidos (torta)
    # ----------------------------------------------------------
    if estados:
        labels = [str(e.get("estado", "Sin estado")) for e in estados]
        sizes  = [int(e.get("cantidad", 0)) for e in estados]
        colores_torta = [VERDE, AZUL, AMARILLO, ROJO, MORADO][:len(labels)]

        fig, ax = plt.subplots(figsize=(5, 4))
        fig.patch.set_facecolor('white')

        wedges, texts, autotexts = ax.pie(
            sizes,
            labels=labels,
            colors=colores_torta,
            autopct='%1.0f%%',
            startangle=90,
            wedgeprops={'edgecolor': 'white', 'linewidth': 2},
            textprops={'fontsize': 9, 'color': GRIS_TEXTO}
        )
        for at in autotexts:
            at.set_fontsize(9)
            at.set_fontweight('bold')
            at.set_color('white')

        ax.set_title('Estado de Pedidos', fontsize=12,
                     fontweight='bold', color='#111827', pad=12)
        plt.tight_layout()
        resultado["grafico_estados"] = _fig_a_base64(fig)
    else:
        resultado["grafico_estados"] = None

    print(f"[OK] Graficos generados correctamente.")
    return jsonify(resultado)


if __name__ == '__main__':
    print("=" * 60)
    print("  Servidor Python de AgroConecta v3.0")
    print("  Fuente precios: SIPSA - DANE Colombia (datos.gov.co)")
    print("  Graficos: Matplotlib")
    print("  GET  http://localhost:5000/api/v1/precios")
    print("  GET  http://localhost:5000/api/v1/precios/estado")
    print("  POST http://localhost:5000/api/v1/graficos")
    print("=" * 60)
    app.run(host='0.0.0.0', port=5000, debug=True)
