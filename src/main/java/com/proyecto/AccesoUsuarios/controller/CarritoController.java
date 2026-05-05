package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.model.Producto;
import com.proyecto.AccesoUsuarios.repository.ProductoRepository;
import com.proyecto.AccesoUsuarios.service.CarritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/carrito")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private ProductoRepository productoRepository;

    // 1. Ver el carrito
    @GetMapping
    public String verCarrito(Model model) {
        model.addAttribute("items", carritoService.obtenerItems());
        model.addAttribute("total", carritoService.obtenerTotal());
        return "carrito"; // Vista html
    }

    // 2. Agregar item (Ruta rápida desde la tienda)
    @GetMapping("/agregar/{id}")
    public String agregarAlCarrito(@PathVariable Long id) {
        carritoService.agregarProducto(id, 1); // Agrega 1 por defecto
        return "redirect:/tienda"; // Vuelve a la tienda para seguir comprando
    }

    // 3. Eliminar item (POST para seguridad CSRF)
    @PostMapping("/eliminar/{id}")
    public String eliminarDelCarrito(@PathVariable Long id) {
        carritoService.eliminarProducto(id);
        return "redirect:/carrito";
    }

    // 4. Actualizar cantidad
    @PostMapping("/actualizar")
    public String actualizarCantidad(@RequestParam Long id, @RequestParam Integer cantidad, RedirectAttributes attributes) {
        boolean superoStock = carritoService.actualizarCantidad(id, cantidad);
        if (superoStock) {
            Producto p = productoRepository.findById(id).orElse(null);
            attributes.addAttribute("error", "stock_insuficiente");
            attributes.addAttribute("producto", p != null ? p.getNombre() : "el producto");
        }
        return "redirect:/carrito";
    }
}
