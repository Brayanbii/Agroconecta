package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.repository.OrdenRepository; // <-- IMPORTANTE
import com.proyecto.AccesoUsuarios.repository.ProductoRepository;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import com.proyecto.AccesoUsuarios.service.CarritoService;
import com.proyecto.AccesoUsuarios.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DashboardController {

    @Autowired
    private UsuarioRepository usuarioRepo;
    
    @Autowired
    private ProductoRepository productoRepo;

    @Autowired
    private OrdenRepository ordenRepo; // <-- Inyectamos el repo de órdenes

    @Autowired
    private CarritoService carritoService;

    // 1. PANEL ADMIN
    @GetMapping("/admin/dashboard")
    public String dashboardAdmin(Model model) {
        model.addAttribute("totalUsuarios", usuarioRepo.count());
        model.addAttribute("totalProductos", productoRepo.count());
        
        // CORRECCIÓN: Ahora sí contamos las ventas reales
        model.addAttribute("totalVentas", ordenRepo.count()); 
        
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
