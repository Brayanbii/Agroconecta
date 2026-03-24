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

    // Añadir producto al carrito
    public void agregarProducto(Long idProducto, Integer cantidad) {
        // Verificar si ya está en el carrito
        Optional<ItemCarrito> itemExistente = items.stream()
                .filter(i -> i.getProducto().getId().equals(idProducto))
                .findFirst();

        if (itemExistente.isPresent()) {
            // Si ya existe, sumamos la cantidad
            ItemCarrito item = itemExistente.get();
            item.setCantidad(item.getCantidad() + cantidad);
        } else {
            // Si no existe, lo buscamos en BD y lo agregamos
            Producto producto = productoRepo.findById(idProducto).orElse(null);
            if (producto != null) {
                items.add(new ItemCarrito(producto, cantidad));
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

    // Actualizar cantidad
    public void actualizarCantidad(Long idProducto, Integer nuevaCantidad) {
        for (ItemCarrito item : items) {
            if (item.getProducto().getId().equals(idProducto)) {
                if (nuevaCantidad != null && nuevaCantidad > 0) {
                    item.setCantidad(nuevaCantidad);
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
