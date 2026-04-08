package com.proyecto.AccesoUsuarios.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.AccesoUsuarios.model.Resena;

import java.util.List;

public interface ResenaRepository extends JpaRepository<Resena, Long> {
    
    // Buscar todas las reseñas de un producto
    List<Resena> findByProductoIdOrderByFechaDesc(Long productoId);

    // Contar el total de reseñas en la plataforma    
    long count();

    // Obtener todas las reseñas ordenadas por fecha (para el dashboard admin)
    List<Resena> findAllByOrderByFechaDesc();
}
