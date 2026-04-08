package com.proyecto.AccesoUsuarios.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class PythonService {

    private static final String BASE_URL       = "http://localhost:5000";
    private static final String PRECIOS_URL    = BASE_URL + "/api/v1/precios";
    private static final String GRAFICOS_URL   = BASE_URL + "/api/v1/graficos";
    private static final String INFORME_URL    = BASE_URL + "/api/v1/informe-campesino";
    private static final String LOGISTICA_URL  = BASE_URL + "/api/v1/logistica-rutas";

    // -------------------------------------------------------
    // GET precios de referencia (para el formulario campesino)
    // -------------------------------------------------------
    public Map<String, Object> obtenerPreciosDesdePython() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            return restTemplate.getForObject(PRECIOS_URL, Map.class);
        } catch (Exception e) {
            System.out.println("Error: Python esta apagado o no responde (precios).");
            return null;
        }
    }

    // -------------------------------------------------------
    // POST datos de ventas → recibe graficos en base64
    // -------------------------------------------------------
    public Map<String, Object> generarGraficos(Map<String, Object> datos) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(datos, headers);

            return restTemplate.postForObject(GRAFICOS_URL, request, Map.class);
        } catch (Exception e) {
            System.out.println("Error: Python esta apagado o no responde (graficos).");
            return null;
        }
    }

    // -------------------------------------------------------
    // POST datos del campesino → recibe super informe en base64
    // -------------------------------------------------------
    public Map<String, Object> generarInformeCampesino(Map<String, Object> datos) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(datos, headers);

            return restTemplate.postForObject(INFORME_URL, request, Map.class);
        } catch (Exception e) {
            System.out.println("Error: Python esta apagado o no responde (informe-campesino).");
            return null;
        }
    }

    // -------------------------------------------------------
    // POST Coordenadas → Obtiene ruta optimizada y metricas (OSRM)
    // -------------------------------------------------------
    public Map<String, Object> calcularRutaLogistica(Map<String, Object> coordenadas) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(coordenadas, headers);

            return restTemplate.postForObject(LOGISTICA_URL, request, Map.class);
        } catch (Exception e) {
            System.out.println("Error: No se pudo conectar a Python para calcular la ruta logística.");
            return null;
        }
    }
}
