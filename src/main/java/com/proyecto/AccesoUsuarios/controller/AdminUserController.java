package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/usuarios")
public class AdminUserController {

    @Autowired
    private UsuarioRepository usuarioRepo;

    // Listar todos los usuarios
    @GetMapping
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioRepo.findAll());
        return "admin_usuarios";
    }

    // Formulario para crear usuario nuevo
    @GetMapping("/nuevo")
    public String nuevoUsuario(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "admin_usuario_form";
    }

    // Guardar usuario (Crear o Editar)
    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario) {
        
        // Verificar si es edición (tiene ID) o creación (no tiene ID)
        if (usuario.getId() != null) {
            // EDICIÓN: Buscamos al usuario original en la BD
            Usuario usuarioExistente = usuarioRepo.findById(usuario.getId()).orElse(null);
            
            if (usuarioExistente != null) {
                // Si el campo contraseña NO está vacío, la actualizamos
                if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                    usuario.setPassword(new BCryptPasswordEncoder().encode(usuario.getPassword()));
                } else {
                    // Si está vacío, mantenemos la contraseña vieja
                    usuario.setPassword(usuarioExistente.getPassword());
                }
            }
        } else {
            // CREACIÓN: La contraseña es obligatoria y se encripta
            if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                usuario.setPassword(new BCryptPasswordEncoder().encode(usuario.getPassword()));
            }
        }
        
        // Guardamos (si el ID existe, JPA actualiza; si no, crea uno nuevo)
        try {
            usuarioRepo.save(usuario);
        } catch (Exception e) {
            // Si intentas poner un nombre repetido, fallará aquí.
            // En un caso real, deberíamos manejar el error y mostrar un mensaje.
            return "redirect:/admin/usuarios?error=nombre_duplicado";
        }
        
        return "redirect:/admin/usuarios";
    }

    // Cargar formulario de edición
    @GetMapping("/editar/{id}")
    public String editarUsuario(@PathVariable Long id, Model model) {
        model.addAttribute("usuario", usuarioRepo.findById(id).orElseThrow());
        return "admin_usuario_form";
    }

    // Eliminar usuario
    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id) {
        usuarioRepo.deleteById(id);
        return "redirect:/admin/usuarios";
    }
}