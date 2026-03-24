package com.proyecto.AccesoUsuarios.repository;

import com.proyecto.AccesoUsuarios.model.DetalleOrden;
import com.proyecto.AccesoUsuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface DetalleOrdenRepository extends JpaRepository<DetalleOrden, Long> {

    // Esta consulta busca todos los items vendidos que pertenecen a un campesino específico
    @Query("SELECT d FROM DetalleOrden d WHERE d.producto.usuario = :campesino ORDER BY d.orden.fechaCreacion DESC")
    List<DetalleOrden> findVentasByCampesino(Usuario campesino);
}