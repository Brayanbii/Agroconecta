package com.proyecto.AccesoUsuarios.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString; // Importante para evitar errores con Lombok

import java.util.List;

@Entity
@Table(name = "usuario")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @NotEmpty(message = "El nombre de usuario es obligatorio")
    private String userName;

    @Column(nullable = false)
    @Size(min = 4, message = "La contraseña debe tener al menos 4 caracteres")
    private String password;

    @Column(nullable = false)
    private String rol;

    @Column(nullable = false)
    @NotEmpty(message = "El nombre completo es obligatorio")
    private String nombreCompleto;

    @Column(nullable = false)
    @NotEmpty(message = "El correo es obligatorio")
    @Email(message = "Debe ser un correo válido")
    private String email;

    private String telefono;

    // --- PERFIL DE USUARIO AVANZADO ---
    @Column(length = 500)
    private String fotoPerfil; // URL o enlace
    
    private String numeroIdentidad;
    
    private java.time.LocalDate fechaNacimiento;
    
    private String genero; // 'Hombre', 'Mujer', 'Otro'
    
    @Column(columnDefinition = "numeric(10,2) default 0.0")
    private Double creditos = 0.0; // AgroCréditos

    // --- VERIFICACIÓN DE IDENTIDAD Y SEGURIDAD (KYC) ---
    // Valores posibles: "PENDIENTE_DATOS", "EN_REVISION", "APROBADO", "RECHAZADO"
    @Column(columnDefinition = "varchar(20) default 'APROBADO'")
    private String estadoVerificacion = "APROBADO"; // Default para usuarios viejos
    
    @Column(length = 500)
    private String fotoCedulaUrl;
    
    @Column(length = 500)
    private String fotoFincaUrl;

    // --- CAMPOS EXCLUSIVOS PARA CAMPESINOS (Vendedores) ---
    private String nombreFinca;
    
    @Column(length = 1000)
    private String descripcionFinca;

    // --- CAMPOS EXCLUSIVOS PARA REPARTIDORES (Delivery) ---
    private String tipoVehiculo;         // MOTO, CAMIONETA, CAMION, MOTOCARGUERO
    private String placaVehiculo;
    private String marcaVehiculo;
    private String modeloVehiculo;
    private Integer anioVehiculo;
    private Double capacidadCargaKg;     // kg que puede cargar
    private String licenciaConduccion;   // B1, B2, C1, C2
    private String colorVehiculo;
    
    // Documentos del repartidor (URLs de imagenes subidas)
    @Column(length = 500)
    private String fotoLicenciaFrontalUrl;
    @Column(length = 500)
    private String fotoLicenciaTraseraUrl;
    @Column(length = 500)
    private String fotoTarjetaPropiedadUrl;
    @Column(length = 500)
    private String fotoSOATUrl;
    @Column(length = 500)
    private String fotoTecnomecanicaUrl;

    // Rechazo de documentos (JSON con los tipos de doc rechazados y motivo)
    @Column(length = 1000)
    private String motivoRechazo;

    // Coordenadas geográficas y ubicación (ubicación por defecto o finca del campesino)
    private Double latitud;
    private Double longitud;
    private String municipioOrigen;

    // Tracking GPS repartidor
    private java.time.LocalDateTime fechaUltimaUbicacion;
    private Boolean disponible; // true = online, puede recibir viajes

    // Reputacion del campesino (para auto-aceptar pedidos)
    private Integer totalEntregas = 0;       // Total de ventas entregadas exitosamente
    private Integer totalRechazos = 0;       // Total de pedidos rechazados
    @Column(columnDefinition = "numeric(3,2) default 0.0")
    private Double calificacionPromedio = 0.0; // Promedio de estrellas
    private Boolean autoAceptar = false;     // Acepta pedidos automaticamente
    private Boolean autoAceptarDisponible = false; // Desbloqueado al llegar a 30 entregas

    // --- RELACIONES PARA BORRADO EN CASCADA ---

    // Si borro al usuario, se borran sus productos
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude // Evita bucles infinitos al imprimir el objeto
    private List<Producto> productos;

    // Si borro al usuario, se borran sus órdenes de compra
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Orden> ordenes;

    // Si borro al usuario, se borran sus direcciones guardadas
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Direccion> direcciones;

    @Transient
    @ToString.Exclude
    private List<Producto> productosFavoritos;
}