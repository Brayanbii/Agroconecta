package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioRepository repo;

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
        usuario.setPassword(new BCryptPasswordEncoder().encode(usuario.getPassword()));
        repo.save(usuario);
        return "redirect:/usuarios";
    }

    @GetMapping("/usuarios/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("usuario", repo.findById(id).orElseThrow());
        return "form";
    }

    @GetMapping("/usuarios/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        repo.deleteById(id);
        return "redirect:/usuarios";
    }

    // --- PERFIL DE USUARIO (CORREGIDO: BUSCA POR EMAIL) ---
    
    @GetMapping("/perfil")
    public String perfil(Model model, Authentication auth) {
        String email = auth.getName(); // Esto devuelve el email con el que se logueó
        Usuario usuario = repo.findByEmail(email).orElseThrow(); // Buscamos por email
        model.addAttribute("usuario", usuario);
        return "form";
    }

    @PostMapping("/perfil/guardar")
    public String guardarPerfil(@ModelAttribute Usuario usuario, Authentication auth) {
        String email = auth.getName();
        Usuario actual = repo.findByEmail(email).orElseThrow(); // Buscamos por email

        // Actualizamos datos personales
        actual.setNombreCompleto(usuario.getNombreCompleto());
        actual.setEmail(usuario.getEmail());
        actual.setTelefono(usuario.getTelefono());

        // Contraseña opcional
        if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
            actual.setPassword(new BCryptPasswordEncoder().encode(usuario.getPassword()));
        }

        repo.save(actual); 
        return "redirect:/home?actualizado";
    }

    // --- REGISTRO PÚBLICO ---

    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    @PostMapping("/registro/guardar")
    public String guardarRegistro(@ModelAttribute Usuario usuario) {
        // Validar email duplicado
        if (repo.findByEmail(usuario.getEmail()).isPresent()) {
            return "redirect:/registro?error_email"; 
        }

        if (!"CAMPESINO".equals(usuario.getRol())) {
            usuario.setRol("CLIENTE");
        }
        
        usuario.setPassword(new BCryptPasswordEncoder().encode(usuario.getPassword()));
        
        try {
            repo.save(usuario);
        } catch (Exception e) {
            return "redirect:/registro?error_email";
        }
        
        return "redirect:/login?registrado"; 
    }
}
