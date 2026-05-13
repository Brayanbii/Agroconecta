package com.proyecto.AccesoUsuarios.repository;

import com.proyecto.AccesoUsuarios.model.MensajeSoporte;
import com.proyecto.AccesoUsuarios.model.TicketSoporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeSoporteRepository extends JpaRepository<MensajeSoporte, Long> {
    List<MensajeSoporte> findByTicketOrderByFechaEnvioAsc(TicketSoporte ticket);
}
