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

    @Column(nullable = false, unique = true)
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

    // --- CAMPOS EXCLUSIVOS PARA CAMPESINOS (Vendedores) ---
    private String nombreFinca;
    
    @Column(length = 1000)
    private String descripcionFinca;

    // Coordenadas geográficas (ubicación por defecto o finca del campesino)
    private Double latitud;
    private Double longitud;

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
}