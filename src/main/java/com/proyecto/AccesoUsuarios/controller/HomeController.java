package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private ProductoRepository productoRepo;

    // Página de Inicio Pública (Landing Page)
    @GetMapping("/")
    public String index(Model model) {
        // Mostramos algunos productos destacados (los primeros 4, por ejemplo)
        model.addAttribute("productos", productoRepo.findAll().stream().limit(4).toList());
        return "index";
    }
}