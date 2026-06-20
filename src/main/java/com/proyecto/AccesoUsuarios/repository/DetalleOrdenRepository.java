package com.proyecto.AccesoUsuarios.repository;

import com.proyecto.AccesoUsuarios.model.DetalleOrden;
import com.proyecto.AccesoUsuarios.model.Producto;
import com.proyecto.AccesoUsuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DetalleOrdenRepository extends JpaRepository<DetalleOrden, Long> {

    // Ventas por campesino especifico (incluye productos borrados gracias a campesinoId)
    @Query("SELECT d FROM DetalleOrden d WHERE (d.producto IS NOT NULL AND d.producto.usuario = :campesino) OR (d.producto IS NULL AND d.campesinoId = :campesinoId) ORDER BY d.orden.fechaCreacion DESC")
    List<DetalleOrden> findVentasByCampesino(@Param("campesino") Usuario campesino, @Param("campesinoId") Long campesinoId);

    // Buscar detalles por producto (para desvincular antes de borrar)
    List<DetalleOrden> findByProducto(Producto producto);

    // Top productos mas vendidos (para grafico de barras del Admin)
    @Query("SELECT d.nombre, SUM(d.cantidad) as totalCantidad, SUM(d.total) as totalVentas " +
           "FROM DetalleOrden d GROUP BY d.nombre ORDER BY totalCantidad DESC")
    List<Object[]> findTopProductos();
}
