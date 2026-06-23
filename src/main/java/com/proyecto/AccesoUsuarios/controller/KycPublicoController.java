package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import com.proyecto.AccesoUsuarios.service.UploadFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class KycPublicoController {

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private UploadFileService uploadService;

    @Autowired
    private PasswordEncoder encoder;

    @GetMapping("/kyc-repartidor")
    public String formulario(Model model) {
        model.addAttribute("repartidores", usuarioRepo.findByRol("REPARTIDOR"));
        return "kyc_repartidor";
    }

    @PostMapping("/kyc-repartidor/subir")
    public String subirDocumentos(
            @RequestParam("tipo") String tipo,
            @RequestParam("email") String email,
            @RequestParam(value = "nombreCompleto", required = false) String nombreCompleto,
            @RequestParam(value = "telefono", required = false) String telefono,
            @RequestParam(value = "municipioOrigen", required = false) String municipio,
            @RequestParam(value = "placaVehiculo", required = false) String placa,
            @RequestParam(value = "tipoVehiculo", required = false) String tipoVehiculo,
            @RequestParam(value = "marcaVehiculo", required = false) String marca,
            @RequestParam(value = "modeloVehiculo", required = false) String modelo,
            @RequestParam(value = "anioVehiculo", required = false) Integer anio,
            @RequestParam(value = "capacidadCargaKg", required = false) Double capacidad,
            @RequestParam(value = "licenciaConduccion", required = false) String licencia,
            @RequestParam(value = "colorVehiculo", required = false) String color,
            @RequestParam(value = "cedula", required = false) MultipartFile cedula,
            @RequestParam(value = "licenciaFrontal", required = false) MultipartFile licenciaFrontal,
            @RequestParam(value = "licenciaTrasera", required = false) MultipartFile licenciaTrasera,
            @RequestParam(value = "tarjetaPropiedad", required = false) MultipartFile tarjetaPropiedad,
            @RequestParam(value = "soat", required = false) MultipartFile soat,
            @RequestParam(value = "tecnomecanica", required = false) MultipartFile tecnomecanica,
            @RequestParam(value = "fotoPerfil", required = false) MultipartFile fotoPerfil,
            @RequestParam(value = "fotoCedulaFrontal", required = false) MultipartFile fotoCedulaFrontal,
            RedirectAttributes redirect) {

        try {
            Usuario rep;

            if ("nuevo".equals(tipo)) {
                if (nombreCompleto == null || nombreCompleto.isBlank()) {
                    redirect.addFlashAttribute("error", "El nombre completo es obligatorio para nuevos repartidores");
                    return "redirect:/kyc-repartidor";
                }
                if (email == null || !email.contains("@") || email.length() < 5) {
                    redirect.addFlashAttribute("error", "Debes ingresar un correo electrónico válido (ej: repartidor@gmail.com)");
                    return "redirect:/kyc-repartidor";
                }
                rep = new Usuario();
                rep.setUserName("rep_" + System.currentTimeMillis());
                rep.setEmail(email);
                rep.setPassword(encoder.encode("123456"));
                rep.setRol("REPARTIDOR");
                rep.setNombreCompleto(nombreCompleto);
                rep.setTelefono(telefono != null ? telefono : "");
                rep.setMunicipioOrigen(municipio != null ? municipio : "");
                rep.setEstadoVerificacion("EN_REVISION");
                usuarioRepo.save(rep);
            } else {
                rep = usuarioRepo.findFirstByEmail(email).orElse(null);
                if (rep == null || !"REPARTIDOR".equals(rep.getRol())) {
                    redirect.addFlashAttribute("error", "Repartidor no encontrado. Usa 'Nuevo Repartidor' para registrarlo.");
                    return "redirect:/kyc-repartidor";
                }
            }

            int docsSubidos = 0;

            if (placa != null && !placa.isBlank()) rep.setPlacaVehiculo(placa);
            if (tipoVehiculo != null && !tipoVehiculo.isBlank()) rep.setTipoVehiculo(tipoVehiculo);
            if (marca != null && !marca.isBlank()) rep.setMarcaVehiculo(marca);
            if (modelo != null && !modelo.isBlank()) rep.setModeloVehiculo(modelo);
            if (anio != null && anio > 0) rep.setAnioVehiculo(anio);
            if (capacidad != null && capacidad > 0) rep.setCapacidadCargaKg(capacidad);
            if (licencia != null && !licencia.isBlank()) rep.setLicenciaConduccion(licencia);
            if (color != null && !color.isBlank()) rep.setColorVehiculo(color);
            if ("nuevo".equals(tipo) && telefono != null && !telefono.isBlank()) rep.setTelefono(telefono);

            if (fotoCedulaFrontal != null && !fotoCedulaFrontal.isEmpty()) {
                rep.setFotoCedulaUrl(uploadService.saveImage(fotoCedulaFrontal));
                docsSubidos++;
            } else if (cedula != null && !cedula.isEmpty()) {
                rep.setFotoCedulaUrl(uploadService.saveImage(cedula));
                docsSubidos++;
            }
            if (licenciaFrontal != null && !licenciaFrontal.isEmpty()) {
                rep.setFotoLicenciaFrontalUrl(uploadService.saveImage(licenciaFrontal));
                docsSubidos++;
            }
            if (licenciaTrasera != null && !licenciaTrasera.isEmpty()) {
                rep.setFotoLicenciaTraseraUrl(uploadService.saveImage(licenciaTrasera));
                docsSubidos++;
            }
            if (tarjetaPropiedad != null && !tarjetaPropiedad.isEmpty()) {
                rep.setFotoTarjetaPropiedadUrl(uploadService.saveImage(tarjetaPropiedad));
                docsSubidos++;
            }
            if (soat != null && !soat.isEmpty()) {
                rep.setFotoSOATUrl(uploadService.saveImage(soat));
                docsSubidos++;
            }
            if (tecnomecanica != null && !tecnomecanica.isEmpty()) {
                rep.setFotoTecnomecanicaUrl(uploadService.saveImage(tecnomecanica));
                docsSubidos++;
            }
            if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
                rep.setFotoPerfil(uploadService.saveImage(fotoPerfil));
                docsSubidos++;
            }

            rep.setEstadoVerificacion("EN_REVISION");
            usuarioRepo.save(rep);

            redirect.addFlashAttribute("exito",
                    "✅ " + docsSubidos + " documentos subidos para " + rep.getNombreCompleto()
                    + " (" + rep.getEmail() + "). Estado: EN_REVISIÓN.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error al subir: " + e.getMessage());
        }
        return "redirect:/kyc-repartidor";
    }
}
