package com.proyecto.AccesoUsuarios.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "resena")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer estrellas; // 1 a 5

    @Column(length = 1000)
    private String comentario;

    @Column(nullable = false)
    private LocalDate fecha;

    // Relación: Una reseña pertenece a un producto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    @JsonIgnore // Para evitar recursión infinita en JSON
    private Producto producto;

    // Relación: Una reseña es escrita por un usuario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnore
    private Usuario usuario;
    
    // Campo transitorio para poder enviar el nombre del usuario autor por JSON fácilmente
    @Transient
    private String nombreAutor;

    public String getNombreAutor() {
        if (usuario != null) {
            return usuario.getNombreCompleto() != null ? usuario.getNombreCompleto() : usuario.getUserName();
        }
        return "Usuario Anónimo";
    }
}
