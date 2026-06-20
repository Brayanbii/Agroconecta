package com.proyecto.AccesoUsuarios.repository;

import com.proyecto.AccesoUsuarios.model.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RutaRepository extends JpaRepository<Ruta, Long> {

    Optional<Ruta> findByCodigoRuta(String codigoRuta);

    // Rutas en estado FORMANDOSE (para el robot agrupador)
    List<Ruta> findByEstadoAndZonaDestino(String estado, String zonaDestino);

    // Rutas listas para salir (para ofrecer a repartidores)
    List<Ruta> findByEstadoOrderByFechaCreacionAsc(String estado);

    // Rutas de un repartidor especifico
    List<Ruta> findByRepartidorIdOrderByFechaCreacionDesc(Long repartidorId);

    // Rutas activas (ASIGNADA o EN_CAMINO)
    @Query("SELECT r FROM Ruta r WHERE r.estado IN ('LISTA_PARA_SALIR', 'ASIGNADA', 'EN_CAMINO')")
    List<Ruta> findRutasActivas();

    // Contar rutas por estado
    long countByEstado(String estado);
}
