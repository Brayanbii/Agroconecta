package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.model.*;
import com.proyecto.AccesoUsuarios.repository.OrdenRepository;
import com.proyecto.AccesoUsuarios.repository.ProductoRepository;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import com.proyecto.AccesoUsuarios.service.AuthUsuarioService;
import com.proyecto.AccesoUsuarios.service.CarritoService;
import com.proyecto.AccesoUsuarios.service.EnvioService;
import com.proyecto.AccesoUsuarios.service.MercadoPagoService;
import com.proyecto.AccesoUsuarios.service.OrdenEstadoService;
import com.proyecto.AccesoUsuarios.service.UnidadConversionService;
import com.mercadopago.resources.preference.Preference;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/ordenes")
public class OrdenAPIController {

    @Autowired private CarritoService carritoService;
    @Autowired private ProductoRepository productoRepo;
    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private OrdenRepository ordenRepo;
    @Autowired private MercadoPagoService mercadoPagoService;
    @Autowired private AuthUsuarioService authUsuarioService;
    @Autowired private EnvioService envioService;
    @Autowired private UnidadConversionService conversionService;

    /**
     * PREVISUALIZAR costo de envio ANTES de crear la orden.
     * Se llama cuando el cliente cambia el tipo de entrega o la direccion.
     */
    @PostMapping("/preview-envio")
    public ResponseEntity<Map<String, Object>> previewEnvio(Authentication auth,
            @RequestBody Map<String, Object> body) {
        Map<String, Object> resp = new HashMap<>();
        if (carritoService.obtenerItems().isEmpty()) {
            resp.put("success", false); resp.put("error", "Carrito vacio"); return ResponseEntity.ok(resp);
        }

        try {
            String tipoEnvio = body.getOrDefault("tipoEnvio", "ECONOMICO").toString();
            Double latCliente = body.containsKey("latitud") ? Double.parseDouble(body.get("latitud").toString()) : null;
            Double lonCliente = body.containsKey("longitud") ? Double.parseDouble(body.get("longitud").toString()) : null;

            // Obtener coordenadas del campesino del primer producto
            ItemCarrito primerItem = carritoService.obtenerItems().get(0);
            Producto primerProd = primerItem.getProducto();
            Double latOrigen = primerProd.getLatitudOrigen();
            Double lonOrigen = primerProd.getLongitudOrigen();

            // Calcular peso total usando conversion de unidades colombianas
            double pesoKg = carritoService.obtenerItems().stream()
                .mapToDouble(i -> conversionService.convertirAKg(
                    i.getCantidad(),
                    i.getProducto() != null ? i.getProducto().getUnidad() : "Kg",
                    i.getProducto() != null ? i.getProducto().getCategoria() : null
                )).sum();

            double distanciaKm = 0;
            if (latOrigen != null && lonOrigen != null && latCliente != null && lonCliente != null) {
                distanciaKm = envioService.calcularDistanciaKm(latOrigen, lonOrigen, latCliente, lonCliente);
            }

            double costoEnvio = envioService.calcularCostoEnvio(distanciaKm, pesoKg, tipoEnvio);
            double subtotal = carritoService.obtenerTotal();
            EnvioService.DesglosePago desglose = envioService.calcularDesglose(subtotal, costoEnvio);

            resp.put("success", true);
            resp.put("distanciaKm", distanciaKm);
            resp.put("pesoKg", pesoKg);
            resp.put("costoEnvio", Math.round(costoEnvio * 100.0) / 100.0);
            resp.put("subtotal", desglose.subtotalProductos);
            resp.put("tarifaPlataforma", desglose.tarifaPlataforma);
            resp.put("costoPasarela", desglose.costoPasarela);
            resp.put("total", desglose.total);
            resp.put("desglose", Map.of(
                "campesino", desglose.pagoCampesino,
                "delivery", desglose.pagoDelivery,
                "plataforma", desglose.gananciaPlataforma,
                "pasarela", desglose.costoPasarela
            ));
        } catch (Exception e) {
            resp.put("success", false); resp.put("error", e.getMessage());
        }
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/crear")
    public ResponseEntity<Map<String, Object>> crearOrden(Authentication auth, HttpServletRequest request,
                                                           @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> resp = new HashMap<>();
        if (carritoService.obtenerItems().isEmpty()) {
            resp.put("success", false); resp.put("error", "Carrito vacio"); return ResponseEntity.ok(resp);
        }
        for (ItemCarrito item : carritoService.obtenerItems()) {
            Producto p = productoRepo.findById(item.getProducto().getId()).orElse(null);
            if (p == null) { resp.put("success", false); resp.put("error", "Producto no disponible"); return ResponseEntity.ok(resp); }
            if (p.getStock() == null || p.getStock() < item.getCantidad()) {
                resp.put("success", false); resp.put("error", "Stock insuficiente: " + p.getNombre()); return ResponseEntity.ok(resp);
            }
        }
        Usuario usuario = authUsuarioService.getAuthenticatedUser(auth);
        if (usuario == null) { resp.put("success", false); resp.put("error", "Usuario no encontrado"); return ResponseEntity.ok(resp); }

        // Leer datos del body
        String tipoEnvio = "ECONOMICO";
        String dirEnvio = "No especificada";
        Double latCliente = null, lngCliente = null;
        if (body != null) {
            if (body.containsKey("tipoEnvio")) tipoEnvio = body.get("tipoEnvio").toString();
            if (body.containsKey("direccionEnvio")) dirEnvio = body.get("direccionEnvio").toString();
            if (body.containsKey("latitud")) latCliente = Double.parseDouble(body.get("latitud").toString());
            if (body.containsKey("longitud")) lngCliente = Double.parseDouble(body.get("longitud").toString());
        }

        double subtotal = carritoService.obtenerTotal();

        // Obtener coordenadas del campesino del primer producto
        ItemCarrito primerItem = carritoService.obtenerItems().get(0);
        Producto primerProd = primerItem.getProducto();
        Double latOrigen = primerProd.getLatitudOrigen();
        Double lonOrigen = primerProd.getLongitudOrigen();

        // Calcular peso total
        double pesoKg = carritoService.obtenerItems().stream()
            .mapToDouble(i -> i.getCantidad()).sum();

        // Calcular distancia
        double distanciaKm = 0;
        if (latOrigen != null && lonOrigen != null && latCliente != null && lngCliente != null) {
            distanciaKm = envioService.calcularDistanciaKm(latOrigen, lonOrigen, latCliente, lngCliente);
        }

        // Calcular costo de envio dinamico
        double costoEnvio = envioService.calcularCostoEnvio(distanciaKm, pesoKg, tipoEnvio);

        // Calcular desglose completo
        EnvioService.DesglosePago desglose = envioService.calcularDesglose(subtotal, costoEnvio);
        double total = desglose.total;

        Orden orden = new Orden();
        orden.setFechaCreacion(LocalDateTime.now());
        orden.setNumeroOrden(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        orden.setUsuario(usuario);
        orden.setTotal(total);
        orden.setEstado(OrdenEstadoService.PENDIENTE);
        orden.setDireccionEnvio(dirEnvio);
        orden.setLatitudEnvio(latCliente);
        orden.setLongitudEnvio(lngCliente);

        // Nuevos campos de delivery
        orden.setTipoEnvio(tipoEnvio);
        orden.setCostoEnvio(costoEnvio);
        orden.setSubtotalProductos(subtotal);
        orden.setTarifaPlataforma(desglose.tarifaPlataforma);
        orden.setCostoPasarela(desglose.costoPasarela);
        orden.setPesoTotalKg(pesoKg);
        orden.setLatitudOrigen(latOrigen);
        orden.setLongitudOrigen(lonOrigen);
        orden.setMunicipioOrigen(primerProd.getMunicipioOrigen());

        // Fecha limite: 3 dias para economico, 1 dia para rapido
        if ("ECONOMICO".equals(tipoEnvio)) {
            orden.setFechaLimiteEntrega(LocalDateTime.now().plusDays(3));
            // En sandbox: orden va directo a PENDIENTE_CAMPESINO
            // En produccion: el webhook de MercadoPago la moveria a PAGADO -> PENDIENTE_CAMPESINO
            orden.setEstado(OrdenEstadoService.PENDIENTE_CAMPESINO);
            System.out.println("📋 Orden ECONOMICA #" + orden.getNumeroOrden() + " → PENDIENTE_CAMPESINO (esperando aceptacion del campesino)");
        } else {
            orden.setFechaLimiteEntrega(LocalDateTime.now().plusDays(1));
            orden.setEstado(OrdenEstadoService.BUSCANDO_REPARTIDOR);
            System.out.println("⚡ Orden RAPIDA #" + orden.getNumeroOrden() + " → BUSCANDO_REPARTIDOR");
        }

        List<DetalleOrden> detalles = new ArrayList<>();
        for (ItemCarrito item : carritoService.obtenerItems()) {
            DetalleOrden d = new DetalleOrden();
            d.setNombre(item.getProducto().getNombre());
            d.setCantidad(item.getCantidad());
            d.setPrecio(item.getProducto().getPrecio());
            d.setTotal(item.getTotal());
            d.setOrden(orden);
            d.setProducto(item.getProducto());
            d.setCampesinoId(item.getProducto().getUsuario().getId());
            detalles.add(d);
            Producto pa = productoRepo.findById(item.getProducto().getId()).orElseThrow();
            pa.setStock(pa.getStock() - item.getCantidad());
            productoRepo.save(pa);
        }
        orden.setDetalles(detalles);
        ordenRepo.save(orden);

        try {
            String serverUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            Preference preference = mercadoPagoService.crearPreferenciaDePago(
                carritoService.obtenerItems(), serverUrl, orden.getId().toString(), desglose.tarifaPlataforma, 0.0);
            carritoService.limpiarCarrito();
            resp.put("success", true);
            resp.put("initPoint", preference.getInitPoint());
            resp.put("ordenId", orden.getId());
            resp.put("total", total);
            Map<String, Object> desgloseResp = new HashMap<>();
            desgloseResp.put("subtotal", subtotal);
            desgloseResp.put("costoEnvio", costoEnvio);
            desgloseResp.put("tarifaPlataforma", desglose.tarifaPlataforma);
            desgloseResp.put("costoPasarela", desglose.costoPasarela);
            desgloseResp.put("total", total);
            desgloseResp.put("distanciaKm", distanciaKm);
            desgloseResp.put("pesoKg", pesoKg);
            desgloseResp.put("tipoEnvio", tipoEnvio);
            desgloseResp.put("campesinoRecibe", desglose.pagoCampesino);
            desgloseResp.put("deliveryRecibe", desglose.pagoDelivery);
            desgloseResp.put("plataformaRecibe", desglose.gananciaPlataforma);
            resp.put("desglose", desgloseResp);
        } catch (Exception e) {
            resp.put("success", false); resp.put("error", "Error MercadoPago: " + e.getMessage());
        }
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/mis-compras")
    public Map<String, Object> misComprasAPI(Authentication auth) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Usuario usuario = authUsuarioService.getAuthenticatedUser(auth);
            if (usuario == null) { resp.put("success", false); resp.put("error", "Usuario no encontrado"); return resp; }
            List<Orden> ordenes = ordenRepo.findByUsuarioOrderByFechaCreacionDesc(usuario);
            List<Map<String, Object>> items = new ArrayList<>();
            for (Orden o : ordenes) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", o.getId());
                map.put("numeroOrden", o.getNumeroOrden());
                map.put("fechaCreacion", o.getFechaCreacion() != null ? o.getFechaCreacion().toString() : null);
                map.put("total", o.getTotal());
                map.put("estado", o.getEstado());
                map.put("direccionEnvio", o.getDireccionEnvio());
                map.put("tipoEnvio", o.getTipoEnvio());
                map.put("costoEnvio", o.getCostoEnvio());
                List<Map<String, Object>> dets = new ArrayList<>();
                if (o.getDetalles() != null) {
                    for (DetalleOrden d : o.getDetalles()) {
                        Map<String, Object> di = new HashMap<>();
                        di.put("nombre", d.getNombre());
                        di.put("precio", d.getPrecio());
                        di.put("cantidad", d.getCantidad());
                        di.put("total", d.getTotal());
                        di.put("estado", d.getEstado());
                        di.put("imagenUrl", d.getProducto() != null ? d.getProducto().getImagenUrl() : "default.png");
                        di.put("unidad", d.getProducto() != null ? d.getProducto().getUnidad() : "Kg");
                        dets.add(di);
                    }
                }
                map.put("items", dets);
                items.add(map);
            }
            resp.put("success", true);
            resp.put("pedidos", items);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("error", e.getMessage());
        }
        return resp;
    }
}
