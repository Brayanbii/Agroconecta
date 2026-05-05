package com.proyecto.AccesoUsuarios.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.proyecto.AccesoUsuarios.model.Orden;
import com.proyecto.AccesoUsuarios.model.Usuario;
import java.util.List;

public interface OrdenRepository extends JpaRepository<Orden, Long> {

    // Para que el cliente vea SUS compras
    List<Orden> findByUsuario(Usuario usuario);
    
    // Para ver las ultimas 5 compras
    List<Orden> findTop5ByUsuarioOrderByFechaCreacionDesc(Usuario usuario);

    // Ventas totales agrupadas por mes (para grafico de lineas del Admin)
    @Query("SELECT MONTH(o.fechaCreacion), SUM(o.total) " +
           "FROM Orden o GROUP BY MONTH(o.fechaCreacion) ORDER BY MONTH(o.fechaCreacion)")
    List<Object[]> findVentasPorMes();

    // Conteo de ordenes agrupadas por estado (para grafico de torta del Admin)
    @Query("SELECT o.estado, COUNT(o) FROM Orden o GROUP BY o.estado")
    List<Object[]> findOrdenesPorEstado();
}
