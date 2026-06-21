package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.model.Producto;
import com.proyecto.AccesoUsuarios.model.FavoritoProducto;
import com.proyecto.AccesoUsuarios.model.Orden;
import com.proyecto.AccesoUsuarios.model.Ruta;
import com.proyecto.AccesoUsuarios.model.DetalleOrden;
import com.proyecto.AccesoUsuarios.model.Resena;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import com.proyecto.AccesoUsuarios.repository.ProductoRepository;
import com.proyecto.AccesoUsuarios.repository.OrdenRepository;
import com.proyecto.AccesoUsuarios.repository.DetalleOrdenRepository;
import com.proyecto.AccesoUsuarios.repository.RutaRepository;
import com.proyecto.AccesoUsuarios.service.OrdenEstadoService;
import com.proyecto.AccesoUsuarios.service.RutaAgrupacionService;
import com.proyecto.AccesoUsuarios.repository.FavoritoProductoRepository;
import com.proyecto.AccesoUsuarios.repository.FavoritoCampesinoRepository;
import com.proyecto.AccesoUsuarios.repository.ResenaRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioRepository repo;

    @Autowired
    private ProductoRepository productoRepo; // Inyectamos el repositorio de productos para la API móvil

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DetalleOrdenRepository detalleRepo;

    @Autowired
    private FavoritoProductoRepository favoritoProductoRepo;

    @Autowired
    private ResenaRepository resenaRepo;

    @Autowired
    private com.proyecto.AccesoUsuarios.service.UploadFileService uploadFileService;

    @Autowired
    private com.proyecto.AccesoUsuarios.service.PythonService pythonService;

    @Autowired
    private com.proyecto.AccesoUsuarios.repository.FavoritoCampesinoRepository favoritoCampesinoRepo;

    @Autowired
    private OrdenRepository ordenRepo;

    @Autowired
    private com.proyecto.AccesoUsuarios.service.OrdenEstadoService ordenEstadoService;

    @Autowired
    private com.proyecto.AccesoUsuarios.service.NotificationService notificationService;

    @Autowired
    private RutaRepository rutaRepo;

    @Autowired
    private RutaAgrupacionService agrupacionService;

    @Autowired
    private com.proyecto.AccesoUsuarios.repository.NotificacionRepository notificacionRepo;

    // ==========================================
    //   VISTAS WEB MVC TRADICIONALES (Thymeleaf)
    // ==========================================

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/home")
    public String home(Model model, Authentication auth) {
        if (auth != null) {
            model.addAttribute("rol", auth.getAuthorities().toString());
        }
        return "home";
    }

    @GetMapping("/usuarios")
    public String listar(Model model) {
        model.addAttribute("usuarios", repo.findAll());
        return "usuarios";
    }

    @GetMapping("/usuarios/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "form";
    }

    @PostMapping("/usuarios/guardar")
    public String guardar(@ModelAttribute Usuario usuario) {
        if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        repo.save(usuario);
        return "redirect:/usuarios";
    }

    @GetMapping("/usuarios/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Optional<Usuario> u = repo.findById(id);
        if (u.isPresent()) {
            model.addAttribute("usuario", u.get());
            return "form";
        }
        return "redirect:/usuarios";
    }

    @GetMapping("/usuarios/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        repo.deleteById(id);
        return "redirect:/usuarios";
    }

    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    @PostMapping("/registro/guardar")
    public String guardarRegistro(@ModelAttribute @Valid Usuario usuario, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "registro";
        }
        if (repo.findByEmailAndRol(usuario.getEmail(), usuario.getRol()).isPresent()) {
            model.addAttribute("errorEmail", "Ya tienes una cuenta con este correo y este rol");
            return "registro";
        }
        if (repo.findByUserName(usuario.getUserName()).isPresent()) {
            model.addAttribute("errorUser", "El nombre de usuario ya existe");
            return "registro";
        }
        
        // Encriptar contraseña y asignar rol por defecto en la web
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        if ("CAMPESINO".equals(usuario.getRol())) {
            usuario.setEstadoVerificacion("PENDIENTE_DATOS");
        } else if ("REPARTIDOR".equals(usuario.getRol())) {
            usuario.setEstadoVerificacion("PENDIENTE_DATOS");
        } else {
            usuario.setRol("CLIENTE");
        }
        repo.save(usuario);
        return "redirect:/login?registrado";
    }

    // ==========================================
    //   APIS DE VERIFICACIÓN (USADAS POR LA APP)
    // ==========================================

    @GetMapping("/api/usuarios/check-email")
    @ResponseBody
    public Map<String, Boolean> checkEmail(@RequestParam String email) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", repo.findFirstByEmail(email).isPresent());
        return response;
    }

    @GetMapping("/api/usuarios/check-username")
    @ResponseBody
    public Map<String, Boolean> checkUsername(@RequestParam String username) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", repo.findByUserName(username).isPresent());
        return response;
    }

    @GetMapping("/api/usuarios/check-telefono")
    @ResponseBody
    public Map<String, Boolean> checkTelefono(@RequestParam String telefono) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", repo.findByTelefono(telefono).isPresent());
        return response;
    }

    // ==========================================
    //   APIS MÓVILES (REGISTRO Y LOGIN)
    // ==========================================

    // Endpoint de registro seguro para la app móvil
    @PostMapping("/api/usuarios/registrar")
    @ResponseBody
    public Map<String, Object> registrarUsuarioMobile(@RequestBody Usuario usuario) {
        Map<String, Object> response = new HashMap<>();
        
        // Validaciones preventivas de duplicados en la BD
        if (repo.findByEmailAndRol(usuario.getEmail(), usuario.getRol()).isPresent()) {
            response.put("success", false);
            response.put("message", "Ya tienes una cuenta con este correo y rol");
            return response;
        }
        if (repo.findByUserName(usuario.getUserName()).isPresent()) {
            response.put("success", false);
            response.put("message", "El nombre de usuario ya existe");
            return response;
        }

        // Asignación de Roles idéntica a la lógica web
        if ("CAMPESINO".equals(usuario.getRol())) {
            usuario.setEstadoVerificacion("PENDIENTE_DATOS");
        } else if ("REPARTIDOR".equals(usuario.getRol())) {
            usuario.setEstadoVerificacion("PENDIENTE_DATOS");
        } else {
            usuario.setRol("CLIENTE");
        }
        
        // Si no viene nombreCompleto desde la app, usar el userName
        if (usuario.getNombreCompleto() == null || usuario.getNombreCompleto().isBlank()) {
            usuario.setNombreCompleto(usuario.getUserName());
        }
        
        // Encriptar contraseña de forma segura
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        
        try {
            repo.save(usuario);
            response.put("success", true);
            response.put("message", "Usuario creado con éxito desde el celular");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error general en la base de datos");
        }
        
        return response;
    }

    // Endpoint de inicio de sesion seguro para la app movil (Busqueda por correo electronico)
    @PostMapping("/api/usuarios/login")
    @ResponseBody
    public Map<String, Object> loginMobile(@RequestBody Map<String, String> credenciales,
                                           HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        String email = credenciales.get("email");
        String password = credenciales.get("password");

        System.out.println("====== [DIAGNOSTICO API MOVIL] ======");
        System.out.println("-> Intentando iniciar sesion para el correo: [" + email + "]");

        List<Usuario> usuarios = repo.findAllByEmail(email);

        if (usuarios.isEmpty()) {
            System.out.println("X ERROR: El correo [" + email + "] no existe.");
            response.put("success", false);
            response.put("message", "El correo electronico no esta registrado.");
            return response;
        }

        Usuario u = null;
        for (Usuario candidato : usuarios) {
            if (passwordEncoder.matches(password, candidato.getPassword())) {
                u = candidato;
                break;
            }
        }

        if (u != null) {
            // Autenticar en Spring Security para que los endpoints protegidos funcionen
            // Usamos el ID del usuario como principal para soportar emails duplicados con roles diferentes
            UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                    String.valueOf(u.getId()), null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + u.getRol()))
                );
            SecurityContextHolder.getContext().setAuthentication(authToken);
            request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            response.put("success", true);
            response.put("message", "Inicio de sesion exitoso!");

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", u.getId());
            userData.put("userName", u.getUserName());
            userData.put("email", u.getEmail());
            userData.put("rol", u.getRol());

            response.put("user", userData);
        } else {
            response.put("success", false);
            response.put("message", "Credenciales incorrectas. Intentalo de nuevo.");
        }

        return response;
    }

    // ==========================================
    //   APIS MÓVILES PARA GESTIÓN DE PRODUCTOS 🌾
    // ==========================================

    // Listar todos los productos en venta para el catálogo del Cliente
    @GetMapping("/api/productos")
    @ResponseBody
    public List<Producto> listarProductosMobile() {
        System.out.println("📢 [API MÓVIL] Enviando catalogo completo de productos...");
        return productoRepo.findAll();
    }

    // Productos de un campesino especifico
    @GetMapping("/api/productos/campesino/{id}")
    @ResponseBody
    public List<Producto> productosPorCampesino(@PathVariable Long id) {
        return productoRepo.findByUsuarioId(id);
    }

    // Mis productos (campesino autenticado)
    @GetMapping("/api/productos/mis-productos")
    @ResponseBody
    public ResponseEntity<?> misProductos(Authentication auth) {
        if (auth == null || !auth.isAuthenticated())
            return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        Usuario user = repo.findFirstByEmail(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
        return ResponseEntity.ok(productoRepo.findByUsuario(user));
    }

    // Perfil publico del campesino para la app movil
    @GetMapping("/api/campesino/{id}/perfil")
    @ResponseBody
    public Map<String, Object> perfilCampesino(@PathVariable Long id) {
        Map<String, Object> resp = new HashMap<>();
        Usuario c = repo.findById(id).orElse(null);
        if (c == null || !"CAMPESINO".equals(c.getRol())) {
            resp.put("error", "Campesino no encontrado");
            return resp;
        }
        resp.put("id", c.getId());
        resp.put("nombreCompleto", c.getNombreCompleto());
        resp.put("nombreFinca", c.getNombreFinca());
        resp.put("descripcionFinca", c.getDescripcionFinca());
        resp.put("municipioOrigen", c.getMunicipioOrigen());
        resp.put("fotoPerfil", c.getFotoPerfil());
        resp.put("fotoFincaUrl", c.getFotoFincaUrl());
        resp.put("estadoVerificacion", c.getEstadoVerificacion());

        List<Producto> productos = productoRepo.findByUsuario(c);
        resp.put("totalProductos", productos.size());

        // Calificacion promedio de todos sus productos
        double prom = 0;
        if (!productos.isEmpty()) {
            prom = productos.stream().mapToDouble(p -> p.getPromedioCalificacion() != null ? p.getPromedioCalificacion() : 0.0).average().orElse(0);
        }
        resp.put("promedioCalificacion", Math.round(prom * 10.0) / 10.0);

        // Total vendidos
        List<com.proyecto.AccesoUsuarios.model.DetalleOrden> ventas = detalleRepo.findVentasByCampesino(c, c.getId());
        int total = ventas.stream().mapToInt(d -> d.getCantidad() != null ? d.getCantidad() : 0).sum();
        resp.put("totalVendidos", total);

        return resp;
    }

    // Guardar un nuevo producto enviado por el Campesino desde el celular
    @PostMapping("/api/productos/registrar")
    @ResponseBody
    @Transactional
    public Map<String, Object> registrarProductoMobile(@RequestBody Producto producto, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Usuario usuario = null;
            if (auth != null && auth.isAuthenticated()) {
                usuario = repo.findFirstByEmail(auth.getName()).orElse(null);
            }
            if (usuario == null) {
                response.put("success", false);
                response.put("message", "Debes iniciar sesion para publicar productos.");
                return response;
            }
            producto.setUsuario(usuario);
            productoRepo.save(producto);
            response.put("success", true);
            response.put("message", "Producto publicado con exito!");
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error al guardar: " + e.getMessage());
        }
        return response;
    }

    // API MOVIL: Obtener un producto por ID
    @GetMapping("/api/productos/{id}")
    @ResponseBody
    public ResponseEntity<?> obtenerProducto(@PathVariable Long id) {
        Producto p = productoRepo.findById(id).orElse(null);
        if (p == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(p);
    }

    // API MOVIL: Actualizar producto (solo el dueno)
    @PutMapping("/api/productos/{id}")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> actualizarProducto(@PathVariable Long id, @RequestBody Producto datos, Authentication auth) {
        Producto p = productoRepo.findById(id).orElse(null);
        if (p == null) return ResponseEntity.notFound().build();
        // Verificar que el usuario autenticado es el dueno o producto huerfano
        if (auth == null || !auth.isAuthenticated()) return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        Usuario user = repo.findFirstByEmail(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Usuario no encontrado"));
        // Si el producto tiene dueno y no es el usuario actual, rechazar
        if (p.getUsuario() != null && !p.getUsuario().getId().equals(user.getId()))
            return ResponseEntity.status(403).body(Map.of("error", "Este producto pertenece a otro campesino"));
        // Si no tiene dueno, asignarselo
        if (p.getUsuario() == null) p.setUsuario(user);
        if (datos.getNombre() != null) p.setNombre(datos.getNombre());
        if (datos.getDescripcion() != null) p.setDescripcion(datos.getDescripcion());
        if (datos.getPrecio() != null) p.setPrecio(datos.getPrecio());
        if (datos.getCategoria() != null) p.setCategoria(datos.getCategoria());
        if (datos.getStock() != null) p.setStock(datos.getStock());
        if (datos.getUnidad() != null) p.setUnidad(datos.getUnidad());
        if (datos.getImagenUrl() != null) p.setImagenUrl(datos.getImagenUrl());
        productoRepo.save(p);
        return ResponseEntity.ok(Map.of("success", true, "message", "Producto actualizado"));
    }

    // API MOVIL: Eliminar producto (dueno o huerfano)
    @DeleteMapping("/api/productos/{id}")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> eliminarProducto(@PathVariable Long id, Authentication auth) {
        Producto p = productoRepo.findById(id).orElse(null);
        if (p == null) return ResponseEntity.notFound().build();
        if (auth == null || !auth.isAuthenticated()) return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        Usuario user = repo.findFirstByEmail(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Usuario no encontrado"));
        if (p.getUsuario() != null && !p.getUsuario().getId().equals(user.getId()))
            return ResponseEntity.status(403).body(Map.of("error", "Este producto pertenece a otro campesino"));
        try {
            // 1. Eliminar favoritos
            List<FavoritoProducto> favs = favoritoProductoRepo.findByProducto(p);
            if (!favs.isEmpty()) favoritoProductoRepo.deleteAll(favs);
            // 2. Desvincular DetalleOrden
            List<DetalleOrden> detalles = detalleRepo.findByProducto(p);
            if (detalles != null) {
                for (DetalleOrden d : detalles) { d.setProducto(null); detalleRepo.save(d); }
            }
            // 3. Eliminar resenas
            List<Resena> resenas = resenaRepo.findByProductoIdOrderByFechaDesc(id);
            if (resenas != null && !resenas.isEmpty()) resenaRepo.deleteAll(resenas);
            // 4. Eliminar producto
            productoRepo.delete(p);
            return ResponseEntity.ok(Map.of("success", true, "message", "Producto eliminado"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al eliminar: " + e.getMessage()));
        }
    }

    // API MOVIL: Subir imagen de producto
    @PostMapping("/api/productos/upload-image")
    @ResponseBody
    public Map<String, Object> uploadImagen(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (file.isEmpty()) { response.put("success", false); response.put("message", "Archivo vacio"); return response; }
            String nombreArchivo = uploadFileService.saveImage(file);
            response.put("success", true); response.put("filename", nombreArchivo);
        } catch (Exception e) {
            response.put("success", false); response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    // API MOVIL: Actualizar stock rapido (sumar/restar/set)
    @PostMapping("/api/productos/{id}/actualizar-stock")
    @ResponseBody
    public Map<String, Object> actualizarStockApi(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (auth == null || !auth.isAuthenticated()) {
                response.put("success", false);
                response.put("error", "No autenticado");
                return response;
            }
            String email = auth.getName();
            Usuario campesino = repo.findFirstByEmail(email).orElseThrow();
            Producto producto = productoRepo.findById(id).orElseThrow();
            if (producto.getUsuario() == null || !producto.getUsuario().getId().equals(campesino.getId())) {
                response.put("success", false);
                response.put("error", "No autorizado");
                return response;
            }
            String accion = (String) body.getOrDefault("accion", "set");
            int valor = body.get("valor") instanceof Number ? ((Number) body.get("valor")).intValue() : 1;
            int stockActual = producto.getStock() != null ? producto.getStock() : 0;
            int nuevoStock;
            switch (accion) {
                case "sumar": nuevoStock = stockActual + valor; break;
                case "restar": nuevoStock = Math.max(0, stockActual - valor); break;
                default: nuevoStock = Math.max(0, valor); break;
            }
            producto.setStock(nuevoStock);
            productoRepo.save(producto);
            response.put("success", true);
            response.put("nuevoStock", nuevoStock);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    // API MOVIL: Informe de analiticas para el campesino
    @GetMapping("/api/analiticas/informe")
    @ResponseBody
    public Map<String, Object> informeAnaliticas(Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (auth == null || !auth.isAuthenticated()) {
                response.put("success", false);
                response.put("error", "No autenticado");
                return response;
            }
            String email = auth.getName();
            Usuario campesino = repo.findFirstByEmail(email).orElseThrow();

            List<DetalleOrden> ventas = detalleRepo.findVentasByCampesino(campesino, campesino.getId());

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

            double totalIngresos = ventas.stream().mapToDouble(d -> d.getTotal() != null ? d.getTotal() : 0.0).sum();
            int totalUnidades = ventas.stream().mapToInt(d -> d.getCantidad() != null ? d.getCantidad() : 0).sum();
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

            Map<String, Object> payload = new HashMap<>();
            payload.put("productos", productos);
            payload.put("ventas_mes", ventasMes);
            payload.put("resumen", resumen);

            Map<String, Object> informe = pythonService.generarInformeCampesino(payload);

            response.put("success", true);
            response.put("resumen", resumen);
            if (informe != null) {
                response.put("grafico_top_productos", informe.get("grafico_top_productos"));
                response.put("grafico_distribucion", informe.get("grafico_distribucion"));
                response.put("grafico_ingresos_mes", informe.get("grafico_ingresos_mes"));
                response.put("grafico_vs_mercado", informe.get("grafico_vs_mercado"));
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    // API MOVIL: Reputacion del campesino (resenas, calificaciones, podio)
    @GetMapping("/api/reputacion/informe")
    @ResponseBody
    public Map<String, Object> informeReputacion(Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (auth == null || !auth.isAuthenticated()) {
                response.put("success", false); response.put("error", "No autenticado"); return response;
            }
            String email = auth.getName();
            Usuario campesino = repo.findFirstByEmail(email).orElseThrow();
            List<Producto> productos = productoRepo.findByUsuario(campesino);

            List<Map<String, Object>> buenasResenas = new ArrayList<>();
            List<Map<String, Object>> oportunidadesMejora = new ArrayList<>();
            double sumaCalif = 0;
            int totalResenas = 0;
            int[] dist = new int[5];

            for (Producto p : productos) {
                if (p.getResenas() != null) {
                    for (Resena r : p.getResenas()) {
                        totalResenas++;
                        sumaCalif += r.getEstrellas();
                        dist[r.getEstrellas() - 1]++;
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", r.getId());
                        item.put("estrellas", r.getEstrellas());
                        item.put("comentario", r.getComentario());
                        item.put("fecha", r.getFecha() != null ? r.getFecha().toString() : "");
                        item.put("nombreAutor", r.getNombreAutor());
                        item.put("nombreProducto", p.getNombre());
                        if (r.getEstrellas() >= 4) buenasResenas.add(item);
                        else oportunidadesMejora.add(item);
                    }
                }
            }
            double califGral = totalResenas > 0 ? Math.round((sumaCalif / totalResenas) * 10.0) / 10.0 : 0.0;
            int pctPositivo = totalResenas > 0 ? (int) Math.round((buenasResenas.size() * 100.0) / totalResenas) : 0;
            buenasResenas.sort((a, b) -> String.valueOf(b.get("fecha")).compareTo(String.valueOf(a.get("fecha"))));
            oportunidadesMejora.sort((a, b) -> String.valueOf(b.get("fecha")).compareTo(String.valueOf(a.get("fecha"))));

            List<Map<String, Object>> mejoresProductos = new ArrayList<>();
            for (Producto p : productos) {
                if (p.getResenas() != null && !p.getResenas().isEmpty() && p.getPromedioCalificacion() >= 3.5) {
                    Map<String, Object> mp = new HashMap<>();
                    mp.put("id", p.getId()); mp.put("nombre", p.getNombre());
                    mp.put("promedioCalificacion", p.getPromedioCalificacion());
                    mp.put("imagenUrl", p.getImagenUrl());
                    mp.put("totalResenas", p.getTotalResenas());
                    mejoresProductos.add(mp);
                }
            }
            mejoresProductos.sort((a, b) -> Double.compare(
                (Double) b.get("promedioCalificacion"), (Double) a.get("promedioCalificacion")));
            String productoEstrella = mejoresProductos.isEmpty() ? "—" : (String) mejoresProductos.get(0).get("nombre");

            int likesPerfil = favoritoCampesinoRepo.countByCampesino(campesino);
            int likesProductos = favoritoProductoRepo.countByProducto_Usuario(campesino);

            response.put("success", true);
            response.put("calificacionGeneral", califGral);
            response.put("totalResenas", totalResenas);
            response.put("porcentajePositivo", pctPositivo);
            response.put("productoEstrella", productoEstrella);
            response.put("oportunidadesCount", oportunidadesMejora.size());
            response.put("likesPerfil", likesPerfil);
            response.put("likesProductos", likesProductos);
            response.put("distribucion", Arrays.asList(dist[4], dist[3], dist[2], dist[1], dist[0]));
            response.put("mejoresProductos", mejoresProductos.size() > 3 ? mejoresProductos.subList(0, 3) : mejoresProductos);
            response.put("buenasResenas", buenasResenas);
            response.put("oportunidadesMejora", oportunidadesMejora);
        } catch (Exception e) {
            response.put("success", false); response.put("error", e.getMessage());
        }
        return response;
    }

    // API MOVIL: Ventas del campesino (pedidos con estado)
    @GetMapping("/api/pedidos/mis-ventas")
    @ResponseBody
    public Map<String, Object> misVentasApi(Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (auth == null || !auth.isAuthenticated()) {
                response.put("success", false); response.put("error", "No autenticado"); return response;
            }
            String email = auth.getName();
            Usuario campesino = repo.findFirstByEmail(email).orElseThrow();
            List<DetalleOrden> ventas = detalleRepo.findVentasByCampesino(campesino, campesino.getId());

            long nuevos = 0, preparados = 0, listos = 0, enCamino = 0, entregados = 0, cancelados = 0;
            List<Map<String, Object>> items = new ArrayList<>();
            for (DetalleOrden v : ventas) {
                String estado = v.getEstado() != null ? v.getEstado() : "NUEVO";
                if ("NUEVO".equals(estado)) nuevos++;
                else if ("PREPARADO".equals(estado)) preparados++;
                else if ("LISTO_PARA_RECOGER".equals(estado)) listos++;
                else if ("ENVIADO".equals(estado)) enCamino++;
                else if ("ENTREGADO".equals(estado)) entregados++;
                else if ("CANCELADO".equals(estado)) cancelados++;

                Map<String, Object> item = new HashMap<>();
                item.put("id", v.getId());
                item.put("nombre", v.getNombre());
                item.put("precio", v.getPrecio());
                item.put("cantidad", v.getCantidad());
                item.put("total", v.getTotal());
                item.put("estado", estado);
                item.put("ordenId", v.getOrden() != null ? v.getOrden().getId() : null);
                item.put("fechaOrden", v.getOrden() != null && v.getOrden().getFechaCreacion() != null ? v.getOrden().getFechaCreacion().toString() : "");
                item.put("clienteNombre", v.getOrden() != null && v.getOrden().getUsuario() != null ? v.getOrden().getUsuario().getNombreCompleto() : "Anonimo");
                item.put("direccionEnvio", v.getOrden() != null ? v.getOrden().getDireccionEnvio() : "");
                item.put("unidad", v.getProducto() != null ? v.getProducto().getUnidad() : "Kg");
                // PIN de recogida (solo visible para campesino)
                item.put("codigoRecogida", v.getOrden() != null && "LISTO_PARA_RECOGER".equals(estado) ? v.getOrden().getCodigoRecogida() : null);
                // Codigo de ruta
                item.put("codigoRuta", v.getOrden() != null && v.getOrden().getRuta() != null ? v.getOrden().getRuta().getCodigoRuta() : null);
                // Tracking repartidor
                if (v.getOrden() != null && v.getOrden().getRuta() != null && v.getOrden().getRuta().getRepartidor() != null) {
                    Usuario rep = v.getOrden().getRuta().getRepartidor();
                    Map<String, Object> repInfo = new HashMap<>();
                    repInfo.put("nombre", rep.getNombreCompleto());
                    repInfo.put("telefono", rep.getTelefono());
                    repInfo.put("tipoVehiculo", rep.getTipoVehiculo());
                    repInfo.put("placa", rep.getPlacaVehiculo());
                    repInfo.put("latitud", rep.getLatitud());
                    repInfo.put("longitud", rep.getLongitud());
                    repInfo.put("fotoPerfil", rep.getFotoPerfil());
                    repInfo.put("calificacion", rep.getCalificacionPromedio() != null ? rep.getCalificacionPromedio() : 0.0);
                    repInfo.put("estadoRuta", v.getOrden().getRuta().getEstado());
                    // Coordenadas de la finca
                    repInfo.put("fincaLat", v.getOrden().getLatitudOrigen());
                    repInfo.put("fincaLng", v.getOrden().getLongitudOrigen());
                    item.put("repartidor", repInfo);
                }
                items.add(item);
            }

            Map<String, Long> conteos = new HashMap<>();
            conteos.put("nuevos", nuevos);
            conteos.put("preparados", preparados);
            conteos.put("listosParaRecoger", listos);
            conteos.put("enCamino", enCamino);
            conteos.put("entregados", entregados);
            conteos.put("cancelados", cancelados);

            response.put("success", true);
            response.put("ventas", items);
            response.put("conteos", conteos);
        } catch (Exception e) {
            response.put("success", false); response.put("error", e.getMessage());
        }
        return response;
    }

    // API MOVIL: Cambiar estado de un pedido
    @PostMapping("/api/pedidos/{id}/estado")
    @ResponseBody
    public Map<String, Object> actualizarEstadoApi(@PathVariable Long id, @RequestBody Map<String, String> body, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (auth == null || !auth.isAuthenticated()) {
                response.put("success", false); response.put("error", "No autenticado"); return response;
            }
            DetalleOrden detalle = detalleRepo.findById(id).orElseThrow();
            Usuario campesino = repo.findFirstByEmail(auth.getName()).orElseThrow();
            if (detalle.getProducto() != null && detalle.getProducto().getUsuario() != null && !detalle.getProducto().getUsuario().getId().equals(campesino.getId())) {
                response.put("success", false); response.put("error", "No autorizado"); return response;
            }
            String nuevoEstado = body.getOrDefault("estado", "NUEVO");
            detalle.setEstado(nuevoEstado);
            // Si marca como LISTO_PARA_RECOGER, generar PIN
            if ("LISTO_PARA_RECOGER".equals(nuevoEstado) && detalle.getOrden() != null) {
                Orden orden = detalle.getOrden();
                if (orden.getCodigoRecogida() == null) {
                    String pin = String.valueOf(1000 + (int)(Math.random() * 899999));
                    orden.setCodigoRecogida(pin);
                    orden.setIntentosRecogida(0);
                    orden.setFechaGeneracionRecogida(java.time.LocalDateTime.now());
                    if (orden.getDetalles() != null) {
                        for (DetalleOrden d : orden.getDetalles()) {
                            d.setEstado("LISTO_PARA_RECOGER");
                        }
                    }
                }
                response.put("codigoRecogida", detalle.getOrden().getCodigoRecogida());
            }
            detalleRepo.save(detalle);
            if (detalle.getOrden() != null) {
                ordenRepo.save(detalle.getOrden());
            }
            response.put("success", true);
            response.put("nuevoEstado", nuevoEstado);
        } catch (Exception e) {
            response.put("success", false); response.put("error", e.getMessage());
        }
        return response;
    }

    // API MOVIL: Logistica de un pedido (datos para mapa)
    @GetMapping("/api/pedidos/logistica/{id}")
    @ResponseBody
    public Map<String, Object> logisticaApi(@PathVariable Long id, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (auth == null || !auth.isAuthenticated()) {
                response.put("success", false); response.put("error", "No autenticado"); return response;
            }
            DetalleOrden detalle = detalleRepo.findById(id).orElseThrow();
            Orden orden = detalle.getOrden();
            Usuario campesino = repo.findFirstByEmail(auth.getName()).orElseThrow();

            Double origenLat = 5.9317;
            Double origenLon = -73.6147;
            if (detalle.getProducto() != null && detalle.getProducto().getLatitudOrigen() != null) {
                origenLat = detalle.getProducto().getLatitudOrigen();
                origenLon = detalle.getProducto().getLongitudOrigen();
            } else if (campesino.getLatitud() != null && campesino.getLongitud() != null) {
                origenLat = campesino.getLatitud();
                origenLon = campesino.getLongitud();
            }
            Double destLat = orden.getLatitudEnvio() != null ? orden.getLatitudEnvio() : 7.1254;
            Double destLon = orden.getLongitudEnvio() != null ? orden.getLongitudEnvio() : -73.1198;

            Map<String, Object> payload = new HashMap<>();
            payload.put("origen", Map.of("lat", origenLat, "lon", origenLon));
            payload.put("destino", Map.of("lat", destLat, "lon", destLon));
            Map<String, Object> ruta = pythonService.calcularRutaLogistica(payload);

            response.put("success", true);
            response.put("origenLat", origenLat); response.put("origenLon", origenLon);
            response.put("destLat", destLat); response.put("destLon", destLon);
            response.put("producto", detalle.getNombre());
            response.put("cantidad", detalle.getCantidad());
            response.put("total", detalle.getTotal());
            response.put("direccionEnvio", orden.getDireccionEnvio());
            response.put("clienteNombre", orden.getUsuario() != null ? orden.getUsuario().getNombreCompleto() : "Anonimo");
            if (ruta != null) {
                response.put("distancia_km", ruta.get("distancia_km"));
                response.put("duracion_min", ruta.get("duracion_min"));
                if (ruta.get("geometria") != null) {
                    response.put("geometria", ruta.get("geometria"));
                }
            }
        } catch (Exception e) {
            response.put("success", false); response.put("error", e.getMessage());
        }
        return response;
    }

    // API MOVIL: Campesino acepta una orden (cambia a ACEPTADO_POR_CAMPESINO)
    @PostMapping("/api/pedidos/orden/{ordenId}/aceptar")
    @ResponseBody
    public Map<String, Object> aceptarOrdenCampesino(@PathVariable Long ordenId, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (auth == null || !auth.isAuthenticated()) {
                response.put("success", false); response.put("error", "No autenticado"); return response;
            }
            Usuario campesino = repo.findFirstByEmail(auth.getName()).orElseThrow();
            Orden orden = ordenRepo.findById(ordenId).orElseThrow();
            
            // Verificar que la orden pertenece a un producto de este campesino
            boolean esDelCampesino = orden.getDetalles().stream()
                .anyMatch(d -> d.getProducto() != null && d.getProducto().getUsuario() != null
                    && d.getProducto().getUsuario().getId().equals(campesino.getId()));
            if (!esDelCampesino) {
                response.put("success", false); response.put("error", "No autorizado"); return response;
            }

            // Crear ruta individual al instante (sin zona, sin filtros, sin condiciones)
            Ruta ruta = new Ruta();
            ruta.setCodigoRuta("RUTA-" + java.time.LocalDateTime.now().getYear() + "-"
                    + String.format("%03d", rutaRepo.count() + 1));
            ruta.setZonaOrigen("Colombia");
            ruta.setZonaDestino("Colombia");
            ruta.setEstado("LISTA_PARA_SALIR");
            ruta.setFechaCreacion(java.time.LocalDateTime.now());
            ruta.setFechaLimite(java.time.LocalDateTime.now().plusHours(24));
            ruta.setPesoTotalKg(1.0);
            ruta.setPedidosCount(1);
            ruta.setPagoTotalEstimado(orden.getTotal() != null ? orden.getTotal() : 0.0);
            ruta = rutaRepo.save(ruta);

            orden.setRuta(ruta);
            orden.setEstado(OrdenEstadoService.AGRUPADO_EN_RUTA);

            String pin = String.valueOf(100000 + (int)(Math.random() * 899999));
            orden.setCodigoRecogida(pin);
            orden.setIntentosRecogida(0);
            orden.setFechaGeneracionRecogida(java.time.LocalDateTime.now());

            String pinEntrega = String.valueOf(100000 + (int)(Math.random() * 899999));
            orden.setCodigoEntrega(pinEntrega);
            orden.setIntentosEntrega(0);
            orden.setFechaGeneracionEntrega(java.time.LocalDateTime.now());

            ordenRepo.save(orden);

            campesino.setTotalEntregas(campesino.getTotalEntregas() != null ? campesino.getTotalEntregas() + 1 : 1);
            if (campesino.getTotalEntregas() >= 30) campesino.setAutoAceptarDisponible(true);
            repo.save(campesino);
            notificationService.notificarClienteEnCamino(orden);

            response.put("success", true);
            response.put("message", "Ruta " + ruta.getCodigoRuta() + " creada");
            response.put("nuevoEstado", OrdenEstadoService.AGRUPADO_EN_RUTA);
            response.put("codigoRuta", ruta.getCodigoRuta());
            response.put("pinRecogida", pin);
            response.put("pinEntrega", pinEntrega);
        } catch (Exception e) {
            response.put("success", false); response.put("error", e.getMessage());
        }
        return response;
    }

    // API MOVIL: Campesino rechaza una orden
    @PostMapping("/api/pedidos/orden/{ordenId}/rechazar")
    @ResponseBody
    public Map<String, Object> rechazarOrdenCampesino(@PathVariable Long ordenId, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (auth == null || !auth.isAuthenticated()) {
                response.put("success", false); response.put("error", "No autenticado"); return response;
            }
            Usuario campesino = repo.findFirstByEmail(auth.getName()).orElseThrow();
            Orden orden = ordenRepo.findById(ordenId).orElseThrow();
            
            boolean esDelCampesino = orden.getDetalles().stream()
                .anyMatch(d -> d.getProducto() != null && d.getProducto().getUsuario() != null
                    && d.getProducto().getUsuario().getId().equals(campesino.getId()));
            if (!esDelCampesino) {
                response.put("success", false); response.put("error", "No autorizado"); return response;
            }

            orden.setEstado(OrdenEstadoService.CANCELADO);
            ordenRepo.save(orden);

            // Incrementar rechazos y bajar calificacion
            campesino.setTotalRechazos(campesino.getTotalRechazos() != null ? campesino.getTotalRechazos() + 1 : 1);
            repo.save(campesino);

            // Notificar al cliente
            notificationService.notificarClientePedidoRechazado(orden);

            response.put("success", true);
            response.put("message", "Pedido rechazado. Se notificara al cliente.");
            response.put("nuevoEstado", OrdenEstadoService.CANCELADO);
        } catch (Exception e) {
            response.put("success", false); response.put("error", e.getMessage());
        }
        return response;
    }

    // API MOVIL: Activar/desactivar auto-aceptar pedidos
    @PostMapping("/api/campesino/auto-aceptar")
    @ResponseBody
    public Map<String, Object> toggleAutoAceptar(Authentication auth, @RequestBody Map<String, Boolean> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            Usuario campesino = repo.findFirstByEmail(auth.getName()).orElseThrow();
            if (!"CAMPESINO".equals(campesino.getRol())) {
                response.put("success", false); response.put("error", "Solo campesinos"); return response;
            }
            // Solo disponible si tiene 30+ entregas
            if (campesino.getTotalEntregas() != null && campesino.getTotalEntregas() >= 30) {
                boolean activar = body.getOrDefault("autoAceptar", false);
                campesino.setAutoAceptar(activar);
                repo.save(campesino);
                response.put("success", true);
                response.put("autoAceptar", activar);
                response.put("message", activar ? "Auto-aceptar activado" : "Auto-aceptar desactivado");
            } else {
                response.put("success", false);
                response.put("message", "Necesitas 30 entregas para desbloquear esta funcion. Llevas: " +
                    (campesino.getTotalEntregas() != null ? campesino.getTotalEntregas() : 0));
                response.put("totalEntregas", campesino.getTotalEntregas());
            }
        } catch (Exception e) {
            response.put("success", false); response.put("error", e.getMessage());
        }
        return response;
    }

    // API MOVIL: Obtener perfil del campesino autenticado
    @GetMapping("/api/campesino/mi-perfil")
    @ResponseBody
    public Map<String, Object> miPerfilApi(Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (auth == null || !auth.isAuthenticated()) {
                response.put("success", false); response.put("error", "No autenticado"); return response;
            }
            Usuario c = repo.findFirstByEmail(auth.getName()).orElseThrow();
            response.put("success", true);
            response.put("id", c.getId());
            response.put("nombreCompleto", nvl(c.getNombreCompleto()));
            response.put("userName", c.getUserName());
            response.put("email", c.getEmail());
            response.put("telefono", nvl(c.getTelefono()));
            response.put("numeroIdentidad", nvl(c.getNumeroIdentidad()));
            response.put("nombreFinca", nvl(c.getNombreFinca()));
            response.put("descripcionFinca", nvl(c.getDescripcionFinca()));
            response.put("municipioOrigen", nvl(c.getMunicipioOrigen()));
            response.put("fotoPerfil", nvl(c.getFotoPerfil()));
            response.put("fotoFincaUrl", nvl(c.getFotoFincaUrl()));
            response.put("estadoVerificacion", nvl(c.getEstadoVerificacion()));
            response.put("latitud", c.getLatitud());
            response.put("longitud", c.getLongitud());
            response.put("fechaNacimiento", c.getFechaNacimiento() != null ? c.getFechaNacimiento().toString() : "");
            List<Producto> productos = productoRepo.findByUsuario(c);
            response.put("totalProductos", productos.size());
        } catch (Exception e) {
            response.put("success", false); response.put("error", e.getMessage());
        }
        return response;
    }

    // API MOVIL: Actualizar perfil del campesino
    @PutMapping("/api/campesino/mi-perfil")
    @ResponseBody
    public Map<String, Object> actualizarMiPerfilApi(@RequestBody Map<String, Object> body, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (auth == null || !auth.isAuthenticated()) {
                response.put("success", false); response.put("error", "No autenticado"); return response;
            }
            Usuario c = repo.findFirstByEmail(auth.getName()).orElseThrow();
            if (body.containsKey("nombreCompleto")) c.setNombreCompleto((String) body.get("nombreCompleto"));
            if (body.containsKey("telefono")) c.setTelefono((String) body.get("telefono"));
            if (body.containsKey("numeroIdentidad")) c.setNumeroIdentidad((String) body.get("numeroIdentidad"));
            if (body.containsKey("nombreFinca")) c.setNombreFinca((String) body.get("nombreFinca"));
            if (body.containsKey("descripcionFinca")) c.setDescripcionFinca((String) body.get("descripcionFinca"));
            if (body.containsKey("municipioOrigen")) c.setMunicipioOrigen((String) body.get("municipioOrigen"));
            if (body.containsKey("latitud")) {
                Object v = body.get("latitud");
                if (v instanceof Number) c.setLatitud(((Number) v).doubleValue());
            }
            if (body.containsKey("longitud")) {
                Object v = body.get("longitud");
                if (v instanceof Number) c.setLongitud(((Number) v).doubleValue());
            }
            if (body.containsKey("fotoPerfil")) c.setFotoPerfil((String) body.get("fotoPerfil"));
            if (body.containsKey("fotoFincaUrl")) c.setFotoFincaUrl((String) body.get("fotoFincaUrl"));
            if (body.containsKey("fechaNacimiento")) {
                try { c.setFechaNacimiento(java.time.LocalDate.parse((String) body.get("fechaNacimiento"))); } catch (Exception ignored) {}
            }
            repo.save(c);
            response.put("success", true);
            response.put("message", "Perfil actualizado");
            response.put("latitud", c.getLatitud());
            response.put("longitud", c.getLongitud());
        } catch (Exception e) {
            response.put("success", false); response.put("error", e.getMessage());
        }
        return response;
    }

    // API MOVIL: AgroWallet - Finanzas del campesino
    @GetMapping("/api/finanzas/informe")
    @ResponseBody
    public Map<String, Object> finanzasApi(Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (auth == null || !auth.isAuthenticated()) {
                response.put("success", false); response.put("error", "No autenticado"); return response;
            }
            String email = auth.getName();
            Usuario campesino = repo.findFirstByEmail(email).orElseThrow();
            List<DetalleOrden> ventas = detalleRepo.findVentasByCampesino(campesino, campesino.getId());

            double ingresosBrutos = 0, pagoPendiente = 0;
            int totalTransacciones = 0, transaccionesCompletadas = 0;
            String[] MESES = {"Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"};
            Map<Integer, Double> ingresosPorMes = new LinkedHashMap<>();
            Map<Long, List<DetalleOrden>> ventasPorOrden = new LinkedHashMap<>();

            for (DetalleOrden d : ventas) {
                if (d.getOrden() != null) {
                    ventasPorOrden.computeIfAbsent(d.getOrden().getId(), k -> new ArrayList<>()).add(d);
                }
            }

            List<Map<String, Object>> historial = new ArrayList<>();
            for (Map.Entry<Long, List<DetalleOrden>> e : ventasPorOrden.entrySet()) {
                List<DetalleOrden> detalles = e.getValue();
                Orden orden = detalles.get(0).getOrden();
                if (orden == null) continue;

                double montoOrden = 0;
                for (DetalleOrden d : detalles) montoOrden += d.getTotal() != null ? d.getTotal() : 0.0;

                String estado = orden.getEstado() != null ? orden.getEstado().toUpperCase() : "NUEVO";
                String fecha = orden.getFechaCreacion() != null ? orden.getFechaCreacion().toLocalDate().toString() : "—";
                totalTransacciones++;

                if (orden.getFechaCreacion() != null) {
                    ingresosPorMes.merge(orden.getFechaCreacion().getMonthValue(), montoOrden, Double::sum);
                }

                if ("ENTREGADO".equals(estado) || "COMPLETADO".equals(estado)) {
                    ingresosBrutos += montoOrden;
                    transaccionesCompletadas++;
                    historial.add(Map.of("fecha", fecha, "tipo", "INGRESO", "descripcion", "Pago recibido — Pedido #" + orden.getId(), "monto", montoOrden, "signo", "+", "estado", "COMPLETADO"));
                    historial.add(Map.of("fecha", fecha, "tipo", "COMISION", "descripcion", "Comision AgroConecta (5%)", "monto", montoOrden * 0.05, "signo", "-", "estado", "APLICADO"));
                } else if ("CANCELADO".equals(estado)) {
                    historial.add(Map.of("fecha", fecha, "tipo", "CANCELADO", "descripcion", "Pedido #" + orden.getId() + " cancelado", "monto", montoOrden, "signo", "x", "estado", "CANCELADO"));
                } else {
                    pagoPendiente += montoOrden;
                    historial.add(Map.of("fecha", fecha, "tipo", "PENDIENTE", "descripcion", "Pago retenido — Pedido #" + orden.getId() + " en proceso", "monto", montoOrden, "signo", "~", "estado", "EN ESPERA"));
                }
            }

            historial.sort((a, b) -> ((String) b.get("fecha")).compareTo((String) a.get("fecha")));
            if (historial.size() > 60) historial = historial.subList(0, 60);

            double comisionTotal = ingresosBrutos * 0.05;
            double ingresosNetos = ingresosBrutos - comisionTotal;

            List<Map<String, Object>> datosMensuales = new ArrayList<>();
            for (Map.Entry<Integer, Double> me : ingresosPorMes.entrySet()) {
                datosMensuales.add(Map.of("mes", MESES[me.getKey() - 1], "total", me.getValue()));
            }

            response.put("success", true);
            response.put("ingresosBrutos", ingresosBrutos);
            response.put("ingresosNetos", ingresosNetos);
            response.put("comisionTotal", comisionTotal);
            response.put("pagoPendiente", pagoPendiente);
            response.put("totalTransacciones", totalTransacciones);
            response.put("transaccionesCompletadas", transaccionesCompletadas);
            response.put("historial", historial);
            response.put("datosMensuales", datosMensuales);
        } catch (Exception ex) {
            response.put("success", false); response.put("error", ex.getMessage());
        }
        return response;
    }

    // API MOVIL: Perfil del cliente autenticado
    @GetMapping("/api/cliente/mi-perfil")
    @ResponseBody
    public Map<String, Object> miPerfilClienteApi(Authentication auth) {
        Map<String, Object> resp = new HashMap<>();
        try {
            if (auth == null || !auth.isAuthenticated()) { resp.put("success", false); resp.put("error", "No autenticado"); return resp; }
            Usuario u = repo.findFirstByEmail(auth.getName()).orElseThrow();
            resp.put("success", true); resp.put("id", u.getId());
            resp.put("nombreCompleto", u.getNombreCompleto()); resp.put("email", u.getEmail());
            resp.put("userName", u.getUserName()); resp.put("rol", u.getRol());
            resp.put("telefono", u.getTelefono()); resp.put("numeroIdentidad", u.getNumeroIdentidad());
            resp.put("fechaNacimiento", u.getFechaNacimiento() != null ? u.getFechaNacimiento().toString() : null);
            resp.put("genero", u.getGenero()); resp.put("fotoPerfil", u.getFotoPerfil());
            resp.put("creditos", u.getCreditos() != null ? u.getCreditos() : 0.0);
            resp.put("estadoVerificacion", u.getEstadoVerificacion());
        } catch (Exception e) { resp.put("success", false); resp.put("error", e.getMessage()); }
        return resp;
    }

    // API MOVIL: Actualizar perfil del cliente (solo datos basicos)
    @PutMapping("/api/cliente/mi-perfil")
    @ResponseBody
    public Map<String, Object> actualizarClienteApi(@RequestBody Map<String, Object> body, Authentication auth) {
        Map<String, Object> resp = new HashMap<>();
        try {
            if (auth == null || !auth.isAuthenticated()) { resp.put("success", false); resp.put("error", "No autenticado"); return resp; }
            Usuario u = repo.findFirstByEmail(auth.getName()).orElseThrow();
            if (body.containsKey("nombreCompleto")) u.setNombreCompleto((String) body.get("nombreCompleto"));
            if (body.containsKey("telefono")) u.setTelefono((String) body.get("telefono"));
            if (body.containsKey("numeroIdentidad")) u.setNumeroIdentidad((String) body.get("numeroIdentidad"));
            if (body.containsKey("fechaNacimiento") && body.get("fechaNacimiento") != null) u.setFechaNacimiento(java.time.LocalDate.parse((String) body.get("fechaNacimiento")));
            if (body.containsKey("genero")) u.setGenero((String) body.get("genero"));
            if (body.containsKey("fotoPerfil")) u.setFotoPerfil((String) body.get("fotoPerfil"));
            if (body.containsKey("nombreFinca")) u.setNombreFinca((String) body.get("nombreFinca"));
            if (body.containsKey("descripcionFinca")) u.setDescripcionFinca((String) body.get("descripcionFinca"));
            if (body.containsKey("municipioOrigen")) u.setMunicipioOrigen((String) body.get("municipioOrigen"));
            repo.save(u);
            resp.put("success", true); resp.put("message", "Perfil actualizado");
        } catch (Exception e) { resp.put("success", false); resp.put("error", e.getMessage()); }
        return resp;
    }

    // API MOVIL: Estado de verificacion KYC del campesino
    @GetMapping("/api/campesino/verificacion/estado")
    @ResponseBody
    public Map<String, Object> estadoVerificacionApi(Authentication auth) {
        Map<String, Object> r = new HashMap<>();
        if (auth == null || !auth.isAuthenticated()) { r.put("success", false); return r; }
        Usuario u = repo.findFirstByEmail(auth.getName()).orElseThrow();
        r.put("success", true); r.put("estado", u.getEstadoVerificacion());
        r.put("numeroIdentidad", u.getNumeroIdentidad()); r.put("nombreFinca", u.getNombreFinca());
        r.put("fotoCedulaUrl", u.getFotoCedulaUrl()); r.put("fotoFincaUrl", u.getFotoFincaUrl());
        r.put("descripcionFinca", u.getDescripcionFinca()); r.put("municipioOrigen", u.getMunicipioOrigen());
        r.put("latitud", u.getLatitud()); r.put("longitud", u.getLongitud());
        return r;
    }

    // API MOVIL: Enviar verificacion KYC para revision
    @PostMapping("/api/campesino/verificacion/enviar")
    @ResponseBody
    public Map<String, Object> enviarVerificacionApi(@RequestBody Map<String, Object> body, Authentication auth) {
        Map<String, Object> r = new HashMap<>();
        if (auth == null || !auth.isAuthenticated()) { r.put("success", false); return r; }
        Usuario u = repo.findFirstByEmail(auth.getName()).orElseThrow();
        if (body.containsKey("numeroIdentidad")) u.setNumeroIdentidad((String) body.get("numeroIdentidad"));
        if (body.containsKey("nombreFinca")) u.setNombreFinca((String) body.get("nombreFinca"));
        if (body.containsKey("descripcionFinca")) u.setDescripcionFinca((String) body.get("descripcionFinca"));
        if (body.containsKey("municipioOrigen") && body.get("municipioOrigen") != null) u.setMunicipioOrigen((String) body.get("municipioOrigen"));
        if (body.containsKey("latitud") && body.get("latitud") != null) u.setLatitud(((Number) body.get("latitud")).doubleValue());
        if (body.containsKey("longitud") && body.get("longitud") != null) u.setLongitud(((Number) body.get("longitud")).doubleValue());
        if (body.containsKey("fotoCedulaUrl")) u.setFotoCedulaUrl((String) body.get("fotoCedulaUrl"));
        if (body.containsKey("fotoFincaUrl")) u.setFotoFincaUrl((String) body.get("fotoFincaUrl"));
        u.setEstadoVerificacion("EN_REVISION");
        repo.save(u);
        r.put("success", true); r.put("estado", "EN_REVISION");
        return r;
    }

    private double[] obtenerCoordenadasPorCiudad(String nombre) {
        if (nombre == null) return new double[]{4.7110, -74.0721};
        String c = nombre.toLowerCase().trim();
        if (c.contains("(")) c = c.substring(0, c.indexOf("(")).trim();
        // Principales ciudades colombianas
        if (c.contains("bogota") || c.contains("bogotá")) return new double[]{4.7110, -74.0721};
        if (c.contains("medellin") || c.contains("medellín")) return new double[]{6.2476, -75.5658};
        if (c.contains("cali")) return new double[]{3.4516, -76.5320};
        if (c.contains("barranquilla")) return new double[]{10.9685, -74.7813};
        if (c.contains("cartagena")) return new double[]{10.3910, -75.5144};
        if (c.contains("bucaramanga")) return new double[]{7.1193, -73.1227};
        if (c.contains("cucuta") || c.contains("cúcuta")) return new double[]{7.8939, -72.5078};
        if (c.contains("pereira")) return new double[]{4.8087, -75.6906};
        if (c.contains("ibague") || c.contains("ibagué")) return new double[]{4.4389, -75.2322};
        if (c.contains("manizales")) return new double[]{5.0703, -75.5138};
        if (c.contains("armenia")) return new double[]{4.5339, -75.6811};
        if (c.contains("barbosa")) return new double[]{5.9317, -73.6147};
        if (c.contains("velez") || c.contains("vélez")) return new double[]{6.0133, -73.6756};
        if (c.contains("socorro")) return new double[]{6.4690, -73.2606};
        if (c.contains("san gil")) return new double[]{6.5552, -73.1336};
        if (c.contains("duitama")) return new double[]{5.8265, -73.0337};
        if (c.contains("sogamoso")) return new double[]{5.7143, -72.9339};
        return new double[]{4.7110, -74.0721}; // default Bogota
    }

    // API: Info del repartidor para el modal del campesino
    @GetMapping("/api/campesino/repartidor-info/{id}")
    @ResponseBody
    public Map<String, Object> repartidorInfo(@PathVariable Long id, Authentication auth) {
        Map<String, Object> resp = new HashMap<>();
        try {
            DetalleOrden detalle = detalleRepo.findById(id).orElse(null);
            if (detalle == null || detalle.getOrden() == null || detalle.getOrden().getRuta() == null) {
                resp.put("success", false); return resp;
            }
            Orden orden = detalle.getOrden();
            Ruta ruta = orden.getRuta();
            Usuario rep = ruta.getRepartidor();
            if (rep == null) { resp.put("success", false); return resp; }

            resp.put("success", true);
            resp.put("codigoRecogida", orden.getCodigoRecogida());
            resp.put("codigoRuta", ruta.getCodigoRuta());
            resp.put("repNombre", rep.getNombreCompleto());
            resp.put("repTelefono", rep.getTelefono());
            resp.put("repVehiculo", rep.getTipoVehiculo());
            resp.put("repPlaca", rep.getPlacaVehiculo());
            resp.put("repRating", rep.getCalificacionPromedio() != null ? rep.getCalificacionPromedio() : 0.0);
            resp.put("repLat", rep.getLatitud());
            resp.put("repLng", rep.getLongitud());
            resp.put("fincaLat", orden.getLatitudOrigen());
            resp.put("fincaLng", orden.getLongitudOrigen());
        } catch (Exception e) {
            resp.put("success", false); resp.put("error", e.getMessage());
        }
        return resp;
    }

    // API: Notificaciones del cliente
    @Transactional(readOnly = true)
    @GetMapping("/api/notificaciones/mis")
    @ResponseBody
    public Map<String, Object> misNotificaciones(Authentication auth) {
        Map<String, Object> resp = new HashMap<>();
        try {
            if (auth == null || !auth.isAuthenticated()) {
                resp.put("success", false); resp.put("error", "No autenticado"); return resp;
            }
            Usuario user = repo.findFirstByEmail(auth.getName()).orElseThrow();
            List<com.proyecto.AccesoUsuarios.model.Notificacion> notifs =
                notificacionRepo.findByUsuarioIdOrderByFechaCreacionDesc(user.getId());
            long noLeidas = notificacionRepo.countByUsuarioIdAndLeidaFalse(user.getId());
            System.out.println("📋 [NOTIF] Usuario " + user.getId() + " tiene " + noLeidas + " no leidas de " + notifs.size() + " total");
            resp.put("success", true);
            resp.put("notificaciones", notifs);
            resp.put("noLeidas", noLeidas);
        } catch (Exception e) {
            resp.put("success", false); resp.put("error", e.getMessage());
        }
        return resp;
    }

    @Transactional
    @PostMapping("/api/notificaciones/marcar-leidas")
    @ResponseBody
    public Map<String, Object> marcarLeidas(Authentication auth) {
        Map<String, Object> resp = new HashMap<>();
        try {
            if (auth == null || !auth.isAuthenticated()) {
                resp.put("success", false); resp.put("error", "No autenticado"); return resp;
            }
            Usuario user = repo.findFirstByEmail(auth.getName()).orElseThrow();
            List<com.proyecto.AccesoUsuarios.model.Notificacion> noLeidas =
                notificacionRepo.findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(user.getId());
            for (com.proyecto.AccesoUsuarios.model.Notificacion n : noLeidas) {
                n.setLeida(true);
            }
            notificacionRepo.saveAll(noLeidas);
            notificacionRepo.flush();
            System.out.println("✅ [NOTIF] Marcadas " + noLeidas.size() + " como leidas para usuario " + user.getId());
            resp.put("success", true);
        } catch (Exception e) {
            resp.put("success", false); resp.put("error", e.getMessage());
        }
        return resp;
    }

    private String nvl(String val) { return val != null ? val : ""; }
}