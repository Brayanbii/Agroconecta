package com.proyecto.AccesoUsuarios.model;

import jakarta.persistence.*;
import lombok.ToString;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.OptionalDouble;
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

    // --- Ubicación del Cultivo / Origen ---
    private Double latitudOrigen;
    private Double longitudOrigen;
    private String municipioOrigen; // Ej: "Barbosa, Santander"

    // --- Campo volátil para distancia del cliente ---
    @Transient
    private Double distanciaKm;

    // Relación: Un producto pertenece a un Campesino (Usuario)
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // Relación: Un producto tiene muchas reseñas
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Resena> resenas;

    @Column(name = "fecha_creacion")
    private java.time.LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = java.time.LocalDateTime.now();
        }
    }

    @Transient
    public Double getPromedioCalificacion() {
        if (resenas == null || resenas.isEmpty()) {
            return 0.0;
        }
        OptionalDouble average = resenas.stream()
            .mapToInt(Resena::getEstrellas)
            .average();
        return average.isPresent() ? Math.round(average.getAsDouble() * 10.0) / 10.0 : 0.0;
    }

    @Transient
    public Integer getTotalResenas() {
        return resenas == null ? 0 : resenas.size();
    }

    @Transient
    public String getUnidadPlural() {
        if (unidad == null || unidad.trim().isEmpty()) return "";
        String u = unidad.trim();
        if (u.equalsIgnoreCase("Kg") || u.equalsIgnoreCase("Kilo") || u.equalsIgnoreCase("Lb")) return u;
        if (u.toLowerCase().endsWith("s")) return u;
        char lastChar = u.toLowerCase().charAt(u.length() - 1);
        if (lastChar == 'd' || lastChar == 'n' || lastChar == 'l' || lastChar == 'r' || lastChar == 'z') {
            return u + "es";
        }
        return u + "s";
    }

    @Transient
    public String getUnidadFormateada(int cantidad) {
        if (cantidad == 1) return unidad != null ? unidad : "";
        return getUnidadPlural();
    }
}