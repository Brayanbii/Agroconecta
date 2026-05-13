package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.model.Producto;
import com.proyecto.AccesoUsuarios.repository.ProductoRepository;
import com.proyecto.AccesoUsuarios.service.CarritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/carrito")
public class CarritoAPIController {

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private ProductoRepository productoRepository;

    @PostMapping("/agregar")
    public ResponseEntity<?> agregarAlCarrito(@RequestBody Map<String, Object> payload) {
        try {
            if (!payload.containsKey("id")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Falta ID de producto"));
            }
            Long id = Long.valueOf(payload.get("id").toString());
            int cantidad = payload.containsKey("cantidad") ? Integer.parseInt(payload.get("cantidad").toString()) : 1;
            
            // Verificamos antes si existe
            Optional<Producto> optProducto = productoRepository.findById(id);
            if (!optProducto.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("error", "Producto no encontrado"));
            }
            
            Producto producto = optProducto.get();
            int stockDisponible = (producto.getStock() != null) ? producto.getStock() : 0;
            
            boolean agregadoConExito = carritoService.agregarProducto(id, cantidad);
            
            if (!agregadoConExito) {
                // Si retorna false, significa que topó el límite
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "stock_insuficiente", 
                    "stock", stockDisponible,
                    "mensaje", "No hay sufiente stock de " + producto.getNombre()
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "status", "ok", 
                "mensaje", "Producto añadido",
                "cantidadItems", carritoService.contarItems()
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error procesando la solicitud"));
        }
    }
}
