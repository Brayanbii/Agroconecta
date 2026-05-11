package com.proyecto.AccesoUsuarios.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Service
public class PythonService {

    private final RestTemplate restTemplate = new RestTemplate();

    // ============================================================
    // 1. OBTENER PRECIOS (Migrado de Python a Java nativo)
    // ============================================================
    public Map<String, Object> obtenerPreciosDesdePython() {
        List<Map<String, Object>> baseProducts = Arrays.asList(
            createBaseProduct("Papa Sabanera", 2500, 400),
            createBaseProduct("Yuca", 1800, 300),
            createBaseProduct("Tomate Chonto", 3200, 600),
            createBaseProduct("Cebolla Junca", 1500, 250),
            createBaseProduct("Zanahoria", 1200, 200),
            createBaseProduct("Plátano Hartón", 2000, 300),
            createBaseProduct("Arroz Blanco", 3500, 250),
            createBaseProduct("Maíz Amarillo", 1100, 150)
        );

        List<Map<String, Object>> data = new ArrayList<>();
        // Semilla horaria: los precios cambian cada hora
        Random random = new Random((long) LocalDateTime.now().getHour() + LocalDateTime.now().getDayOfYear()); 
        
        int id = 1;
        for (Map<String, Object> prod : baseProducts) {
            String nombre = (String) prod.get("nombre");
            int base = (int) prod.get("precio_base");
            int var = (int) prod.get("variacion");
            
            int precioActual = base + (random.nextInt(var * 2 + 1) - var);
            precioActual = Math.max(500, Math.round(precioActual / 50.0f) * 50);
            
            String tendencia = "estable";
            double diff = (precioActual - base) / (double) base * 100;
            if (diff > 4) tendencia = "alta";
            else if (diff < -4) tendencia = "baja";

            Map<String, Object> item = new HashMap<>();
            item.put("id", id++);
            item.put("nombre", nombre);
            item.put("precio", precioActual);
            item.put("tendencia", tendencia);
            data.add(item);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", data);
        response.put("fuente", "Mercado AgroConecta | Actualizado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        response.put("timestamp", LocalDateTime.now().toString());
        return response;
    }

    private Map<String, Object> createBaseProduct(String name, int base, int var) {
        Map<String, Object> map = new HashMap<>();
        map.put("nombre", name);
        map.put("precio_base", base);
        map.put("variacion", var);
        return map;
    }

    // ============================================================
    // 2. GENERAR GRÁFICOS ADMIN (Migrado a Java nativo)
    // ============================================================
    public Map<String, Object> generarGraficos(Map<String, Object> datos) {
        if (datos == null) return new HashMap<>();
        
        List<Map<String, Object>> productos = (List<Map<String, Object>>) datos.getOrDefault("productos", new ArrayList<>());
        List<Map<String, Object>> ventasMes = (List<Map<String, Object>>) datos.getOrDefault("ventas_mes", new ArrayList<>());
        List<Map<String, Object>> estados = (List<Map<String, Object>>) datos.getOrDefault("estados", new ArrayList<>());

        Map<String, Object> resultado = new HashMap<>();

        // GRAFICO 1: Productos (Barras)
        if (!productos.isEmpty()) {
            productos.sort((a, b) -> Integer.compare(
                ((Number) b.getOrDefault("cantidad", 0)).intValue(),
                ((Number) a.getOrDefault("cantidad", 0)).intValue()
            ));
            List<String> labels = productos.stream().limit(8).map(p -> String.valueOf(p.get("nombre"))).collect(Collectors.toList());
            List<Integer> data = productos.stream().limit(8).map(p -> ((Number) p.getOrDefault("cantidad", 0)).intValue()).collect(Collectors.toList());
            resultado.put("grafico_productos", createChart("bar", labels, "Unidades vendidas", data));
        }

        // GRAFICO 2: Ventas Mes (Área)
        if (!ventasMes.isEmpty()) {
            List<String> labels = ventasMes.stream().map(v -> String.valueOf(v.get("mes"))).collect(Collectors.toList());
            List<Double> data = ventasMes.stream().map(v -> ((Number) v.getOrDefault("total", 0)).doubleValue()).collect(Collectors.toList());
            resultado.put("grafico_meses", createChart("area", labels, "Ingresos ($)", data));
        }

        // GRAFICO 3: Estados (Donut)
        if (!estados.isEmpty()) {
            List<String> labels = estados.stream().map(e -> String.valueOf(e.get("estado"))).collect(Collectors.toList());
            List<Integer> sizes = estados.stream().map(e -> ((Number) e.getOrDefault("cantidad", 0)).intValue()).collect(Collectors.toList());
            Map<String, Object> donut = new HashMap<>();
            donut.put("type", "donut");
            donut.put("labels", labels);
            donut.put("series", sizes);
            resultado.put("grafico_estados", donut);
        }

        return resultado;
    }

    // ============================================================
    // 3. INFORME CAMPESINO (Migrado a Java nativo)
    // ============================================================
    public Map<String, Object> generarInformeCampesino(Map<String, Object> datos) {
        if (datos == null) return new HashMap<>();
        
        List<Map<String, Object>> productos = (List<Map<String, Object>>) datos.getOrDefault("productos", new ArrayList<>());
        List<Map<String, Object>> ventasMes = (List<Map<String, Object>>) datos.getOrDefault("ventas_mes", new ArrayList<>());
        
        Map<String, Object> resultado = new HashMap<>();

        if (!productos.isEmpty()) {
            // Top Vendidos
            productos.sort((a, b) -> Integer.compare(
                ((Number) b.getOrDefault("cantidad", 0)).intValue(),
                ((Number) a.getOrDefault("cantidad", 0)).intValue()
            ));
            List<String> topLabels = productos.stream().limit(8).map(p -> String.valueOf(p.get("nombre"))).collect(Collectors.toList());
            List<Integer> topData = productos.stream().limit(8).map(p -> ((Number) p.getOrDefault("cantidad", 0)).intValue()).collect(Collectors.toList());
            resultado.put("grafico_top_productos", createChartBase(topLabels, "Unidades Vendidas", topData));

            // Distribución
            productos.sort((a, b) -> Double.compare(
                ((Number) b.getOrDefault("total", 0)).doubleValue(),
                ((Number) a.getOrDefault("total", 0)).doubleValue()
            ));
            List<String> distLabels = productos.stream().limit(6).map(p -> String.valueOf(p.get("nombre"))).collect(Collectors.toList());
            List<Double> distData = productos.stream().limit(6).map(p -> ((Number) p.getOrDefault("total", 0)).doubleValue()).collect(Collectors.toList());
            Map<String, Object> distMap = new HashMap<>();
            distMap.put("labels", distLabels);
            distMap.put("series", distData);
            resultado.put("grafico_distribucion", distMap);
            
            // Comparativa vs Mercado SIPSA
            List<Map<String, Object>> mercado = (List<Map<String, Object>>) obtenerPreciosDesdePython().get("data");
            List<String> vsLabels = new ArrayList<>();
            List<Double> vsCamp = new ArrayList<>();
            List<Double> vsMerc = new ArrayList<>();
            
            for (int i = 0; i < Math.min(6, productos.size()); i++) {
                String nombre = String.valueOf(productos.get(i).get("nombre"));
                double precioC = ((Number) productos.get(i).getOrDefault("precio_promedio", 0)).doubleValue();
                if (precioC <= 0) continue;
                
                String nombreCorto = nombre.toLowerCase().substring(0, Math.min(nombre.length(), 5));
                for (Map<String, Object> m : mercado) {
                    String mNombre = String.valueOf(m.get("nombre")).toLowerCase();
                    if (mNombre.contains(nombreCorto) || nombreCorto.contains(mNombre.substring(0, Math.min(mNombre.length(), 5)))) {
                        vsLabels.add(nombre.length() > 12 ? nombre.substring(0, 12) : nombre);
                        vsCamp.add(precioC);
                        vsMerc.add(((Number) m.get("precio")).doubleValue());
                        break;
                    }
                }
            }
            if (!vsLabels.isEmpty()) {
                Map<String, Object> vsMap = new HashMap<>();
                vsMap.put("labels", vsLabels);
                Map<String, Object> s1 = new HashMap<>(); s1.put("name", "Tu Precio"); s1.put("data", vsCamp);
                Map<String, Object> s2 = new HashMap<>(); s2.put("name", "Precio Mercado (SIPSA)"); s2.put("data", vsMerc);
                vsMap.put("series", Arrays.asList(s1, s2));
                resultado.put("grafico_vs_mercado", vsMap);
            }
        }

        if (!ventasMes.isEmpty()) {
            List<String> labels = ventasMes.stream().map(v -> String.valueOf(v.get("mes"))).collect(Collectors.toList());
            List<Double> data = ventasMes.stream().map(v -> ((Number) v.getOrDefault("total", 0)).doubleValue()).collect(Collectors.toList());
            resultado.put("grafico_ingresos_mes", createChartBase(labels, "Ingresos COP", data));
        }

        resultado.put("resumen", datos.get("resumen"));
        return resultado;
    }

    // ============================================================
    // 4. RUTAS OSRM (Migrado a Java nativo)
    // ============================================================
    public Map<String, Object> calcularRutaLogistica(Map<String, Object> coordenadas) {
        try {
            Map<String, Object> origen = (Map<String, Object>) coordenadas.get("origen");
            Map<String, Object> destino = (Map<String, Object>) coordenadas.get("destino");
            if (origen == null || destino == null) return null;
            
            String originLon = String.valueOf(origen.get("lon"));
            String originLat = String.valueOf(origen.get("lat"));
            String destLon = String.valueOf(destino.get("lon"));
            String destLat = String.valueOf(destino.get("lat"));
            
            String url = String.format(Locale.US, "http://router.project-osrm.org/route/v1/driving/%s,%s;%s,%s?overview=full&geometries=geojson", 
                                       originLon, originLat, destLon, destLat);
                                       
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && "Ok".equals(response.get("code"))) {
                List<Map<String, Object>> routes = (List<Map<String, Object>>) response.get("routes");
                if (routes != null && !routes.isEmpty()) {
                    Map<String, Object> route = routes.get(0);
                    double distanceKm = ((Number) route.get("distance")).doubleValue() / 1000.0;
                    double durationMin = ((Number) route.get("duration")).doubleValue() / 60.0;
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("status", "success");
                    result.put("distancia_km", Math.round(distanceKm * 100.0) / 100.0);
                    result.put("duracion_min", (int) durationMin);
                    result.put("geometria", route.get("geometry"));
                    return result;
                }
            }
        } catch (Exception e) {
            System.out.println("Error obteniendo ruta OSRM desde Java: " + e.getMessage());
        }
        return null;
    }

    // Helpers para JSON nativo
    private Map<String, Object> createChart(String type, List<String> labels, String seriesName, List<?> data) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("labels", labels);
        Map<String, Object> series = new HashMap<>();
        series.put("name", seriesName);
        series.put("data", data);
        map.put("series", Arrays.asList(series));
        return map;
    }
    
    private Map<String, Object> createChartBase(List<String> labels, String seriesName, List<?> data) {
        Map<String, Object> map = new HashMap<>();
        map.put("labels", labels);
        Map<String, Object> series = new HashMap<>();
        series.put("name", seriesName);
        series.put("data", data);
        map.put("series", Arrays.asList(series));
        return map;
    }
}
