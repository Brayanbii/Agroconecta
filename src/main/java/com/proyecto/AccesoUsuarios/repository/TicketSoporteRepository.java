package com.proyecto.AccesoUsuarios.repository;

import com.proyecto.AccesoUsuarios.model.TicketSoporte;
import com.proyecto.AccesoUsuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketSoporteRepository extends JpaRepository<TicketSoporte, Long> {
    List<TicketSoporte> findByUsuarioOrderByFechaActualizacionDesc(Usuario usuario);
    List<TicketSoporte> findByEstadoOrderByFechaActualizacionDesc(String estado);
    List<TicketSoporte> findAllByOrderByFechaActualizacionDesc();
}
