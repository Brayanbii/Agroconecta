package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import com.proyecto.AccesoUsuarios.service.UploadFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/campesino/verificacion")
public class CampesinoVerificacionController {

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private UploadFileService uploadService;

    @GetMapping
    public String mostrarFormulario(Model model, Authentication auth) {
        String email = auth.getName();
        Usuario campesino = usuarioRepo.findFirstByEmail(email).orElseThrow();

        // Si ya está aprobado o rechazado, redirigir al dashboard (allí el dashboard manejará su estado)
        if ("APROBADO".equals(campesino.getEstadoVerificacion())) {
            return "redirect:/campesino/productos";
        }

        model.addAttribute("campesino", campesino);
        return "campesino_verificacion";
    }

    @PostMapping("/guardar")
    public String guardarVerificacion(
            @RequestParam("numeroIdentidad") String numeroIdentidad,
            @RequestParam("nombreFinca") String nombreFinca,
            @RequestParam(value = "direccionFinca", required = false) String direccionFinca,
            @RequestParam(value = "municipioOrigen", required = false) String municipioOrigen,
            @RequestParam("latitudFinca") Double latitudFinca,
            @RequestParam("longitudFinca") Double longitudFinca,
            @RequestParam("fotoCedula") MultipartFile fotoCedula,
            @RequestParam("fotoFinca") MultipartFile fotoFinca,
            Authentication auth) throws IOException {

        String email = auth.getName();
        Usuario campesino = usuarioRepo.findFirstByEmail(email).orElseThrow();

        // Actualizar datos de texto y mapa
        campesino.setNumeroIdentidad(numeroIdentidad);
        campesino.setNombreFinca(nombreFinca);
        if (direccionFinca != null && !direccionFinca.isEmpty()) {
            campesino.setDescripcionFinca(direccionFinca);
        }
        if (municipioOrigen != null && !municipioOrigen.isEmpty()) {
            campesino.setMunicipioOrigen(municipioOrigen);
        }
        campesino.setLatitud(latitudFinca);
        campesino.setLongitud(longitudFinca);

        // Subir fotos
        if (!fotoCedula.isEmpty()) {
            String urlCedula = uploadService.saveImage(fotoCedula);
            campesino.setFotoCedulaUrl(urlCedula);
        }

        if (!fotoFinca.isEmpty()) {
            String urlFinca = uploadService.saveImage(fotoFinca);
            campesino.setFotoFincaUrl(urlFinca);
        }

        // Cambiar estado a EN_REVISION
        campesino.setEstadoVerificacion("EN_REVISION");

        usuarioRepo.save(campesino);

        return "redirect:/campesino/verificacion?exito=true";
    }
}
