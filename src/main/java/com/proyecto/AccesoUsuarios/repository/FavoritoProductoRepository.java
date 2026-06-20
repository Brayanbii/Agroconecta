package com.proyecto.AccesoUsuarios.repository;

import com.proyecto.AccesoUsuarios.model.FavoritoProducto;
import com.proyecto.AccesoUsuarios.model.Producto;
import com.proyecto.AccesoUsuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoritoProductoRepository extends JpaRepository<FavoritoProducto, Long> {
    Optional<FavoritoProducto> findByClienteAndProducto(Usuario cliente, Producto producto);
    int countByProducto(Producto producto);
    boolean existsByClienteAndProducto(Usuario cliente, Producto producto);
    int countByProducto_Usuario(Usuario campesino);
    List<FavoritoProducto> findByClienteOrderByFechaCreacionDesc(Usuario cliente);
    List<FavoritoProducto> findByProducto(Producto producto);
    void deleteByProducto(Producto producto);
}
