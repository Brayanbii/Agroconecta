package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.model.Orden;
import com.proyecto.AccesoUsuarios.repository.OrdenRepository;
import com.proyecto.AccesoUsuarios.service.OrdenEstadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/pedidos")
public class PedidoController {

    @Autowired
    private OrdenRepository ordenRepo;

    @Autowired
    private OrdenEstadoService ordenEstadoService;

    // Ver todos los pedidos (Admin) con desglose de delivery
    @GetMapping
    public String listarPedidos(Model model) {
        model.addAttribute("pedidos", ordenRepo.findAll());
        model.addAttribute("estadoService", ordenEstadoService);
        model.addAttribute("todosLosEstados", ordenEstadoService.getTodosLosEstados());
        return "admin_pedidos";
    }

    // Cambiar estado
    @PostMapping("/cambiar-estado")
    public String cambiarEstado(@RequestParam Long id, @RequestParam String estado) {
        Orden orden = ordenRepo.findById(id).orElseThrow();
        if (ordenEstadoService.puedeTransicionar(orden, estado)) {
            orden.setEstado(estado);
            ordenRepo.save(orden);
        }
        return "redirect:/admin/pedidos";
    }
}