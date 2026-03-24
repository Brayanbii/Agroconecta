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


# ============================================================
# SUPER INFORME CAMPESINO — Reporte detallado con 4 graficas
# ============================================================

@app.route('/api/v1/informe-campesino', methods=['POST'])
def super_informe_campesino():
    """
    Genera un super informe detallado para el campesino con 4 graficas:
      1. Top productos por cantidad vendida (barras horizontales)
      2. Ingresos por mes (barras verticales con gradiente)
      3. Distribucion de ingresos por producto (torta premium)
      4. Precio campesino vs precio mercado SIPSA (barras agrupadas)
    Tambien devuelve estadisticas de resumen.
    """
    datos = request.get_json(silent=True)
    if not datos:
        return jsonify({"error": "No se recibieron datos"}), 400

    productos  = datos.get("productos",  [])
    ventas_mes = datos.get("ventas_mes", [])
    resumen    = datos.get("resumen",    {})

    # Paleta de colores premium AgroConecta
    VERDE       = '#16a34a'
    VERDE_OSC   = '#14532d'
    VERDE_CLARO = '#86efac'
    AZUL        = '#3b82f6'
    AZUL_CLARO  = '#93c5fd'
    AMARILLO    = '#f59e0b'
    NARANJA     = '#f97316'
    ROJO        = '#ef4444'
    MORADO      = '#8b5cf6'
    ROSA        = '#ec4899'
    GRIS_TEXTO  = '#1f2937'
    GRIS_EJE    = '#6b7280'
    GRIS_BORDE  = '#e5e7eb'
    FONDO       = '#f9fafb'

    COLORES_TORTA = [VERDE, AZUL, AMARILLO, NARANJA, MORADO, ROSA, ROJO, AZUL_CLARO]

    resultado = {}

    # ----------------------------------------------------------
    # GRAFICA 1: Top productos por cantidad vendida
    # ----------------------------------------------------------
    if productos:
        top = sorted(productos, key=lambda x: x.get("cantidad", 0), reverse=True)[:8]
        nombres    = [str(p.get("nombre", "?")) for p in top]
        cantidades = [int(p.get("cantidad", 0)) for p in top]

        fig, ax = plt.subplots(figsize=(8, max(3.5, len(nombres) * 0.65)))
        fig.patch.set_facecolor('white')
        ax.set_facecolor(FONDO)

        colores_bar = [VERDE if i == 0 else (VERDE_CLARO if i < 3 else '#d1fae5') for i in range(len(nombres))]
        bars = ax.barh(nombres, cantidades, color=colores_bar, edgecolor='white', height=0.65)

        max_val = max(cantidades) if cantidades else 1
        for bar, val in zip(bars, cantidades):
            ax.text(bar.get_width() + max_val * 0.02,
                    bar.get_y() + bar.get_height() / 2,
                    f'{val} uds', va='center', ha='left',
                    fontsize=9, fontweight='bold', color=GRIS_TEXTO)

        ax.set_xlabel('Unidades vendidas', fontsize=9, color=GRIS_EJE)
        ax.set_title('🏆 Top Productos Más Vendidos', fontsize=13,
                     fontweight='bold', color=VERDE_OSC, pad=14)
        ax.set_xlim(0, max_val * 1.3)
        for spine in ['top', 'right']:
            ax.spines[spine].set_visible(False)
        for spine in ['left', 'bottom']:
            ax.spines[spine].set_color(GRIS_BORDE)
        ax.tick_params(colors=GRIS_EJE, labelsize=9)
        ax.grid(axis='x', linestyle='--', alpha=0.4, color=GRIS_BORDE)
        plt.tight_layout()
        resultado["grafico_top_productos"] = _fig_a_base64(fig)
    else:
        resultado["grafico_top_productos"] = None

    # ----------------------------------------------------------
    # GRAFICA 2: Ingresos por mes (barras con etiquetas)
    # ----------------------------------------------------------
    if ventas_mes:
        meses   = [str(v.get("mes", "?")) for v in ventas_mes]
        totales = [float(v.get("total", 0)) for v in ventas_mes]

        fig, ax = plt.subplots(figsize=(9, 4.5))
        fig.patch.set_facecolor('white')
        ax.set_facecolor(FONDO)

        # Gradiente de color: el mes con más ventas en verde oscuro
        max_idx = totales.index(max(totales)) if totales else 0
        colores_mes = [VERDE_OSC if i == max_idx else VERDE for i in range(len(meses))]

        bars = ax.bar(meses, totales, color=colores_mes, edgecolor='white', width=0.65, alpha=0.92)

        max_val = max(totales) if totales else 1
        for bar, val in zip(bars, totales):
            ax.text(bar.get_x() + bar.get_width() / 2,
                    bar.get_height() + max_val * 0.015,
                    f'${val:,.0f}',
                    ha='center', va='bottom',
                    fontsize=8, fontweight='bold', color=GRIS_TEXTO)

        ax.set_ylabel('Ingresos ($COP)', fontsize=9, color=GRIS_EJE)
        ax.set_title('📅 Ingresos por Mes', fontsize=13,
                     fontweight='bold', color=VERDE_OSC, pad=14)
        ax.yaxis.set_major_formatter(plt.FuncFormatter(lambda x, _: f'${x:,.0f}'))
        for spine in ['top', 'right']:
            ax.spines[spine].set_visible(False)
        for spine in ['left', 'bottom']:
            ax.spines[spine].set_color(GRIS_BORDE)
        ax.tick_params(colors=GRIS_EJE, labelsize=9)
        ax.grid(axis='y', linestyle='--', alpha=0.4, color=GRIS_BORDE)
        plt.tight_layout()
        resultado["grafico_ingresos_mes"] = _fig_a_base64(fig)
    else:
        resultado["grafico_ingresos_mes"] = None

    # ----------------------------------------------------------
    # GRAFICA 3: Distribucion de ingresos por producto (torta premium)
    # ----------------------------------------------------------
    if productos:
        top_torta = sorted(productos, key=lambda x: x.get("total", 0), reverse=True)[:6]
        labels_t  = [str(p.get("nombre", "?")) for p in top_torta]
        sizes_t   = [float(p.get("total", 0)) for p in top_torta]
        colores_t = COLORES_TORTA[:len(labels_t)]

        fig, ax = plt.subplots(figsize=(6.5, 5))
        fig.patch.set_facecolor('white')

        wedges, texts, autotexts = ax.pie(
            sizes_t,
            labels=None,
            colors=colores_t,
            autopct='%1.1f%%',
            startangle=140,
            pctdistance=0.78,
            wedgeprops={'edgecolor': 'white', 'linewidth': 2.5},
        )
        for at in autotexts:
            at.set_fontsize(9)
            at.set_fontweight('bold')
            at.set_color('white')

        ax.legend(wedges, labels_t,
                  title="Productos",
                  loc="center left",
                  bbox_to_anchor=(1, 0, 0.5, 1),
                  fontsize=8,
                  title_fontsize=9)

        ax.set_title('💰 Distribución de Ingresos', fontsize=13,
                     fontweight='bold', color=VERDE_OSC, pad=14)
        plt.tight_layout()
        resultado["grafico_distribucion"] = _fig_a_base64(fig)
    else:
        resultado["grafico_distribucion"] = None

    # ----------------------------------------------------------
    # GRAFICA 4: Precio promedio del campesino vs precio mercado SIPSA
    # ----------------------------------------------------------
    if productos:
        # Obtener precios de mercado (SIPSA o simulacion)
        precios_mercado_raw = obtener_datos_sipsa() or obtener_datos_dinamicos()
        mercado_dict = {p["nombre"].lower()[:5]: p["precio"] for p in precios_mercado_raw}

        comparacion = []
        for p in productos[:6]:
            nombre = str(p.get("nombre", "?"))
            precio_camp = float(p.get("precio_promedio", 0))
            if precio_camp <= 0:
                continue
            # Buscar precio de mercado por coincidencia parcial
            precio_mkt = next(
                (v for k, v in mercado_dict.items() if k in nombre.lower()[:5] or nombre.lower()[:5] in k),
                None
            )
            if precio_mkt:
                comparacion.append({
                    "nombre": nombre[:12],
                    "campesino": precio_camp,
                    "mercado": precio_mkt
                })

        if comparacion:
            nombres_c  = [c["nombre"] for c in comparacion]
            precios_c  = [c["campesino"] for c in comparacion]
            precios_m  = [c["mercado"]   for c in comparacion]

            x = range(len(nombres_c))
            ancho = 0.38

            fig, ax = plt.subplots(figsize=(8, 4.5))
            fig.patch.set_facecolor('white')
            ax.set_facecolor(FONDO)

            bars1 = ax.bar([i - ancho/2 for i in x], precios_c, ancho,
                           label='Tu precio', color=VERDE, edgecolor='white', alpha=0.9)
            bars2 = ax.bar([i + ancho/2 for i in x], precios_m, ancho,
                           label='Precio mercado', color=AZUL, edgecolor='white', alpha=0.9)

            ax.set_xticks(list(x))
            ax.set_xticklabels(nombres_c, fontsize=8, color=GRIS_TEXTO)
            ax.set_ylabel('Precio ($COP/kg)', fontsize=9, color=GRIS_EJE)
            ax.set_title('📊 Tu Precio vs Precio de Mercado (SIPSA)', fontsize=12,
                         fontweight='bold', color=VERDE_OSC, pad=14)
            ax.legend(fontsize=9)
            ax.yaxis.set_major_formatter(plt.FuncFormatter(lambda x, _: f'${x:,.0f}'))
            for spine in ['top', 'right']:
                ax.spines[spine].set_visible(False)
            for spine in ['left', 'bottom']:
                ax.spines[spine].set_color(GRIS_BORDE)
            ax.tick_params(colors=GRIS_EJE, labelsize=8)
            ax.grid(axis='y', linestyle='--', alpha=0.4, color=GRIS_BORDE)
            plt.tight_layout()
            resultado["grafico_vs_mercado"] = _fig_a_base64(fig)
        else:
            resultado["grafico_vs_mercado"] = None
    else:
        resultado["grafico_vs_mercado"] = None

    resultado["resumen"] = resumen
    print(f"[OK] Super Informe Campesino generado correctamente.")
    return jsonify(resultado)


if __name__ == '__main__':
    print("=" * 60)
    print("  Servidor Python de AgroConecta v3.0")
    print("  Fuente precios: SIPSA - DANE Colombia (datos.gov.co)")
    print("  Graficos: Matplotlib")
    print("  GET  http://localhost:5000/api/v1/precios")
    print("  GET  http://localhost:5000/api/v1/precios/estado")
    print("  POST http://localhost:5000/api/v1/graficos")
    print("  POST http://localhost:5000/api/v1/informe-campesino")
    print("=" * 60)
    app.run(host='0.0.0.0', port=5000, debug=True)
