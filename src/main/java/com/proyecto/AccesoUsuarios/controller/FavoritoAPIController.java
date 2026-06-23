package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.model.FavoritoProducto;
import com.proyecto.AccesoUsuarios.model.Producto;
import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.FavoritoProductoRepository;
import com.proyecto.AccesoUsuarios.repository.ProductoRepository;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import com.proyecto.AccesoUsuarios.service.AuthUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/favoritos")
public class FavoritoAPIController {

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private ProductoRepository productoRepo;

    @Autowired
    private FavoritoProductoRepository favoritoRepo;

    @Autowired
    private AuthUsuarioService authUsuarioService;

    @GetMapping
    public ResponseEntity<?> getMisFavoritos(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        }
        Usuario usuario = authUsuarioService.getAuthenticatedUser(auth);
        if (usuario == null) return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));

        List<FavoritoProducto> favs = favoritoRepo.findByClienteOrderByFechaCreacionDesc(usuario);
        List<Producto> productos = favs.stream()
                .map(FavoritoProducto::getProducto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(productos != null ? productos : List.of());
    }

    @PostMapping("/toggle/{id}")
    public ResponseEntity<Map<String, Object>> toggleFavorito(@PathVariable Long id, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        if (auth == null || !auth.isAuthenticated()) {
            response.put("success", false);
            response.put("message", "Debes iniciar sesión para guardar favoritos.");
            return ResponseEntity.status(401).body(response);
        }
        Usuario usuario = authUsuarioService.getAuthenticatedUser(auth);
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

        Optional<FavoritoProducto> existente = favoritoRepo.findByClienteAndProducto(usuario, producto);
        boolean isFavorito;
        if (existente.isPresent()) {
            favoritoRepo.delete(existente.get());
            isFavorito = false;
        } else {
            FavoritoProducto fav = new FavoritoProducto();
            fav.setCliente(usuario);
            fav.setProducto(producto);
            fav.setFechaCreacion(java.time.LocalDateTime.now());
            favoritoRepo.save(fav);
            isFavorito = true;
        }

        response.put("success", true);
        response.put("isFavorito", isFavorito);
        response.put("message", isFavorito ? "Producto añadido a favoritos." : "Producto eliminado de favoritos.");
        return ResponseEntity.ok(response);
    }
}
