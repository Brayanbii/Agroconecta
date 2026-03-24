package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.model.Orden;
import com.proyecto.AccesoUsuarios.repository.OrdenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/pedidos")
public class PedidoController {

    @Autowired
    private OrdenRepository ordenRepo;

    // Ver todos los pedidos (Admin)
    @GetMapping
    public String listarPedidos(Model model) {
        model.addAttribute("pedidos", ordenRepo.findAll());
        return "admin_pedidos";
    }

    // Cambiar estado (ej: de Pendiente a Enviado)
    @PostMapping("/cambiar-estado")
    public String cambiarEstado(@RequestParam Long id, @RequestParam String estado) {
        Orden orden = ordenRepo.findById(id).orElseThrow();
        orden.setEstado(estado);
        ordenRepo.save(orden);
        return "redirect:/admin/pedidos";
    }
}