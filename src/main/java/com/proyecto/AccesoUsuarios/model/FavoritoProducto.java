package com.proyecto.AccesoUsuarios.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "favorito_producto", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"cliente_id", "producto_id"})
})
@Getter
@Setter
public class FavoritoProducto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
