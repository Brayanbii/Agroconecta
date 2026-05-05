package com.proyecto.AccesoUsuarios.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "direccion")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con el Usuario (Propietario de la dirección)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = true) // Nullable provisional
    @JsonIgnore // Evitar loops infinitos al serializar REST JSON
    @ToString.Exclude
    private Usuario usuario;

    // Alias: "Casa", "Trabajo", "Finca", u "Otro"
    @Column(length = 50)
    private String alias;

    // La calle, carrera, transv, ej: "Carrera 70 # 64 - 25"
    @Column(nullable = false, length = 255)
    private String direccionCompleta;

    // Piso, bloque, apto (Opcional)
    @Column(length = 255)
    private String detalles;

    // Coordenadas geoespaciales
    private Double latitud;
    private Double longitud;

    // Determina si es la dirección seleccionada actualmente por el usuario para envío
    @Column(nullable = false)
    private Boolean esPrincipal = false;
}
