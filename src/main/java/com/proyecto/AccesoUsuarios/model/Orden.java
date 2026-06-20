package com.proyecto.AccesoUsuarios.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orden")
@Getter
@Setter
@ToString(exclude = "detalles")
@EqualsAndHashCode(exclude = "detalles")
@AllArgsConstructor
@NoArgsConstructor
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numeroOrden;
    private LocalDateTime fechaCreacion;
    private Double total;

    // Estado del pedido: Pendiente, PAGADO, ESPERANDO_AGRUPACION, etc.
    private String estado;

    // Datos de Envio (Ubicacion del Cliente)
    private String direccionEnvio;
    private Double latitudEnvio;
    private Double longitudEnvio;

    // Nuevos campos para el sistema de delivery
    @Column(columnDefinition = "varchar(20) default 'ECONOMICO'")
    private String tipoEnvio;           // ECONOMICO (grupal, 2-3 dias) o RAPIDO (individual, 24h)
    private Double costoEnvio;          // Costo calculado del envio
    private Double subtotalProductos;   // Suma de productos sin fees
    private Double tarifaPlataforma;    // 10% comision plataforma
    private Double costoPasarela;       // 5% pasarela de pago
    private Double pesoTotalKg;         // Peso total de los productos

    // Coordenadas del campesino (origen) - para calculo de distancia
    private Double latitudOrigen;
    private Double longitudOrigen;
    private String municipioOrigen;

    // Fecha limite para entrega economica
    private LocalDateTime fechaLimiteEntrega;

    // --- SISTEMA DE VERIFICACION PIN ---
    @Column(length = 6)
    private String codigoRecogida;        // PIN campesino→repartidor
    @Column(length = 6)
    private String codigoEntrega;         // PIN cliente→repartidor
    private Integer intentosRecogida = 0;
    private Integer intentosEntrega = 0;
    private LocalDateTime fechaGeneracionRecogida;
    private LocalDateTime fechaGeneracionEntrega;
    @Column(columnDefinition = "TEXT")
    private String logVerificaciones;     // JSON array de intentos

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario; // El Cliente

    @ManyToOne
    @JoinColumn(name = "ruta_id")
    private Ruta ruta; // La ruta a la que pertenece (si ya fue agrupada)

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL)
    private List<DetalleOrden> detalles; // Los productos comprados
}