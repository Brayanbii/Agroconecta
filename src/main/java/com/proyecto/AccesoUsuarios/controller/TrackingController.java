package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.model.Orden;
import com.proyecto.AccesoUsuarios.model.Ruta;
import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.OrdenRepository;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import com.proyecto.AccesoUsuarios.service.AuthUsuarioService;
import com.proyecto.AccesoUsuarios.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/tracking")
public class TrackingController {

    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private OrdenRepository ordenRepo;
    @Autowired private AuthUsuarioService authUsuarioService;
    @Autowired private NotificationService notificationService;

    /**
     * Repartidor actualiza su ubicacion GPS
     */
    @PostMapping("/actualizar-ubicacion")
    public ResponseEntity<Map<String, Object>> actualizarUbicacion(
            Authentication auth,
            @RequestBody Map<String, Double> body) {

        Usuario repartidor = authUsuarioService.getAuthenticatedUser(auth);
        if (repartidor == null || !"REPARTIDOR".equals(repartidor.getRol())) {
            return ResponseEntity.ok(Map.of("success", false, "message", "No autorizado"));
        }

        Double lat = body.get("latitud");
        Double lng = body.get("longitud");

        if (lat != null && lng != null) {
            repartidor.setLatitud(lat);
            repartidor.setLongitud(lng);
            repartidor.setFechaUltimaUbicacion(LocalDateTime.now());
            repartidor.setDisponible(true);
            usuarioRepo.save(repartidor);
        }

        return ResponseEntity.ok(Map.of("success", true, "message", "Ubicacion actualizada"));
    }

    /**
     * Repartidor se marca offline
     */
    @PostMapping("/offline")
    public ResponseEntity<Map<String, Object>> marcarOffline(Authentication auth) {
        Usuario repartidor = authUsuarioService.getAuthenticatedUser(auth);
        if (repartidor == null) return ResponseEntity.ok(Map.of("success", false));
        repartidor.setDisponible(false);
        usuarioRepo.save(repartidor);
        return ResponseEntity.ok(Map.of("success", true, "message", "Desconectado"));
    }

    /**
     * Cliente ve ubicacion del repartidor de su orden
     */
    @GetMapping("/orden/{ordenId}")
    public ResponseEntity<Map<String, Object>> trackingOrden(@PathVariable Long ordenId) {
        Map<String, Object> resp = new HashMap<>();
        Orden orden = ordenRepo.findById(ordenId).orElse(null);
        if (orden == null) {
            resp.put("success", false); resp.put("error", "Orden no encontrada");
            return ResponseEntity.ok(resp);
        }

        // Buscar el repartidor via la ruta
        Ruta ruta = orden.getRuta();
        Usuario repartidor = ruta != null ? ruta.getRepartidor() : null;

        resp.put("success", true);
        resp.put("ordenId", orden.getId());
        resp.put("estado", orden.getEstado());
        resp.put("direccionEnvio", orden.getDireccionEnvio());

        if (repartidor != null) {
            Map<String, Object> repData = new HashMap<>();
            repData.put("id", repartidor.getId());
            repData.put("nombre", repartidor.getNombreCompleto());
            repData.put("latitud", repartidor.getLatitud());
            repData.put("longitud", repartidor.getLongitud());
            repData.put("fechaUltimaUbicacion",
                repartidor.getFechaUltimaUbicacion() != null ?
                    repartidor.getFechaUltimaUbicacion().toString() : null);
            resp.put("repartidor", repData);

            // Si esta cerca del destino, notificar
            if (repartidor.getLatitud() != null && orden.getLatitudEnvio() != null) {
                double distancia = calcularDistancia(
                    repartidor.getLatitud(), repartidor.getLongitud(),
                    orden.getLatitudEnvio(), orden.getLongitudEnvio());
                resp.put("distanciaKm", Math.round(distancia * 10.0) / 10.0);

                if (distancia < 2.0) {
                    notificationService.notificarClienteProximoALlegar(orden);
                }
            }
        }

        resp.put("destinoLat", orden.getLatitudEnvio());
        resp.put("destinoLng", orden.getLongitudEnvio());

        return ResponseEntity.ok(resp);
    }

    private double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }
}
