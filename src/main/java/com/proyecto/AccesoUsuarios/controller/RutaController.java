package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.model.DetalleOrden;
import com.proyecto.AccesoUsuarios.model.Orden;
import com.proyecto.AccesoUsuarios.model.Ruta;
import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.OrdenRepository;
import com.proyecto.AccesoUsuarios.repository.RutaRepository;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import com.proyecto.AccesoUsuarios.service.*;
import com.proyecto.AccesoUsuarios.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rutas")
public class RutaController {

    @Autowired private RutaRepository rutaRepo;
    @Autowired private OrdenRepository ordenRepo;
    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private AuthUsuarioService authUsuarioService;
    @Autowired private RutaAgrupacionService agrupacionService;
    @Autowired private NotificationService notificationService;
    @Autowired private OrdenEstadoService ordenEstadoService;
    @Autowired private EnvioService envioService;
    @Autowired private PythonService pythonService;

    @GetMapping("/osrm-route")
    public ResponseEntity<Map<String, Object>> osrmRoute(
            @RequestParam double lat1, @RequestParam double lng1,
            @RequestParam double lat2, @RequestParam double lng2) {
        Map<String, Object> resp = new HashMap<>();
        try {
            String url = String.format(java.util.Locale.US,
                "https://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?overview=full&geometries=geojson",
                lng1, lat1, lng2, lat2);
            java.net.URL apiUrl = new java.net.URL(url);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) apiUrl.openConnection();
            conn.setRequestProperty("User-Agent", "AgroConecta/1.0");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            if (conn.getResponseCode() == 200) {
                String text = new String(conn.getInputStream().readAllBytes());
                conn.disconnect();
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                @SuppressWarnings("unchecked")
                Map<String, Object> data = mapper.readValue(text, Map.class);
                if ("Ok".equals(data.get("code"))) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> routes = (List<Map<String, Object>>) data.get("routes");
                    if (routes != null && !routes.isEmpty()) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> geom = (Map<String, Object>) routes.get(0).get("geometry");
                        @SuppressWarnings("unchecked")
                        List<List<Number>> coords = (List<List<Number>>) geom.get("coordinates");
                        if (coords != null && coords.size() > 1) {
                            List<List<Double>> puntos = new ArrayList<>();
                            for (List<Number> c : coords) {
                                puntos.add(List.of(c.get(1).doubleValue(), c.get(0).doubleValue()));
                            }
                            resp.put("success", true);
                            resp.put("points", puntos);
                            resp.put("distancia_km", Math.round(((Number)routes.get(0).get("distance")).doubleValue() / 10.0) / 100.0);
                            resp.put("duracion_min", ((Number)routes.get(0).get("duration")).doubleValue() / 60.0);
                            return ResponseEntity.ok(resp);
                        }
                    }
                }
                conn.disconnect();
            }
        } catch (Exception e) {
            System.out.println("[OSRM] Error directo: " + e.getMessage());
        }
        resp.put("success", false);
        return ResponseEntity.ok(resp);
    }

    /**
     * Lista rutas disponibles para repartidores con distancia desde su ubicacion.
     * Si se envian lat/lng, calcula los km hasta cada punto de recogida.
     * Si no hay GPS, usa la ciudad registrada del repartidor como filtro.
     * Radio por defecto: 200km.
     */
    @GetMapping("/disponibles")
    public ResponseEntity<Map<String, Object>> rutasDisponibles(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(defaultValue = "200") Double radioKm,
            Authentication auth) {

        Map<String, Object> resp = new HashMap<>();
        // Buscar rutas LISTA_PARA_SALIR + FORMANDOSE
        List<Ruta> rutasListas = rutaRepo.findByEstadoOrderByFechaCreacionAsc("LISTA_PARA_SALIR");
        List<Ruta> rutasFormando = rutaRepo.findByEstadoOrderByFechaCreacionAsc("FORMANDOSE");
        List<Ruta> todas = new ArrayList<>(rutasListas);
        todas.addAll(rutasFormando);

        // Ciudad del repartidor como fallback
        String ciudadRepartidor = null;
        if (lat == null || lng == null) {
            try {
                Usuario rep = authUsuarioService.getAuthenticatedUser(auth);
                if (rep != null && rep.getMunicipioOrigen() != null && !rep.getMunicipioOrigen().isBlank()) {
                    ciudadRepartidor = rep.getMunicipioOrigen().trim().toLowerCase();
                }
            } catch (Exception ignored) {}
        }

        List<Map<String, Object>> data = new ArrayList<>();
        for (Ruta r : todas) {
            double distanciaKm = 0;
            boolean incluir = true;

            if (lat != null && lng != null &&
                r.getLatitudCentroOrigen() != null && r.getLongitudCentroOrigen() != null) {
                distanciaKm = envioService.calcularDistanciaKm(lat, lng,
                    r.getLatitudCentroOrigen(), r.getLongitudCentroOrigen());
                incluir = distanciaKm <= radioKm;
            } else if (ciudadRepartidor != null && r.getZonaOrigen() != null) {
                // Filtrar por ciudad del repartidor
                String zonaRuta = r.getZonaOrigen().toLowerCase();
                incluir = zonaRuta.contains(ciudadRepartidor) || ciudadRepartidor.contains(zonaRuta);
            }
            // Si no hay GPS ni ciudad, incluir = true (todas)

            if (incluir) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", r.getId());
                map.put("codigoRuta", r.getCodigoRuta());
                map.put("zonaOrigen", r.getZonaOrigen());
                map.put("zonaDestino", r.getZonaDestino());
                map.put("pedidosCount", r.getPedidosCount());
                map.put("pesoTotalKg", r.getPesoTotalKg());
                map.put("pagoTotalEstimado", r.getPagoTotalEstimado());
                map.put("forzarSalida", r.getForzarSalida());
                map.put("fechaLimite", r.getFechaLimite() != null ? r.getFechaLimite().toString() : null);
                map.put("distanciaKm", Math.round(distanciaKm * 10.0) / 10.0);
                map.put("tipoVehiculoRequerido", r.getTipoVehiculoRequerido());
                map.put("capacidadMaximaKg", r.getCapacidadMaximaKg());
                map.put("pesoMinimoSalidaKg", r.getPesoMinimoSalidaKg());
                map.put("estado", r.getEstado());
                data.add(map);
            }
        }

        data.sort((a, b) -> {
            double da = ((Number) a.getOrDefault("distanciaKm", 999)).doubleValue();
            double db = ((Number) b.getOrDefault("distanciaKm", 999)).doubleValue();
            return Double.compare(da, db);
        });

        resp.put("success", true);
        resp.put("rutas", data);
        resp.put("radioKm", radioKm);
        resp.put("filtroCiudad", ciudadRepartidor);
        return ResponseEntity.ok(resp);
    }

    /**
     * Obtener una ruta especifica con todos sus detalles.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerRuta(@PathVariable Long id) {
        Ruta r = rutaRepo.findById(id).orElse(null);
        if (r == null) return ResponseEntity.ok(Map.of("success", false, "message", "Ruta no encontrada"));

        Map<String, Object> map = new HashMap<>();
        map.put("id", r.getId());
        map.put("codigoRuta", r.getCodigoRuta());
        map.put("zonaOrigen", r.getZonaOrigen());
        map.put("zonaDestino", r.getZonaDestino());
        map.put("pedidosCount", r.getPedidosCount());
        map.put("pesoTotalKg", r.getPesoTotalKg());
        map.put("pagoTotalEstimado", r.getPagoTotalEstimado());
        map.put("estado", r.getEstado());
        map.put("tipoVehiculoRequerido", r.getTipoVehiculoRequerido());
        map.put("capacidadMaximaKg", r.getCapacidadMaximaKg());
        map.put("fechaLimite", r.getFechaLimite() != null ? r.getFechaLimite().toString() : null);
        map.put("latitudOrigen", coordValida(r.getLatitudCentroOrigen()) ? r.getLatitudCentroOrigen() : null);
        map.put("longitudOrigen", coordValida(r.getLongitudCentroOrigen()) ? r.getLongitudCentroOrigen() : null);
        map.put("latitudDestino", coordValida(r.getLatitudCentroDestino()) ? r.getLatitudCentroDestino() : null);
        map.put("longitudDestino", coordValida(r.getLongitudCentroDestino()) ? r.getLongitudCentroDestino() : null);
        if (r.getRepartidor() != null) map.put("repartidorNombre", r.getRepartidor().getNombreCompleto());

        // Valores por defecto para info de finca
        String zona = r.getZonaOrigen();
        String nombreFinca = (zona != null && !zona.isBlank() && !zona.equals("Sin origen")) ? zona : "Finca";
        String direccionFinca = "";
        String municipioFinca = "";
        String campesinoNombre = "";
        Double fincaLat = null;
        Double fincaLng = null;

        List<Map<String, Object>> pedidosList = new ArrayList<>();
        if (r.getPedidos() != null && !r.getPedidos().isEmpty()) {
            Orden primeraOrden = r.getPedidos().get(0);
            map.put("direccionEntrega", primeraOrden.getDireccionEnvio());

            // Coordenadas: buscar en TODAS las ordenes, no solo la primera
            if (!coordValida((Double) map.get("latitudOrigen"))) {
                for (Orden pedido : r.getPedidos()) {
                    if (coordValida(pedido.getLatitudOrigen())) {
                        map.put("latitudOrigen", pedido.getLatitudOrigen());
                        map.put("longitudOrigen", pedido.getLongitudOrigen());
                        break;
                    }
                }
            }

            // Extraer datos del campesino desde TODAS las ordenes (no solo la primera,
            // por si el producto de la primera fue borrado)
            if (campesinoNombre.isEmpty()) {
                for (Orden pedido : r.getPedidos()) {
                    if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) continue;
                    for (DetalleOrden detalle : pedido.getDetalles()) {
                        if (detalle.getProducto() != null && detalle.getProducto().getUsuario() != null) {
                             Usuario camp = detalle.getProducto().getUsuario();
                            campesinoNombre = camp.getNombreCompleto() != null ? camp.getNombreCompleto() : "";
                            nombreFinca = camp.getNombreFinca() != null && !camp.getNombreFinca().isBlank() 
                                ? camp.getNombreFinca() : nombreFinca;
                            municipioFinca = camp.getMunicipioOrigen() != null ? camp.getMunicipioOrigen() : "";
                            if (fincaLat == null && camp.getLatitud() != null && camp.getLongitud() != null) {
                                fincaLat = camp.getLatitud();
                                fincaLng = camp.getLongitud();
                            }
                            break;
                        }
                    }
                    if (!campesinoNombre.isEmpty()) break;
                }
            }

            for (Orden pedido : r.getPedidos()) {
                Map<String, Object> pedidoMap = new HashMap<>();
                pedidoMap.put("id", pedido.getId());
                pedidoMap.put("numeroOrden", pedido.getNumeroOrden());
                pedidoMap.put("estado", pedido.getEstado());
                pedidoMap.put("codigoRecogida", pedido.getCodigoRecogida());
                pedidoMap.put("codigoEntrega", pedido.getCodigoEntrega());

                // Datos del campesino dueño del pedido
                List<Map<String, Object>> detallesList = new ArrayList<>();
                String pedCampesinoNombre = "";
                String pedCampesinoFinca = "";
                Long pedCampesinoId = null;

                if (pedido.getDetalles() != null) {
                    for (DetalleOrden detalle : pedido.getDetalles()) {
                        Map<String, Object> detMap = new HashMap<>();
                        detMap.put("nombre", detalle.getNombre());
                        detMap.put("cantidad", detalle.getCantidad());
                        detMap.put("unidad", detalle.getProducto() != null ? detalle.getProducto().getUnidad() : "");
                        detMap.put("estado", detalle.getEstado());

                        // Extraer datos del campesino desde el primer detalle
                        if (detalle.getProducto() != null && detalle.getProducto().getUsuario() != null) {
                            Usuario camp = detalle.getProducto().getUsuario();
                            if (pedCampesinoNombre.isEmpty()) {
                                pedCampesinoNombre = camp.getNombreCompleto();
                                pedCampesinoFinca = camp.getNombreFinca() != null ? camp.getNombreFinca() : "";
                                pedCampesinoId = camp.getId();
                            }
                        }
                        detallesList.add(detMap);
                    }
                }

                pedidoMap.put("campesinoNombre", pedCampesinoNombre);
                pedidoMap.put("campesinoFinca", pedCampesinoFinca);
                pedidoMap.put("campesinoId", pedCampesinoId);
                pedidoMap.put("detalles", detallesList);
                pedidosList.add(pedidoMap);
            }

            // Datos del cliente (destino de entrega)
            Usuario cliente = primeraOrden.getUsuario();
            if (cliente != null) {
                String nombreCompleto = cliente.getNombreCompleto();
                if (nombreCompleto != null && nombreCompleto.contains(" ")) {
                    String[] partes = nombreCompleto.split(" ");
                    String nombre = partes[0];
                    String apellidoInicial = partes[partes.length - 1].substring(0, 1).toUpperCase() + ".";
                    map.put("clienteNombre", nombre + " " + apellidoInicial);
                } else {
                    map.put("clienteNombre", nombreCompleto != null ? nombreCompleto : "Cliente");
                }
                map.put("clienteDireccion", primeraOrden.getDireccionEnvio());
                map.put("clienteTelefono", cliente.getTelefono() != null ? cliente.getTelefono() : "");
            }
        }
        map.put("pedidos", pedidosList);

        // Fallback 1: si la extraccion fallo (producto borrado), usar IDs de los pedidos
        if (campesinoNombre.isEmpty() || direccionFinca.isEmpty() || municipioFinca.isEmpty() || fincaLat == null) {
            for (Map<String, Object> pm : pedidosList) {
                Long ci = toLong(pm.get("campesinoId"));
                if (ci != null) {
                    Usuario camp = usuarioRepo.findById(ci).orElse(null);
                    if (camp != null) {
                        if (campesinoNombre.isEmpty()) campesinoNombre = camp.getNombreCompleto() != null ? camp.getNombreCompleto() : "";
                        if (nombreFinca == null || nombreFinca.isBlank() || nombreFinca.equals("Finca") || nombreFinca.equals("Sin origen")) {
                            nombreFinca = camp.getNombreFinca() != null && !camp.getNombreFinca().isBlank() ? camp.getNombreFinca() : nombreFinca;
                        }
                        if (municipioFinca.isEmpty()) municipioFinca = camp.getMunicipioOrigen() != null ? camp.getMunicipioOrigen() : "";
                        if (fincaLat == null && camp.getLatitud() != null && camp.getLongitud() != null) {
                            fincaLat = camp.getLatitud();
                            fincaLng = camp.getLongitud();
                        }
                        break;
                    }
                }
            }
        }

        // Fallback 2: rellenar municipio con datos de la orden si siguen vacios
        if (municipioFinca.isEmpty()) {
            for (Orden pedido : r.getPedidos()) {
                if (pedido.getMunicipioOrigen() != null && !pedido.getMunicipioOrigen().isBlank()) {
                    municipioFinca = pedido.getMunicipioOrigen().trim();
                    break;
                }
            }
        }
        // Construir direccionFinca desde datos geograficos reales (NO descripcion)
        if (direccionFinca.isEmpty()) {
            if (!municipioFinca.isEmpty()) {
                direccionFinca = municipioFinca;
            } else {
                String zo = r.getZonaOrigen();
                if (zo != null && !zo.isBlank() && !zo.equals("Sin origen")) {
                    direccionFinca = zo;
                }
            }
        }

        // Fallback 3: nombreFinca garantizado
        if (nombreFinca == null || nombreFinca.isBlank() || nombreFinca.equals("Finca") || nombreFinca.equals("Sin origen")) {
            if (campesinoNombre != null && !campesinoNombre.isBlank()) {
                nombreFinca = "Finca de " + campesinoNombre;
            } else if (r.getZonaOrigen() != null && !r.getZonaOrigen().isBlank() && !r.getZonaOrigen().equals("Sin origen")) {
                nombreFinca = r.getZonaOrigen();
            } else {
                nombreFinca = "Finca";
            }
        }
        map.put("nombreFinca", nombreFinca);
        map.put("campesinoNombre", campesinoNombre.isEmpty() ? "Campesino" : campesinoNombre);
        map.put("direccionFinca", direccionFinca);
        map.put("municipioFinca", municipioFinca);

        // Fallback coordenadas: fincaLat del perfil campesino (datos reales de BD)
        if (!coordValida((Double) map.get("latitudOrigen")) && coordValida(fincaLat)) {
            map.put("latitudOrigen", fincaLat);
            map.put("longitudOrigen", fincaLng);
        }

        return ResponseEntity.ok(Map.of("success", true, "ruta", map));
    }

    /**
     * Repartidor acepta una ruta
     */
    @PostMapping("/{id}/aceptar")
    public ResponseEntity<Map<String, Object>> aceptarRuta(@PathVariable Long id, Authentication auth) {
        Map<String, Object> resp = new HashMap<>();
        Usuario repartidor = authUsuarioService.getAuthenticatedUser(auth);
        if (repartidor == null || !"REPARTIDOR".equals(repartidor.getRol())) {
            resp.put("success", false); resp.put("message", "Solo repartidores pueden aceptar rutas");
            return ResponseEntity.ok(resp);
        }

        Ruta ruta = rutaRepo.findById(id).orElse(null);
        if (ruta == null) {
            resp.put("success", false); resp.put("message", "Ruta no encontrada");
            return ResponseEntity.ok(resp);
        }
        if (!"LISTA_PARA_SALIR".equals(ruta.getEstado())) {
            resp.put("success", false); resp.put("message", "Esta ruta ya fue tomada");
            return ResponseEntity.ok(resp);
        }

        ruta.setEstado("ASIGNADA");
        ruta.setRepartidor(repartidor);
        ruta.setFechaAsignacion(java.time.LocalDateTime.now());
        rutaRepo.save(ruta);

        // Actualizar estado de las ordenes
        for (Orden o : ruta.getPedidos()) {
            o.setEstado(OrdenEstadoService.RUTA_ASIGNADA);
            notificationService.notificarClienteEnCamino(o);
        }

        resp.put("success", true);
        resp.put("message", "Ruta aceptada. Los pedidos estan listos para recogida.");
        resp.put("codigoRuta", ruta.getCodigoRuta());
        return ResponseEntity.ok(resp);
    }

    /**
     * Repartidor marca el inicio del viaje (EN_CAMINO)
     */
    @PostMapping("/{id}/iniciar-viaje")
    public ResponseEntity<Map<String, Object>> iniciarViaje(@PathVariable Long id, Authentication auth) {
        Usuario repartidor = authUsuarioService.getAuthenticatedUser(auth);
        Ruta ruta = rutaRepo.findById(id).orElse(null);
        if (ruta == null || !repartidor.getId().equals(ruta.getRepartidor().getId())) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Ruta no autorizada"));
        }

        ruta.setEstado("EN_CAMINO");
        rutaRepo.save(ruta);

        for (Orden o : ruta.getPedidos()) {
            o.setEstado(OrdenEstadoService.EN_CAMINO);
            notificationService.notificarClienteEnCamino(o);
        }

        return ResponseEntity.ok(Map.of("success", true, "message", "Viaje iniciado"));
    }

    /**
     * Repartidor confirma que recogio los pedidos en la finca
     */
    @PostMapping("/{id}/recoger-pedido")
    public ResponseEntity<Map<String, Object>> recogerPedido(@PathVariable Long id, Authentication auth) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Usuario repartidor = authUsuarioService.getAuthenticatedUser(auth);
            if (repartidor == null) {
                resp.put("success", false); resp.put("message", "No autenticado");
                return ResponseEntity.ok(resp);
            }
            Ruta ruta = rutaRepo.findById(id).orElse(null);
            if (ruta == null || ruta.getRepartidor() == null) {
                resp.put("success", false); resp.put("message", "Ruta no encontrada");
                return ResponseEntity.ok(resp);
            }
            if (!repartidor.getId().equals(ruta.getRepartidor().getId())) {
                resp.put("success", false); resp.put("message", "No eres el repartidor asignado");
                return ResponseEntity.ok(resp);
            }
            List<Orden> pedidos = ruta.getPedidos();
            if (pedidos == null || pedidos.isEmpty()) {
                resp.put("success", false); resp.put("message", "No hay pedidos");
                return ResponseEntity.ok(resp);
            }
            for (Orden o : pedidos) {
                o.setEstado(OrdenEstadoService.RECOGIDO);
            }
            ordenRepo.saveAll(pedidos);
            resp.put("success", true); resp.put("message", "Pedido recogido");
        } catch (Exception e) {
            resp.put("success", false); resp.put("message", "Error en el servidor. Intenta de nuevo.");
        }
        return ResponseEntity.ok(resp);
    }

    // ============ SISTEMA PIN DE VERIFICACION ============

    private String generarPin() {
        return String.valueOf(1000 + (int)(Math.random() * 899999));
    }

    private void registrarLog(Orden o, String tipo, String resultado, String detalle) {
        try {
            String ts = java.time.LocalDateTime.now().toString();
            String entry = String.format("{\"fecha\":\"%s\",\"tipo\":\"%s\",\"resultado\":\"%s\",\"detalle\":\"%s\"}",
                ts, tipo, resultado, detalle != null ? detalle : "");
            String actual = o.getLogVerificaciones();
            o.setLogVerificaciones((actual != null ? actual + "," : "") + entry);
        } catch (Exception ignored) {}
    }

    /**
     * Genera PIN de recogida para todos los pedidos de la ruta.
     * Lo ve el campesino y se lo da al repartidor al llegar a la finca.
     */
    @PostMapping("/{id}/generar-pin-recogida")
    public ResponseEntity<Map<String, Object>> generarPinRecogida(@PathVariable Long id, Authentication auth) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Usuario user = authUsuarioService.getAuthenticatedUser(auth);
            Ruta ruta = rutaRepo.findById(id).orElse(null);
            if (ruta == null || ruta.getPedidos() == null || ruta.getPedidos().isEmpty()) {
                resp.put("success", false); resp.put("message", "Ruta sin pedidos");
                return ResponseEntity.ok(resp);
            }
            // Si el campesino ya genero un PIN, usar ese. Si no, generar uno nuevo.
            String pinExistente = null;
            for (Orden o : ruta.getPedidos()) {
                if (o.getCodigoRecogida() != null && !o.getCodigoRecogida().isEmpty()) {
                    pinExistente = o.getCodigoRecogida();
                    break;
                }
            }
            String pin = pinExistente != null ? pinExistente : generarPin();
            for (Orden o : ruta.getPedidos()) {
                o.setCodigoRecogida(pin);
                o.setIntentosRecogida(0);
                o.setFechaGeneracionRecogida(java.time.LocalDateTime.now());
                registrarLog(o, "PIN_RECOGIDA_GENERADO", "OK", "PIN generado para recogida");
            }
            ordenRepo.saveAll(ruta.getPedidos());
            resp.put("success", true);
            resp.put("pin", pin);
            resp.put("message", "PIN de recogida generado");
        } catch (Exception e) {
            resp.put("success", false); resp.put("message", "PIN incorrecto o error en el servidor. Intenta de nuevo.");
        }
        return ResponseEntity.ok(resp);
    }

    /**
     * Repartidor valida el PIN de recogida dado por el campesino.
     */
    @PostMapping("/{id}/validar-pin-recogida")
    public ResponseEntity<Map<String, Object>> validarPinRecogida(@PathVariable Long id,
            @RequestBody Map<String, String> body, Authentication auth) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Usuario repartidor = authUsuarioService.getAuthenticatedUser(auth);
            if (repartidor == null) {
                resp.put("success", false); resp.put("message", "No autenticado");
                return ResponseEntity.ok(resp);
            }
            Ruta ruta = rutaRepo.findById(id).orElse(null);
            if (ruta == null || ruta.getRepartidor() == null || !repartidor.getId().equals(ruta.getRepartidor().getId())) {
                resp.put("success", false); resp.put("message", "Ruta no autorizada");
                return ResponseEntity.ok(resp);
            }
            String pinIngresado = body.getOrDefault("pin", "").trim();
            if (pinIngresado.isEmpty()) {
                resp.put("success", false); resp.put("message", "Ingresa el PIN de recogida");
                return ResponseEntity.ok(resp);
            }
            List<Orden> pedidos = ruta.getPedidos();
            if (pedidos == null || pedidos.isEmpty()) {
                resp.put("success", false); resp.put("message", "No hay pedidos");
                return ResponseEntity.ok(resp);
            }
            // Validar contra el primer pedido (todos comparten el mismo PIN)
            Orden primera = pedidos.get(0);
            if (primera.getCodigoRecogida() == null) {
                resp.put("success", false); resp.put("message", "No se ha generado PIN de recogida");
                return ResponseEntity.ok(resp);
            }
            if (primera.getIntentosRecogida() >= 5) {
                resp.put("success", false); resp.put("message", "Demasiados intentos. Solicita un nuevo PIN al campesino.");
                return ResponseEntity.ok(resp);
            }
            primera.setIntentosRecogida((primera.getIntentosRecogida() != null ? primera.getIntentosRecogida() : 0) + 1);
            if (!pinIngresado.equals(primera.getCodigoRecogida())) {
                registrarLog(primera, "PIN_RECOGIDA_ERROR", "FAIL", "Intento " + primera.getIntentosRecogida() + ": PIN incorrecto");
                ordenRepo.save(primera);
                int restantes = 5 - primera.getIntentosRecogida();
                resp.put("success", false);
                resp.put("message", "PIN incorrecto. Te quedan " + restantes + " intentos.");
                resp.put("intentosRestantes", restantes);
                return ResponseEntity.ok(resp);
            }
            // PIN correcto
            for (Orden o : pedidos) {
                o.setEstado(OrdenEstadoService.RECOGIDO);
                registrarLog(o, "PIN_RECOGIDA_OK", "OK", "PIN validado correctamente por repartidor");
                o.setCodigoRecogida(null); // Invalidar PIN despues de uso
                // Actualizar estado visible del campesino
                if (o.getDetalles() != null) {
                    for (DetalleOrden d : o.getDetalles()) {
                        d.setEstado("ENVIADO"); // En camino al cliente
                    }
                }
            }
            ordenRepo.saveAll(pedidos);
            resp.put("success", true);
            resp.put("message", "PIN correcto. Pedido recogido.");
        } catch (Exception e) {
            resp.put("success", false); resp.put("message", "Error en el servidor. Intenta de nuevo.");
        }
        return ResponseEntity.ok(resp);
    }

    /**
     * Genera PIN de entrega. Lo ve el cliente y se lo da al repartidor.
     */
    @PostMapping("/{id}/generar-pin-entrega")
    public ResponseEntity<Map<String, Object>> generarPinEntrega(@PathVariable Long id, Authentication auth) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Ruta ruta = rutaRepo.findById(id).orElse(null);
            if (ruta == null || ruta.getPedidos() == null || ruta.getPedidos().isEmpty()) {
                resp.put("success", false); resp.put("message", "Ruta sin pedidos");
                return ResponseEntity.ok(resp);
            }
            String pin = generarPin();
            for (Orden o : ruta.getPedidos()) {
                o.setCodigoEntrega(pin);
                o.setIntentosEntrega(0);
                o.setFechaGeneracionEntrega(java.time.LocalDateTime.now());
                registrarLog(o, "PIN_ENTREGA_GENERADO", "OK", "PIN generado para entrega");
            }
            ordenRepo.saveAll(ruta.getPedidos());
            // Notificar al cliente con el PIN de entrega
            for (Orden o : ruta.getPedidos()) {
                notificationService.notificarClientePinEntrega(o, pin);
            }
            resp.put("success", true);
            resp.put("pin", pin);
            resp.put("message", "PIN de entrega generado");
        } catch (Exception e) {
            resp.put("success", false); resp.put("message", "Error en el servidor. Intenta de nuevo.");
        }
        return ResponseEntity.ok(resp);
    }

    /**
     * Repartidor valida PIN de entrega dado por el cliente. Si es correcto completa la entrega.
     */
    @PostMapping("/{id}/validar-pin-entrega")
    public ResponseEntity<Map<String, Object>> validarPinEntrega(@PathVariable Long id,
            @RequestBody Map<String, String> body, Authentication auth) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Usuario repartidor = authUsuarioService.getAuthenticatedUser(auth);
            if (repartidor == null) {
                resp.put("success", false); resp.put("message", "No autenticado");
                return ResponseEntity.ok(resp);
            }
            Ruta ruta = rutaRepo.findById(id).orElse(null);
            if (ruta == null || ruta.getRepartidor() == null || !repartidor.getId().equals(ruta.getRepartidor().getId())) {
                resp.put("success", false); resp.put("message", "Ruta no autorizada");
                return ResponseEntity.ok(resp);
            }
            String pinIngresado = body.getOrDefault("pin", "").trim();
            if (pinIngresado.isEmpty()) {
                resp.put("success", false); resp.put("message", "Ingresa el PIN de entrega");
                return ResponseEntity.ok(resp);
            }
            List<Orden> pedidos = ruta.getPedidos();
            if (pedidos == null || pedidos.isEmpty()) {
                resp.put("success", false); resp.put("message", "No hay pedidos");
                return ResponseEntity.ok(resp);
            }
            Orden primera = pedidos.get(0);
            if (primera.getCodigoEntrega() == null) {
                resp.put("success", false); resp.put("message", "No se ha generado PIN de entrega");
                return ResponseEntity.ok(resp);
            }
            if (primera.getIntentosEntrega() >= 5) {
                resp.put("success", false); resp.put("message", "Demasiados intentos. Solicita un nuevo PIN al cliente.");
                return ResponseEntity.ok(resp);
            }
            primera.setIntentosEntrega((primera.getIntentosEntrega() != null ? primera.getIntentosEntrega() : 0) + 1);
            if (!pinIngresado.equals(primera.getCodigoEntrega())) {
                registrarLog(primera, "PIN_ENTREGA_ERROR", "FAIL", "Intento " + primera.getIntentosEntrega() + ": PIN incorrecto");
                ordenRepo.save(primera);
                int restantes = 5 - primera.getIntentosEntrega();
                resp.put("success", false);
                resp.put("message", "PIN incorrecto. Te quedan " + restantes + " intentos.");
                resp.put("intentosRestantes", restantes);
                return ResponseEntity.ok(resp);
            }
            // PIN correcto - completar entrega
            for (Orden o : pedidos) {
                o.setEstado(OrdenEstadoService.ENTREGADO);
                registrarLog(o, "PIN_ENTREGA_OK", "OK", "PIN validado. Entrega completada.");
                o.setCodigoEntrega(null);
                // Actualizar estado visible del campesino y cliente
                if (o.getDetalles() != null) {
                    for (DetalleOrden d : o.getDetalles()) {
                        d.setEstado("ENTREGADO");
                    }
                }
            }
            ordenRepo.saveAll(pedidos);
            // Completar ruta si todos entregados
            ruta.setEstado("COMPLETADA");
            ruta.setFechaCompletada(java.time.LocalDateTime.now());
            rutaRepo.save(ruta);
            resp.put("success", true);
            resp.put("message", "PIN correcto. Entrega completada.");
        } catch (Exception e) {
            resp.put("success", false); resp.put("message", "Error en el servidor. Intenta de nuevo.");
        }
        return ResponseEntity.ok(resp);
    }

    private Long toLong(Object obj) {
        if (obj instanceof Number) return ((Number) obj).longValue();
        if (obj instanceof String) try { return Long.parseLong((String) obj); } catch (NumberFormatException e) { return null; }
        return null;
    }

    private boolean coordValida(Double val) {
        return val != null && !val.isNaN() && !val.isInfinite() && Math.abs(val) > 0.0001;
    }

    /**
     * Repartidor completa una entrega (escanea QR del cliente)
     */
    @PostMapping("/entrega/{ordenId}")
    public ResponseEntity<Map<String, Object>> completarEntrega(@PathVariable Long ordenId) {
        Orden orden = ordenRepo.findById(ordenId).orElse(null);
        if (orden == null) return ResponseEntity.ok(Map.of("success", false, "message", "Orden no encontrada"));

        orden.setEstado(OrdenEstadoService.ENTREGADO);
        ordenRepo.save(orden);

        notificationService.notificarCampesinoEntregaExitosa(orden);
        notificationService.notificarClienteEntregaExitosa(orden);

        // Verificar si todos los pedidos de la ruta estan entregados
        if (orden.getRuta() != null) {
            Ruta ruta = orden.getRuta();
            boolean todosEntregados = ruta.getPedidos().stream()
                .allMatch(o -> OrdenEstadoService.ENTREGADO.equals(o.getEstado()));
            if (todosEntregados) {
                ruta.setEstado("COMPLETADA");
                ruta.setFechaCompletada(java.time.LocalDateTime.now());
                rutaRepo.save(ruta);
            }
        }

        return ResponseEntity.ok(Map.of("success", true, "message", "Entrega completada"));
    }

    /**
     * Mis rutas (historial del repartidor)
     */
    @GetMapping("/mis-rutas")
    public ResponseEntity<Map<String, Object>> misRutas(Authentication auth) {
        Usuario repartidor = authUsuarioService.getAuthenticatedUser(auth);
        if (repartidor == null) return ResponseEntity.ok(Map.of("success", false));

        List<Ruta> rutas = rutaRepo.findByRepartidorIdOrderByFechaCreacionDesc(repartidor.getId());
        List<Map<String, Object>> data = rutas.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("codigoRuta", r.getCodigoRuta());
            map.put("estado", r.getEstado());
            map.put("zonaOrigen", r.getZonaOrigen());
            map.put("zonaDestino", r.getZonaDestino());
            map.put("pedidosCount", r.getPedidosCount());
            map.put("pesoTotalKg", r.getPesoTotalKg());
            map.put("pagoTotalEstimado", r.getPagoTotalEstimado());
            map.put("fechaCreacion", r.getFechaCreacion() != null ? r.getFechaCreacion().toString() : null);
            map.put("fechaAsignacion", r.getFechaAsignacion() != null ? r.getFechaAsignacion().toString() : null);
            map.put("fechaCompletada", r.getFechaCompletada() != null ? r.getFechaCompletada().toString() : null);
            if (r.getRepartidor() != null) {
                map.put("repartidorId", r.getRepartidor().getId());
                map.put("repartidorRating", r.getRepartidor().getCalificacionPromedio());
            }
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("success", true, "rutas", data));
    }

    /**
     * Cliente califica al repartidor despues de una entrega
     */
    @PostMapping("/{id}/calificar")
    public ResponseEntity<Map<String, Object>> calificarRepartidor(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            Authentication auth) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Usuario cliente = authUsuarioService.getAuthenticatedUser(auth);
            if (cliente == null) {
                resp.put("success", false); resp.put("message", "No autenticado");
                return ResponseEntity.ok(resp);
            }
            Ruta ruta = rutaRepo.findById(id).orElse(null);
            if (ruta == null || ruta.getRepartidor() == null) {
                resp.put("success", false); resp.put("message", "Ruta no encontrada");
                return ResponseEntity.ok(resp);
            }
            if (!"COMPLETADA".equals(ruta.getEstado())) {
                resp.put("success", false); resp.put("message", "La ruta aun no esta completada");
                return ResponseEntity.ok(resp);
            }
            int estrellas = ((Number) body.getOrDefault("estrellas", 0)).intValue();
            if (estrellas < 1 || estrellas > 5) {
                resp.put("success", false); resp.put("message", "Estrellas deben ser entre 1 y 5");
                return ResponseEntity.ok(resp);
            }
            String comentario = body.get("comentario") != null ? body.get("comentario").toString() : "";

            Usuario rep = ruta.getRepartidor();
            int total = rep.getTotalEntregas() != null ? rep.getTotalEntregas() : 0;
            double promActual = rep.getCalificacionPromedio() != null ? rep.getCalificacionPromedio() : 0.0;
            double nuevoProm = ((promActual * total) + estrellas) / (total + 1);
            rep.setCalificacionPromedio(Math.round(nuevoProm * 10.0) / 10.0);
            rep.setTotalEntregas(total + 1);
            usuarioRepo.save(rep);

            System.out.println("⭐ [CALIFICACION] Repartidor " + rep.getNombreCompleto() +
                " recibio " + estrellas + " estrellas. Promedio: " + rep.getCalificacionPromedio());
            resp.put("success", true);
            resp.put("message", "Calificacion guardada. Nuevo promedio: " + rep.getCalificacionPromedio());
        } catch (Exception e) {
            resp.put("success", false); resp.put("message", "Error en el servidor. Intenta de nuevo.");
        }
        return ResponseEntity.ok(resp);
    }

    /**
     * Ejecutar robot manualmente (solo admin)
     */
    @PostMapping("/admin/ejecutar-robot")
    public ResponseEntity<Map<String, Object>> ejecutarRobot() {
        return ResponseEntity.ok(agrupacionService.ejecutarManual());
    }

    /**
     * Diagnosticar: ver cuantas ordenes hay en cada estado
     */
    @GetMapping("/admin/diagnostico")
    public ResponseEntity<Map<String, Object>> diagnostico() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("totalOrdenes", ordenRepo.count());
        resp.put("ordenesPorEstado", ordenRepo.findOrdenesPorEstado());
        resp.put("rutasPorEstado", rutaRepo.findAll().stream()
            .collect(java.util.stream.Collectors.groupingBy(Ruta::getEstado, java.util.stream.Collectors.counting())));
        return ResponseEntity.ok(resp);
    }

    /**
     * Crear datos de prueba: convierte ordenes existentes a ESPERANDO_AGRUPACION
     */
    @PostMapping("/admin/crear-datos-prueba")
    public ResponseEntity<Map<String, Object>> crearDatosPrueba() {
        List<Orden> ordenes = ordenRepo.findAll();
        int convertidas = 0;
        for (Orden o : ordenes) {
            if (!OrdenEstadoService.ESPERANDO_AGRUPACION.equals(o.getEstado())) {
                o.setTipoEnvio("ECONOMICO");
                o.setPesoTotalKg(5.0);
                o.setCostoEnvio(3500.0);
                o.setEstado(OrdenEstadoService.ESPERANDO_AGRUPACION);
                o.setSubtotalProductos(10000.0);
                o.setDireccionEnvio(o.getDireccionEnvio() != null ? o.getDireccionEnvio() : "Bogota - Chapinero");
                convertidas++;
            }
        }
        ordenRepo.saveAll(ordenes);
        return ResponseEntity.ok(Map.of("success", true, "message", convertidas + " ordenes convertidas a ESPERANDO_AGRUPACION"));
    }

    /**
     * Ver TODAS las ordenes con su estado y tipo
     */
    @GetMapping("/admin/ordenes")
    public ResponseEntity<Map<String, Object>> todasLasOrdenes() {
        List<Orden> ordenes = ordenRepo.findAll();
        List<Map<String, Object>> data = ordenes.stream().map(o -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", o.getId());
            map.put("numeroOrden", o.getNumeroOrden());
            map.put("estado", o.getEstado());
            map.put("tipoEnvio", o.getTipoEnvio());
            map.put("total", o.getTotal());
            map.put("pesoTotalKg", o.getPesoTotalKg());
            map.put("municipioOrigen", o.getMunicipioOrigen());
            map.put("direccionEnvio", o.getDireccionEnvio());
            map.put("cliente", o.getUsuario() != null ? o.getUsuario().getEmail() : "-");
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("success", true, "ordenes", data));
    }

    /**
     * Campesino consulta el PIN de recogida de su pedido.
     */
    @GetMapping("/pin-recogida/{ordenId}")
    public ResponseEntity<Map<String, Object>> obtenerPinRecogida(@PathVariable Long ordenId, Authentication auth) {
        Usuario user = authUsuarioService.getAuthenticatedUser(auth);
        Orden orden = ordenRepo.findById(ordenId).orElse(null);
        if (orden == null || user == null) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Orden no encontrada"));
        }
        // Solo el campesino dueño del producto puede ver el PIN
        boolean esCampesino = false;
        if (orden.getDetalles() != null) {
            for (DetalleOrden d : orden.getDetalles()) {
                if (d.getProducto() != null && d.getProducto().getUsuario() != null
                    && d.getProducto().getUsuario().getId().equals(user.getId())) {
                    esCampesino = true; break;
                }
            }
        }
        if (!esCampesino && !"ADMIN".equals(user.getRol())) {
            return ResponseEntity.ok(Map.of("success", false, "message", "No autorizado"));
        }
        return ResponseEntity.ok(Map.of("success", true, "pin", orden.getCodigoRecogida() != null ? orden.getCodigoRecogida() : "",
            "estado", orden.getEstado()));
    }

    /**
     * Cliente consulta el PIN de entrega de su pedido.
     */
    @GetMapping("/pin-entrega/{ordenId}")
    public ResponseEntity<Map<String, Object>> obtenerPinEntrega(@PathVariable Long ordenId, Authentication auth) {
        Usuario user = authUsuarioService.getAuthenticatedUser(auth);
        Orden orden = ordenRepo.findById(ordenId).orElse(null);
        if (orden == null || user == null) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Orden no encontrada"));
        }
        // Solo el cliente dueño de la orden puede ver el PIN
        if ((orden.getUsuario() == null || !orden.getUsuario().getId().equals(user.getId())) && !"ADMIN".equals(user.getRol())) {
            return ResponseEntity.ok(Map.of("success", false, "message", "No autorizado"));
        }
        return ResponseEntity.ok(Map.of("success", true, "pin", orden.getCodigoEntrega() != null ? orden.getCodigoEntrega() : "",
            "estado", orden.getEstado()));
    }
}
