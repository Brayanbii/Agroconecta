package com.proyecto.AccesoUsuarios.service;

import com.proyecto.AccesoUsuarios.model.ItemCarrito;
import com.proyecto.AccesoUsuarios.model.Producto;
import com.proyecto.AccesoUsuarios.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@SessionScope // ¡Clave! Esto hace que el carrito viva mientras dura la sesión del usuario
public class CarritoService {

    private List<ItemCarrito> items = new ArrayList<>();

    @Autowired
    private ProductoRepository productoRepo;

    // Añadir producto al carrito (con validación de stock)
    public void agregarProducto(Long idProducto, Integer cantidad) {
        Producto producto = productoRepo.findById(idProducto).orElse(null);
        if (producto == null) return;

        // Stock disponible (si es null, tratamos como 0)
        int stockDisponible = (producto.getStock() != null) ? producto.getStock() : 0;

        // Verificar si ya está en el carrito
        Optional<ItemCarrito> itemExistente = items.stream()
                .filter(i -> i.getProducto().getId().equals(idProducto))
                .findFirst();

        if (itemExistente.isPresent()) {
            // Si ya existe, sumamos la cantidad pero sin exceder el stock
            ItemCarrito item = itemExistente.get();
            int nuevaCantidad = item.getCantidad() + cantidad;
            item.setCantidad(Math.min(nuevaCantidad, stockDisponible));
        } else {
            // Si no existe, lo agregamos limitando al stock
            int cantidadReal = Math.min(cantidad, stockDisponible);
            if (cantidadReal > 0) {
                items.add(new ItemCarrito(producto, cantidadReal));
            }
        }
    }

    // Eliminar producto del carrito
    public void eliminarProducto(Long idProducto) {
        items.removeIf(i -> i.getProducto().getId().equals(idProducto));
    }

    // Obtener items
    public List<ItemCarrito> obtenerItems() {
        return items;
    }

    // Calcular total a pagar
    public Double obtenerTotal() {
        return items.stream()
                .mapToDouble(ItemCarrito::getTotal)
                .sum();
    }
    
    // Vaciar carrito (para cuando compre)
    public void limpiarCarrito() {
        items.clear();
    }

    // Actualizar cantidad (con validación de stock)
    public void actualizarCantidad(Long idProducto, Integer nuevaCantidad) {
        for (ItemCarrito item : items) {
            if (item.getProducto().getId().equals(idProducto)) {
                if (nuevaCantidad != null && nuevaCantidad > 0) {
                    // Recargar producto desde BD para tener stock actualizado
                    Producto producto = productoRepo.findById(idProducto).orElse(null);
                    int stockDisponible = (producto != null && producto.getStock() != null) ? producto.getStock() : 0;
                    // No permitir más que el stock disponible
                    item.setCantidad(Math.min(nuevaCantidad, stockDisponible));
                }
                break;
            }
        }
    }
    
    // Método para contar items (para el ícono del carrito)
    public Integer contarItems() {
        return items.stream().mapToInt(ItemCarrito::getCantidad).sum();
    }
}
