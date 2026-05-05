package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.model.*;
import com.proyecto.AccesoUsuarios.repository.OrdenRepository;
import com.proyecto.AccesoUsuarios.repository.ProductoRepository;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import com.proyecto.AccesoUsuarios.service.CarritoService;
import com.proyecto.AccesoUsuarios.service.PdfService;
import com.proyecto.AccesoUsuarios.service.MercadoPagoService;
import com.proyecto.AccesoUsuarios.repository.DireccionRepository;
import com.mercadopago.resources.preference.Preference;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    private DireccionRepository direccionRepo;

    @Autowired
    private MercadoPagoService mercadoPagoService;

    @Autowired
    private OrdenRepository ordenRepo;
    
    @Autowired
    private ProductoRepository productoRepo;

    @Autowired
    private PdfService pdfService;

    @GetMapping("/checkout")
    public String checkoutMapa(Authentication auth, Model model) {
        if (carritoService.obtenerItems().isEmpty()) {
            return "redirect:/tienda";
        }
        
        model.addAttribute("items", carritoService.obtenerItems());
        
        // Default coordinates
        model.addAttribute("lat_default", 4.7110);
        model.addAttribute("lng_default", -74.0721);
        model.addAttribute("total", carritoService.obtenerTotal());
        
        Usuario usuario = usuarioRepo.findByEmail(auth.getName()).orElse(null);
        if (usuario != null) {
            List<Direccion> misDirecciones = direccionRepo.findByUsuario(usuario);
            model.addAttribute("misDirecciones", misDirecciones);
            
            Direccion dirPrincipal = misDirecciones.stream().filter(Direccion::getEsPrincipal).findFirst().orElse(null);
            if (dirPrincipal != null) {
                model.addAttribute("lat_default", dirPrincipal.getLatitud());
                model.addAttribute("lng_default", dirPrincipal.getLongitud());
                model.addAttribute("direccionPrincipal", dirPrincipal);
            }
        }
        
        model.addAttribute("costoEnvio", 3500.0);
        model.addAttribute("tarifaServicio", carritoService.obtenerTotal() * 0.05);
        return "checkout_mapa";
    }

    @PostMapping("/pagar")
    public String pagarOrden(Authentication auth, Model model, HttpServletRequest request,
                             @RequestParam(required = false) String direccionEnvio,
                             @RequestParam(required = false) Double latitudEnvio,
                             @RequestParam(required = false) Double longitudEnvio) {
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
        orden.setDireccionEnvio(direccionEnvio != null && !direccionEnvio.isEmpty() ? direccionEnvio : "Dirección no especificada");
        orden.setLatitudEnvio(latitudEnvio);
        orden.setLongitudEnvio(longitudEnvio);

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
        ordenRepo.save(orden); // Ahora está Pendiente con ID en base de datos.
        
        // Fase 2: Mandar preferencia a MP
        try {
            String serverUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            Preference preference = mercadoPagoService.crearPreferenciaDePago(carritoService.obtenerItems(), serverUrl, orden.getId().toString());
            
            // Vaciar carrito luego de crear la preferencia (Opcional, en la vida real se vacía en el success)
            carritoService.limpiarCarrito();
            
            // Re-dirigir a la pasarela mágica!
            return "redirect:" + preference.getInitPoint();
            
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/checkout?error=mercadopago_fallo";
        }
    }
    
    @GetMapping("/success")
    public String pagoExitoso(@RequestParam(required = false) String collection_id,
                              @RequestParam(required = false) String collection_status,
                              @RequestParam(required = false) String external_reference,
                              Model model) {
        // En la Fase 4 el Webhook cambiará el estado, pero como esto es el FrontEnd 
        // redirigido post-pago, le mostramos al usuario el ticket:
        if (external_reference != null) {
            try {
                Orden orden = ordenRepo.findById(Long.parseLong(external_reference)).orElse(null);
                if (orden != null) {
                    model.addAttribute("orden", orden);
                    model.addAttribute("mp_id", collection_id);
                    return "compra_exitosa";
                }
            } catch(Exception e) {
                // Ignore parse errors
            }
        }
        return "redirect:/orden/mis-compras"; 
    }

    @GetMapping("/pending")
    public String pagoPendiente() {
        return "redirect:/orden/mis-compras?estado=pendiente";
    }

    @GetMapping("/failure")
    public String pagoFallo(Model model) {
        model.addAttribute("error", "El pago fue rechazado por Mercado Pago. Intenta usar otra tarjeta.");
        return "redirect:/orden/checkout?error=pago_rechazado";
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