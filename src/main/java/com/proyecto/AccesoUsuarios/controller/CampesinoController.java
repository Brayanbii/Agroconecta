package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.model.DetalleOrden;
import com.proyecto.AccesoUsuarios.model.Producto;
import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.ProductoRepository;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import com.proyecto.AccesoUsuarios.repository.DetalleOrdenRepository;
import com.proyecto.AccesoUsuarios.service.PythonService;
import com.proyecto.AccesoUsuarios.service.UploadFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
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

    @Autowired
    private PythonService pythonService;

    @GetMapping("/nuevo")
    public String nuevoProducto(Model model) {
        model.addAttribute("producto", new Producto());

        // --- CONEXIÓN CON PYTHON: Precios de Referencia ---
        Map<String, Object> respuesta = pythonService.obtenerPreciosDesdePython();
        if (respuesta != null) {
            model.addAttribute("preciosReferencia", respuesta.get("data"));
            model.addAttribute("fuentePrecios", respuesta.get("fuente"));
        }
        // --------------------------------------------------

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

        // --- CONEXIÓN CON PYTHON: Precios de Referencia ---
        Map<String, Object> respuesta = pythonService.obtenerPreciosDesdePython();
        if (respuesta != null) {
            model.addAttribute("preciosReferencia", respuesta.get("data"));
            model.addAttribute("fuentePrecios", respuesta.get("fuente"));
        }
        // --------------------------------------------------

        return "campesino_producto_form";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id) {
        Producto p = productoRepo.findById(id).get();

        // 1. Desvincular el producto de sus DetalleOrden para evitar error de FK
        //    (el historial de ventas se preserva: nombre, precio, cantidad, total ya estan guardados)
        List<DetalleOrden> detalles = detalleRepo.findByProducto(p);
        for (DetalleOrden d : detalles) {
            d.setProducto(null);
            detalleRepo.save(d);
        }

        // 2. Solo borramos el archivo si NO es un link de internet y NO es la default
        if (p.getImagenUrl() != null
                && !p.getImagenUrl().startsWith("http")
                && !"default.jpg".equals(p.getImagenUrl())) {
            uploadService.deleteImage(p.getImagenUrl());
        }

        // 3. Borrar el producto
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