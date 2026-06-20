package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.model.DetalleOrden;
import com.proyecto.AccesoUsuarios.model.Orden;
import com.proyecto.AccesoUsuarios.model.Producto;
import com.proyecto.AccesoUsuarios.model.Resena;
import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.OrdenRepository;
import com.proyecto.AccesoUsuarios.repository.ProductoRepository;
import com.proyecto.AccesoUsuarios.repository.ResenaRepository;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import com.proyecto.AccesoUsuarios.service.AuthUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/resenas")
public class ResenaAPIController {

    @Autowired
    private ResenaRepository resenaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private OrdenRepository ordenRepository;

    @Autowired
    private AuthUsuarioService authUsuarioService;

    // Verificar si el usuario puede comentar (ha comprado el producto)
    @GetMapping("/puede-comentar/{productoId}")
    public ResponseEntity<?> puedeComentar(@PathVariable Long productoId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.ok(Map.of("puedeComentar", false, "yaComento", false, "mensaje", "Inicia sesion para opinar"));
        }
        Usuario usuario = authUsuarioService.getAuthenticatedUser(auth);
        if (usuario == null) {
            return ResponseEntity.ok(Map.of("puedeComentar", false, "yaComento", false, "mensaje", "Usuario no encontrado"));
        }
        
        boolean haComprado = false;
        List<Orden> ordenes = ordenRepository.findByUsuario(usuario);
        for (Orden orden : ordenes) {
            if (orden.getDetalles() != null) {
                for (DetalleOrden d : orden.getDetalles()) {
                    if (d.getProducto() != null && d.getProducto().getId().equals(productoId)) {
                        haComprado = true;
                        break;
                    }
                }
            }
            if (haComprado) break;
        }
        
        if (!haComprado) {
            return ResponseEntity.ok(Map.of("puedeComentar", false, "yaComento", false,
                "mensaje", "Compra este producto para poder calificarlo"));
        }
        
        boolean yaComento = resenaRepository.findByProductoIdAndUsuarioId(productoId, usuario.getId()).isPresent();
        return ResponseEntity.ok(Map.of("puedeComentar", true, "yaComento", yaComento,
            "mensaje", yaComento ? "Ya has calificado este producto" : "Puedes calificar este producto"));
    }

    // Obtener las reseñas de un producto
    @GetMapping("/producto/{id}")
    public ResponseEntity<List<Resena>> obtenerResenasPorProducto(@PathVariable Long id) {
        List<Resena> resenas = resenaRepository.findByProductoIdOrderByFechaDesc(id);
        
        // Cargar el nombre del autor explícitamente en el @Transient nombreAutor antes de enviarlo
        resenas.forEach(r -> r.setNombreAutor(r.getNombreAutor()));
        
        return ResponseEntity.ok(resenas);
    }

    // Guardar o actualizar una reseña (UPSERT)
    // - El comentario es OPCIONAL: se puede calificar solo con estrellas
    // - Si el usuario ya tiene reseña en ese producto, se actualiza
    @PostMapping
    public ResponseEntity<?> guardarResena(@RequestBody Map<String, Object> payload) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return ResponseEntity.status(401).body(Map.of("error", "Debes iniciar sesión para calificar"));
        }

        try {
            Long productoId = Long.valueOf(payload.get("productoId").toString());
            Integer estrellas = Integer.valueOf(payload.get("estrellas").toString());
            // Comentario es opcional: puede ser null o vacío
            String comentario = payload.get("comentario") != null ? payload.get("comentario").toString().trim() : "";

            if (estrellas < 1 || estrellas > 5) {
                return ResponseEntity.badRequest().body(Map.of("error", "Estrellas entre 1 y 5"));
            }

            Optional<Producto> optProducto = productoRepository.findById(productoId);
            Usuario usuario = authUsuarioService.getAuthenticatedUser(auth);

            if (optProducto.isPresent() && usuario != null) {
                Producto producto = optProducto.get();
                
                // Buscar si ya existe una reseña de este usuario para este producto
                Optional<Resena> existente = resenaRepository.findByProductoIdAndUsuarioId(productoId, usuario.getId());
                
                Resena resena;
                boolean esActualizacion = false;
                
                if (existente.isPresent()) {
                    // ACTUALIZAR la reseña existente
                    resena = existente.get();
                    resena.setEstrellas(estrellas);
                    resena.setComentario(comentario.isEmpty() ? null : comentario);
                    resena.setFecha(LocalDate.now());
                    esActualizacion = true;
                } else {
                    // CREAR nueva reseña
                    resena = new Resena();
                    resena.setProducto(producto);
                    resena.setUsuario(usuario);
                    resena.setEstrellas(estrellas);
                    resena.setComentario(comentario.isEmpty() ? null : comentario);
                    resena.setFecha(LocalDate.now());
                }

                resenaRepository.save(resena);

                return ResponseEntity.ok(Map.of(
                    "mensaje", esActualizacion ? "Calificación actualizada" : "Reseña guardada exitosamente",
                    "resenaId", resena.getId(),
                    "actualizada", esActualizacion
                ));
            }

            return ResponseEntity.badRequest().body(Map.of("error", "Producto o Usuario no válidos"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error procesando la solicitud: " + e.getMessage()));
        }
    }

    // Eliminar una reseña (solo el autor puede borrarla)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarResena(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return ResponseEntity.status(401).body(Map.of("error", "Debes iniciar sesión"));
        }

        try {
            Optional<Resena> optResena = resenaRepository.findById(id);
            if (optResena.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Reseña no encontrada"));
            }

            Resena resena = optResena.get();
            Usuario usuario = authUsuarioService.getAuthenticatedUser(auth);

            if (usuario == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Usuario no encontrado"));
            }

            // Solo el autor puede borrar su propia reseña
            if (!resena.getUsuario().getId().equals(usuario.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "No tienes permiso para eliminar esta reseña"));
            }

            resenaRepository.delete(resena);

            return ResponseEntity.ok(Map.of("mensaje", "Reseña eliminada exitosamente", "success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error al eliminar la reseña"));
        }
    }
}
