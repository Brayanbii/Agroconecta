package com.proyecto.AccesoUsuarios.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import com.proyecto.AccesoUsuarios.service.PythonService;

@RestController
@RequestMapping("/api/sipsa")
public class SipsaController {

    @Autowired
    private PythonService pythonService;

    // Base de datos "Oráculo" simulada de más de 30 productos para el prototipo
    private static final Map<String, Integer> CATALOGO_PRECIOS = new HashMap<>();

    static {
        // FRUTAS - VARIEDADES ESPECÍFICAS
        CATALOGO_PRECIOS.put("naranja valencia", 1800);
        CATALOGO_PRECIOS.put("naranja tangelo", 2500);
        CATALOGO_PRECIOS.put("naranja sweety", 2200);
        CATALOGO_PRECIOS.put("naranja ombligona", 2400);
        CATALOGO_PRECIOS.put("naranja grey", 3000); // Naranja tipo grapefruit / toronja o grey
        CATALOGO_PRECIOS.put("naranja", 2000); // genérico
        
        CATALOGO_PRECIOS.put("papaya melona", 1500);
        CATALOGO_PRECIOS.put("papaya hawaiana", 2200);
        CATALOGO_PRECIOS.put("papaya redonda", 1600);
        CATALOGO_PRECIOS.put("papaya maradol", 1800);
        CATALOGO_PRECIOS.put("papaya", 1800);
        
        CATALOGO_PRECIOS.put("limon tahiti", 3500);
        CATALOGO_PRECIOS.put("limon comun", 2800);
        CATALOGO_PRECIOS.put("limon mandarin", 2500);
        CATALOGO_PRECIOS.put("limon", 3000);
        
        CATALOGO_PRECIOS.put("mango tommy", 3000);
        CATALOGO_PRECIOS.put("mango de azucar", 4500);
        CATALOGO_PRECIOS.put("mango yulima", 3200);
        CATALOGO_PRECIOS.put("mango", 3500);
        
        CATALOGO_PRECIOS.put("aguacate hass", 6500);
        CATALOGO_PRECIOS.put("aguacate papelillo", 5000);
        CATALOGO_PRECIOS.put("aguacate lorena", 4800);
        CATALOGO_PRECIOS.put("aguacate", 5500);

        CATALOGO_PRECIOS.put("uva isabella", 4500);
        CATALOGO_PRECIOS.put("uva red globe", 9000);
        CATALOGO_PRECIOS.put("uva verde", 8500);
        CATALOGO_PRECIOS.put("uva", 8500);
        
        CATALOGO_PRECIOS.put("fresa", 6000);
        CATALOGO_PRECIOS.put("manzana royal", 5500);
        CATALOGO_PRECIOS.put("manzana verde", 6000);
        CATALOGO_PRECIOS.put("manzana", 4500);
        CATALOGO_PRECIOS.put("pera", 3800);
        CATALOGO_PRECIOS.put("banano criollo", 1500);
        CATALOGO_PRECIOS.put("banano uraba", 1200);
        CATALOGO_PRECIOS.put("banano", 1200);
        CATALOGO_PRECIOS.put("lulo", 4000);
        CATALOGO_PRECIOS.put("maracuya", 3800);
        CATALOGO_PRECIOS.put("mora de castilla", 4200);
        CATALOGO_PRECIOS.put("sandia", 1500);
        CATALOGO_PRECIOS.put("melon cantalupo", 2500);
        CATALOGO_PRECIOS.put("melon", 2200);

        // VERDURAS Y TUBÉRCULOS - VARIEDADES ESPECÍFICAS
        CATALOGO_PRECIOS.put("papa pastusa", 2200);
        CATALOGO_PRECIOS.put("papa sabanera", 3500);
        CATALOGO_PRECIOS.put("papa criolla sucia", 3000);
        CATALOGO_PRECIOS.put("papa criolla lavada", 3800);
        CATALOGO_PRECIOS.put("papa r-12", 2000);
        CATALOGO_PRECIOS.put("papa", 2500);
        
        CATALOGO_PRECIOS.put("cebolla cabezona blanca", 2000);
        CATALOGO_PRECIOS.put("cebolla cabezona roja", 2500);
        CATALOGO_PRECIOS.put("cebolla junca", 1800);
        CATALOGO_PRECIOS.put("cebolla larga", 1800);
        CATALOGO_PRECIOS.put("cebolla", 2000);
        
        CATALOGO_PRECIOS.put("tomate chonto", 3000);
        CATALOGO_PRECIOS.put("tomate de arbol", 3500);
        CATALOGO_PRECIOS.put("tomate milano", 3200);
        CATALOGO_PRECIOS.put("tomate", 3200);

        CATALOGO_PRECIOS.put("platano harton verde", 2500);
        CATALOGO_PRECIOS.put("platano maduro", 2200);
        CATALOGO_PRECIOS.put("platano guineo", 1500);
        CATALOGO_PRECIOS.put("platano", 2500);

        CATALOGO_PRECIOS.put("yuca llanera", 2000);
        CATALOGO_PRECIOS.put("yuca armenia", 1800);
        CATALOGO_PRECIOS.put("yuca", 1800);
        
        CATALOGO_PRECIOS.put("zanahoria", 1500);
        CATALOGO_PRECIOS.put("mazorca", 1100);
        CATALOGO_PRECIOS.put("maiz", 1100);
        CATALOGO_PRECIOS.put("frijol bola roja", 6500);
        CATALOGO_PRECIOS.put("frijol cargamanto", 7000);
        CATALOGO_PRECIOS.put("frijol verde", 4500);
        CATALOGO_PRECIOS.put("frijol", 4500);
        CATALOGO_PRECIOS.put("arveja verde", 5000);
        CATALOGO_PRECIOS.put("lenteja", 3800);
        CATALOGO_PRECIOS.put("garbanzo", 4000);
        CATALOGO_PRECIOS.put("lechuga batavia", 1200);
        CATALOGO_PRECIOS.put("lechuga crespa", 1500);
        CATALOGO_PRECIOS.put("repollo", 1500);
        CATALOGO_PRECIOS.put("brocoli", 2800);
        CATALOGO_PRECIOS.put("coliflor", 2500);
        CATALOGO_PRECIOS.put("cilantro", 800);
        CATALOGO_PRECIOS.put("perejil", 800);
        CATALOGO_PRECIOS.put("apio", 1200);
        CATALOGO_PRECIOS.put("espinaca", 1500);
        CATALOGO_PRECIOS.put("pepino cohombro", 1400);
        CATALOGO_PRECIOS.put("pepino", 1400);
        CATALOGO_PRECIOS.put("pimenton rojo", 2800);
        CATALOGO_PRECIOS.put("pimenton verde", 2500);
        CATALOGO_PRECIOS.put("pimenton", 2800);
        CATALOGO_PRECIOS.put("aji", 3500);

        // GRANOS Y PROCESADOS BÁSICOS
        CATALOGO_PRECIOS.put("arroz", 3500);
        CATALOGO_PRECIOS.put("cafe", 18000);
        CATALOGO_PRECIOS.put("cacao", 15000);
        CATALOGO_PRECIOS.put("panela", 3000);
        CATALOGO_PRECIOS.put("azucar", 3200);
    }

    @GetMapping("/precio")
    public ResponseEntity<Map<String, Object>> consultarPrecio(@RequestParam("producto") String productoInput) {
        Map<String, Object> response = new HashMap<>();
        
        if (productoInput == null || productoInput.trim().isEmpty()) {
            response.put("encontrado", false);
            return ResponseEntity.ok(response);
        }

        String inputNormalizado = normalizar(productoInput);
        
        // 1. INTENTO DE CONEXIÓN CON EL GOBIERNO (DANE / DATOS ABIERTOS)
        try {
            RestTemplate restTemplate = new RestTemplate();
            org.springframework.http.client.SimpleClientHttpRequestFactory requestFactory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(3000);
            requestFactory.setReadTimeout(3000);
            restTemplate.setRequestFactory(requestFactory);
            
            // Consultamos Socrata
            String url = "https://www.datos.gov.co/resource/ch4u-f3i5.json?$q=" + productoInput + "&$limit=1";
            Map[] resultadoDane = restTemplate.getForObject(url, Map[].class);
            
            if (resultadoDane != null && resultadoDane.length > 0) {
                Map<String, Object> filaOficial = resultadoDane[0];
                String precioOficialStr = (String) filaOficial.getOrDefault("precio_promedio", 
                                          filaOficial.getOrDefault("precio_mayorista", 
                                          filaOficial.getOrDefault("precio", "0")));
                String nombreOficial = (String) filaOficial.getOrDefault("producto", 
                                       filaOficial.getOrDefault("articulo", productoInput));
                
                double precioD = Double.parseDouble(precioOficialStr);
                if (precioD > 0) {
                    response.put("encontrado", true);
                    response.put("nombre_sipsa", capitalizar(nombreOficial));
                    response.put("precio", (int) precioD);
                    response.put("fuente", "DANE Oficial (Datos Abiertos Colombia)");
                    return ResponseEntity.ok(response);
                }
            }
        } catch (Exception e) {
            System.out.println("No se pudo conectar al DANE Oficial, activando motor de respaldo...");
        }

        // 2. SISTEMA DE RESPALDO (TOKEN-BASED SEMANTIC MATCHING SOSTENIDO POR PYTHON ETL CACHED DATA)
        String mejorCoincidencia = null;
        double mejorScore = 0.0;
        int mejorPrecio = 0;
        
        String[] tokensInput = inputNormalizado.split(" ");
        
        // Obtener datos del script Python cacheado
        Map<String, Object> pythonResponse = pythonService.obtenerPreciosDesdePython();
        List<Map<String, Object>> catalogoReal = (List<Map<String, Object>>) pythonResponse.get("data");
        
        for (Map<String, Object> item : catalogoReal) {
            String productoCat = normalizar(String.valueOf(item.get("nombre")));
            int precioItem = ((Number) item.get("precio")).intValue();
            
            if (inputNormalizado.equals(productoCat)) {
                mejorCoincidencia = String.valueOf(item.get("nombre"));
                mejorPrecio = precioItem;
                mejorScore = 999.0; // Coincidencia exacta perfecta
                break;
            }
            
            String[] tokensCat = productoCat.split(" ");
            double scoreActual = 0.0;
            
            for (String tokenIn : tokensInput) {
                if (tokenIn.length() < 3) continue; // Ignorar preposiciones cortas
                
                double mejorTokenScore = 0.0;
                for (String tokenCat : tokensCat) {
                    if (tokenIn.equals(tokenCat)) {
                        mejorTokenScore = 1.0; // Token idéntico
                    } else {
                        int dist = calcularLevenshtein(tokenIn, tokenCat);
                        int maxLength = Math.max(tokenIn.length(), tokenCat.length());
                        double similarity = 1.0 - ((double) dist / maxLength);
                        if (similarity > 0.75) { // Si es muy similar, dar puntos
                            mejorTokenScore = Math.max(mejorTokenScore, similarity);
                        }
                    }
                }
                scoreActual += mejorTokenScore;
            }
            
            scoreActual = scoreActual / Math.max(1, tokensCat.length * 0.5); 

            if (scoreActual > mejorScore && scoreActual >= 1.0) { // Mínimo un buen token
                mejorScore = scoreActual;
                mejorCoincidencia = String.valueOf(item.get("nombre"));
                mejorPrecio = precioItem;
            }
        }

        if (mejorCoincidencia != null) {
            response.put("encontrado", true);
            response.put("nombre_sipsa", capitalizar(mejorCoincidencia));
            response.put("precio", mejorPrecio);
            response.put("fuente", pythonResponse.get("fuente"));
        } else {
            response.put("encontrado", false);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Normaliza un string: minúsculas, sin tildes, sin "s" al final para plurales simples
     */
    private String normalizar(String texto) {
        String limpio = Normalizer.normalize(texto.toLowerCase().trim(), Normalizer.Form.NFD);
        limpio = limpio.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        
        // Quitar "s" final o "es" final si la palabra es suficientemente larga
        String[] palabras = limpio.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String p : palabras) {
            if (p.length() > 4 && p.endsWith("es")) {
                p = p.substring(0, p.length() - 2);
            } else if (p.length() > 3 && p.endsWith("s")) {
                p = p.substring(0, p.length() - 1);
            }
            sb.append(p).append(" ");
        }
        return sb.toString().trim();
    }

    /**
     * Capitaliza la primera letra
     */
    private String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) return texto;
        return texto.substring(0, 1).toUpperCase() + texto.substring(1);
    }

    /**
     * Algoritmo de Distancia de Levenshtein para encontrar similitud de palabras
     */
    private int calcularLevenshtein(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                    dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
                }
            }
        }
        return dp[s1.length()][s2.length()];
    }

    @GetMapping("/catalogo")
    public ResponseEntity<List<Map<String, Object>>> obtenerCatalogo() {
        Map<String, Object> pythonResponse = pythonService.obtenerPreciosDesdePython();
        List<Map<String, Object>> catalogoReal = (List<Map<String, Object>>) pythonResponse.get("data");
        
        List<Map<String, Object>> catalogoList = new ArrayList<>();
        for (Map<String, Object> entry : catalogoReal) {
            Map<String, Object> item = new HashMap<>();
            item.put("producto", capitalizar(String.valueOf(entry.get("nombre"))));
            item.put("precio", ((Number) entry.get("precio")).intValue());
            item.put("categoria", entry.getOrDefault("categoria", "Otros"));
            catalogoList.add(item);
        }
        
        // Ordenar alfabéticamente por defecto
        catalogoList.sort((a, b) -> ((String) a.get("producto")).compareTo((String) b.get("producto")));
        return ResponseEntity.ok(catalogoList);
    }
}
