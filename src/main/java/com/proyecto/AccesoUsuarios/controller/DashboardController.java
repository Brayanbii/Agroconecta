package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.repository.DetalleOrdenRepository;
import com.proyecto.AccesoUsuarios.repository.OrdenRepository;
import com.proyecto.AccesoUsuarios.repository.ProductoRepository;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import com.proyecto.AccesoUsuarios.service.CarritoService;
import com.proyecto.AccesoUsuarios.service.PythonService;
import com.proyecto.AccesoUsuarios.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
            model.addAttribute("graficoProductos", graficos.get("grafico_productos"));
            model.addAttribute("graficoMeses",     graficos.get("grafico_meses"));
            model.addAttribute("graficoEstados",   graficos.get("grafico_estados"));
        }

        return "dashboard_admin";
    }

    // 2. PANEL CAMPESINO (Solo muestra SUS productos)
    @GetMapping("/campesino/productos")
    public String dashboardCampesino(Model model, Authentication auth) {
        String email = auth.getName();
        Usuario campesino = usuarioRepo.findByEmail(email).orElseThrow();
        model.addAttribute("productos", productoRepo.findByUsuario(campesino)); 
        return "mis_productos";
    }

    // 3. PANEL CLIENTE (TIENDA) — con buscador funcional
    @GetMapping("/tienda")
    public String tienda(Model model, Authentication auth,
                         @RequestParam(value = "buscar", required = false) String buscar) {
        // 1. Cargar productos (filtrados si hay búsqueda)
        if (buscar != null && !buscar.trim().isEmpty()) {
            model.addAttribute("productos", productoRepo.findByNombreContainingIgnoreCase(buscar.trim()));
            model.addAttribute("busqueda", buscar.trim());
        } else {
            model.addAttribute("productos", productoRepo.findAll());
            model.addAttribute("busqueda", "");
        }
        
        // 2. Cargar cantidad del carrito
        model.addAttribute("cantidadCarrito", carritoService.contarItems());

        // 3. LÓGICA NUEVA: Obtener el nombre real del cliente
        String email = auth.getName();
        // Buscamos al usuario por su email
        Usuario usuario = usuarioRepo.findByEmail(email).orElse(null);
        
        if (usuario != null) {
            // Mandamos el nombre completo a la vista
            model.addAttribute("nombreCliente", usuario.getNombreCompleto());
        } else {
            model.addAttribute("nombreCliente", "Cliente");
        }

        return "tienda";
    }
}
