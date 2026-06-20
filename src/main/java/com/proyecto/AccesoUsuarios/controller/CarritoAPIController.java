package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.model.ItemCarrito;
import com.proyecto.AccesoUsuarios.model.Producto;
import com.proyecto.AccesoUsuarios.repository.ProductoRepository;
import com.proyecto.AccesoUsuarios.service.CarritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/carrito")
public class CarritoAPIController {

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private ProductoRepository productoRepository;

    // Obtener todos los items del carrito
    @GetMapping
    public ResponseEntity<?> obtenerCarrito() {
        try {
            List<ItemCarrito> items = carritoService.obtenerItems();
            List<Map<String, Object>> result = new ArrayList<>();
            for (ItemCarrito item : items) {
                if (item.getProducto() == null) continue;
                Producto p = item.getProducto();
                Map<String, Object> map = new HashMap<>();
                map.put("id", p.getId());
                map.put("nombre", p.getNombre());
                map.put("precio", p.getPrecio());
                map.put("categoria", p.getCategoria());
                map.put("unidad", p.getUnidad());
                map.put("stock", p.getStock());
                map.put("imagenUrl", p.getImagenUrl());
                map.put("cantidad", item.getCantidad());
                map.put("total", item.getTotal());
                result.add(map);
            }
            double total = carritoService.obtenerTotal();
            int cantidadTotal = carritoService.contarItems();
            return ResponseEntity.ok(Map.of(
                "items", result,
                "subtotal", total,
                "total", total,
                "cantidad", cantidadTotal
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // Actualizar cantidad de un item
    @PutMapping("/actualizar")
    public ResponseEntity<?> actualizarCantidad(@RequestBody Map<String, Object> payload) {
        try {
            Long id = Long.valueOf(payload.get("id").toString());
            int cantidad = Integer.parseInt(payload.get("cantidad").toString());
            boolean ok = carritoService.actualizarCantidad(id, cantidad);
            if (!ok) return ResponseEntity.badRequest().body(Map.of("error", "Stock insuficiente"));
            return ResponseEntity.ok(Map.of("status", "ok", "cantidadItems", carritoService.contarItems()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Eliminar un item del carrito
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<?> eliminarDelCarrito(@PathVariable Long id) {
        try {
            carritoService.eliminarProducto(id);
            return ResponseEntity.ok(Map.of("status", "ok", "cantidadItems", carritoService.contarItems()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Limpiar todo el carrito
    @PostMapping("/limpiar")
    public ResponseEntity<?> limpiarCarrito() {
        carritoService.limpiarCarrito();
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

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
