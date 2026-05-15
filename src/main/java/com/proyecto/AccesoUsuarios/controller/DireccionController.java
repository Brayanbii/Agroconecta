package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.model.Direccion;
import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.DireccionRepository;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/direcciones")
public class DireccionController {

    @Autowired
    private DireccionRepository direccionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Obtener las direcciones del usuario actual
    @GetMapping
    public ResponseEntity<?> getMisDirecciones(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
        }
        
        Optional<Usuario> userOpt = usuarioRepository.findByEmail(auth.getName());
        if (userOpt.isPresent()) {
            List<Direccion> direcciones = direccionRepository.findByUsuario(userOpt.get());
            // Para evitar lazily loaded json issues u otro, podemos asegurar retornar la lista cruda
            return ResponseEntity.ok(direcciones);
        }
        
        return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
    }

    // Agregar una nueva dirección
    @PostMapping
    public ResponseEntity<?> agregarDireccion(@RequestBody Direccion nuevaDireccion, Authentication auth) {
        
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Debes iniciar sesión para guardar direcciones."));
        }
        
        Optional<Usuario> userOpt = usuarioRepository.findByEmail(auth.getName());
        if (userOpt.isPresent()) {
            Usuario usuario = userOpt.get();
            
            List<Direccion> existents = direccionRepository.findByUsuario(usuario);
            if (existents.isEmpty()) {
                nuevaDireccion.setEsPrincipal(true);
            } else if (Boolean.TRUE.equals(nuevaDireccion.getEsPrincipal())) {
                existents.forEach(d -> d.setEsPrincipal(false));
                direccionRepository.saveAll(existents);
            }
            
            nuevaDireccion.setUsuario(usuario);
            Direccion guardada = direccionRepository.save(nuevaDireccion);
            return ResponseEntity.ok(guardada);
        }
        
        return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
    }

    // Cambiar la dirección principal
    @PutMapping("/{id}/principal")
    public ResponseEntity<?> setPrincipal(@PathVariable Long id, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return ResponseEntity.status(401).build();

        Optional<Usuario> userOpt = usuarioRepository.findByEmail(auth.getName());
        if (!userOpt.isPresent()) return ResponseEntity.notFound().build();

        List<Direccion> direcciones = direccionRepository.findByUsuario(userOpt.get());
        
        boolean found = false;
        for (Direccion d : direcciones) {
            if (d.getId().equals(id)) {
                d.setEsPrincipal(true);
                found = true;
            } else {
                d.setEsPrincipal(false);
            }
        }
        
        if (found) {
            direccionRepository.saveAll(direcciones);
            return ResponseEntity.ok(Map.of("mensaje", "Dirección principal actualizada"));
        }
        
        return ResponseEntity.notFound().build();
    }

    // Eliminar una dirección
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarDireccion(@PathVariable Long id, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return ResponseEntity.status(401).build();

        Optional<Usuario> userOpt = usuarioRepository.findByEmail(auth.getName());
        if (!userOpt.isPresent()) return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));

        Optional<Direccion> direccionOpt = direccionRepository.findById(id);
        if (direccionOpt.isPresent()) {
            Direccion direccion = direccionOpt.get();
            if (direccion.getUsuario().getId().equals(userOpt.get().getId())) {
                direccionRepository.delete(direccion);
                return ResponseEntity.ok(Map.of("mensaje", "Dirección eliminada exitosamente"));
            } else {
                return ResponseEntity.status(403).body(Map.of("error", "No tienes permiso para eliminar esta dirección"));
            }
        }
        return ResponseEntity.notFound().build();
    }
}
