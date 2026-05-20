package com.proyecto.AccesoUsuarios.repository;

import com.proyecto.AccesoUsuarios.model.FavoritoCampesino;
import com.proyecto.AccesoUsuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoritoCampesinoRepository extends JpaRepository<FavoritoCampesino, Long> {
    Optional<FavoritoCampesino> findByClienteAndCampesino(Usuario cliente, Usuario campesino);
    int countByCampesino(Usuario campesino);
    boolean existsByClienteAndCampesino(Usuario cliente, Usuario campesino);
    java.util.List<FavoritoCampesino> findByCliente(Usuario cliente);
}
