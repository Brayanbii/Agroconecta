package com.proyecto.AccesoUsuarios.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "favorito_campesino", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"cliente_id", "campesino_id"})
})
@Getter
@Setter
public class FavoritoCampesino {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @ManyToOne
    @JoinColumn(name = "campesino_id", nullable = false)
    private Usuario campesino;

    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
