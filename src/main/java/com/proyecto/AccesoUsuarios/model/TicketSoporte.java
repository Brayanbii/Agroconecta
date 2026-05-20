package com.proyecto.AccesoUsuarios.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ticket_soporte")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketSoporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    private Usuario usuario; // Cliente o campesino

    @Column(nullable = false)
    private String asunto;

    @Column(nullable = false)
    private String estado; // "ABIERTO", "EN_PROGRESO", "CERRADO"

    @Column(nullable = false)
    private String tipo = "PETICION"; // PETICION, QUEJA, RECLAMO, SUGERENCIA, TECNICO

    @Column(nullable = false)
    private String prioridad = "BAJA"; // BAJA, MEDIA, ALTA, CRITICA

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaEnvio ASC")
    @ToString.Exclude
    private List<MensajeSoporte> mensajes;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = fechaCreacion;
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
