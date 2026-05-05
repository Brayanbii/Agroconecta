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
    public boolean agregarProducto(Long idProducto, Integer cantidad) {
        Producto producto = productoRepo.findById(idProducto).orElse(null);
        if (producto == null) return false;

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
            if (nuevaCantidad > stockDisponible) {
                item.setCantidad(stockDisponible);
                return false; // Límite de stock excedido
            } else {
                item.setCantidad(nuevaCantidad);
                return true; // Éxito
            }
        } else {
            // Si no existe, lo agregamos limitando al stock
            if (cantidad > stockDisponible) {
                if (stockDisponible > 0) {
                    items.add(new ItemCarrito(producto, stockDisponible));
                }
                return false; // Límite de stock excedido
            } else {
                if (cantidad > 0) {
                    items.add(new ItemCarrito(producto, cantidad));
                }
                return true; // Éxito
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
    public boolean actualizarCantidad(Long idProducto, Integer nuevaCantidad) {
        for (ItemCarrito item : items) {
            if (item.getProducto().getId().equals(idProducto)) {
                if (nuevaCantidad != null && nuevaCantidad > 0) {
                    // Recargar producto desde BD para tener stock actualizado
                    Producto producto = productoRepo.findById(idProducto).orElse(null);
                    int stockDisponible = (producto != null && producto.getStock() != null) ? producto.getStock() : 0;
                    
                    if (nuevaCantidad > stockDisponible) {
                        item.setCantidad(stockDisponible);
                        return true; // Se excedió el cupo y fue ajustado
                    } else {
                        item.setCantidad(nuevaCantidad);
                        return false; // Todo okey
                    }
                }
                break;
            }
        }
        return false;
    }
    
    // Método para contar items (para el ícono del carrito)
    public Integer contarItems() {
        return items.stream().mapToInt(ItemCarrito::getCantidad).sum();
    }
}
