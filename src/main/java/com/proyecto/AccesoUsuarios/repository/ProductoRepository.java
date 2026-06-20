package com.proyecto.AccesoUsuarios.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.AccesoUsuarios.model.Producto;
import com.proyecto.AccesoUsuarios.model.Usuario;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByUsuario(Usuario usuario);
    List<Producto> findByUsuarioId(Long usuarioId);
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
}
