package com.proyecto.AccesoUsuarios.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "producto")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Double precio;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "imagen_url")
    private String imagenUrl; // URL de la imagen del producto
    
    // Campos traídos de tu proyecto Laravel
    private String categoria; // Ej: Verduras, Frutas
    
    private Integer stock; // Cantidad disponible
    
    private String unidad; // Ej: "Kg", "Libra", "Bulto"

    // Relación: Un producto pertenece a un Campesino (Usuario)
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}