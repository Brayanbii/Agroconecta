package com.proyecto.AccesoUsuarios.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contacto_horeca")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ContactoHoreca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String email;
    private String telefono;
    private String empresa;
    private String tipoNegocio; // Hotel, Restaurante, Cafeteria, etc.
    private String mensaje;

    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
    }
}
