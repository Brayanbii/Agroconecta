package com.proyecto.AccesoUsuarios.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orden")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numeroOrden; // Código único (ej: "ORD-123")
    private LocalDateTime fechaCreacion;
    private Double total;
    
    // Estado del pedido: Pendiente, Enviado, Entregado
    private String estado;

    // Datos de Envío (Ubicación del Cliente)
    private String direccionEnvio;
    private Double latitudEnvio;
    private Double longitudEnvio;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario; // El Cliente

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL)
    private List<DetalleOrden> detalles; // Los productos comprados
}