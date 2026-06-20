package com.proyecto.AccesoUsuarios.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.AccesoUsuarios.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUserName(String username);
    
    List<Usuario> findAllByEmail(String email);

    Optional<Usuario> findFirstByEmail(String email);

    Optional<Usuario> findByEmailAndRol(String email, String rol);

    Optional<Usuario> findByTelefono(String telefono);

}
