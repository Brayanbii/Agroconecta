package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioRepository repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/home")
    public String home(Model model, Authentication auth) {
        model.addAttribute("rol", auth.getAuthorities().toString());
        return "home";
    }

    @GetMapping("/usuarios")
    public String listar(Model model) {
        model.addAttribute("usuarios", repo.findAll());
        return "usuarios";
    }

    @GetMapping("/usuarios/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "form";
    }

    @PostMapping("/usuarios/guardar")
    public String guardar(@ModelAttribute Usuario usuario) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        repo.save(usuario);
        return "redirect:/usuarios";
    }

    @GetMapping("/usuarios/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("usuario", repo.findById(id).orElseThrow());
        return "form";
    }

    // Eliminar usuario (POST para seguridad CSRF)
    @PostMapping("/usuarios/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        repo.deleteById(id);
        return "redirect:/usuarios";
    }

    // --- EL PERFIL AHORA SE MANEJA EN PerfilController ---

    // --- REGISTRO PÚBLICO ---

    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    @PostMapping("/registro/guardar")
    public String guardarRegistro(@Valid @ModelAttribute Usuario usuario, BindingResult result, Model model) {
        // Si hay errores de validación (@NotEmpty, @Email, @Size), volvemos al formulario
        if (result.hasErrors()) {
            return "registro";
        }

        // Validar email duplicado
        if (repo.findByEmail(usuario.getEmail()).isPresent()) {
            return "redirect:/registro?error_email"; 
        }

        // Validar username duplicado
        if (repo.findByUserName(usuario.getUserName()).isPresent()) {
            return "redirect:/registro?error_username"; 
        }

        // Validar telefono duplicado
        if (usuario.getTelefono() != null && !usuario.getTelefono().trim().isEmpty() && repo.findByTelefono(usuario.getTelefono()).isPresent()) {
            return "redirect:/registro?error_telefono";
        }

        if (!"CAMPESINO".equals(usuario.getRol())) {
            usuario.setRol("CLIENTE");
        } else {
            // Es Campesino: Requiere verificación
            usuario.setEstadoVerificacion("PENDIENTE_DATOS");
        }
        
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        
        try {
            repo.save(usuario);
        } catch (Exception e) {
            return "redirect:/registro?error_general";
        }
        
        return "redirect:/login?registrado"; 
    }

    // --- API PÚBLICA (Validación Asíncrona) ---
    @GetMapping("/api/usuarios/check-email")
    @ResponseBody
    public java.util.Map<String, Boolean> checkEmail(@RequestParam String email) {
        boolean exists = repo.findByEmail(email).isPresent();
        return java.util.Collections.singletonMap("exists", exists);
    }

    @GetMapping("/api/usuarios/check-username")
    @ResponseBody
    public java.util.Map<String, Boolean> checkUsername(@RequestParam String username) {
        boolean exists = repo.findByUserName(username).isPresent();
        return java.util.Collections.singletonMap("exists", exists);
    }

    @GetMapping("/api/usuarios/check-telefono")
    @ResponseBody
    public java.util.Map<String, Boolean> checkTelefono(@RequestParam String telefono) {
        boolean exists = repo.findByTelefono(telefono).isPresent();
        return java.util.Collections.singletonMap("exists", exists);
    }
}
