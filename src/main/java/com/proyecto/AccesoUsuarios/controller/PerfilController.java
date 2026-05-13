package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.proyecto.AccesoUsuarios.service.UploadFileService;

import java.io.IOException;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private com.proyecto.AccesoUsuarios.repository.OrdenRepository ordenRepository;

    @Autowired
    private UploadFileService uploadService;

    @GetMapping("/{seccion}")
    public String verPerfil(@PathVariable(required = false) String seccion, Authentication auth, Model model) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        // Obtener usuario actualizado de BD usando el email de Spring Security
        String email = auth.getName();
        Usuario usuarioDB = usuarioRepository.findByEmail(email).orElse(null);
        
        if (usuarioDB == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("usuario", usuarioDB);

        // Validar la sección seleccionada
        if (seccion == null || seccion.isEmpty()) {
            seccion = "ajustes";
        }
        
        model.addAttribute("seccionActiva", seccion);

        if ("ordenes".equals(seccion)) {
            model.addAttribute("ultimasOrdenes", ordenRepository.findTop5ByUsuarioOrderByFechaCreacionDesc(usuarioDB));
        }
        
        if ("favoritos".equals(seccion)) {
            // Lazy loading forces us to fetch them here or use an explicit fetch, but wait, size() initializes it
            if (usuarioDB.getProductosFavoritos() != null) {
                usuarioDB.getProductosFavoritos().size(); // Trigger lazy initialization
                model.addAttribute("favoritos", usuarioDB.getProductosFavoritos());
            }
        }
        
        return "perfil";
    }

    @GetMapping
    public String verPerfilDefault(Authentication auth, Model model) {
        return verPerfil("ajustes", auth, model);
    }

    @PostMapping("/actualizar")
    public String actualizarPerfil(@ModelAttribute Usuario datosNuevos, 
                                   @RequestParam(value = "imgPerfil", required = false) MultipartFile imgPerfil,
                                   @RequestParam(value = "imgPortada", required = false) MultipartFile imgPortada,
                                   Authentication auth, RedirectAttributes redirectAttributes) throws IOException {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = auth.getName();
        Usuario usuarioDB = usuarioRepository.findByEmail(email).orElse(null);
        
        if (usuarioDB != null) {
            usuarioDB.setNombreCompleto(datosNuevos.getNombreCompleto());
            usuarioDB.setTelefono(datosNuevos.getTelefono());
            usuarioDB.setNumeroIdentidad(datosNuevos.getNumeroIdentidad());
            usuarioDB.setFechaNacimiento(datosNuevos.getFechaNacimiento());
            usuarioDB.setGenero(datosNuevos.getGenero());
            
            if (imgPerfil != null && !imgPerfil.isEmpty()) {
                String nombreImagen = uploadService.saveImage(imgPerfil);
                usuarioDB.setFotoPerfil(nombreImagen);
            } else if (datosNuevos.getFotoPerfil() != null && !datosNuevos.getFotoPerfil().isEmpty()) {
                usuarioDB.setFotoPerfil(datosNuevos.getFotoPerfil());
            }
            
            if (imgPortada != null && !imgPortada.isEmpty()) {
                String nombreImagen = uploadService.saveImage(imgPortada);
                usuarioDB.setFotoFincaUrl(nombreImagen);
            } else if (datosNuevos.getFotoFincaUrl() != null && !datosNuevos.getFotoFincaUrl().isEmpty()) {
                usuarioDB.setFotoFincaUrl(datosNuevos.getFotoFincaUrl());
            }
            
            // Si el usuario envía datos de su negocio (para campesinos)
            if (datosNuevos.getNombreFinca() != null) {
                usuarioDB.setNombreFinca(datosNuevos.getNombreFinca());
            }
            if (datosNuevos.getDescripcionFinca() != null) {
                usuarioDB.setDescripcionFinca(datosNuevos.getDescripcionFinca());
            }
            if (datosNuevos.getMunicipioOrigen() != null) {
                usuarioDB.setMunicipioOrigen(datosNuevos.getMunicipioOrigen());
            }

            usuarioRepository.save(usuarioDB);
            redirectAttributes.addFlashAttribute("perfilExito", "¡Perfil actualizado con éxito!");
        }

        return "redirect:/perfil/ajustes";
    }
}
