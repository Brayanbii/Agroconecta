package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

@Controller
@RequestMapping("/admin/usuarios")
public class AdminUserController {

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
    public String guardarUsuario(@Valid @ModelAttribute Usuario usuario, BindingResult result, Model model) {
        
        // Si es edición, ignoramos errores de password (puede venir vacío para mantener la actual)
        if (usuario.getId() != null) {
            long erroresSinPassword = result.getFieldErrors().stream()
                    .filter(e -> !"password".equals(e.getField()))
                    .count();
            if (erroresSinPassword > 0) {
                return "admin_usuario_form";
            }
        } else {
            // Si es creación, todos los errores importan
            if (result.hasErrors()) {
                return "admin_usuario_form";
            }
        }

        // Verificar si es edición (tiene ID) o creación (no tiene ID)
        if (usuario.getId() != null) {
            // EDICIÓN: Buscamos al usuario original en la BD
            Usuario usuarioExistente = usuarioRepo.findById(usuario.getId()).orElse(null);
            
            if (usuarioExistente != null) {
                // Si el campo contraseña NO está vacío, la actualizamos
                if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                    usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
                } else {
                    // Si está vacío, mantenemos la contraseña vieja
                    usuario.setPassword(usuarioExistente.getPassword());
                }
            }
        } else {
            // CREACIÓN: La contraseña es obligatoria y se encripta
            if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
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

    // Eliminar usuario (Soft Delete / Vetar)
    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id) {
        Usuario usuario = usuarioRepo.findById(id).orElse(null);
        if (usuario != null) {
            // Soft delete
            usuario.setEstadoVerificacion("VETADO");
            usuarioRepo.save(usuario);
        }
        return "redirect:/admin/usuarios";
    }

    // --- ECOSISTEMA DE VERIFICACIÓN (KYC) ---
    
    @GetMapping("/verificaciones")
    public String listarVerificaciones(Model model) {
        List<Usuario> campesinos = usuarioRepo.findAll().stream()
            .filter(u -> "CAMPESINO".equals(u.getRol()))
            .toList();
            
        long pendientesCount = campesinos.stream()
            .filter(u -> "EN_REVISION".equals(u.getEstadoVerificacion()))
            .count();
            
        model.addAttribute("campesinos", campesinos);
        model.addAttribute("pendientesCount", pendientesCount);
        return "admin_verificaciones";
    }

    @PostMapping("/verificaciones/aprobar/{id}")
    public String aprobarCampesino(@PathVariable Long id) {
        Usuario campesino = usuarioRepo.findById(id).orElseThrow();
        campesino.setEstadoVerificacion("APROBADO");
        usuarioRepo.save(campesino);
        return "redirect:/admin/usuarios/verificaciones?aprobado=true";
    }

    @PostMapping("/verificaciones/rechazar/{id}")
    public String rechazarCampesino(@PathVariable Long id) {
        Usuario campesino = usuarioRepo.findById(id).orElseThrow();
        campesino.setEstadoVerificacion("RECHAZADO");
        usuarioRepo.save(campesino);
        return "redirect:/admin/usuarios/verificaciones?rechazado=true";
    }

    @PostMapping("/verificaciones/vetar/{id}")
    public String vetarCampesino(@PathVariable Long id) {
        Usuario campesino = usuarioRepo.findById(id).orElseThrow();
        campesino.setEstadoVerificacion("VETADO");
        usuarioRepo.save(campesino);
        return "redirect:/admin/usuarios/verificaciones?vetado=true";
    }

    @PostMapping("/verificaciones/reactivar/{id}")
    public String reactivarCampesino(@PathVariable Long id) {
        Usuario campesino = usuarioRepo.findById(id).orElseThrow();
        campesino.setEstadoVerificacion("APROBADO");
        usuarioRepo.save(campesino);
        return "redirect:/admin/usuarios/verificaciones?aprobado=true";
    }
}