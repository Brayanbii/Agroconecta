package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.repository.DetalleOrdenRepository;
import com.proyecto.AccesoUsuarios.repository.OrdenRepository;
import com.proyecto.AccesoUsuarios.repository.ProductoRepository;
import com.proyecto.AccesoUsuarios.repository.ResenaRepository;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import com.proyecto.AccesoUsuarios.service.CarritoService;
import com.proyecto.AccesoUsuarios.service.PythonService;
import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.model.Producto;
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
        
        model.addAttribute("productos", productoRepo.findByUsuario(campesino)); 
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

        // 1.5 ALGORITMO DE PRODUCTOS MÁS DESTACADOS (Más vendidos)
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
            // Si empatan en ventas, desempatar por calificación
            return Double.compare(p2.getPromedioCalificacion(), p1.getPromedioCalificacion());
        });
        
        // Limitar a los 8 mejores
        if (productosDestacados.size() > 8) {
            productosDestacados = productosDestacados.subList(0, 8);
        }
        model.addAttribute("productosDestacados", productosDestacados);

        // 1.6 ALGORITMO DE RECOMENDACIÓN (Machine Learning Heurístico)
        List<Producto> productosRecomendados = new ArrayList<>();
        if (auth != null && auth.isAuthenticated()) {
            Usuario usuarioObj = usuarioRepo.findByEmail(auth.getName()).orElse(null);
            if (usuarioObj != null) {
                // Algoritmo de Recomendación Basado en Compras Históricas (Content-Based Filtering)
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

                    // Ordenar categorías favoritas descendentemente
                    List<Map.Entry<String, Integer>> catsOrdenadas = new ArrayList<>(catFrecuencia.entrySet());
                    catsOrdenadas.sort((a, b) -> b.getValue().compareTo(a.getValue())); 

                    // Extraer las top 2 categorías de interés del cliente
                    List<String> topCategorias = new ArrayList<>();
                    if (catsOrdenadas.size() > 0) topCategorias.add(catsOrdenadas.get(0).getKey());
                    if (catsOrdenadas.size() > 1) topCategorias.add(catsOrdenadas.get(1).getKey());

                    // Filtrar productos del catálogo que encajen con sus gustos, PERO que NO haya comprado antes
                    for (Producto p : productosList) {
                        if (!productosCompradosIds.contains(p.getId()) && topCategorias.contains(p.getCategoria())) {
                            productosRecomendados.add(p);
                        }
                    }
                }
            }
        }
        
        // Fallback: Si no hay suficientes datos para perfilar al cliente (Cold Start Problem),
        // usamos los mejores calificados que no estén en Destacados.
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

        // 3. LÓGICA NUEVA: Obtener el nombre real del cliente (solo si está logueado)
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            Usuario usuario = usuarioRepo.findByEmail(email).orElse(null);
            if (usuario != null) {
                model.addAttribute("nombreCliente", usuario.getNombreCompleto());
            } else {
                model.addAttribute("nombreCliente", "Cliente");
            }
        } else {
            model.addAttribute("nombreCliente", "Invitado");
        }

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
        List<Producto> otrosProductos = new ArrayList<>(productoRepo.findByUsuario(producto.getUsuario()));
        otrosProductos.removeIf(p -> p.getId().equals(producto.getId())); // no mostrar el mismo producto
        model.addAttribute("otrosProductos", otrosProductos);
        
        // Cantidad de items en el carrito
        model.addAttribute("cantidadCarrito", carritoService.contarItems());

        boolean puedeComentar = false;
        // LÓGICA: Obtener el nombre real del cliente y verificar si compró el producto
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            Usuario usuario = usuarioRepo.findByEmail(email).orElse(null);
            if (usuario != null) {
                model.addAttribute("nombreCliente", usuario.getNombreCompleto());
                
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
            } else {
                model.addAttribute("nombreCliente", "Cliente");
            }
        } else {
            model.addAttribute("nombreCliente", "Invitado");
        }
        
        model.addAttribute("puedeComentar", puedeComentar);

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
