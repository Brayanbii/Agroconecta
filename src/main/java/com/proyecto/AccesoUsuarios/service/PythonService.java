package com.proyecto.AccesoUsuarios.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.springframework.scheduling.annotation.Scheduled;
import jakarta.annotation.PostConstruct;

@Service
public class PythonService {

    private final RestTemplate restTemplate = new RestTemplate();

    // ============================================================
    // 1. OBTENER PRECIOS (Desde SIPSA DANE Python script con Caché)
    // ============================================================
    
    private volatile List<Map<String, Object>> cacheSipsa = null;
    private volatile LocalDateTime ultimaActualizacion = null;
    private volatile boolean actualizando = false;

    /**
     * Al arrancar el servidor, poblar el caché en segundo plano
     * para que la primera petición del usuario NO se bloquee.
     */
    @PostConstruct
    public void inicializarCacheAlArrancar() {
        new Thread(() -> {
            System.out.println("[SIPSA] Poblando caché en segundo plano al arrancar...");
            actualizarCacheSipsa();
            System.out.println("[SIPSA] Caché inicial listo.");
        }, "sipsa-init").start();
    }

    public Map<String, Object> obtenerPreciosDesdePython() {
        LocalDateTime ahora = LocalDateTime.now();
        
        // Si no hay caché y no se está actualizando, lanzar actualización async
        if (cacheSipsa == null && !actualizando) {
            new Thread(this::actualizarCacheSipsa, "sipsa-async").start();
        }

        // Combinar datos reales de SIPSA con nuestro extenso catálogo base
        List<Map<String, Object>> baseProducts = obtenerCatalogoBase();
        List<Map<String, Object>> datosCombinados = new ArrayList<>();
        Set<String> nombresAgregados = new HashSet<>();

        // 1. Agregar los de SIPSA (tienen prioridad)
        if (cacheSipsa != null) {
            for (Map<String, Object> p : cacheSipsa) {
                String nombre = String.valueOf(p.get("nombre")).toLowerCase().trim();
                
                // Asignar categoría si no la tiene
                if (!p.containsKey("categoria")) {
                    p.put("categoria", asignarCategoria(nombre));
                }
                
                datosCombinados.add(p);
                nombresAgregados.add(nombre);
            }
        }

        // 2. Agregar los del catálogo base que no estén en SIPSA
        for (Map<String, Object> p : baseProducts) {
            String nombre = String.valueOf(p.get("nombre")).toLowerCase().trim();
            if (!nombresAgregados.contains(nombre)) {
                // El base ya tiene categoría asignada en createBaseProduct
                datosCombinados.add(p);
                nombresAgregados.add(nombre);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", datosCombinados);
        
        String fechaAct = ultimaActualizacion != null 
            ? ultimaActualizacion.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) 
            : ahora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            
        response.put("fuente", "DANE Oficial SIPSA | Actualizado: " + fechaAct);
        response.put("timestamp", ahora.toString());
        return response;
    }

    // Tarea programada: Ejecutar todos los días a las 2:05 PM hora local de Colombia
    @Scheduled(cron = "0 5 14 * * ?", zone = "America/Bogota")
    public void scheduleDailySipsaUpdate() {
        System.out.println("Iniciando actualización diaria programada de SIPSA DANE...");
        actualizarCacheSipsa();
    }

    private synchronized void actualizarCacheSipsa() {
        if (actualizando) return; // Ya hay otro hilo ejecutando
        actualizando = true;
        try {
            // Prioridad: producción (Render/Docker) → dev local
            String[] paths = {"/app/sipsa_etl.py", "sipsa_etl.py", "src/main/resources/python/sipsa_etl.py"};
            String scriptPath = null;
            for (String p : paths) {
                if (new java.io.File(p).exists()) {
                    scriptPath = p;
                    break;
                }
            }
            if (scriptPath == null) {
                scriptPath = paths[0]; // intentar con el primero aunque no exista
            }
            ProcessBuilder pb = new ProcessBuilder("python3", scriptPath);
            pb.redirectErrorStream(true);
            Process p = pb.start();

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    // Capturar solo la línea que parece ser JSON para evitar logs basura
                    if (line.trim().startsWith("{")) {
                        sb.append(line);
                    }
                }
            }
            p.waitFor();

            ObjectMapper mapper = new ObjectMapper();
            if (sb.length() > 0) {
                Map<String, Object> jsonMap = mapper.readValue(sb.toString(), Map.class);

                if ("success".equals(jsonMap.get("status"))) {
                    cacheSipsa = (List<Map<String, Object>>) jsonMap.get("data");
                    ultimaActualizacion = LocalDateTime.now();
                    System.out.println("[SIPSA] CACHE ACTUALIZADO EXITOSAMENTE A LAS " + ultimaActualizacion);
                } else {
                    System.out.println("[SIPSA] Error en script interno: " + jsonMap.get("message"));
                }
            } else {
                System.out.println("[SIPSA] No se obtuvo JSON válido del script de Python");
            }
        } catch (Exception e) {
            System.err.println("[SIPSA] Error ejecutando sipsa_etl.py: " + e.getMessage());
        } finally {
            actualizando = false;
        }
    }

    private List<Map<String, Object>> obtenerCatalogoBase() {
        List<Map<String, Object>> base = new ArrayList<>(Arrays.asList(
            // --- CAFÉ Y CACAO ---
            createBaseProduct("Café Pergamino Seco", 18500),
            createBaseProduct("Café Verde Malla 14", 17000),
            createBaseProduct("Café Tostado en Grano", 25000),
            createBaseProduct("Café Molido Tradicional", 22000),
            createBaseProduct("Café Especial (Gourmet)", 35000),
            createBaseProduct("Cacao Seco (Grano)", 15000),
            createBaseProduct("Cacao en Pasta", 18000),
            
            // --- TUBÉRCULOS Y RAÍCES ---
            createBaseProduct("Papa Sabanera", 3500),
            createBaseProduct("Papa Pastusa", 2200),
            createBaseProduct("Papa Criolla Lavada", 3800),
            createBaseProduct("Papa Criolla Sucia", 3000),
            createBaseProduct("Papa R-12", 2000),
            createBaseProduct("Papa Rubí", 2100),
            createBaseProduct("Yuca Llanera", 2000),
            createBaseProduct("Yuca Armenia", 1800),
            createBaseProduct("Arracacha Blanca", 3500),
            createBaseProduct("Arracacha Amarilla", 3200),
            createBaseProduct("Ñame Espino", 2800),
            createBaseProduct("Ñame Criollo", 2500),
            
            // --- FRUTAS (CÍTRICOS) ---
            createBaseProduct("Naranja Valencia", 1800),
            createBaseProduct("Naranja Tangelo", 2500),
            createBaseProduct("Naranja Sweety", 2200),
            createBaseProduct("Naranja Ombligona", 2400),
            createBaseProduct("Limón Tahití", 3500),
            createBaseProduct("Limón Común", 2800),
            createBaseProduct("Limón Mandarino", 2500),
            createBaseProduct("Mandarina Arrayana", 2200),
            createBaseProduct("Mandarina Oneco", 2400),
            
            // --- FRUTAS (TROPICALES) ---
            createBaseProduct("Mango Tommy", 3000),
            createBaseProduct("Mango de Azúcar", 4500),
            createBaseProduct("Mango Yulima", 3200),
            createBaseProduct("Mango Farchild", 3400),
            createBaseProduct("Aguacate Hass", 6500),
            createBaseProduct("Aguacate Papelillo", 5000),
            createBaseProduct("Aguacate Lorena", 4800),
            createBaseProduct("Aguacate Choquette", 5200),
            createBaseProduct("Papaya Melona", 1500),
            createBaseProduct("Papaya Hawaiana", 2200),
            createBaseProduct("Papaya Maradol", 1800),
            createBaseProduct("Banano Criollo", 1500),
            createBaseProduct("Banano Urabá", 1200),
            createBaseProduct("Banano Bocadillo", 1800),
            createBaseProduct("Plátano Hartón Verde", 2500),
            createBaseProduct("Plátano Maduro", 2200),
            createBaseProduct("Plátano Guineo", 1500),
            createBaseProduct("Plátano Dominico", 2000),
            
            // --- FRUTAS (OTRAS) ---
            createBaseProduct("Uva Isabella", 4500),
            createBaseProduct("Uva Red Globe", 9000),
            createBaseProduct("Uva Verde Sin Semilla", 8500),
            createBaseProduct("Fresa", 6000),
            createBaseProduct("Mora de Castilla", 4200),
            createBaseProduct("Lulo", 4000),
            createBaseProduct("Maracuyá", 3800),
            createBaseProduct("Granadilla", 5000),
            createBaseProduct("Pitahaya Amarilla", 7000),
            createBaseProduct("Guanábana", 3500),
            createBaseProduct("Manzana Royal Gala", 5500),
            createBaseProduct("Manzana Verde", 6000),
            createBaseProduct("Melón Cantalupo", 2500),
            createBaseProduct("Sandía (Patilla)", 1500),
            createBaseProduct("Tomate de Árbol", 3500),
            
            // --- HORTALIZAS Y VERDURAS ---
            createBaseProduct("Tomate Chonto", 3000),
            createBaseProduct("Tomate Milano (Larga Vida)", 3200),
            createBaseProduct("Cebolla Cabezona Blanca", 2000),
            createBaseProduct("Cebolla Cabezona Roja", 2500),
            createBaseProduct("Cebolla Larga (Junca)", 1800),
            createBaseProduct("Zanahoria", 1500),
            createBaseProduct("Pimentón Rojo", 2800),
            createBaseProduct("Pimentón Verde", 2500),
            createBaseProduct("Ají Dulce", 3500),
            createBaseProduct("Ají Picante", 4000),
            createBaseProduct("Pepino Cohombro", 1400),
            createBaseProduct("Lechuga Batavia", 1200),
            createBaseProduct("Lechuga Crespa", 1500),
            createBaseProduct("Repollo Verde", 1500),
            createBaseProduct("Repollo Morado", 1800),
            createBaseProduct("Brócoli", 2800),
            createBaseProduct("Coliflor", 2500),
            createBaseProduct("Cilantro", 800),
            createBaseProduct("Perejil Liso", 800),
            createBaseProduct("Perejil Crespo", 900),
            createBaseProduct("Apio", 1200),
            createBaseProduct("Espinaca", 1500),
            createBaseProduct("Habichuela", 2500),
            createBaseProduct("Arveja Verde en Vaina", 5000),
            
            // --- GRANOS Y OTROS ---
            createBaseProduct("Frijol Bola Roja", 6500),
            createBaseProduct("Frijol Cargamanto", 7000),
            createBaseProduct("Frijol Nima", 5500),
            createBaseProduct("Lenteja Importada", 3800),
            createBaseProduct("Garbanzo", 4000),
            createBaseProduct("Maíz Amarillo Cáscara", 1500),
            createBaseProduct("Maíz Blanco Trillado", 2200),
            createBaseProduct("Mazorca Tierna", 1100),
            createBaseProduct("Arroz Blanco Molienda", 3500),
            createBaseProduct("Panela Cuadrada", 3000),
            createBaseProduct("Panela Redonda", 3200),
            createBaseProduct("Miel de Abejas (Litro)", 18000),
            createBaseProduct("Queso Campesino", 12000),
            createBaseProduct("Huevos AA (Unidad)", 500)
        ));
        return base;
    }

    private Map<String, Object> createBaseProduct(String name, int base) {
        Map<String, Object> map = new HashMap<>();
        map.put("nombre", name);
        map.put("precio", base);
        map.put("tendencia", "estable");
        map.put("categoria", asignarCategoria(name.toLowerCase()));
        return map;
    }

    private String asignarCategoria(String nombre) {
        if (nombre == null) return "Otros";
        
        // Frutas
        if (nombre.matches(".*(naranja|lim[oó]n|mandarina|mango|aguacate|papaya|banano|pl[aá]tano|uva|fresa|mora|lulo|maracuy[aá]|granadilla|pitahaya|guan[aá]bana|manzana|mel[oó]n|sand[ií]a|patilla|tomate de [aá]rbol|pera|durazno|pi[ñn]a).*")) {
            return "Frutas";
        }
        // Tubérculos y Raíces
        if (nombre.matches(".*(papa|yuca|arracacha|[ñn]ame|batata|ulluco|chugua|rubia).*")) {
            return "Tubérculos y Raíces";
        }
        // Verduras y Hortalizas
        if (nombre.matches(".*(tomate|cebolla|zanahoria|piment[oó]n|aj[ií]|pepino|lechuga|repollo|br[oó]coli|coliflor|cilantro|perejil|apio|espinaca|habichuela|acelga|ahuyama|calabaza|ajo|remolacha|r[aá]bano).*")) {
            return "Verduras y Hortalizas";
        }
        // Granos y Cereales
        if (nombre.matches(".*(frijol|fr[ií]jol|lenteja|garbanzo|ma[ií]z|mazorca|arroz|arveja|soya|trigo|cebada|avena).*")) {
            return "Granos y Cereales";
        }
        // Café y Cacao
        if (nombre.matches(".*(caf[eé]|cacao).*")) {
            return "Café y Cacao";
        }
        // Huevos y Lácteos
        if (nombre.matches(".*(huevo|leche|queso|mantequilla|cuajada|suero|kumis|yogurt).*")) {
            return "Huevos y Lácteos";
        }
        // Otros procesados y abarrotes
        if (nombre.matches(".*(panela|miel|az[uú]car|sal|aceite|manteca|harina|pasta|arepa|carne|pescado|pollo|cerdo).*")) {
            return "Abarrotes y Proteínas";
        }
        
        return "Otros";
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
