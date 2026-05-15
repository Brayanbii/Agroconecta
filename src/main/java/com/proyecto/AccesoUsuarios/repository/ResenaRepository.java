package com.proyecto.AccesoUsuarios.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.AccesoUsuarios.model.Resena;

import java.util.List;
import java.util.Optional;

public interface ResenaRepository extends JpaRepository<Resena, Long> {
    
    // Buscar todas las reseñas de un producto
    List<Resena> findByProductoIdOrderByFechaDesc(Long productoId);

    // Buscar la reseña de un usuario específico para un producto
    Optional<Resena> findByProductoIdAndUsuarioId(Long productoId, Long usuarioId);

    // Contar el total de reseñas en la plataforma    
    long count();

    // Obtener todas las reseñas ordenadas por fecha (para el dashboard admin)
    List<Resena> findAllByOrderByFechaDesc();
}
