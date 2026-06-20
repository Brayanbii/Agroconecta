package com.proyecto.AccesoUsuarios.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ruta")
@Getter
@Setter
@ToString(exclude = "pedidos")
@EqualsAndHashCode(exclude = "pedidos")
@AllArgsConstructor
@NoArgsConstructor
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String codigoRuta;          // "RUTA-2026-015"

    private String zonaOrigen;          // "Boyacá"
    private String zonaDestino;          // "Bogotá - Chapinero"

    private Double pesoTotalKg;         // Peso acumulado de todos los pedidos
    private Integer pedidosCount;       // Cantidad de pedidos en la ruta
    private Double pagoTotalEstimado;   // Suma de costos de envío de todos los pedidos

    // Estado de la ruta
    @Column(columnDefinition = "varchar(30) default 'FORMANDOSE'")
    private String estado;              // FORMANDOSE, LISTA_PARA_SALIR, ASIGNADA,
                                        // EN_CAMINO, COMPLETADA, CANCELADA

    // Umbrales y control
    private Double umbralPesoKg;        // Peso minimo para salir (ej: 100 kg)
    private Boolean forzarSalida;       // true si es urgente (fecha limite o hora)

    // Capacidad del vehiculo necesario para esta ruta
    @Column(columnDefinition = "varchar(20) default 'MOTO'")
    private String tipoVehiculoRequerido; // MOTO(50kg), AUTOMOVIL(300kg), CAMION(500kg)
    private Double capacidadMaximaKg;   // Capacidad maxima segun tipo de vehiculo
    private Double pesoMinimoSalidaKg;  // Peso minimo para que valga la pena el viaje

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaLimite;  // Fecha maxima para despachar
    private LocalDateTime fechaAsignacion;
    private LocalDateTime fechaCompletada;

    // Coordenadas de la ruta (para el mapa)
    private Double latitudCentroOrigen;
    private Double longitudCentroOrigen;
    private Double latitudCentroDestino;
    private Double longitudCentroDestino;

    // Relacion con el repartidor que acepto la ruta
    @ManyToOne
    @JoinColumn(name = "repartidor_id")
    private Usuario repartidor;

    // Pedidos que pertenecen a esta ruta
    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL)
    private List<Orden> pedidos = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
        if (pedidosCount == null) pedidosCount = 0;
        if (pesoTotalKg == null) pesoTotalKg = 0.0;
        if (pagoTotalEstimado == null) pagoTotalEstimado = 0.0;
        if (forzarSalida == null) forzarSalida = false;
        if (tipoVehiculoRequerido == null) tipoVehiculoRequerido = "MOTO";
        recalcularVehiculo();
    }

    /**
     * Determina el tipo de vehiculo necesario segun el peso acumulado.
     * MOTO: hasta 50kg | AUTOMOVIL: hasta 300kg | CAMION: hasta 500kg
     */
    public void recalcularVehiculo() {
        double peso = pesoTotalKg != null ? pesoTotalKg : 0;

        if (peso <= 50) {
            tipoVehiculoRequerido = "MOTO";
            capacidadMaximaKg = 50.0;
            pesoMinimoSalidaKg = 10.0;  // al menos 10kg para justificar viaje en moto
        } else if (peso <= 300) {
            tipoVehiculoRequerido = "AUTOMOVIL";
            capacidadMaximaKg = 300.0;
            pesoMinimoSalidaKg = 50.0;  // al menos 50kg para auto
        } else {
            tipoVehiculoRequerido = "CAMION";
            capacidadMaximaKg = 500.0;
            pesoMinimoSalidaKg = 150.0; // al menos 150kg para camion
        }
    }

    /**
     * Agrega un pedido a la ruta y actualiza totales
     */
    public void agregarPedido(Orden orden) {
        orden.setRuta(this);
        pedidos.add(orden);
        pesoTotalKg = (pesoTotalKg != null ? pesoTotalKg : 0.0) +
                      (orden.getPesoTotalKg() != null ? orden.getPesoTotalKg() : 0.0);
        pagoTotalEstimado = (pagoTotalEstimado != null ? pagoTotalEstimado : 0.0) +
                            (orden.getCostoEnvio() != null ? orden.getCostoEnvio() : 0.0);
        pedidosCount = pedidos.size();
    }
}
