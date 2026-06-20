package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.model.DetalleOrden;
import com.proyecto.AccesoUsuarios.model.Producto;
import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.model.Orden;
import com.proyecto.AccesoUsuarios.model.Resena;
import com.proyecto.AccesoUsuarios.repository.ProductoRepository;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import com.proyecto.AccesoUsuarios.repository.DetalleOrdenRepository;
import com.proyecto.AccesoUsuarios.repository.FavoritoCampesinoRepository;
import com.proyecto.AccesoUsuarios.repository.FavoritoProductoRepository;
import com.proyecto.AccesoUsuarios.repository.OrdenRepository;
import com.proyecto.AccesoUsuarios.service.PythonService;
import com.proyecto.AccesoUsuarios.service.UploadFileService;
import com.proyecto.AccesoUsuarios.service.OrdenEstadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/campesino/productos")
public class CampesinoController {

    @Autowired
    private ProductoRepository productoRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private DetalleOrdenRepository detalleRepo;

    @Autowired
    private OrdenRepository ordenRepo;

    @Autowired
    private UploadFileService uploadService;

    @Autowired
    private PythonService pythonService;
    
    @Autowired
    private FavoritoCampesinoRepository favoritoCampesinoRepo;

    @Autowired
    private FavoritoProductoRepository favoritoProductoRepo;

    @GetMapping("/nuevo")
    public String nuevoProducto(Model model, Authentication auth) {
        String email = auth.getName();
        Usuario campesino = usuarioRepo.findFirstByEmail(email).orElseThrow();

        // --- VALIDACIÓN KYC ---
        if (!"APROBADO".equals(campesino.getEstadoVerificacion())) {
            return "redirect:/campesino/verificacion";
        }

        Producto p = new Producto();
        // Cargar ubicación por defecto del campesino (si la tiene)
        if (campesino.getLatitud() != null && campesino.getLongitud() != null) {
            p.setLatitudOrigen(campesino.getLatitud());
            p.setLongitudOrigen(campesino.getLongitud());
            // No seteamos el municipio aún, el JS en el cliente hará el Reverse Geocoding
        }

        model.addAttribute("producto", p);

        // --- CONEXIÓN CON PYTHON: Precios de Referencia ---
        Map<String, Object> respuesta = pythonService.obtenerPreciosDesdePython();
        if (respuesta != null) {
            model.addAttribute("preciosReferencia", respuesta.get("data"));
            model.addAttribute("fuentePrecios", respuesta.get("fuente"));
        }
        // --------------------------------------------------

        return "campesino_producto_form";
    }

    @GetMapping("/prueba")
    public String modoPrueba(Model model, Authentication auth) {
        String email = auth.getName();
        Usuario campesino = usuarioRepo.findFirstByEmail(email).orElseThrow();

        // --- VALIDACIÓN KYC ---
        if (!"APROBADO".equals(campesino.getEstadoVerificacion())) {
            return "redirect:/campesino/verificacion";
        }

        Producto p = new Producto();
        model.addAttribute("producto", p);

        // --- CONEXIÓN CON PYTHON: Precios de Referencia ---
        Map<String, Object> respuesta = pythonService.obtenerPreciosDesdePython();
        if (respuesta != null) {
            model.addAttribute("preciosReferencia", respuesta.get("data"));
            model.addAttribute("fuentePrecios", respuesta.get("fuente"));
        }
        // --------------------------------------------------

        return "campesino_producto_prueba";
    }

    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute Producto producto, 
                                  @RequestParam("img") MultipartFile file,
                                  @RequestParam(value = "img2", required = false) MultipartFile file2,
                                  @RequestParam(value = "img3", required = false) MultipartFile file3,
                                  @RequestParam(value = "img4", required = false) MultipartFile file4,
                                  Authentication auth) throws IOException {
        
        String email = auth.getName();
        Usuario campesino = usuarioRepo.findFirstByEmail(email).orElseThrow();
        producto.setUsuario(campesino);

        // LÓGICA HÍBRIDA (Archivo vs Link)
        
        // 1. ¿Subió la primera foto?
        if (!file.isEmpty()) {
            String nombreImagen = uploadService.saveImage(file);
            producto.setImagenUrl(nombreImagen);
        } else {
            if (producto.getId() == null) {
                if (producto.getImagenUrl() == null || producto.getImagenUrl().isEmpty()) {
                    producto.setImagenUrl("default.jpg");
                }
            } else {
                Producto p = productoRepo.findById(producto.getId()).get();
                if (producto.getImagenUrl() == null || producto.getImagenUrl().isEmpty()) {
                    producto.setImagenUrl(p.getImagenUrl());
                }
            }
        }

        // --- IMAGEN ADICIONAL 2 ---
        if (file2 != null && !file2.isEmpty()) {
            String nombreImagen2 = uploadService.saveImage(file2);
            producto.setImagenUrl2(nombreImagen2);
        } else {
            if (producto.getImagenUrl2() == null || producto.getImagenUrl2().trim().isEmpty()) {
                if (producto.getId() != null) {
                    Producto p = productoRepo.findById(producto.getId()).orElse(null);
                    if (p != null && p.getImagenUrl2() != null && !p.getImagenUrl2().startsWith("http")) {
                        uploadService.deleteImage(p.getImagenUrl2());
                    }
                }
                producto.setImagenUrl2(null);
            } else {
                if (producto.getId() != null) {
                    Producto p = productoRepo.findById(producto.getId()).orElse(null);
                    if (p != null && (producto.getImagenUrl2() == null || producto.getImagenUrl2().isEmpty())) {
                        producto.setImagenUrl2(p.getImagenUrl2());
                    }
                }
            }
        }

        // --- IMAGEN ADICIONAL 3 ---
        if (file3 != null && !file3.isEmpty()) {
            String nombreImagen3 = uploadService.saveImage(file3);
            producto.setImagenUrl3(nombreImagen3);
        } else {
            if (producto.getImagenUrl3() == null || producto.getImagenUrl3().trim().isEmpty()) {
                if (producto.getId() != null) {
                    Producto p = productoRepo.findById(producto.getId()).orElse(null);
                    if (p != null && p.getImagenUrl3() != null && !p.getImagenUrl3().startsWith("http")) {
                        uploadService.deleteImage(p.getImagenUrl3());
                    }
                }
                producto.setImagenUrl3(null);
            } else {
                if (producto.getId() != null) {
                    Producto p = productoRepo.findById(producto.getId()).orElse(null);
                    if (p != null && (producto.getImagenUrl3() == null || producto.getImagenUrl3().isEmpty())) {
                        producto.setImagenUrl3(p.getImagenUrl3());
                    }
                }
            }
        }

        // --- IMAGEN ADICIONAL 4 ---
        if (file4 != null && !file4.isEmpty()) {
            String nombreImagen4 = uploadService.saveImage(file4);
            producto.setImagenUrl4(nombreImagen4);
        } else {
            if (producto.getImagenUrl4() == null || producto.getImagenUrl4().trim().isEmpty()) {
                if (producto.getId() != null) {
                    Producto p = productoRepo.findById(producto.getId()).orElse(null);
                    if (p != null && p.getImagenUrl4() != null && !p.getImagenUrl4().startsWith("http")) {
                        uploadService.deleteImage(p.getImagenUrl4());
                    }
                }
                producto.setImagenUrl4(null);
            } else {
                if (producto.getId() != null) {
                    Producto p = productoRepo.findById(producto.getId()).orElse(null);
                    if (p != null && (producto.getImagenUrl4() == null || producto.getImagenUrl4().isEmpty())) {
                        producto.setImagenUrl4(p.getImagenUrl4());
                    }
                }
            }
        }

        boolean esNuevo = (producto.getId() == null);
        productoRepo.save(producto);
        
        String nombreCodificado = java.net.URLEncoder.encode(producto.getNombre(), java.nio.charset.StandardCharsets.UTF_8.toString());
        if (esNuevo) {
            return "redirect:/campesino/productos?creadoExito=" + nombreCodificado;
        } else {
            return "redirect:/campesino/productos?editadoExito=" + nombreCodificado;
        }
    }

    @GetMapping("/editar/{id}")
    public String editarProducto(@PathVariable Long id, Model model) {
        Producto producto = productoRepo.findById(id).orElseThrow();
        model.addAttribute("producto", producto);

        // --- CONEXIÓN CON PYTHON: Precios de Referencia ---
        Map<String, Object> respuesta = pythonService.obtenerPreciosDesdePython();
        if (respuesta != null) {
            model.addAttribute("preciosReferencia", respuesta.get("data"));
            model.addAttribute("fuentePrecios", respuesta.get("fuente"));
        }
        // --------------------------------------------------

        return "campesino_producto_form";
    }

    @Transactional
    @PostMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id) {
        Producto p = productoRepo.findById(id).get();

        // 1. Desvincular el producto de sus DetalleOrden para evitar error de FK
        //    (el historial de ventas se preserva: nombre, precio, cantidad, total ya estan guardados)
        List<DetalleOrden> detalles = detalleRepo.findByProducto(p);
        for (DetalleOrden d : detalles) {
            d.setProducto(null);
            detalleRepo.save(d);
        }

        // 2. Solo borramos el archivo si NO es un link de internet y NO es la default
        if (p.getImagenUrl() != null
                && !p.getImagenUrl().startsWith("http")
                && !"default.jpg".equals(p.getImagenUrl())) {
            uploadService.deleteImage(p.getImagenUrl());
        }
        if (p.getImagenUrl2() != null
                && !p.getImagenUrl2().startsWith("http")) {
            uploadService.deleteImage(p.getImagenUrl2());
        }
        if (p.getImagenUrl3() != null
                && !p.getImagenUrl3().startsWith("http")) {
            uploadService.deleteImage(p.getImagenUrl3());
        }
        if (p.getImagenUrl4() != null
                && !p.getImagenUrl4().startsWith("http")) {
            uploadService.deleteImage(p.getImagenUrl4());
        }

        // 3. Borrar el producto
        productoRepo.deleteById(id);
        return "redirect:/campesino/productos";
    }

    @GetMapping("/pedidos")
    public String misPedidos(Model model, Authentication auth) {
        String email = auth.getName();
        Usuario campesino = usuarioRepo.findFirstByEmail(email).orElseThrow();
        List<DetalleOrden> ventas = detalleRepo.findVentasByCampesino(campesino, campesino.getId());
        
        // Agrupar por estado para la vista
        long nuevos = ventas.stream().filter(v -> "NUEVO".equals(v.getEstado()) || v.getEstado() == null).count();
        long preparados = ventas.stream().filter(v -> "PREPARADO".equals(v.getEstado())).count();
        long listos = ventas.stream().filter(v -> "LISTO_PARA_RECOGER".equals(v.getEstado())).count();
        long enCamino = ventas.stream().filter(v -> "ENVIADO".equals(v.getEstado())).count();
        long entregados = ventas.stream().filter(v -> "ENTREGADO".equals(v.getEstado())).count();
        long cancelados = ventas.stream().filter(v -> "CANCELADO".equals(v.getEstado())).count();
        
        model.addAttribute("ventas", ventas);
        model.addAttribute("nuevosCount", nuevos);
        model.addAttribute("preparadosCount", preparados);
        model.addAttribute("listosCount", listos);
        model.addAttribute("enCaminoCount", enCamino);
        model.addAttribute("entregadosCount", entregados);
        model.addAttribute("canceladosCount", cancelados);
        
        for (DetalleOrden v : ventas) {
            if ("LISTO_PARA_RECOGER".equals(v.getEstado()) && v.getOrden() != null) {
                Orden orden = v.getOrden();
                Map<String, Object> info = new HashMap<>();
                info.put("codigoRecogida", orden.getCodigoRecogida());
                if (orden.getRuta() != null) {
                    info.put("codigoRuta", orden.getRuta().getCodigoRuta());
                    info.put("estadoRuta", orden.getRuta().getEstado());
                    if (orden.getRuta().getRepartidor() != null) {
                            Usuario rep = orden.getRuta().getRepartidor();
                            info.put("repNombre", rep.getNombreCompleto());
                            info.put("repTelefono", rep.getTelefono());
                            info.put("repVehiculo", rep.getTipoVehiculo());
                            info.put("repPlaca", rep.getPlacaVehiculo());
                            info.put("repRating", rep.getCalificacionPromedio() != null ? rep.getCalificacionPromedio() : 0.0);
                            info.put("repLat", rep.getLatitud());
                            info.put("repLng", rep.getLongitud());
                            
                            Double fLat = campesino.getLatitud();
                            Double fLng = campesino.getLongitud();
                            if (fLat == null || fLng == null) { fLat = 5.9317; fLng = -73.6147; }
                        info.put("fincaLat", fLat);
                            info.put("fincaLng", fLng);
                    }
                }
                try {
                    v.setRepartidorInfoJson(new ObjectMapper().writeValueAsString(info));
                } catch (Exception e) {
                    v.setRepartidorInfoJson(null);
                }
            }
        }
        
        return "campesino_pedidos";
    }

    @PostMapping("/pedidos/{id}/estado")
    public String actualizarEstadoPedido(@PathVariable Long id, @RequestParam String estado, Authentication auth) {
        DetalleOrden detalle = detalleRepo.findById(id).orElseThrow();
        Usuario campesino = usuarioRepo.findFirstByEmail(auth.getName()).orElseThrow();
        
        // Verificar que el detalle pertenezca a un producto de este campesino
        if (detalle.getProducto() != null && detalle.getProducto().getUsuario().getId().equals(campesino.getId())) {
            detalle.setEstado(estado);
            detalleRepo.save(detalle);

            // Si el campesino acepta (PREPARADO), mover orden a cola de agrupacion
            if ("PREPARADO".equals(estado) && detalle.getOrden() != null) {
                Orden orden = detalle.getOrden();
                if (OrdenEstadoService.PENDIENTE_CAMPESINO.equals(orden.getEstado())
                        || "PENDIENTE".equals(orden.getEstado())) {
                    orden.setEstado(OrdenEstadoService.ESPERANDO_AGRUPACION);
                    ordenRepo.save(orden);
                }
            }
            
            // Si marca como LISTO_PARA_RECOGER, generar PIN y propagar a toda la orden
            if ("LISTO_PARA_RECOGER".equals(estado) && detalle.getOrden() != null) {
                Orden orden = ordenRepo.findById(detalle.getOrden().getId()).orElse(null);
                if (orden != null) {
                    String pin = orden.getCodigoRecogida();
                    if (pin == null || pin.isEmpty()) {
                        pin = String.valueOf(1000 + (int)(Math.random() * 899999));
                        orden.setCodigoRecogida(pin);
                        orden.setIntentosRecogida(0);
                        orden.setFechaGeneracionRecogida(java.time.LocalDateTime.now());
                        ordenRepo.saveAndFlush(orden);
                        System.out.println("[PIN] Generado PIN recogida: " + pin + " para orden #" + orden.getId());
                    } else {
                        System.out.println("[PIN] PIN ya existe: " + pin + " para orden #" + orden.getId());
                    }
                    // Marcar todos los detalles de esta orden como LISTO_PARA_RECOGER
                    if (orden.getDetalles() != null) {
                        for (DetalleOrden d : orden.getDetalles()) {
                            if (!"LISTO_PARA_RECOGER".equals(d.getEstado())) {
                                d.setEstado("LISTO_PARA_RECOGER");
                                detalleRepo.save(d);
                            }
                        }
                    }
                    detalleRepo.flush();
                }
            }
        }
        
        return "redirect:/campesino/productos/pedidos";
    }

    @GetMapping("/pedidos/estado-ajax/{id}")
    @ResponseBody
    public Map<String, Object> estadoPedidoAjax(@PathVariable Long id, Authentication auth) {
        Map<String, Object> res = new HashMap<>();
        try {
            DetalleOrden detalle = detalleRepo.findById(id).orElse(null);
            Usuario campesino = usuarioRepo.findFirstByEmail(auth.getName()).orElseThrow();
            if (detalle != null && detalle.getProducto() != null && detalle.getProducto().getUsuario().getId().equals(campesino.getId())) {
                res.put("estado", detalle.getEstado());
            } else {
                res.put("error", "No autorizado");
            }
        } catch (Exception e) {
            res.put("error", e.getMessage());
        }
        return res;
    }

    // -------------------------------------------------------
    // INFORMACION DEL REPARTIDOR — Página independiente
    // -------------------------------------------------------
    @GetMapping("/repartidor/{detalleId}")
    public String verRepartidor(@PathVariable Long detalleId, Model model, Authentication auth) {
        DetalleOrden detalle = detalleRepo.findById(detalleId).orElseThrow();
        Usuario campesino = usuarioRepo.findFirstByEmail(auth.getName()).orElseThrow();

        if (detalle.getProducto() == null || !detalle.getProducto().getUsuario().getId().equals(campesino.getId())) {
            return "redirect:/campesino/productos/pedidos";
        }

        Orden orden = detalle.getOrden();
        if (orden == null || orden.getRuta() == null || orden.getRuta().getRepartidor() == null) {
            return "redirect:/campesino/productos/pedidos";
        }

        Usuario rep = orden.getRuta().getRepartidor();

        model.addAttribute("detalle", detalle);
        model.addAttribute("codigoRecogida", orden.getCodigoRecogida());
        model.addAttribute("codigoRuta", orden.getRuta().getCodigoRuta());
        model.addAttribute("estadoRuta", orden.getRuta().getEstado());
        model.addAttribute("repNombre", rep.getNombreCompleto());
        model.addAttribute("repTelefono", rep.getTelefono());
        model.addAttribute("repVehiculo", rep.getTipoVehiculo());
        model.addAttribute("repPlaca", rep.getPlacaVehiculo());
        model.addAttribute("repRating", rep.getCalificacionPromedio() != null ? rep.getCalificacionPromedio() : 0.0);
        model.addAttribute("repLat", rep.getLatitud());
        model.addAttribute("repLng", rep.getLongitud());
        model.addAttribute("fincaLat", orden.getLatitudOrigen());
        model.addAttribute("fincaLng", orden.getLongitudOrigen());
        model.addAttribute("fincaNombre", campesino.getNombreFinca() != null ? campesino.getNombreFinca() : "Mi Finca");

        // Distancia Haversine
        if (rep.getLatitud() != null && rep.getLongitud() != null
                && orden.getLatitudOrigen() != null && orden.getLongitudOrigen() != null) {
            double d = haversine(rep.getLatitud(), rep.getLongitud(),
                    orden.getLatitudOrigen(), orden.getLongitudOrigen());
            model.addAttribute("distanciaKm", Math.round(d * 10.0) / 10.0);
            // ETA: ~40 km/h en promedio para moto en zona rural
            double etaMin = Math.round((d / 40.0) * 60.0);
            model.addAttribute("etaMinutos", (int) etaMin);
        } else {
            model.addAttribute("distanciaKm", null);
            model.addAttribute("etaMinutos", null);
        }

        return "campesino_repartidor_info";
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // -------------------------------------------------------
    // LOGISTICA PYTHON — Trazado de mapas con OSRM
    // -------------------------------------------------------
    @GetMapping("/logistica/{idDetalle}")
    public String verLogistica(@PathVariable Long idDetalle, Model model, Authentication auth) {
        DetalleOrden detalle = detalleRepo.findById(idDetalle).orElseThrow();
        Orden orden = detalle.getOrden();
        Usuario campesino = usuarioRepo.findFirstByEmail(auth.getName()).orElseThrow();

        // Prioridad 1: Ubicación exacta de la finca donde se registró el producto
        // Prioridad 2: Ubicación del campesino (perfil)
        // Prioridad 3: Barbosa, Santander (por defecto)
        Double origenLat = 5.9317;
        Double origenLon = -73.6147;

        if (detalle.getProducto() != null && detalle.getProducto().getLatitudOrigen() != null) {
            origenLat = detalle.getProducto().getLatitudOrigen();
            origenLon = detalle.getProducto().getLongitudOrigen();
        } else if (campesino.getLatitud() != null && campesino.getLongitud() != null) {
            origenLat = campesino.getLatitud();
            origenLon = campesino.getLongitud();
        }

        // Si el cliente no marcó en el mapa durante el checkout, usamos Bucaramanga por defecto
        Double destLat = orden.getLatitudEnvio() != null ? orden.getLatitudEnvio() : 7.1254;
        Double destLon = orden.getLongitudEnvio() != null ? orden.getLongitudEnvio() : -73.1198;

        Map<String, Object> origen = new HashMap<>();
        origen.put("lat", origenLat);
        origen.put("lon", origenLon);

        Map<String, Object> destino = new HashMap<>();
        destino.put("lat", destLat);
        destino.put("lon", destLon);

        Map<String, Object> payload = new HashMap<>();
        payload.put("origen", origen);
        payload.put("destino", destino);

        // Llamar a Python (OSRM)
        Map<String, Object> respuestaLogistica = pythonService.calcularRutaLogistica(payload);

        model.addAttribute("detalle", detalle);
        model.addAttribute("origenLat", origenLat);
        model.addAttribute("origenLon", origenLon);
        model.addAttribute("destLat", destLat);
        model.addAttribute("destLon", destLon);

        if (respuestaLogistica != null && respuestaLogistica.containsKey("geometria")) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                model.addAttribute("geometriaJson", mapper.writeValueAsString(respuestaLogistica.get("geometria")));
                model.addAttribute("distancia_km", respuestaLogistica.get("distancia_km"));
                model.addAttribute("duracion_min", respuestaLogistica.get("duracion_min"));
            } catch (Exception e) {
                System.out.println("Error parseando geometria: " + e.getMessage());
            }
        }

        return "campesino_logistica";
    }

    // -------------------------------------------------------
    // SUPER INFORME PYTHON — Reporte detallado con graficas
    // -------------------------------------------------------
    @GetMapping("/informe")
    public String superInforme(Model model, Authentication auth) {
        String email = auth.getName();
        Usuario campesino = usuarioRepo.findFirstByEmail(email).orElseThrow();

        // 1. Obtener todas las ventas del campesino
        List<DetalleOrden> ventas = detalleRepo.findVentasByCampesino(campesino, campesino.getId());

        // 2. Agrupar por producto (nombre, cantidad total, ingresos totales, precio promedio)
        Map<String, Map<String, Object>> porProducto = new LinkedHashMap<>();
        for (DetalleOrden d : ventas) {
            String nombre = d.getNombre() != null ? d.getNombre() : "Sin nombre";
            porProducto.computeIfAbsent(nombre, k -> {
                Map<String, Object> m = new HashMap<>();
                m.put("nombre", k);
                m.put("cantidad", 0);
                m.put("total", 0.0);
                m.put("precio_promedio", d.getPrecio() != null ? d.getPrecio() : 0.0);
                return m;
            });
            Map<String, Object> entry = porProducto.get(nombre);
            entry.put("cantidad", (int) entry.get("cantidad") + (d.getCantidad() != null ? d.getCantidad() : 0));
            entry.put("total", (double) entry.get("total") + (d.getTotal() != null ? d.getTotal() : 0.0));
        }
        List<Map<String, Object>> productos = new ArrayList<>(porProducto.values());

        // 3. Agrupar por mes (nombre del mes, ingresos totales)
        String[] MESES = {"Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"};
        Map<Integer, Double> porMes = new LinkedHashMap<>();
        for (DetalleOrden d : ventas) {
            if (d.getOrden() != null && d.getOrden().getFechaCreacion() != null) {
                int mes = d.getOrden().getFechaCreacion().getMonthValue();
                porMes.merge(mes, d.getTotal() != null ? d.getTotal() : 0.0, Double::sum);
            }
        }
        List<Map<String, Object>> ventasMes = new ArrayList<>();
        for (Map.Entry<Integer, Double> entry : porMes.entrySet()) {
            Map<String, Object> m = new HashMap<>();
            m.put("mes", MESES[entry.getKey() - 1]);
            m.put("total", entry.getValue());
            ventasMes.add(m);
        }

        // 4. Calcular resumen estadístico
        double totalIngresos = ventas.stream().mapToDouble(d -> d.getTotal() != null ? d.getTotal() : 0.0).sum();
        int totalUnidades    = ventas.stream().mapToInt(d -> d.getCantidad() != null ? d.getCantidad() : 0).sum();
        String productoEstrella = productos.stream()
                .max((a, b) -> Integer.compare((int) a.get("cantidad"), (int) b.get("cantidad")))
                .map(p -> (String) p.get("nombre")).orElse("N/A");
        String mejorMes = ventasMes.stream()
                .max((a, b) -> Double.compare((double) a.get("total"), (double) b.get("total")))
                .map(m -> (String) m.get("mes")).orElse("N/A");

        Map<String, Object> resumen = new HashMap<>();
        resumen.put("total_ingresos", totalIngresos);
        resumen.put("total_unidades", totalUnidades);
        resumen.put("total_productos", porProducto.size());
        resumen.put("producto_estrella", productoEstrella);
        resumen.put("mejor_mes", mejorMes);
        resumen.put("nombre_campesino", campesino.getNombreCompleto() != null ? campesino.getNombreCompleto() : email);

        // 4.5. Extraer coordenadas para el mapa de calor
        List<Map<String, Object>> coordenadas = new ArrayList<>();
        for (DetalleOrden d : ventas) {
            if (d.getOrden() != null && d.getOrden().getLatitudEnvio() != null && d.getOrden().getLongitudEnvio() != null) {
                Map<String, Object> coord = new HashMap<>();
                coord.put("lat", d.getOrden().getLatitudEnvio());
                coord.put("lng", d.getOrden().getLongitudEnvio());
                // Usar cantidad como intensidad del calor
                coord.put("intensidad", d.getCantidad() != null ? d.getCantidad() : 1);
                coordenadas.add(coord);
            }
        }

        // 5. Enviar a Python y recibir gráficas
        Map<String, Object> payload = new HashMap<>();
        payload.put("productos", productos);
        payload.put("ventas_mes", ventasMes);
        payload.put("resumen", resumen);

        Map<String, Object> informe = pythonService.generarInformeCampesino(payload);

        // 6. Pasar todo al modelo
        model.addAttribute("resumen", resumen);
        model.addAttribute("totalVentas", ventas.size());
        if (informe != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                model.addAttribute("graficoTopProductos", mapper.writeValueAsString(informe.get("grafico_top_productos")));
                model.addAttribute("graficoIngresosMes",  mapper.writeValueAsString(informe.get("grafico_ingresos_mes")));
                model.addAttribute("graficoDistribucion", mapper.writeValueAsString(informe.get("grafico_distribucion")));
                model.addAttribute("graficoVsMercado",    mapper.writeValueAsString(informe.get("grafico_vs_mercado")));
                model.addAttribute("coordenadasVentas",   mapper.writeValueAsString(coordenadas));
            } catch (Exception e) {
                System.out.println("Error serializando JSON de informe campesino: " + e.getMessage());
            }
        }

        return "campesino_informe";
    }

    // -------------------------------------------------------
    // REPUTACIÓN Y RESEÑAS
    // -------------------------------------------------------
    @Transactional
    @GetMapping("/reputacion")
    public String verReputacion(Model model, Authentication auth) {
        String email = auth.getName();
        Usuario campesino = usuarioRepo.findFirstByEmail(email).orElseThrow();
        
        List<Producto> productos = productoRepo.findByUsuario(campesino);
        
        List<Resena> buenasResenas = new ArrayList<>();
        List<Resena> oportunidadesMejora = new ArrayList<>();
        
        double sumaCalificaciones = 0;
        int totalResenas = 0;
        int[] distribucion = new int[5]; // índice 0=1estrella, 4=5estrellas
        
        for (Producto p : productos) {
            if (p.getResenas() != null) {
                for (Resena r : p.getResenas()) {
                    totalResenas++;
                    sumaCalificaciones += r.getEstrellas();
                    distribucion[r.getEstrellas() - 1]++;
                    if (r.getEstrellas() >= 4) {
                        buenasResenas.add(r);
                    } else {
                        oportunidadesMejora.add(r);
                    }
                }
            }
        }
        
        double calificacionGeneral = totalResenas > 0 ? sumaCalificaciones / totalResenas : 0.0;
        calificacionGeneral = Math.round(calificacionGeneral * 10.0) / 10.0;
        
        // Porcentaje de reseñas positivas (4-5 estrellas)
        int porcentajePositivo = totalResenas > 0 ? (int) Math.round((buenasResenas.size() * 100.0) / totalResenas) : 0;
        
        // Ordenar reseñas por fecha desc (más recientes primero)
        buenasResenas.sort((a, b) -> b.getFecha().compareTo(a.getFecha()));
        oportunidadesMejora.sort((a, b) -> b.getFecha().compareTo(a.getFecha()));
        
        // Filtrar solo productos con promedio >= 3.5 y que tengan reseñas, ordenados por calificación desc
        List<Producto> mejoresProductos = new ArrayList<>();
        for (Producto p : productos) {
            if (p.getResenas() != null && !p.getResenas().isEmpty() && p.getPromedioCalificacion() >= 3.5) {
                mejoresProductos.add(p);
            }
        }
        mejoresProductos.sort((a, b) -> Double.compare(b.getPromedioCalificacion(), a.getPromedioCalificacion()));
        
        // Producto estrella (el mejor calificado)
        String productoEstrella = mejoresProductos.isEmpty() ? "—" : mejoresProductos.get(0).getNombre();
        
        model.addAttribute("campesino", campesino);
        model.addAttribute("productos", productos);
        model.addAttribute("mejoresProductos", mejoresProductos);
        model.addAttribute("buenasResenas", buenasResenas);
        model.addAttribute("oportunidadesMejora", oportunidadesMejora);
        model.addAttribute("calificacionGeneral", calificacionGeneral);
        model.addAttribute("totalResenas", totalResenas);
        model.addAttribute("porcentajePositivo", porcentajePositivo);
        model.addAttribute("productoEstrella", productoEstrella);
        model.addAttribute("dist1", distribucion[0]);
        model.addAttribute("dist2", distribucion[1]);
        model.addAttribute("dist3", distribucion[2]);
        model.addAttribute("dist4", distribucion[3]);
        model.addAttribute("dist5", distribucion[4]);
        
        // --- NUEVO: ESTADÍSTICAS DE FAVORITOS (LIKES) ---
        int likesPerfil = favoritoCampesinoRepo.countByCampesino(campesino);
        int likesProductos = favoritoProductoRepo.countByProducto_Usuario(campesino);
        
        model.addAttribute("likesPerfil", likesPerfil);
        model.addAttribute("likesProductos", likesProductos);
        
        return "campesino_reputacion";
    }
    // --- GESTIÓN RÁPIDA DE INVENTARIO ---
    @GetMapping("/inventario")
    public String verInventario(Model model, Authentication auth) {
        String email = auth.getName();
        Usuario campesino = usuarioRepo.findFirstByEmail(email).orElseThrow();
        
        if (!"APROBADO".equals(campesino.getEstadoVerificacion())) {
            return "redirect:/campesino/verificacion";
        }
        
        List<Producto> productos = productoRepo.findByUsuario(campesino);
        
        int stockTotal = 0;
        int agotados = 0;
        int bajoStock = 0;
        double valorInventario = 0;
        int productosConStockSano = 0;
        
        for (Producto p : productos) {
            int stock = p.getStock() != null ? p.getStock() : 0;
            double precio = p.getPrecio() != null ? p.getPrecio() : 0;
            stockTotal += stock;
            valorInventario += (precio * stock);
            if (stock == 0) agotados++;
            else if (stock < 10) bajoStock++;
            else productosConStockSano++;
        }
        
        int saludInventario = productos.isEmpty() ? 0 : (int) Math.round((productosConStockSano * 100.0) / productos.size());
        
        model.addAttribute("productos", productos);
        model.addAttribute("usuario", campesino);
        model.addAttribute("stockTotal", stockTotal);
        model.addAttribute("agotados", agotados);
        model.addAttribute("bajoStock", bajoStock);
        model.addAttribute("valorInventario", valorInventario);
        model.addAttribute("saludInventario", saludInventario);
        
        return "campesino_inventario";
    }
    
    @PostMapping("/inventario/actualizar")
    @ResponseBody
    public Map<String, Object> actualizarStockRapido(@RequestParam("productoId") Long id, @RequestParam("accion") String accion, @RequestParam(value="valor", defaultValue="1") Integer valor, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = auth.getName();
            Usuario campesino = usuarioRepo.findFirstByEmail(email).orElseThrow();
            Producto producto = productoRepo.findById(id).orElseThrow();
            
            // Seguridad: verificar que el producto pertenece a este campesino
            if (!producto.getUsuario().getId().equals(campesino.getId())) {
                response.put("success", false);
                response.put("error", "No autorizado");
                return response;
            }
            
            int stockActual = producto.getStock() != null ? producto.getStock() : 0;
            
            if ("sumar".equals(accion)) {
                producto.setStock(stockActual + valor);
            } else if ("restar".equals(accion)) {
                int nuevoStock = Math.max(0, stockActual - valor);
                producto.setStock(nuevoStock);
            } else if ("set".equals(accion)) {
                producto.setStock(Math.max(0, valor));
            }
            
            productoRepo.save(producto);
            
            response.put("success", true);
            response.put("nuevoStock", producto.getStock());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    // --- PERFIL DE CAMPESINO (STOREFRONT) ---
    @GetMapping("/perfil")
    public String verPerfil(Model model, Authentication auth) {
        String email = auth.getName();
        Usuario campesino = usuarioRepo.findFirstByEmail(email).orElseThrow();
        
        List<Producto> productos = productoRepo.findByUsuario(campesino);
        
        model.addAttribute("usuario", campesino);
        model.addAttribute("productos", productos);
        return "campesino_perfil";
    }

    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(
            @RequestParam("descripcionFinca") String descripcionFinca,
            @RequestParam("nombreFinca") String nombreFinca,
            @RequestParam(value = "fotoPerfilFile", required = false) MultipartFile fotoPerfilFile,
            @RequestParam(value = "fotoPortadaFile", required = false) MultipartFile fotoPortadaFile,
            @RequestParam(value = "borrarFotoPerfil", required = false, defaultValue = "false") boolean borrarFotoPerfil,
            @RequestParam(value = "borrarFotoPortada", required = false, defaultValue = "false") boolean borrarFotoPortada,
            Authentication auth) throws IOException {
            
        String email = auth.getName();
        Usuario campesino = usuarioRepo.findFirstByEmail(email).orElseThrow();
        
        campesino.setDescripcionFinca(descripcionFinca);
        campesino.setNombreFinca(nombreFinca);
        
        // 1. Lógica para Foto de Perfil
        if (borrarFotoPerfil && (fotoPerfilFile == null || fotoPerfilFile.isEmpty())) {
            // Eliminar físico si existe localmente
            if (campesino.getFotoPerfil() != null && !campesino.getFotoPerfil().startsWith("http") && !"default.jpg".equals(campesino.getFotoPerfil())) {
                uploadService.deleteImage(campesino.getFotoPerfil());
            }
            campesino.setFotoPerfil(null);
        } else if (fotoPerfilFile != null && !fotoPerfilFile.isEmpty()) {
            // Eliminar físico de la imagen anterior para no acumular basura
            if (campesino.getFotoPerfil() != null && !campesino.getFotoPerfil().startsWith("http") && !"default.jpg".equals(campesino.getFotoPerfil())) {
                uploadService.deleteImage(campesino.getFotoPerfil());
            }
            String nombreImagen = uploadService.saveImage(fotoPerfilFile);
            campesino.setFotoPerfil(nombreImagen);
        }
        
        // 2. Lógica para Foto de Portada
        if (borrarFotoPortada && (fotoPortadaFile == null || fotoPortadaFile.isEmpty())) {
            // Eliminar físico si existe localmente
            if (campesino.getFotoFincaUrl() != null && !campesino.getFotoFincaUrl().startsWith("http")) {
                uploadService.deleteImage(campesino.getFotoFincaUrl());
            }
            campesino.setFotoFincaUrl(null);
        } else if (fotoPortadaFile != null && !fotoPortadaFile.isEmpty()) {
            // Eliminar físico de la imagen anterior para no acumular basura
            if (campesino.getFotoFincaUrl() != null && !campesino.getFotoFincaUrl().startsWith("http")) {
                uploadService.deleteImage(campesino.getFotoFincaUrl());
            }
            String nombreImagen = uploadService.saveImage(fotoPortadaFile);
            campesino.setFotoFincaUrl(nombreImagen);
        }
        
        usuarioRepo.save(campesino);
        return "redirect:/campesino/productos/perfil?exito=true";
    }

    // -------------------------------------------------------
    // MI PERFIL / MI FINCA — Edición web (consume API REST)
    // -------------------------------------------------------
    @GetMapping("/mi-finca")
    public String miFinca(Model model, Authentication auth) {
        String email = auth.getName();
        Usuario campesino = usuarioRepo.findFirstByEmail(email).orElseThrow();
        model.addAttribute("usuario", campesino);
        return "campesino_mi_finca";
    }

    // -------------------------------------------------------
    // AGROWALLET — HISTORIAL FINANCIERO
    // -------------------------------------------------------
    @GetMapping("/finanzas")
    public String verFinanzas(Model model, Authentication auth) {
        String email = auth.getName();
        Usuario campesino = usuarioRepo.findFirstByEmail(email).orElseThrow();

        List<DetalleOrden> ventas = detalleRepo.findVentasByCampesino(campesino, campesino.getId());

        // --- CÁLCULOS FINANCIEROS ---
        double ingresosBrutos = 0;
        double pagoPendiente = 0;
        int totalTransacciones = 0;
        int transaccionesCompletadas = 0;

        String[] MESES = {"Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"};
        Map<Integer, Double> ingresosPorMes = new LinkedHashMap<>();

        // Agrupar ventas por Orden para generar movimientos financieros
        Map<Long, List<DetalleOrden>> ventasPorOrden = new LinkedHashMap<>();
        for (DetalleOrden d : ventas) {
            if (d.getOrden() != null) {
                ventasPorOrden.computeIfAbsent(d.getOrden().getId(), k -> new ArrayList<>()).add(d);
            }
        }

        // Generar movimientos financieros (estilo extracto bancario)
        List<Map<String, Object>> historial = new ArrayList<>();

        for (Map.Entry<Long, List<DetalleOrden>> entry : ventasPorOrden.entrySet()) {
            List<DetalleOrden> detalles = entry.getValue();
            Orden orden = detalles.get(0).getOrden();
            if (orden == null) continue;

            double montoOrden = 0;
            for (DetalleOrden d : detalles) {
                montoOrden += d.getTotal() != null ? d.getTotal() : 0.0;
            }

            String estado = orden.getEstado() != null ? orden.getEstado().toUpperCase() : "NUEVO";
            String fecha = orden.getFechaCreacion() != null ? orden.getFechaCreacion().toLocalDate().toString() : "—";
            totalTransacciones++;

            // Agrupar por mes
            if (orden.getFechaCreacion() != null) {
                int mes = orden.getFechaCreacion().getMonthValue();
                ingresosPorMes.merge(mes, montoOrden, Double::sum);
            }

            if ("ENTREGADO".equals(estado) || "COMPLETADO".equals(estado)) {
                ingresosBrutos += montoOrden;
                transaccionesCompletadas++;
                double comision = montoOrden * 0.05;
                double neto = montoOrden - comision;

                // Movimiento 1: Ingreso recibido
                Map<String, Object> txIngreso = new HashMap<>();
                txIngreso.put("fecha", fecha);
                txIngreso.put("tipo", "INGRESO");
                txIngreso.put("icono", "fa-arrow-down");
                txIngreso.put("colorIcono", "green");
                txIngreso.put("descripcion", "Pago recibido — Pedido #" + orden.getId());
                txIngreso.put("monto", montoOrden);
                txIngreso.put("signo", "+");
                txIngreso.put("estado", "COMPLETADO");
                historial.add(txIngreso);

                // Movimiento 2: Comisión descontada
                Map<String, Object> txComision = new HashMap<>();
                txComision.put("fecha", fecha);
                txComision.put("tipo", "COMISION");
                txComision.put("icono", "fa-percent");
                txComision.put("colorIcono", "amber");
                txComision.put("descripcion", "Comisión AgroConecta (5%)");
                txComision.put("monto", comision);
                txComision.put("signo", "-");
                txComision.put("estado", "APLICADO");
                historial.add(txComision);

            } else if ("CANCELADO".equals(estado)) {
                // Movimiento: Orden cancelada
                Map<String, Object> txCancel = new HashMap<>();
                txCancel.put("fecha", fecha);
                txCancel.put("tipo", "CANCELADO");
                txCancel.put("icono", "fa-ban");
                txCancel.put("colorIcono", "red");
                txCancel.put("descripcion", "Pedido #" + orden.getId() + " cancelado");
                txCancel.put("monto", montoOrden);
                txCancel.put("signo", "x");
                txCancel.put("estado", "CANCELADO");
                historial.add(txCancel);

            } else {
                pagoPendiente += montoOrden;
                // Movimiento: Pago en espera
                Map<String, Object> txPend = new HashMap<>();
                txPend.put("fecha", fecha);
                txPend.put("tipo", "PENDIENTE");
                txPend.put("icono", "fa-hourglass-half");
                txPend.put("colorIcono", "blue");
                txPend.put("descripcion", "Pago retenido — Pedido #" + orden.getId() + " en proceso");
                txPend.put("monto", montoOrden);
                txPend.put("signo", "~");
                txPend.put("estado", "EN ESPERA");
                historial.add(txPend);
            }
        }

        // Ordenar por fecha descendente
        historial.sort((a, b) -> ((String) b.get("fecha")).compareTo((String) a.get("fecha")));
        if (historial.size() > 60) historial = historial.subList(0, 60);

        // Comisión total y neto
        double comisionTotal = ingresosBrutos * 0.05;
        double ingresosNetos = ingresosBrutos - comisionTotal;

        // Datos para gráfica
        List<Map<String, Object>> datosMensuales = new ArrayList<>();
        for (Map.Entry<Integer, Double> e : ingresosPorMes.entrySet()) {
            Map<String, Object> m = new HashMap<>();
            m.put("mes", MESES[e.getKey() - 1]);
            m.put("total", e.getValue());
            datosMensuales.add(m);
        }

        // --- PASAR AL MODELO ---
        model.addAttribute("usuario", campesino);
        model.addAttribute("ingresosBrutos", ingresosBrutos);
        model.addAttribute("ingresosNetos", ingresosNetos);
        model.addAttribute("comisionTotal", comisionTotal);
        model.addAttribute("pagoPendiente", pagoPendiente);
        model.addAttribute("totalTransacciones", totalTransacciones);
        model.addAttribute("transaccionesCompletadas", transaccionesCompletadas);
        model.addAttribute("historial", historial);

        try {
            ObjectMapper mapper = new ObjectMapper();
            model.addAttribute("datosMensualesJson", mapper.writeValueAsString(datosMensuales));
        } catch (Exception ex) {
            model.addAttribute("datosMensualesJson", "[]");
        }

        return "campesino_finanzas";
    }

    // Backfill: poblar campesinoId en registros existentes (ejecutar UNA vez)
    @Transactional
    @GetMapping("/admin/backfill-campesino-id")
    @ResponseBody
    public String backfillCampesinoId() {
        List<DetalleOrden> todos = detalleRepo.findAll();
        int fixed = 0;
        for (DetalleOrden d : todos) {
            if (d.getCampesinoId() == null && d.getProducto() != null && d.getProducto().getUsuario() != null) {
                d.setCampesinoId(d.getProducto().getUsuario().getId());
                detalleRepo.save(d);
                fixed++;
            }
        }
        return "Backfill completado: " + fixed + " registros actualizados";
    }

}
