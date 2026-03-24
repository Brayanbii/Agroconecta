package com.proyecto.AccesoUsuarios.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.AccesoUsuarios.model.Producto;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    // Aquí podremos agregar métodos como: buscar por categoría, por campesino, etc.
    // Ejemplo: List<Producto> findByCategoria(String categoria);
}