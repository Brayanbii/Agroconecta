package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.model.FavoritoCampesino;
import com.proyecto.AccesoUsuarios.model.FavoritoProducto;
import com.proyecto.AccesoUsuarios.model.Producto;
import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.FavoritoCampesinoRepository;
import com.proyecto.AccesoUsuarios.repository.FavoritoProductoRepository;
import com.proyecto.AccesoUsuarios.repository.ProductoRepository;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/favoritos")
public class FavoritosController {

    @Autowired
    private FavoritoCampesinoRepository favoritoCampesinoRepo;

    @Autowired
    private FavoritoProductoRepository favoritoProductoRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private ProductoRepository productoRepo;

    @PostMapping("/campesino/{id}")
    public ResponseEntity<?> toggleFavoritoCampesino(@PathVariable Long id, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        }
        
        Usuario cliente = usuarioRepo.findByEmail(auth.getName()).orElse(null);
        if (cliente == null) return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
        
        Usuario campesino = usuarioRepo.findById(id).orElse(null);
        if (campesino == null) return ResponseEntity.status(404).body(Map.of("error", "Campesino no encontrado"));
        
        Optional<FavoritoCampesino> favOpt = favoritoCampesinoRepo.findByClienteAndCampesino(cliente, campesino);
        boolean liked;
        if (favOpt.isPresent()) {
            favoritoCampesinoRepo.delete(favOpt.get());
            liked = false;
        } else {
            FavoritoCampesino fav = new FavoritoCampesino();
            fav.setCliente(cliente);
            fav.setCampesino(campesino);
            favoritoCampesinoRepo.save(fav);
            liked = true;
        }
        
        int totalLikes = favoritoCampesinoRepo.countByCampesino(campesino);
        return ResponseEntity.ok(Map.of("liked", liked, "totalLikes", totalLikes));
    }

    @PostMapping("/producto/{id}")
    public ResponseEntity<?> toggleFavoritoProducto(@PathVariable Long id, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        }
        
        Usuario cliente = usuarioRepo.findByEmail(auth.getName()).orElse(null);
        if (cliente == null) return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
        
        Producto producto = productoRepo.findById(id).orElse(null);
        if (producto == null) return ResponseEntity.status(404).body(Map.of("error", "Producto no encontrado"));
        
        Optional<FavoritoProducto> favOpt = favoritoProductoRepo.findByClienteAndProducto(cliente, producto);
        boolean liked;
        if (favOpt.isPresent()) {
            favoritoProductoRepo.delete(favOpt.get());
            liked = false;
        } else {
            FavoritoProducto fav = new FavoritoProducto();
            fav.setCliente(cliente);
            fav.setProducto(producto);
            favoritoProductoRepo.save(fav);
            liked = true;
        }
        
        int totalLikes = favoritoProductoRepo.countByProducto(producto);
        return ResponseEntity.ok(Map.of("liked", liked, "totalLikes", totalLikes));
    }
}
