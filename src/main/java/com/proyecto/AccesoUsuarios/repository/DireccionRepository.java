package com.proyecto.AccesoUsuarios.repository;

import com.proyecto.AccesoUsuarios.model.Direccion;
import com.proyecto.AccesoUsuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DireccionRepository extends JpaRepository<Direccion, Long> {
    
    // Obtener todas las direcciones de un usuario
    List<Direccion> findByUsuario(Usuario usuario);
    
    // Obtener la dirección principal de un usuario
    Direccion findByUsuarioAndEsPrincipalTrue(Usuario usuario);
}
