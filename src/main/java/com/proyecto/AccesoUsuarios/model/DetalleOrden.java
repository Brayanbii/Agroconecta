package com.proyecto.AccesoUsuarios.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "detalle_orden")
@Getter
@Setter
@ToString(exclude = {"orden", "producto"})
@EqualsAndHashCode(exclude = {"orden", "producto"})
@AllArgsConstructor
@NoArgsConstructor
public class DetalleOrden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre; // Guardamos el nombre por si el producto cambia luego
    private Double precio;
    private Integer cantidad;
    private Double total;

    @ManyToOne
    @JoinColumn(name = "orden_id")
    private Orden orden;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;
}