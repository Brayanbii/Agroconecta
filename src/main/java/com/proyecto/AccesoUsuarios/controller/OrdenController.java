package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.model.*;
import com.proyecto.AccesoUsuarios.repository.OrdenRepository;
import com.proyecto.AccesoUsuarios.repository.ProductoRepository;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import com.proyecto.AccesoUsuarios.service.CarritoService;
import com.proyecto.AccesoUsuarios.service.PdfService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/orden")
public class OrdenController {

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private OrdenRepository ordenRepo;
    
    @Autowired
    private ProductoRepository productoRepo;

    @Autowired
    private PdfService pdfService;

    @GetMapping("/pagar")
    public String pagarOrden(Authentication auth, Model model) {
        if (carritoService.obtenerItems().isEmpty()) {
            return "redirect:/tienda";
        }

        // VALIDACIÓN DE STOCK: Verificar que hay suficiente stock para cada producto
        for (ItemCarrito item : carritoService.obtenerItems()) {
            // Recargamos el producto desde BD para tener el stock actualizado
            Producto productoActual = productoRepo.findById(item.getProducto().getId()).orElse(null);
            
            if (productoActual == null) {
                return "redirect:/carrito?error=producto_no_existe";
            }
            if (productoActual.getStock() == null || productoActual.getStock() < item.getCantidad()) {
                // Stock insuficiente → redirigir al carrito con error
                return "redirect:/carrito?error=stock_insuficiente&producto=" + productoActual.getNombre();
            }
        }

        String email = auth.getName(); 
        Usuario usuario = usuarioRepo.findByEmail(email).orElseThrow();

        Orden orden = new Orden();
        orden.setFechaCreacion(LocalDateTime.now());
        orden.setNumeroOrden(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        orden.setUsuario(usuario);
        orden.setTotal(carritoService.obtenerTotal());
        orden.setEstado("Pendiente");

        List<DetalleOrden> detalles = new ArrayList<>();
        for (ItemCarrito item : carritoService.obtenerItems()) {
            DetalleOrden detalle = new DetalleOrden();
            detalle.setNombre(item.getProducto().getNombre());
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecio(item.getProducto().getPrecio());
            detalle.setTotal(item.getTotal());
            detalle.setOrden(orden);
            detalle.setProducto(item.getProducto());
            detalles.add(detalle);
            
            // Descontar stock (ya validado arriba)
            Producto p = productoRepo.findById(item.getProducto().getId()).orElseThrow();
            p.setStock(p.getStock() - item.getCantidad());
            productoRepo.save(p);
        }
        
        orden.setDetalles(detalles);
        ordenRepo.save(orden);
        carritoService.limpiarCarrito();
        
        model.addAttribute("orden", orden);
        return "compra_exitosa";
    }
    
    @GetMapping("/mis-compras")
    public String misCompras(Authentication auth, Model model) {
        String email = auth.getName();
        Usuario usuario = usuarioRepo.findByEmail(email).orElseThrow(); // CORREGIDO
        
        List<Orden> ordenes = ordenRepo.findByUsuario(usuario);
        model.addAttribute("ordenes", ordenes);
        return "mis_compras";
    }

    @GetMapping("/recibo/{id}")
    public void generarPdf(@PathVariable Long id, HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Recibo_AgroConecta_" + id + ".pdf";
        response.setHeader(headerKey, headerValue);

        Orden orden = ordenRepo.findById(id).orElseThrow();
        pdfService.exportar(response, orden);
    }
}