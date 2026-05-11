package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.repository.DetalleOrdenRepository;
import com.proyecto.AccesoUsuarios.repository.OrdenRepository;
import com.proyecto.AccesoUsuarios.repository.ProductoRepository;
import com.proyecto.AccesoUsuarios.repository.ResenaRepository;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import com.proyecto.AccesoUsuarios.service.CarritoService;
import com.proyecto.AccesoUsuarios.service.PythonService;
import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.model.Producto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class DashboardController {

    @Autowired
    private UsuarioRepository usuarioRepo;
    
    @Autowired
    private ProductoRepository productoRepo;

    @Autowired
    private OrdenRepository ordenRepo; // <-- Inyectamos el repo de órdenes

    @Autowired
    private DetalleOrdenRepository detalleRepo;

    @Autowired
    private ResenaRepository resenaRepo;

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private PythonService pythonService;

    // 1. PANEL ADMIN
    @GetMapping("/admin/dashboard")
    public String dashboardAdmin(Model model) {
        // --- Fecha actual dinámica ---
        String fechaHoy = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("d MMM, yyyy", new Locale("es", "CO")));
        model.addAttribute("fechaHoy", fechaHoy);

        // --- KPI Cards ---
        model.addAttribute("totalUsuarios",  usuarioRepo.count());
        model.addAttribute("totalProductos", productoRepo.count());
        model.addAttribute("totalVentas",    ordenRepo.count());
        model.addAttribute("totalResenas",   resenaRepo.count());

        // --- Últimas Reseñas para la tabla del admin ---
        model.addAttribute("ultimasResenas", resenaRepo.findAllByOrderByFechaDesc());

        // --- Datos para graficos: Top productos ---
        List<Map<String, Object>> productos = new ArrayList<>();
        for (Object[] row : detalleRepo.findTopProductos()) {
            Map<String, Object> item = new HashMap<>();
            item.put("nombre",   row[0]);
            item.put("cantidad", row[1]);
            item.put("total",    row[2]);
            productos.add(item);
        }

        // --- Datos para graficos: Ventas por mes ---
        List<Map<String, Object>> ventasMes = new ArrayList<>();
        String[] meses = {"Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"};
        for (Object[] row : ordenRepo.findVentasPorMes()) {
            Map<String, Object> item = new HashMap<>();
            int mesNum = ((Number) row[0]).intValue();
            item.put("mes",   meses[mesNum - 1]);
            item.put("total", row[1]);
            ventasMes.add(item);
        }

        // --- Datos para graficos: Estados de ordenes ---
        List<Map<String, Object>> estados = new ArrayList<>();
        for (Object[] row : ordenRepo.findOrdenesPorEstado()) {
            Map<String, Object> item = new HashMap<>();
            item.put("estado",   row[0] != null ? row[0] : "Sin estado");
            item.put("cantidad", row[1]);
            estados.add(item);
        }

        // --- Llamar a Python para generar los graficos ---
        Map<String, Object> datosPython = new HashMap<>();
        datosPython.put("productos",   productos);
        datosPython.put("ventas_mes",  ventasMes);
        datosPython.put("estados",     estados);

        Map<String, Object> graficos = pythonService.generarGraficos(datosPython);
        if (graficos != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                model.addAttribute("graficoProductos", mapper.writeValueAsString(graficos.get("grafico_productos")));
                model.addAttribute("graficoMeses",     mapper.writeValueAsString(graficos.get("grafico_meses")));
                model.addAttribute("graficoEstados",   mapper.writeValueAsString(graficos.get("grafico_estados")));
            } catch (Exception e) {
                System.out.println("Error serializando JSON para graficos: " + e.getMessage());
            }
        }

        return "dashboard_admin";
    }

    // 2. PANEL CAMPESINO (Solo muestra SUS productos)
    @GetMapping("/campesino/productos")
    public String dashboardCampesino(Model model, Authentication auth) {
        String email = auth.getName();
        Usuario campesino = usuarioRepo.findByEmail(email).orElseThrow();
        
        // --- VALIDACIÓN DE IDENTIDAD (KYC) ---
        if (!"APROBADO".equals(campesino.getEstadoVerificacion())) {
            return "redirect:/campesino/verificacion";
        }
        
        model.addAttribute("productos", productoRepo.findByUsuario(campesino)); 
        return "mis_productos";
    }

    // 3. PANEL CLIENTE (TIENDA) — con buscador y radar de cercanía
    @GetMapping("/tienda")
    public String tienda(Model model, Authentication auth,
                         @RequestParam(value = "buscar", required = false) String buscar,
                         @RequestParam(value = "lat", required = false) Double latCliente,
                         @RequestParam(value = "lon", required = false) Double lonCliente) {
        
        List<Producto> productosList;

        // 1. Cargar productos (filtrados si hay búsqueda)
        if (buscar != null && !buscar.trim().isEmpty()) {
            productosList = productoRepo.findByNombreContainingIgnoreCase(buscar.trim());
            model.addAttribute("busqueda", buscar.trim());
        } else {
            productosList = productoRepo.findAll();
            model.addAttribute("busqueda", "");
        }

        // 2. Lógica de Distancia Matemática (Haversine)
        if (latCliente != null && lonCliente != null) {
            for (Producto p : productosList) {
                if (p.getLatitudOrigen() != null && p.getLongitudOrigen() != null) {
                    double dist = calcularDistancia(latCliente, lonCliente, p.getLatitudOrigen(), p.getLongitudOrigen());
                    // Redondear a 1 decimal
                    p.setDistanciaKm(Math.round(dist * 10.0) / 10.0);
                } else {
                    // Si el producto no tiene ubicación definida, lo mandamos al fondo de la lista
                    p.setDistanciaKm(9999.9);
                }
            }
            // Ordenar la lista del más cercano al más lejano
            productosList.sort(Comparator.comparing(Producto::getDistanciaKm));
            model.addAttribute("modoCercania", true);
        } else {
            model.addAttribute("modoCercania", false);
        }

        model.addAttribute("productos", productosList);
        
        // 2. Cargar cantidad del carrito
        model.addAttribute("cantidadCarrito", carritoService.contarItems());

        // 3. LÓGICA NUEVA: Obtener el nombre real del cliente (solo si está logueado)
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            Usuario usuario = usuarioRepo.findByEmail(email).orElse(null);
            if (usuario != null) {
                model.addAttribute("nombreCliente", usuario.getNombreCompleto());
            } else {
                model.addAttribute("nombreCliente", "Cliente");
            }
        } else {
            model.addAttribute("nombreCliente", "Invitado");
        }

        return "tienda";
    }

    // --- Helper: Fórmula de Haversine para Distancias Reales ---
    private double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        final int RADIO_TIERRA_KM = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return RADIO_TIERRA_KM * c;
    }
}
