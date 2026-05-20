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
    private OrdenRepository ordenRepo; // <-- Inyectamos el repo de órdenes

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
        
        // --- DATOS REALES PARA EL DASHBOARD VISUAL ---
        List<DetalleOrden> misVentas = detalleRepo.findVentasByCampesino(campesino);
        
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
                
                // Pedidos Pendientes (contamos ordenes unicas)
                if("PENDIENTE".equalsIgnoreCase(o.getEstado())) {
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

        // Agrupar por Categorías para las secciones independientes
        List<Producto> verduras = new ArrayList<>();
        List<Producto> frutas = new ArrayList<>();
        List<Producto> tuberculos = new ArrayList<>();
        List<Producto> lacteos = new ArrayList<>();
        List<Producto> granos = new ArrayList<>();
        List<Producto> otros = new ArrayList<>();

        for (Producto p : productosList) {
            String cat = p.getCategoria() != null ? p.getCategoria().toLowerCase() : "";
            if (cat.contains("verdura")) {
                verduras.add(p);
            } else if (cat.contains("fruta")) {
                frutas.add(p);
            } else if (cat.contains("tubérculo") || cat.contains("tuberculo") || cat.contains("papa") || cat.contains("yuca")) {
                tuberculos.add(p);
            } else if (cat.contains("lácteo") || cat.contains("lacteo") || cat.contains("queso") || cat.contains("leche")) {
                lacteos.add(p);
            } else if (cat.contains("grano") || cat.contains("frijol") || cat.contains("arroz") || cat.contains("lenteja")) {
                granos.add(p);
            } else {
                otros.add(p);
            }
        }

        model.addAttribute("verduras", verduras);
        model.addAttribute("frutas", frutas);
        model.addAttribute("tuberculos", tuberculos);
        model.addAttribute("lacteos", lacteos);
        model.addAttribute("granos", granos);
        model.addAttribute("otros", otros);

        // 1.6 ALGORITMO DE RECOMENDACIÓN (Machine Learning Heurístico)
        // Ya no necesitamos recomendaciones por defecto aquí, la tienda mostrará secciones categorizadas

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

        // 3. LÓGICA NUEVA: Obtener el nombre real del cliente y sus favoritos
        List<Long> favoritosIds = new ArrayList<>();
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            Usuario usuario = usuarioRepo.findByEmail(email).orElse(null);
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
                .orElseThrow(() -> new IllegalArgumentException("Producto inválido:" + id));
        
        model.addAttribute("producto", producto);
        
        // Calcular frescura (mensajeDinámico)
        long diasFresco = 0;
        String mensajeFrescura = "Cosechado hoy";
        if (producto.getFechaCreacion() != null) {
            diasFresco = java.time.temporal.ChronoUnit.DAYS.between(producto.getFechaCreacion().toLocalDate(), java.time.LocalDate.now());
            if (diasFresco == 0) {
                mensajeFrescura = "Cosechado Hoy";
            } else if (diasFresco > 0 && diasFresco <= 3) {
                mensajeFrescura = "Producto muy fresco";
            } else {
                mensajeFrescura = "Cosechado hace " + diasFresco + " días";
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
        // LÓGICA: Obtener el nombre real del cliente y verificar si compró el producto
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            Usuario usuario = usuarioRepo.findByEmail(email).orElse(null);
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
                
                // Buscar si este usuario ya tiene una reseña para este producto
                var miResenaOpt = resenaRepo.findByProductoIdAndUsuarioId(producto.getId(), usuario.getId());
                if (miResenaOpt.isPresent()) {
                    var miResena = miResenaOpt.get();
                    model.addAttribute("miResenaId", miResena.getId());
                    model.addAttribute("miResenaEstrellas", miResena.getEstrellas());
                    model.addAttribute("miResenaComentario", miResena.getComentario() != null ? miResena.getComentario() : "");
                    model.addAttribute("yaCalificó", true);
                } else {
                    model.addAttribute("miResenaId", null);
                    model.addAttribute("miResenaEstrellas", 0);
                    model.addAttribute("miResenaComentario", "");
                    model.addAttribute("yaCalificó", false);
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
                model.addAttribute("yaCalificó", false);
                model.addAttribute("miResenaId", null);
                model.addAttribute("miResenaEstrellas", 0);
                model.addAttribute("miResenaComentario", "");
            }
        } else {
            model.addAttribute("nombreCliente", "Invitado");
            model.addAttribute("favoritosIds", new java.util.ArrayList<>());
            model.addAttribute("yaCalificó", false);
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
        
        // Ventas y calificación promedio
        List<com.proyecto.AccesoUsuarios.model.DetalleOrden> ventas = detalleRepo.findVentasByCampesino(campesino);
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
            Usuario usuario = usuarioRepo.findByEmail(email).orElse(null);
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
            Usuario usuarioObj = usuarioRepo.findByEmail(auth.getName()).orElse(null);
            if (usuarioObj != null) {
                isFavorito = favoritoCampesinoRepo.existsByClienteAndCampesino(usuarioObj, campesino);
            }
        }
        model.addAttribute("isFavorito", isFavorito);
        
        return "campesino_perfil_publico";
    }

    // 6. COLECCIONES (Ver Todo de cada sección del Home)
    @GetMapping("/tienda/coleccion/{tipo}")
    public String coleccion(@org.springframework.web.bind.annotation.PathVariable String tipo, 
                            Model model, Authentication auth) {
        
        List<Producto> todosProductos = productoRepo.findAll();
        List<Producto> resultado = new ArrayList<>();
        String titulo = "Colección";
        String icono = "fas fa-store";
        String color = "green"; // Color temático de la colección
        
        switch (tipo) {
            case "destacados":
                // Reutilizar algoritmo de más vendidos
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
                titulo = "Productos Más Destacados";
                icono = "fas fa-fire";
                color = "orange";
                break;

            case "nuevos":
                // Ordenar por fecha de creación descendente (más nuevo primero)
                resultado = new ArrayList<>(todosProductos);
                resultado.sort((p1, p2) -> {
                    if (p1.getFechaCreacion() == null && p2.getFechaCreacion() == null) return 0;
                    if (p1.getFechaCreacion() == null) return 1;
                    if (p2.getFechaCreacion() == null) return -1;
                    return p2.getFechaCreacion().compareTo(p1.getFechaCreacion());
                });
                titulo = "Recién Cosechados";
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
                // Reutilizar el mismo algoritmo de recomendación
                if (auth != null && auth.isAuthenticated()) {
                    Usuario usuarioObj = usuarioRepo.findByEmail(auth.getName()).orElse(null);
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
                    if ("Verduras y Hortalizas".equalsIgnoreCase(p.getCategoria())) resultado.add(p);
                }
                titulo = "Verduras Frescas";
                icono = "fas fa-leaf";
                color = "green";
                break;

            case "frutas":
                for (Producto p : todosProductos) {
                    if ("Frutas".equalsIgnoreCase(p.getCategoria())) resultado.add(p);
                }
                titulo = "Frutas Recién Cosechadas";
                icono = "fas fa-apple-whole";
                color = "red";
                break;

            case "tuberculos":
                for (Producto p : todosProductos) {
                    if ("Tubérculos y Raíces".equalsIgnoreCase(p.getCategoria())) resultado.add(p);
                }
                titulo = "Tubérculos del Campo";
                icono = "fas fa-seedling";
                color = "orange";
                break;

            case "lacteos":
            case "huevos":
                for (Producto p : todosProductos) {
                    if ("Huevos y Lácteos".equalsIgnoreCase(p.getCategoria())) resultado.add(p);
                }
                if (tipo.equals("huevos")) {
                    titulo = "Huevos de Granja";
                    icono = "fas fa-egg";
                    color = "orange";
                } else {
                    titulo = "Lácteos Frescos";
                    icono = "fas fa-cheese";
                    color = "orange";
                }
                break;

            case "granos":
                for (Producto p : todosProductos) {
                    if ("Granos y Cereales".equalsIgnoreCase(p.getCategoria())) resultado.add(p);
                }
                titulo = "Granos y Cereales";
                icono = "fas fa-wheat-awn";
                color = "orange";
                break;

            case "cafe":
                for (Producto p : todosProductos) {
                    if ("Café y Cacao".equalsIgnoreCase(p.getCategoria())) resultado.add(p);
                }
                titulo = "Café Colombiano y Cacao";
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
            Usuario usuario = usuarioRepo.findByEmail(auth.getName()).orElse(null);
            model.addAttribute("nombreCliente", usuario != null ? usuario.getNombreCompleto() : "Cliente");
        } else {
            model.addAttribute("nombreCliente", "Invitado");
        }

        return "coleccion";
    }

    // 7. MIS FAVORITOS (Nueva vista independiente)
    @GetMapping("/favoritos")
    public String misFavoritos(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        
        Usuario usuario = usuarioRepo.findByEmail(auth.getName()).orElse(null);
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
