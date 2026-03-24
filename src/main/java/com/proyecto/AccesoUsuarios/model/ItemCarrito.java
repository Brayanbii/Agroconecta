package com.proyecto.AccesoUsuarios.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemCarrito {
    private Producto producto;
    private Integer cantidad;

    public Double getTotal() {
        return producto.getPrecio() * cantidad;
    }
}