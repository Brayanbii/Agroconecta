package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.model.MensajeSoporte;
import com.proyecto.AccesoUsuarios.model.TicketSoporte;
import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.MensajeSoporteRepository;
import com.proyecto.AccesoUsuarios.repository.TicketSoporteRepository;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class SoporteController {

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private TicketSoporteRepository ticketRepo;

    @Autowired
    private MensajeSoporteRepository mensajeRepo;

    // ==========================================
    // 1. DASHBOARD PARA EL ROL 'SOPORTE'
    // ==========================================
    @GetMapping("/soporte/dashboard")
    public String dashboardSoporte(Model model, Authentication auth) {
        // Cargar todos los tickets
        List<TicketSoporte> tickets = ticketRepo.findAllByOrderByFechaActualizacionDesc();
        model.addAttribute("tickets", tickets);

        // Contadores
        long abiertos = tickets.stream().filter(t -> "ABIERTO".equals(t.getEstado())).count();
        long enProgreso = tickets.stream().filter(t -> "EN_PROGRESO".equals(t.getEstado())).count();
        long cerrados = tickets.stream().filter(t -> "CERRADO".equals(t.getEstado())).count();
        long escalados = tickets.stream().filter(t -> "ESCALADO".equals(t.getEstado())).count();
        
        // Simular tickets urgentes (Abiertos hace más de 24h)
        long urgentes = tickets.stream()
            .filter(t -> "ABIERTO".equals(t.getEstado()) && t.getFechaCreacion().isBefore(java.time.LocalDateTime.now().minusHours(24)))
            .count();

        // Encontrar usuario problemático (el que más tickets tiene)
        Map<String, Long> userTicketCount = tickets.stream()
            .collect(Collectors.groupingBy(t -> t.getUsuario().getNombreCompleto(), Collectors.counting()));
        String usuarioProblematico = userTicketCount.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("N/A");

        model.addAttribute("abiertosCount", abiertos);
        model.addAttribute("enProgresoCount", enProgreso);
        model.addAttribute("cerradosCount", cerrados);
        model.addAttribute("escaladosCount", escalados);
        model.addAttribute("urgentesCount", urgentes);
        model.addAttribute("usuarioProblematico", usuarioProblematico);
        
        return "soporte_dashboard";
    }

    @Autowired
    private com.proyecto.AccesoUsuarios.repository.OrdenRepository ordenRepo;

    @GetMapping("/soporte/ticket/{id}")
    public String verTicket(@PathVariable Long id, Model model) {
        TicketSoporte ticket = ticketRepo.findById(id).orElseThrow();
        Usuario cliente = ticket.getUsuario();
        
        model.addAttribute("ticket", ticket);
        model.addAttribute("mensajes", mensajeRepo.findByTicketOrderByFechaEnvioAsc(ticket));
        
        // Contexto adicional del cliente para el agente de soporte
        model.addAttribute("ultimasCompras", ordenRepo.findTop5ByUsuarioOrderByFechaCreacionDesc(cliente));
        
        return "soporte_ticket_detalle";
    }

    @PostMapping("/soporte/ticket/{id}/responder")
    public String responderTicketSoporte(@PathVariable Long id, @RequestParam String mensaje, Authentication auth) {
        TicketSoporte ticket = ticketRepo.findById(id).orElseThrow();
        Usuario agente = usuarioRepo.findByEmail(auth.getName()).orElseThrow();

        if ("ABIERTO".equals(ticket.getEstado())) {
            ticket.setEstado("EN_PROGRESO");
            ticketRepo.save(ticket);
        }

        MensajeSoporte nuevoMensaje = new MensajeSoporte();
        nuevoMensaje.setTicket(ticket);
        nuevoMensaje.setRemitente(agente);
        nuevoMensaje.setContenido(mensaje);
        mensajeRepo.save(nuevoMensaje);

        // Actualizar fecha del ticket
        ticket.setFechaActualizacion(java.time.LocalDateTime.now());
        ticketRepo.save(ticket);

        return "redirect:/soporte/ticket/" + id;
    }

    @PostMapping("/soporte/ticket/{id}/estado")
    public String cambiarEstadoTicket(@PathVariable Long id, @RequestParam String estado) {
        TicketSoporte ticket = ticketRepo.findById(id).orElseThrow();
        ticket.setEstado(estado);
        ticketRepo.save(ticket);
        return "redirect:/soporte/ticket/" + id;
    }


    @PostMapping("/soporte/ticket/{id}/accion")
    public String ejecutarAccionEspecial(@PathVariable Long id, @RequestParam String accion, Authentication auth) {
        TicketSoporte ticket = ticketRepo.findById(id).orElseThrow();
        Usuario cliente = ticket.getUsuario();
        
        String mensajeSistema = "";

        switch (accion) {
            case "ESCALAR":
                ticket.setEstado("ESCALADO");
                mensajeSistema = "⚠️ EL TICKET HA SIDO ESCALADO A UN SUPERVISOR O ÁREA TÉCNICA.";
                break;
            case "SUSPENDER":
                cliente.setEstadoVerificacion("VETADO");
                usuarioRepo.save(cliente);
                mensajeSistema = "⛔ LA CUENTA DEL USUARIO HA SIDO SUSPENDIDA TEMPORALMENTE POR SOPORTE.";
                ticket.setEstado("CERRADO");
                break;
            case "REEMBOLSAR":
                mensajeSistema = "💸 SE HA AUTORIZADO EL REEMBOLSO AL USUARIO. El proceso bancario puede tardar hasta 72h.";
                ticket.setEstado("CERRADO");
                break;
        }

        if (!mensajeSistema.isEmpty()) {
            MensajeSoporte sysMsg = new MensajeSoporte();
            sysMsg.setTicket(ticket);
            sysMsg.setRemitente(null); // Sistema
            sysMsg.setContenido(mensajeSistema);
            mensajeRepo.save(sysMsg);
            
            ticket.setFechaActualizacion(java.time.LocalDateTime.now());
            ticketRepo.save(ticket);
        }

        return "redirect:/soporte/ticket/" + id;
    }

    // ==========================================
    // 2. API REST PARA EL WIDGET DE CHAT (CLIENTES/CAMPESINOS)
    // ==========================================

    @GetMapping("/api/soporte/mis-tickets")
    @ResponseBody
    public ResponseEntity<?> getMisTickets(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).body("No autenticado");
        Usuario usuario = usuarioRepo.findByEmail(auth.getName()).orElse(null);
        if (usuario == null) return ResponseEntity.status(401).build();

        List<TicketSoporte> tickets = ticketRepo.findByUsuarioOrderByFechaActualizacionDesc(usuario);
        
        // Mapear a JSON seguro
        List<Map<String, Object>> result = tickets.stream().map(t -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", t.getId());
            map.put("asunto", t.getAsunto());
            map.put("estado", t.getEstado());
            map.put("tipo", t.getTipo());
            map.put("prioridad", t.getPrioridad());
            map.put("fecha", t.getFechaActualizacion().toString());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/api/soporte/crear-ticket")
    @ResponseBody
    public ResponseEntity<?> crearTicket(
            @RequestParam String asunto, 
            @RequestParam String mensaje, 
            @RequestParam(defaultValue = "PETICION") String tipo,
            @RequestParam(defaultValue = "BAJA") String prioridad,
            Authentication auth) {
        
        if (auth == null) return ResponseEntity.status(401).build();
        Usuario usuario = usuarioRepo.findByEmail(auth.getName()).orElse(null);
        if (usuario == null) return ResponseEntity.status(401).build();

        // 1. Crear el ticket
        TicketSoporte ticket = new TicketSoporte();
        ticket.setUsuario(usuario);
        ticket.setAsunto(asunto);
        ticket.setEstado("ABIERTO");
        ticket.setTipo(tipo.toUpperCase());
        ticket.setPrioridad(prioridad.toUpperCase());
        ticket = ticketRepo.save(ticket);

        // 2. Guardar el primer mensaje del usuario
        MensajeSoporte msg = new MensajeSoporte();
        msg.setTicket(ticket);
        msg.setRemitente(usuario);
        msg.setContenido(mensaje);
        mensajeRepo.save(msg);

        // 3. Respuesta automática del sistema (AgroBot)
        String respuestaBot = "";
        switch (ticket.getTipo()) {
            case "QUEJA":
            case "RECLAMO":
                respuestaBot = "Hola " + usuario.getNombreCompleto() + ". Lamentamos mucho los inconvenientes que estás experimentando. Hemos marcado tu caso como " + ticket.getTipo() + " con prioridad " + ticket.getPrioridad() + ". Un asesor especializado de AgroSoporte lo atenderá lo más pronto posible.";
                break;
            case "SUGERENCIA":
                respuestaBot = "¡Hola " + usuario.getNombreCompleto() + "! Gracias por ayudarnos a mejorar AgroConecta. Hemos recibido tu sugerencia y la enviaremos al equipo de desarrollo.";
                break;
            case "TECNICO":
                respuestaBot = "Hola " + usuario.getNombreCompleto() + ". Hemos registrado tu incidencia técnica. Si puedes, ten lista cualquier captura de pantalla. Nuestro equipo de soporte técnico se pondrá en contacto pronto.";
                break;
            default:
                respuestaBot = "Hola " + usuario.getNombreCompleto() + ". Hemos recibido tu petición. Un agente de soporte se pondrá en contacto contigo. Nuestros horarios son de Lunes a Viernes de 8am a 6pm.";
                break;
        }

        MensajeSoporte autoMsg = new MensajeSoporte();
        autoMsg.setTicket(ticket);
        autoMsg.setRemitente(null); // Sistema
        autoMsg.setContenido(respuestaBot);
        mensajeRepo.save(autoMsg);

        Map<String, Object> response = new HashMap<>();
        response.put("ticketId", ticket.getId());
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/soporte/ticket/{id}/mensajes")
    @ResponseBody
    public ResponseEntity<?> getMensajes(@PathVariable Long id, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        Usuario usuario = usuarioRepo.findByEmail(auth.getName()).orElse(null);
        
        TicketSoporte ticket = ticketRepo.findById(id).orElse(null);
        if (ticket == null || !ticket.getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.status(403).build(); // No autorizado para ver este ticket
        }

        List<MensajeSoporte> mensajes = mensajeRepo.findByTicketOrderByFechaEnvioAsc(ticket);
        
        List<Map<String, Object>> result = mensajes.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("contenido", m.getContenido());
            map.put("fecha", m.getFechaEnvio().toString());
            map.put("esMio", m.getRemitente() != null && m.getRemitente().getId().equals(usuario.getId()));
            map.put("remitente", m.getRemitente() != null ? (m.getRemitente().getRol().equals("SOPORTE") ? "Agente de Soporte" : m.getRemitente().getNombreCompleto()) : "Sistema");
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/api/soporte/ticket/{id}/enviar")
    @ResponseBody
    public ResponseEntity<?> enviarMensaje(@PathVariable Long id, @RequestParam String mensaje, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        Usuario usuario = usuarioRepo.findByEmail(auth.getName()).orElse(null);
        
        TicketSoporte ticket = ticketRepo.findById(id).orElse(null);
        if (ticket == null || !ticket.getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.status(403).build();
        }

        MensajeSoporte msg = new MensajeSoporte();
        msg.setTicket(ticket);
        msg.setRemitente(usuario);
        msg.setContenido(mensaje);
        mensajeRepo.save(msg);

        ticket.setFechaActualizacion(java.time.LocalDateTime.now());
        ticketRepo.save(ticket);

        return ResponseEntity.ok(Map.of("status", "success"));
    }

    // ==========================================
    // 3. API DE DISPONIBILIDAD DE SOPORTE
    // ==========================================

    /**
     * Endpoint público para que el widget del cliente sepa si hay un agente en línea.
     */
    @GetMapping("/api/soporte/disponibilidad")
    @ResponseBody
    public ResponseEntity<?> checkDisponibilidad() {
        long agentesOnline = usuarioRepo.countByRolAndDisponibleSoporte("SOPORTE", true);
        Map<String, Object> result = new HashMap<>();
        result.put("online", agentesOnline > 0);
        result.put("agentes", agentesOnline);
        return ResponseEntity.ok(result);
    }

    /**
     * Endpoint para que el agente de soporte cambie su estado a online/offline.
     */
    @PostMapping("/soporte/toggle-disponibilidad")
    @ResponseBody
    public ResponseEntity<?> toggleDisponibilidad(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        Usuario agente = usuarioRepo.findByEmail(auth.getName()).orElse(null);
        if (agente == null || !"SOPORTE".equals(agente.getRol())) {
            return ResponseEntity.status(403).build();
        }

        // Toggle el estado
        Boolean nuevoEstado = !Boolean.TRUE.equals(agente.getDisponibleSoporte());
        agente.setDisponibleSoporte(nuevoEstado);
        usuarioRepo.save(agente);

        return ResponseEntity.ok(Map.of("disponible", nuevoEstado));
    }

    /**
     * Endpoint para que el widget obtenga el nombre real del usuario logueado.
     */
    @GetMapping("/api/soporte/mi-info")
    @ResponseBody
    public ResponseEntity<?> getMiInfo(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        Usuario usuario = usuarioRepo.findByEmail(auth.getName()).orElse(null);
        if (usuario == null) return ResponseEntity.status(401).build();

        Map<String, Object> result = new HashMap<>();
        result.put("nombre", usuario.getNombreCompleto());
        result.put("rol", usuario.getRol());
        result.put("disponible", usuario.getDisponibleSoporte() != null ? usuario.getDisponibleSoporte() : false);
        return ResponseEntity.ok(result);
    }
}
