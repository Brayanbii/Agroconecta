package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.model.ContactoHoreca;
import com.proyecto.AccesoUsuarios.repository.ContactoHorecaRepository;
import com.proyecto.AccesoUsuarios.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@Controller
public class HomeController {

    @Autowired
    private ProductoRepository productoRepo;

    @Autowired
    private ContactoHorecaRepository horecaRepo;

    // Página de Inicio Pública (Landing Page)
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("productos", productoRepo.findAll().stream().limit(4).toList());
        return "index";
    }

    @GetMapping("/politica-cookies")
    public String politicaCookies() {
        return "politica_cookies";
    }

    @GetMapping("/terminos-y-condiciones")
    public String terminosCondiciones() {
        return "terminos_condiciones";
    }

    @GetMapping("/politica-privacidad")
    public String politicaPrivacidad() {
        return "politica_privacidad";
    }

    @GetMapping("/contacto")
    public String contacto() {
        return "contacto";
    }

    @GetMapping("/como-funciona")
    public String comoFunciona() {
        return "como_funciona";
    }

    @GetMapping("/impacto-social")
    public String impactoSocial() {
        return "impacto_social";
    }

    @GetMapping("/sobre-nosotros")
    public String sobreNosotros() {
        return "sobre_nosotros";
    }

    @PostMapping("/api/horeca/contacto")
    @ResponseBody
    public Map<String, Object> contactoHoreca(@RequestBody Map<String, String> body) {
        Map<String, Object> resp = new HashMap<>();
        try {
            ContactoHoreca c = new ContactoHoreca();
            c.setNombre(body.get("nombre"));
            c.setEmail(body.get("email"));
            c.setTelefono(body.get("telefono"));
            c.setEmpresa(body.get("empresa"));
            c.setTipoNegocio(body.get("tipoNegocio"));
            c.setMensaje(body.get("mensaje"));
            horecaRepo.save(c);
            resp.put("success", true);
            resp.put("message", "Recibimos tu informacion. Te contactaremos pronto.");
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Error al guardar. Intenta de nuevo.");
        }
        return resp;
    }

    @PostMapping("/api/ia/descripcion")
    @ResponseBody
    public Map<String, Object> generarDescripcion(@RequestBody Map<String, String> body) {
        Map<String, Object> resp = new HashMap<>();
        String nombre = body.get("nombre");
        String categoria = body.get("categoria");
        
        try {
            String prompt = "Genera una descripcion vendedora corta (maximo 3 oraciones) para un producto agricola colombiano llamado \"" + nombre + "\"." + (categoria != null && !categoria.isEmpty() ? " Categoria: " + categoria + "." : "") + " No uses comillas, no saludes, ve directo al texto de venta.";
            String apiKey = System.getenv("GEMINI_API_KEY") != null ? System.getenv("GEMINI_API_KEY") : "";

            java.net.URL url = new java.net.URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("x-goog-api-key", apiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);

            String json = String.format("{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}", prompt.replace("\"", "\\\"").replace("\n", " "));
            conn.getOutputStream().write(json.getBytes());
            
            if (conn.getResponseCode() == 200) {
                String text = new String(conn.getInputStream().readAllBytes());
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<String, Object> data = mapper.readValue(text, Map.class);
                java.util.List<Map<String, Object>> candidates = (java.util.List<Map<String, Object>>) data.get("candidates");
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                java.util.List<Map<String, Object>> parts = (java.util.List<Map<String, Object>>) content.get("parts");
                resp.put("success", true);
                resp.put("texto", parts.get(0).get("text"));
                conn.disconnect();
                return resp;
            }
            conn.disconnect();
        } catch (Exception ignored) {}
        
        // Fallback offline
        String[] plantillas = {
            nombre + " fresco del campo colombiano, cultivado con amor y dedicacion por nuestros campesinos. Ideal para tus comidas diarias con la mejor calidad.",
            "Prueba nuestro " + nombre + " recien cosechado, directo de la finca a tu mesa. Sin intermediarios, solo frescura y sabor autentico colombiano.",
            nombre + " 100% natural, cultivado en las mejores tierras de Colombia. Perfecto para preparar platos saludables y deliciosos en casa.",
            "Lleva a tu hogar el autentico sabor del campo con este " + nombre + " seleccionado especialmente para ti. Calidad premium garantizada.",
            "Del corazon de " + (categoria != null && !categoria.isEmpty() ? categoria : "Colombia") + " llega este " + nombre + " con toda la frescura que tu familia merece. Pidelo ya."
        };
        resp.put("success", true);
        resp.put("texto", plantillas[(int)(Math.random() * plantillas.length)]);
        resp.put("offline", true);
        return resp;
    }
}