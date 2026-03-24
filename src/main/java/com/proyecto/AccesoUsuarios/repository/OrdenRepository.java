package com.proyecto.AccesoUsuarios.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.AccesoUsuarios.model.Orden;
import com.proyecto.AccesoUsuarios.model.Usuario;
import java.util.List;

public interface OrdenRepository extends JpaRepository<Orden, Long> {
    List<Orden> findByUsuario(Usuario usuario); // Para que el cliente vea SUS compras
}