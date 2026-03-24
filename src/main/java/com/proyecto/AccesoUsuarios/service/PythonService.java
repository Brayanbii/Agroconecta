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
}
