package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.model.Producto;
import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.ProductoRepository;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/favoritos")
public class FavoritoAPIController {

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private ProductoRepository productoRepo;

    @PostMapping("/toggle/{id}")
    public ResponseEntity<Map<String, Object>> toggleFavorito(@PathVariable Long id, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        
        if (auth == null || !auth.isAuthenticated()) {
            response.put("success", false);
            response.put("message", "Debes iniciar sesión para guardar favoritos.");
            return ResponseEntity.status(401).body(response);
        }

        Usuario usuario = usuarioRepo.findByEmail(auth.getName()).orElse(null);
        if (usuario == null) {
            response.put("success", false);
            response.put("message", "Usuario no encontrado.");
            return ResponseEntity.status(404).body(response);
        }

        Optional<Producto> productoOpt = productoRepo.findById(id);
        if (!productoOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "Producto no encontrado.");
            return ResponseEntity.status(404).body(response);
        }

        Producto producto = productoOpt.get();
        List<Producto> favoritos = usuario.getProductosFavoritos();
        
        boolean isFavorito = false;
        if (favoritos != null && favoritos.contains(producto)) {
            favoritos.remove(producto);
        } else {
            if (favoritos == null) {
                favoritos = new java.util.ArrayList<>();
                usuario.setProductosFavoritos(favoritos);
            }
            favoritos.add(producto);
            isFavorito = true;
        }

        usuarioRepo.save(usuario);

        response.put("success", true);
        response.put("isFavorito", isFavorito);
        response.put("message", isFavorito ? "Producto añadido a favoritos." : "Producto eliminado de favoritos.");
        return ResponseEntity.ok(response);
    }
}
