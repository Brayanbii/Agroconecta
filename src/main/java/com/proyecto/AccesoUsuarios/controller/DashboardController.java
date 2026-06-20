package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.repository.DetalleOrdenRepository;
import com.proyecto.AccesoUsuarios.repository.OrdenRepository;
import com.proyecto.AccesoUsuarios.repository.ProductoRepository;
import com.proyecto.AccesoUsuarios.repository.ResenaRepository;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import com.proyecto.AccesoUsuarios.repository.FavoritoCampesinoRepository;
import com.proyecto.AccesoUsuarios.service.CarritoService;
import com.proyecto.AccesoUsuarios.service.PythonService;
import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.model.Producto;
import com.proyecto.AccesoUsuarios.model.DetalleOrden;
import com.proyecto.AccesoUsuarios.model.Orden;
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
import java.text.Normalizer;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class DashboardController {

    @Autowired
    private UsuarioRepository usuarioRepo;
    
    @Autowired
    private ProductoRepository productoRepo;

    @Autowired
    private OrdenRepository ordenRepo; // <-- Inyectamos el repo de Ã³rdenes

    @Autowired
    private DetalleOrdenRepository detalleRepo;

    @Autowired
    private ResenaRepository resenaRepo;

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private PythonService pythonService;
    
    @Autowired
    private FavoritoCampesinoRepository favoritoCampesinoRepo;

    // 1. PANEL ADMIN
    @GetMapping("/admin/dashboard")
    public String dashboardAdmin(Model model) {
        // --- Fecha actual dinÃ¡mica ---
        String fechaHoy = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("d MMM, yyyy", new Locale("es", "CO")));
        model.addAttribute("fechaHoy", fechaHoy);

        // --- KPI Cards ---
        model.addAttribute("totalUsuarios",  usuarioRepo.count());
        model.addAttribute("totalProductos", productoRepo.count());
        model.addAttribute("totalVentas",    ordenRepo.count());
        model.addAttribute("totalResenas",   resenaRepo.count());

        // --- Ãšltimas ReseÃ±as para la tabla del admin ---
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
        Usuario campesino = usuarioRepo.findFirstByEmail(email).orElseThrow();
        
        // --- VALIDACIÃ“N DE IDENTIDAD (KYC) ---
        if (!"APROBADO".equals(campesino.getEstadoVerificacion())) {
            return "redirect:/campesino/verificacion";
        }
        
        // --- DATOS REALES PARA EL DASHBOARD VISUAL ---
        List<DetalleOrden> misVentas = detalleRepo.findVentasByCampesino(campesino, campesino.getId());
        
        double ventasHoy = 0.0;
        double gananciaMes = 0.0;
        int pedidosPendientes = 0;
        
        LocalDate hoy = LocalDate.now();
        int mesActual = hoy.getMonthValue();
        int anoActual = hoy.getYear();
        
        Set<Long> ordenesPendientesSet = new HashSet<>();
        Map<String, Integer> ventasPorProducto = new HashMap<>();

        for(DetalleOrden d : misVentas) {
            Orden o = d.getOrden();
            if(o != null && o.getFechaCreacion() != null) {
                LocalDate fechaOrden = o.getFechaCreacion().toLocalDate();
                
                // Ganancia del mes
                if(fechaOrden.getMonthValue() == mesActual && fechaOrden.getYear() == anoActual) {
                    gananciaMes += d.getTotal();
                }
                
                // Ventas Hoy
                if(fechaOrden.isEqual(hoy)) {
                    ventasHoy += d.getTotal();
                }
                
                // Pedidos Pendientes (contamos ordenes unicas que tienen items sin enviar)
                String estadoItem = d.getEstado();
                if(estadoItem != null && (estadoItem.equalsIgnoreCase("NUEVO") || 
                                          estadoItem.equalsIgnoreCase("PREPARANDO") || 
                                          estadoItem.equalsIgnoreCase("LISTO_PARA_RECOGER"))) {
                    ordenesPendientesSet.add(o.getId());
                }
            }
            
            // Conteo para Producto Estrella
            if(d.getProducto() != null) {
                String nombreProd = d.getProducto().getNombre();
                ventasPorProducto.put(nombreProd, ventasPorProducto.getOrDefault(nombreProd, 0) + d.getCantidad());
            }
        }
        
        pedidosPendientes = ordenesPendientesSet.size();
        
        // Determinar el producto estrella
        String productoEstrella = "Ninguno";
        int maxVentas = 0;
        for (Map.Entry<String, Integer> entry : ventasPorProducto.entrySet()) {
            if (entry.getValue() > maxVentas) {
                maxVentas = entry.getValue();
                productoEstrella = entry.getKey();
            }
        }

        model.addAttribute("ventasHoy", ventasHoy);
        model.addAttribute("pedidosPendientes", pedidosPendientes);
        model.addAttribute("gananciaMes", gananciaMes);
        model.addAttribute("productoMasVendido", productoEstrella);

        List<Producto> prods = productoRepo.findByUsuario(campesino);
        model.addAttribute("productos", prods);
        model.addAttribute("usuario", campesino);
        return "mis_productos";
    }

    // 3. PANEL CLIENTE (TIENDA) â€” con buscador y radar de cercanÃ­a
    @GetMapping("/tienda")
    public String tienda(Model model, Authentication auth,
                         @RequestParam(value = "buscar", required = false) String buscar,
                         @RequestParam(value = "lat", required = false) Double latCliente,
                         @RequestParam(value = "lon", required = false) Double lonCliente) {
        
        List<Producto> productosList;

        // 1. Cargar productos (filtrados si hay bÃºsqueda)
        if (buscar != null && !buscar.trim().isEmpty()) {
            productosList = productoRepo.findByNombreContainingIgnoreCase(buscar.trim());
            model.addAttribute("busqueda", buscar.trim());
        } else {
            productosList = productoRepo.findAll();
            model.addAttribute("busqueda", "");
        }

        // 1.5 ALGORITMO DE PRODUCTOS MÃS DESTACADOS (Mas vendidos)
        List<com.proyecto.AccesoUsuarios.model.DetalleOrden> todosLosDetalles = detalleRepo.findAll();
        Map<Producto, Integer> ventasPorProducto = new HashMap<>();
        for (com.proyecto.AccesoUsuarios.model.DetalleOrden d : todosLosDetalles) {
            if (d.getProducto() != null && d.getCantidad() != null) {
                ventasPorProducto.put(d.getProducto(), ventasPorProducto.getOrDefault(d.getProducto(), 0) + d.getCantidad());
            }
        }
        
        List<Producto> productosDestacados = new ArrayList<>(productosList);
        productosDestacados.sort((p1, p2) -> {
            int v1 = ventasPorProducto.getOrDefault(p1, 0);
            int v2 = ventasPorProducto.getOrDefault(p2, 0);
            if (v1 != v2) {
                return Integer.compare(v2, v1); // Orden descendente por ventas
            }
            // Si empatan en ventas, desempatar por calificaciÃ³n
            return Double.compare(p2.getPromedioCalificacion(), p1.getPromedioCalificacion());
        });
        
        // Limitar a los 8 mejores
        if (productosDestacados.size() > 8) {
            productosDestacados = productosDestacados.subList(0, 8);
        }
        model.addAttribute("productosDestacados", productosDestacados);

        // 1.6 ALGORITMO DE RECOMENDACIÃ“N (Machine Learning HeurÃ­stico)
        List<Producto> productosRecomendados = new ArrayList<>();
        if (auth != null && auth.isAuthenticated()) {
            Usuario usuarioObj = usuarioRepo.findFirstByEmail(auth.getName()).orElse(null);
            if (usuarioObj != null) {
                // Algoritmo de RecomendaciÃ³n Basado en Compras HistÃ³ricas (Content-Based Filtering)
                List<com.proyecto.AccesoUsuarios.model.Orden> ordenes = ordenRepo.findByUsuario(usuarioObj);
                if (ordenes != null && !ordenes.isEmpty()) {
                    Map<String, Integer> catFrecuencia = new HashMap<>();
                    List<Long> productosCompradosIds = new ArrayList<>();

                    for (com.proyecto.AccesoUsuarios.model.Orden o : ordenes) {
                        if (o.getDetalles() != null) {
                            for (com.proyecto.AccesoUsuarios.model.DetalleOrden d : o.getDetalles()) {
                                if (d.getProducto() != null) {
                                    productosCompradosIds.add(d.getProducto().getId());
                                    String cat = d.getProducto().getCategoria();
                                    if (cat != null) {
                                        catFrecuencia.put(cat, catFrecuencia.getOrDefault(cat, 0) + d.getCantidad());
                                    }
                                }
                            }
                        }
                    }

                    // Ordenar categorÃ­as favoritas descendentemente
                    List<Map.Entry<String, Integer>> catsOrdenadas = new ArrayList<>(catFrecuencia.entrySet());
                    catsOrdenadas.sort((a, b) -> b.getValue().compareTo(a.getValue())); 

                    // Extraer las top 2 categorÃ­as de interÃ©s del cliente
                    List<String> topCategorias = new ArrayList<>();
                    if (catsOrdenadas.size() > 0) topCategorias.add(catsOrdenadas.get(0).getKey());
                    if (catsOrdenadas.size() > 1) topCategorias.add(catsOrdenadas.get(1).getKey());

                    // Filtrar productos del catÃ¡logo que encajen con sus gustos, PERO que NO haya comprado antes
                    for (Producto p : productosList) {
                        if (!productosCompradosIds.contains(p.getId()) && topCategorias.contains(p.getCategoria())) {
                            productosRecomendados.add(p);
                        }
                    }
                }
            }
        }
        
        // Fallback: Si no hay suficientes datos para perfilar al cliente (Cold Start Problem),
        // usamos los mejores calificados que no estÃ©n en Destacados.
        if (productosRecomendados.size() < 4) {
            List<Producto> fallback = new ArrayList<>(productosList);
            fallback.removeAll(productosDestacados); 
            fallback.sort((p1, p2) -> Double.compare(p2.getPromedioCalificacion(), p1.getPromedioCalificacion()));
            
            for (Producto p : fallback) {
                if (!productosRecomendados.contains(p)) {
                    productosRecomendados.add(p);
                }
                if (productosRecomendados.size() >= 15) break; 
            }
        } else {
            // Ordenamos las recomendaciones personalizadas por calidad (rating)
            productosRecomendados.sort((p1, p2) -> Double.compare(p2.getPromedioCalificacion(), p1.getPromedioCalificacion()));
            if (productosRecomendados.size() > 15) {
                productosRecomendados = productosRecomendados.subList(0, 15);
            }
        }
        
        model.addAttribute("productosRecomendados", productosRecomendados);

        // 2. LÃ³gica de Distancia MatemÃ¡tica (Haversine)
        if (latCliente != null && lonCliente != null) {
            for (Producto p : productosList) {
                if (p.getLatitudOrigen() != null && p.getLongitudOrigen() != null) {
                    double dist = calcularDistancia(latCliente, lonCliente, p.getLatitudOrigen(), p.getLongitudOrigen());
                    // Redondear a 1 decimal
                    p.setDistanciaKm(Math.round(dist * 10.0) / 10.0);
                } else {
                    // Si el producto no tiene ubicaciÃ³n definida, lo mandamos al fondo de la lista
                    p.setDistanciaKm(9999.9);
                }
            }
            // Ordenar la lista del Mas cercano al Mas lejano
            productosList.sort(Comparator.comparing(Producto::getDistanciaKm));
            model.addAttribute("modoCercania", true);
        } else {
            model.addAttribute("modoCercania", false);
        }

        model.addAttribute("productos", productosList);
        
        // 2. Cargar cantidad del carrito
        model.addAttribute("cantidadCarrito", carritoService.contarItems());

        // 3. LÃ“GICA NUEVA: Obtener el nombre real del cliente y sus favoritos
        List<Long> favoritosIds = new ArrayList<>();
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            Usuario usuario = usuarioRepo.findFirstByEmail(email).orElse(null);
            if (usuario != null) {
                model.addAttribute("nombreCliente", usuario.getNombreCompleto());
                if (usuario.getProductosFavoritos() != null) {
                    usuario.getProductosFavoritos().forEach(fav -> favoritosIds.add(fav.getId()));
                }
            } else {
                model.addAttribute("nombreCliente", "Cliente");
            }
        } else {
            model.addAttribute("nombreCliente", "Invitado");
        }
        model.addAttribute("favoritosIds", favoritosIds);

        return "tienda";
    }

    // 4. DETALLE DEL PRODUCTO INDIVIDUAL
    @GetMapping("/tienda/producto/{id}")
    public String detalleProducto(@org.springframework.web.bind.annotation.PathVariable Long id, Model model, Authentication auth) {
        Producto producto = productoRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto invÃ¡lido:" + id));
        
        model.addAttribute("producto", producto);
        
        // Calcular frescura (mensajeDinÃ¡mico)
        long diasFresco = 0;
        String mensajeFrescura = "Cosechado hoy";
        if (producto.getFechaCreacion() != null) {
            diasFresco = java.time.temporal.ChronoUnit.DAYS.between(producto.getFechaCreacion().toLocalDate(), java.time.LocalDate.now());
            if (diasFresco == 0) {
                mensajeFrescura = "Cosechado Hoy";
            } else if (diasFresco > 0 && diasFresco <= 3) {
                mensajeFrescura = "Producto muy fresco";
            } else {
                mensajeFrescura = "Cosechado hace " + diasFresco + " dÃ­as";
            }
        }
        model.addAttribute("mensajeFrescura", mensajeFrescura);
        
        // Cargar otros productos del mismo campesino
        List<Producto> otrosProductos = new ArrayList<>();
        if (producto.getUsuario() != null) {
            otrosProductos.addAll(productoRepo.findByUsuario(producto.getUsuario()));
            otrosProductos.removeIf(p -> p.getId().equals(producto.getId())); // no mostrar el mismo producto
        }
        model.addAttribute("otrosProductos", otrosProductos);
        
        // Cantidad de items en el carrito
        model.addAttribute("cantidadCarrito", carritoService.contarItems());

        boolean puedeComentar = false;
        Long usuarioLogueadoId = null;
        // LÃ“GICA: Obtener el nombre real del cliente y verificar si comprÃ³ el producto
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            Usuario usuario = usuarioRepo.findFirstByEmail(email).orElse(null);
            if (usuario != null) {
                model.addAttribute("nombreCliente", usuario.getNombreCompleto());
                usuarioLogueadoId = usuario.getId();
                
                // Verificar si tiene alguna orden con este producto
                List<com.proyecto.AccesoUsuarios.model.Orden> ordenes = ordenRepo.findByUsuario(usuario);
                for (com.proyecto.AccesoUsuarios.model.Orden orden : ordenes) {
                    if (orden.getDetalles() != null) {
                        for (com.proyecto.AccesoUsuarios.model.DetalleOrden detalle : orden.getDetalles()) {
                            if (detalle.getProducto() != null && detalle.getProducto().getId().equals(producto.getId())) {
                                puedeComentar = true;
                                break;
                            }
                        }
                    }
                    if (puedeComentar) break;
                }
                
                // Buscar si este usuario ya tiene una reseÃ±a para este producto
                var miResenaOpt = resenaRepo.findByProductoIdAndUsuarioId(producto.getId(), usuario.getId());
                if (miResenaOpt.isPresent()) {
                    var miResena = miResenaOpt.get();
                    model.addAttribute("miResenaId", miResena.getId());
                    model.addAttribute("miResenaEstrellas", miResena.getEstrellas());
                    model.addAttribute("miResenaComentario", miResena.getComentario() != null ? miResena.getComentario() : "");
                    model.addAttribute("yaCalificÃ³", true);
                } else {
                    model.addAttribute("miResenaId", null);
                    model.addAttribute("miResenaEstrellas", 0);
                    model.addAttribute("miResenaComentario", "");
                    model.addAttribute("yaCalificÃ³", false);
                }
                
                // Extraer y enviar IDs de productos favoritos para la UI
                List<Long> favoritosIds = new java.util.ArrayList<>();
                if (usuario.getProductosFavoritos() != null) {
                    for (Producto pFav : usuario.getProductosFavoritos()) {
                        favoritosIds.add(pFav.getId());
                    }
                }
                model.addAttribute("favoritosIds", favoritosIds);
                
            } else {
                model.addAttribute("nombreCliente", "Cliente");
                model.addAttribute("favoritosIds", new java.util.ArrayList<>());
                model.addAttribute("yaCalificÃ³", false);
                model.addAttribute("miResenaId", null);
                model.addAttribute("miResenaEstrellas", 0);
                model.addAttribute("miResenaComentario", "");
            }
        } else {
            model.addAttribute("nombreCliente", "Invitado");
            model.addAttribute("favoritosIds", new java.util.ArrayList<>());
            model.addAttribute("yaCalificÃ³", false);
            model.addAttribute("miResenaId", null);
            model.addAttribute("miResenaEstrellas", 0);
            model.addAttribute("miResenaComentario", "");
        }
        
        model.addAttribute("puedeComentar", puedeComentar);
        model.addAttribute("usuarioLogueadoId", usuarioLogueadoId);

        return "producto_detalle";
    }

    // 5. PERFIL PUBLICO DEL CAMPESINO
    @GetMapping("/tienda/campesino/{id}")
    public String perfilCampesinoPublico(@org.springframework.web.bind.annotation.PathVariable Long id, Model model, Authentication auth) {
        Usuario campesino = usuarioRepo.findById(id).orElse(null);
        if (campesino == null || !"CAMPESINO".equals(campesino.getRol())) {
            return "redirect:/tienda";
        }
        
        model.addAttribute("campesino", campesino);
        
        List<Producto> productos = productoRepo.findByUsuario(campesino);
        model.addAttribute("productos", productos);
        
        // Ventas y calificaciÃ³n promedio
        List<com.proyecto.AccesoUsuarios.model.DetalleOrden> ventas = detalleRepo.findVentasByCampesino(campesino, campesino.getId());
        int totalVendidos = ventas.stream().mapToInt(d -> d.getCantidad() != null ? d.getCantidad() : 0).sum();
        model.addAttribute("totalVendidos", totalVendidos);
        
        double promedio = 0;
        if (!productos.isEmpty()) {
            double sum = productos.stream()
                .mapToDouble(p -> p.getPromedioCalificacion() != null ? p.getPromedioCalificacion() : 0.0)
                .sum();
            promedio = sum / productos.size();
        }
        model.addAttribute("promedioCalificacion", Math.round(promedio * 10.0) / 10.0);
        
        model.addAttribute("cantidadCarrito", carritoService.contarItems());

        List<Long> favoritosIds = new ArrayList<>();
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            Usuario usuario = usuarioRepo.findFirstByEmail(email).orElse(null);
            if (usuario != null) {
                model.addAttribute("nombreCliente", usuario.getNombreCompleto());
                if (usuario.getProductosFavoritos() != null) {
                    usuario.getProductosFavoritos().forEach(fav -> favoritosIds.add(fav.getId()));
                }
            } else {
                model.addAttribute("nombreCliente", "Cliente");
            }
        } else {
            model.addAttribute("nombreCliente", "Invitado");
        }
        
        model.addAttribute("favoritosIds", favoritosIds);
        
        // --- NUEVO: FAVORITOS CAMPESINO ---
        int likesPerfil = favoritoCampesinoRepo.countByCampesino(campesino);
        model.addAttribute("likesPerfil", likesPerfil);
        
        boolean isFavorito = false;
        if (auth != null && auth.isAuthenticated()) {
            Usuario usuarioObj = usuarioRepo.findFirstByEmail(auth.getName()).orElse(null);
            if (usuarioObj != null) {
                isFavorito = favoritoCampesinoRepo.existsByClienteAndCampesino(usuarioObj, campesino);
            }
        }
        model.addAttribute("isFavorito", isFavorito);
        
        return "campesino_perfil_publico";
    }

    // 6. COLECCIONES (Ver Todo de cada secciÃ³n del Home)
    @GetMapping("/tienda/coleccion/{tipo}")
    public String coleccion(@org.springframework.web.bind.annotation.PathVariable String tipo, 
                            Model model, Authentication auth) {
        
        List<Producto> todosProductos = productoRepo.findAll();
        List<Producto> resultado = new ArrayList<>();
        String titulo = "Coleccion";
        String icono = "fas fa-store";
        String color = "green"; // Color temÃ¡tico de la Coleccion
        
        switch (tipo) {
            case "destacados":
                // Reutilizar algoritmo de Mas vendidos
                List<com.proyecto.AccesoUsuarios.model.DetalleOrden> detalles = detalleRepo.findAll();
                Map<Long, Integer> ventasMap = new HashMap<>();
                for (com.proyecto.AccesoUsuarios.model.DetalleOrden d : detalles) {
                    if (d.getProducto() != null && d.getCantidad() != null) {
                        ventasMap.put(d.getProducto().getId(), ventasMap.getOrDefault(d.getProducto().getId(), 0) + d.getCantidad());
                    }
                }
                resultado = new ArrayList<>(todosProductos);
                resultado.sort((p1, p2) -> {
                    int v1 = ventasMap.getOrDefault(p1.getId(), 0);
                    int v2 = ventasMap.getOrDefault(p2.getId(), 0);
                    if (v1 != v2) return Integer.compare(v2, v1);
                    return Double.compare(p2.getPromedioCalificacion(), p1.getPromedioCalificacion());
                });
                titulo = "Productos Mas Destacados";
                icono = "fas fa-fire";
                color = "orange";
                break;

            case "nuevos":
                // Ordenar por fecha de creaciÃ³n descendente (Mas nuevo primero)
                resultado = new ArrayList<>(todosProductos);
                resultado.sort((p1, p2) -> {
                    if (p1.getFechaCreacion() == null && p2.getFechaCreacion() == null) return 0;
                    if (p1.getFechaCreacion() == null) return 1;
                    if (p2.getFechaCreacion() == null) return -1;
                    return p2.getFechaCreacion().compareTo(p1.getFechaCreacion());
                });
                titulo = "Recien Cosechados";
                icono = "fas fa-leaf";
                color = "green";
                break;

            case "ofertas":
                // Simular ofertas: productos con id divisible entre 3
                for (Producto p : todosProductos) {
                    if (p.getId() % 3 == 0) {
                        resultado.add(p);
                    }
                }
                titulo = "Ofertas Especiales";
                icono = "fas fa-tag";
                color = "red";
                break;

            case "recomendados":
                // Reutilizar el mismo algoritmo de recomendaciÃ³n
                if (auth != null && auth.isAuthenticated()) {
                    Usuario usuarioObj = usuarioRepo.findFirstByEmail(auth.getName()).orElse(null);
                    if (usuarioObj != null) {
                        List<com.proyecto.AccesoUsuarios.model.Orden> ordenes = ordenRepo.findByUsuario(usuarioObj);
                        if (ordenes != null && !ordenes.isEmpty()) {
                            Map<String, Integer> catFrec = new HashMap<>();
                            List<Long> compradosIds = new ArrayList<>();
                            for (com.proyecto.AccesoUsuarios.model.Orden o : ordenes) {
                                if (o.getDetalles() != null) {
                                    for (com.proyecto.AccesoUsuarios.model.DetalleOrden d : o.getDetalles()) {
                                        if (d.getProducto() != null) {
                                            compradosIds.add(d.getProducto().getId());
                                            String cat = d.getProducto().getCategoria();
                                            if (cat != null) catFrec.put(cat, catFrec.getOrDefault(cat, 0) + d.getCantidad());
                                        }
                                    }
                                }
                            }
                            List<Map.Entry<String, Integer>> catsOrd = new ArrayList<>(catFrec.entrySet());
                            catsOrd.sort((a, b) -> b.getValue().compareTo(a.getValue()));
                            List<String> topCats = new ArrayList<>();
                            if (catsOrd.size() > 0) topCats.add(catsOrd.get(0).getKey());
                            if (catsOrd.size() > 1) topCats.add(catsOrd.get(1).getKey());

                            for (Producto p : todosProductos) {
                                if (!compradosIds.contains(p.getId()) && topCats.contains(p.getCategoria())) {
                                    resultado.add(p);
                                }
                            }
                        }
                    }
                }
                // Fallback (Cold Start)
                if (resultado.size() < 4) {
                    List<Producto> fallback = new ArrayList<>(todosProductos);
                    fallback.sort((p1, p2) -> Double.compare(p2.getPromedioCalificacion(), p1.getPromedioCalificacion()));
                    for (Producto p : fallback) {
                        if (!resultado.contains(p)) resultado.add(p);
                        if (resultado.size() >= 30) break;
                    }
                }
                titulo = "Recomendados para ti";
                icono = "fas fa-magic";
                color = "purple";
                break;

            case "verduras":
                for (Producto p : todosProductos) {
                    if (matchesCategory("Verduras y Hortalizas", p.getCategoria())) resultado.add(p);
                }
                titulo = "Verduras Frescas";
                icono = "fas fa-leaf";
                color = "green";
                break;

            case "frutas":
                for (Producto p : todosProductos) {
                    if (matchesCategory("Frutas", p.getCategoria())) resultado.add(p);
                }
                titulo = "Frutas Recien Cosechadas";
                icono = "fas fa-apple-whole";
                color = "red";
                break;

            case "tuberculos":
                for (Producto p : todosProductos) {
                    if (matchesCategory("Tuberculos y Raices", p.getCategoria())) resultado.add(p);
                }
                titulo = "Tuberculos del Campo";
                icono = "fas fa-seedling";
                color = "orange";
                break;

            case "lacteos":
                for (Producto p : todosProductos) {
                    if (matchesCategory("Lacteos", p.getCategoria()) || matchesCategory("Huevos y Lacteos", p.getCategoria())) resultado.add(p);
                }
                titulo = "Lacteos Frescos";
                icono = "fas fa-cheese";
                color = "blue";
                break;

            case "huevos":
                for (Producto p : todosProductos) {
                    if (matchesCategory("Huevos", p.getCategoria()) || matchesCategory("Huevos y Lacteos", p.getCategoria())) resultado.add(p);
                }
                titulo = "Huevos de Granja";
                icono = "fas fa-egg";
                color = "orange";
                break;

            case "granos":
                for (Producto p : todosProductos) {
                    if (matchesCategory("Granos y Cereales", p.getCategoria())) resultado.add(p);
                }
                titulo = "Granos y Cereales";
                icono = "fas fa-wheat-awn";
                color = "orange";
                break;

            case "cafe":
                for (Producto p : todosProductos) {
                    if (matchesCategory("Cafe y Cacao", p.getCategoria())) resultado.add(p);
                }
                titulo = "Cafe Colombiano y Cacao";
                icono = "fas fa-mug-hot";
                color = "orange";
                break;

            default:
                return "redirect:/tienda";
        }

        model.addAttribute("productos", resultado);
        model.addAttribute("tituloColeccion", titulo);
        model.addAttribute("iconoColeccion", icono);
        model.addAttribute("colorColeccion", color);
        model.addAttribute("tipoColeccion", tipo);
        model.addAttribute("cantidadCarrito", carritoService.contarItems());
        
        // Nombre del cliente
        if (auth != null && auth.isAuthenticated()) {
            Usuario usuario = usuarioRepo.findFirstByEmail(auth.getName()).orElse(null);
            model.addAttribute("nombreCliente", usuario != null ? usuario.getNombreCompleto() : "Cliente");
        } else {
            model.addAttribute("nombreCliente", "Invitado");
        }

        return "coleccion";
    }

    // 6.5 DASHBOARD DELIVERY (Repartidor Web)
    @GetMapping("/delivery/dashboard")
    public String dashboardDelivery(Model model, Authentication auth) {
        Usuario repartidor = usuarioRepo.findFirstByEmail(auth.getName()).orElseThrow();
        model.addAttribute("usuario", repartidor);
        model.addAttribute("nombreCompleto", repartidor.getNombreCompleto());
        return "delivery_dashboard";
    }

    // 7. MIS FAVORITOS (Nueva vista independiente)
    @GetMapping("/favoritos")
    public String misFavoritos(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        
        Usuario usuario = usuarioRepo.findFirstByEmail(auth.getName()).orElse(null);
        if (usuario == null) {
            return "redirect:/login";
        }
        
        List<Producto> favoritos = usuario.getProductosFavoritos();
        model.addAttribute("favoritos", favoritos != null ? favoritos : new ArrayList<>());
        
        List<com.proyecto.AccesoUsuarios.model.FavoritoCampesino> campesinosFavoritos = favoritoCampesinoRepo.findByCliente(usuario);
        model.addAttribute("campesinosFavoritos", campesinosFavoritos != null ? campesinosFavoritos : new ArrayList<>());
        
        model.addAttribute("nombreCliente", usuario.getNombreCompleto());
        model.addAttribute("cantidadCarrito", carritoService.contarItems());
        
        return "favoritos";
    }

    // --- Helper: FÃ³rmula de Haversine para Distancias Reales ---
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

    private boolean matchesCategory(String expected, String actual) {
        if (actual == null) return false;
        String e = java.text.Normalizer.normalize(expected, java.text.Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}", "").toLowerCase();
        String a = java.text.Normalizer.normalize(actual, java.text.Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}", "").toLowerCase();
        return e.equals(a);
    }
}