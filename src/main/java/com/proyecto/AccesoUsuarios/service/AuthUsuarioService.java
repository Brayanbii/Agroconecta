package com.proyecto.AccesoUsuarios.service;

import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthUsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepo;

    public Usuario getAuthenticatedUser(Authentication auth) {
        String principal = auth.getName();
        try {
            Long id = Long.valueOf(principal);
            return usuarioRepo.findById(id).orElse(null);
        } catch (NumberFormatException e) {
            return usuarioRepo.findFirstByEmail(principal).orElse(null);
        }
    }
}
