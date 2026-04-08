package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.model.Producto;
import com.proyecto.AccesoUsuarios.model.Resena;
import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.ProductoRepository;
import com.proyecto.AccesoUsuarios.repository.ResenaRepository;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
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

    // Obtener las reseñas de un producto
    @GetMapping("/producto/{id}")
    public ResponseEntity<List<Resena>> obtenerResenasPorProducto(@PathVariable Long id) {
        List<Resena> resenas = resenaRepository.findByProductoIdOrderByFechaDesc(id);
        
        // Cargar el nombre del autor explícitamente en el @Transient nombreAutor antes de enviarlo
        resenas.forEach(r -> r.setNombreAutor(r.getNombreAutor()));
        
        return ResponseEntity.ok(resenas);
    }

    // Guardar una nueva reseña
    @PostMapping
    public ResponseEntity<?> guardarResena(@RequestBody Map<String, Object> payload) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return ResponseEntity.status(401).body(Map.of("error", "Debes iniciar sesión para comentar"));
        }

        try {
            Long productoId = Long.valueOf(payload.get("productoId").toString());
            Integer estrellas = Integer.valueOf(payload.get("estrellas").toString());
            String comentario = payload.get("comentario").toString();

            if (estrellas < 1 || estrellas > 5) {
                return ResponseEntity.badRequest().body(Map.of("error", "Estrellas entre 1 y 5"));
            }

            Optional<Producto> optProducto = productoRepository.findById(productoId);
            // El authentication.getName() en esta app devuelve el EMAIL, no el userName
            Optional<Usuario> optUsuario = usuarioRepository.findByEmail(auth.getName());

            if (optProducto.isPresent() && optUsuario.isPresent()) {
                Resena resena = new Resena();
                resena.setProducto(optProducto.get());
                resena.setUsuario(optUsuario.get());
                resena.setEstrellas(estrellas);
                resena.setComentario(comentario);
                resena.setFecha(LocalDate.now());

                resenaRepository.save(resena);

                return ResponseEntity.ok(Map.of("mensaje", "Reseña guardada exitosamente"));
            }

            return ResponseEntity.badRequest().body(Map.of("error", "Producto o Usuario no válidos"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error procesando la solicitud"));
        }
    }
}
