package com.proyecto.AccesoUsuarios.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.AccesoUsuarios.model.Producto;
import com.proyecto.AccesoUsuarios.model.Usuario;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Buscar productos de un campesino específico
    List<Producto> findByUsuario(Usuario usuario);

    // Buscar productos por nombre (contiene, ignorando mayúsculas/minúsculas)
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
}
