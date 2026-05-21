package com.proyecto.AccesoUsuarios.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.AccesoUsuarios.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Método original (lo dejamos por si acaso)
    Optional<Usuario> findByUserName(String username);
    
    // NUEVO MÉTODO: Buscar por Email
    Optional<Usuario> findByEmail(String email);

    // NUEVO MÉTODO: Buscar por Teléfono
    Optional<Usuario> findByTelefono(String telefono);

}