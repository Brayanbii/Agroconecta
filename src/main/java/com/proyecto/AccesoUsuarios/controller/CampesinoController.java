package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.model.Producto;
import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.ProductoRepository;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import com.proyecto.AccesoUsuarios.repository.DetalleOrdenRepository;
import com.proyecto.AccesoUsuarios.service.UploadFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/campesino/productos")
public class CampesinoController {

    @Autowired
    private ProductoRepository productoRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private DetalleOrdenRepository detalleRepo;

    @Autowired
    private UploadFileService uploadService;

    @GetMapping("/nuevo")
    public String nuevoProducto(Model model) {
        model.addAttribute("producto", new Producto());
        return "campesino_producto_form";
    }

    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute Producto producto, 
                                  @RequestParam("img") MultipartFile file,
                                  Authentication auth) throws IOException {
        
        String email = auth.getName();
        Usuario campesino = usuarioRepo.findByEmail(email).orElseThrow();
        producto.setUsuario(campesino);

        // LÓGICA HÍBRIDA (Archivo vs Link)
        
        // 1. ¿Subió un archivo? (Prioridad Alta)
        if (!file.isEmpty()) {
            String nombreImagen = uploadService.saveImage(file);
            producto.setImagenUrl(nombreImagen);
        } 
        // 2. No subió archivo, ¿pero escribió un Link? (Prioridad Media)
        else {
            // Si es nuevo y no puso link -> Default
            if (producto.getId() == null) {
                if (producto.getImagenUrl() == null || producto.getImagenUrl().isEmpty()) {
                    producto.setImagenUrl("default.jpg");
                }
            } 
            // Si es edición
            else {
                Producto p = productoRepo.findById(producto.getId()).get();
                // Si borró el link y no subió archivo -> Mantenemos la anterior
                if (producto.getImagenUrl() == null || producto.getImagenUrl().isEmpty()) {
                    producto.setImagenUrl(p.getImagenUrl());
                }
                // Si escribió un link nuevo, Spring ya lo asignó automáticamente a 'producto.imagenUrl'
            }
        }

        productoRepo.save(producto);
        return "redirect:/campesino/productos";
    }

    @GetMapping("/editar/{id}")
    public String editarProducto(@PathVariable Long id, Model model) {
        Producto producto = productoRepo.findById(id).orElseThrow();
        model.addAttribute("producto", producto);
        return "campesino_producto_form";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id) {
        Producto p = productoRepo.findById(id).get();
        
        // Solo borramos el archivo si NO es un link de internet y NO es la default
        if (!p.getImagenUrl().startsWith("http") && !"default.jpg".equals(p.getImagenUrl())) {
            uploadService.deleteImage(p.getImagenUrl());
        }
        
        productoRepo.deleteById(id);
        return "redirect:/campesino/productos";
    }

    @GetMapping("/ventas")
    public String misVentas(Model model, Authentication auth) {
        String email = auth.getName();
        Usuario campesino = usuarioRepo.findByEmail(email).orElseThrow();
        model.addAttribute("ventas", detalleRepo.findVentasByCampesino(campesino));
        return "campesino_ventas";
    }
}