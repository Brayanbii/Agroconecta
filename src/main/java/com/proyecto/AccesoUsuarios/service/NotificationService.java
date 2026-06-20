package com.proyecto.AccesoUsuarios.service;

import com.proyecto.AccesoUsuarios.model.Notificacion;
import com.proyecto.AccesoUsuarios.model.Orden;
import com.proyecto.AccesoUsuarios.model.Ruta;
import com.proyecto.AccesoUsuarios.repository.NotificacionRepository;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Servicio de notificaciones.
 * Guarda en BD y loguea en consola.
 * Para Firebase FCM: agregar firebase-admin al pom.xml y configurar credenciales.
 */
@Service
public class NotificationService {

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private NotificacionRepository notificacionRepo;

    private void guardar(Long usuarioId, String titulo, String mensaje, String tipo) {
        try {
            Notificacion n = new Notificacion();
            n.setUsuarioId(usuarioId);
            n.setTitulo(titulo);
            n.setMensaje(mensaje);
            n.setTipo(tipo);
            n.setLeida(false);
            n.setFechaCreacion(LocalDateTime.now());
            notificacionRepo.save(n);
        } catch (Exception ignored) {}
    }

    // ═══════════ NOTIFICACIONES PARA CAMPESINOS ═══════════

    /**
     * Se notifica al cliente cuando su pedido fue rechazado por el campesino
     */
    public void notificarClientePedidoRechazado(Orden orden) {
        System.out.println("📢 [NOTIFICACION CLIENTE - RECHAZADO]");
        System.out.println("   Para: " + (orden.getUsuario() != null ? orden.getUsuario().getEmail() : "cliente"));
        System.out.println("   Titulo: Pedido rechazado por el productor");
        System.out.println("   Mensaje: Orden #" + orden.getNumeroOrden() +
            " fue rechazada. Te reembolsaremos el dinero.");
        // TODO: Firebase FCM a cliente
    }

    /**
     * Se notifica al campesino que tiene un pedido pendiente de aceptacion
     */
    public void notificarCampesinoPedidoPendiente(Orden orden) {
        if (orden.getDetalles() == null || orden.getDetalles().isEmpty()) return;
        String productoInfo = orden.getDetalles().get(0).getNombre() +
            " (" + orden.getDetalles().get(0).getCantidad() + " " +
            (orden.getDetalles().get(0).getProducto() != null ?
                orden.getDetalles().get(0).getProducto().getUnidad() : "uds") + ")";
        System.out.println("📢 [NOTIFICACION CAMPESINO - NUEVO PEDIDO]");
        System.out.println("   Titulo: Nuevo pedido pendiente!");
        System.out.println("   Mensaje: Tienes una compra de " + productoInfo +
            ". Acepta o rechaza el pedido. Tu ganancia: $" +
            String.format("%.0f", orden.getSubtotalProductos() != null ?
                orden.getSubtotalProductos() * 0.75 : 0));
        System.out.println("   Orden: #" + orden.getNumeroOrden());
    }

    /**
     * Se notifica al campesino cuando su producto fue entregado
     */
    public void notificarCampesinoEntregaExitosa(Orden orden) {
        double ganancia = orden.getSubtotalProductos() != null ?
            orden.getSubtotalProductos() * 0.75 : 0;

        System.out.println("📢 [NOTIFICACION CAMPESINO - ENTREGA]");
        System.out.println("   Titulo: Producto entregado con exito!");
        System.out.println("   Mensaje: Orden #" + orden.getNumeroOrden() +
            " entregada. +$" + String.format("%.0f", ganancia) + " en tu billetera.");
    }

    // ═══════════ NOTIFICACIONES PARA REPARTIDORES ═══════════

    /**
     * Se notifica a los repartidores cercanos cuando hay una ruta disponible
     */
    public void notificarRepartidoresRutaDisponible(Ruta ruta) {
        System.out.println("📢 [NOTIFICACION REPARTIDORES]");
        System.out.println("   Titulo: Nueva ruta disponible!");
        System.out.println("   Zona Origen: " + ruta.getZonaOrigen());
        System.out.println("   Zona Destino: " + ruta.getZonaDestino());
        System.out.println("   Pedidos: " + ruta.getPedidosCount());
        System.out.println("   Peso: " + String.format("%.1f", ruta.getPesoTotalKg()) + " kg");
        System.out.println("   Pago estimado: $" + String.format("%.0f", ruta.getPagoTotalEstimado()));
        System.out.println("   Ruta: #" + ruta.getCodigoRuta());

        // TODO: Firebase FCM - enviar push a repartidores en radio de 50km
        // List<Usuario> repartidores = usuarioRepo.findByRolAndDisponible("REPARTIDOR", true);
        // for (Usuario r : repartidores) {
        //     firebaseService.sendToUser(r.getTokenFCM(), titulo, mensaje, data);
        // }
    }

    /**
     * Se notifica al repartidor que acepto una ruta
     */
    public void notificarRepartidorRutaAsignada(Orden orden) {
        System.out.println("📢 [NOTIFICACION REPARTIDOR - RUTA ASIGNADA]");
        System.out.println("   Titulo: Ruta aceptada con exito!");
        System.out.println("   Orden: #" + orden.getNumeroOrden());
    }

    // ═══════════ NOTIFICACIONES PARA CLIENTES ═══════════

    /**
     * Se notifica al cliente cuando su pedido esta en camino
     */
    public void notificarClienteEnCamino(Orden orden) {
        System.out.println("📢 [NOTIFICACION CLIENTE]");
        System.out.println("   Para: " + orden.getUsuario().getEmail());
        System.out.println("   Titulo: Tu pedido esta en camino!");
        System.out.println("   Mensaje: Orden #" + orden.getNumeroOrden() +
            " esta en camino. Llegara pronto a " + orden.getDireccionEnvio());
    }

    /**
     * Se notifica al cliente con el PIN de entrega cuando el repartidor va hacia el
     */
    public void notificarClientePinEntrega(Orden orden, String pin) {
        String email = orden.getUsuario() != null ? orden.getUsuario().getEmail() : "cliente";
        Long clienteId = orden.getUsuario() != null ? orden.getUsuario().getId() : null;
        String titulo = "Tu repartidor viene en camino! Ten listo tu PIN";
        String mensaje = "Orden #" + orden.getNumeroOrden() +
            " - Cuando el repartidor llegue a " + orden.getDireccionEnvio() +
            ", dictale este codigo: " + pin;

        System.out.println("📢 [NOTIFICACION CLIENTE - PIN ENTREGA]");
        System.out.println("   Para: " + email);
        System.out.println("   Titulo: " + titulo);
        System.out.println("   Mensaje: " + mensaje);
        System.out.println("   PIN: " + pin);

        if (clienteId != null) {
            guardar(clienteId, titulo, mensaje, "PIN_ENTREGA");
        }
    }

    /**
     * Se notifica al cliente cuando el repartidor esta cerca
     */
    public void notificarClienteProximoALlegar(Orden orden) {
        System.out.println("📢 [NOTIFICACION CLIENTE - CERCA]");
        System.out.println("   Para: " + orden.getUsuario().getEmail());
        System.out.println("   Titulo: Tu repartidor esta a 5 minutos!");
        System.out.println("   Orden: #" + orden.getNumeroOrden());
    }

    /**
     * Se notifica al cliente cuando su pedido fue entregado
     */
    public void notificarClienteEntregaExitosa(Orden orden) {
        System.out.println("📢 [NOTIFICACION CLIENTE - ENTREGADO]");
        System.out.println("   Para: " + orden.getUsuario().getEmail());
        System.out.println("   Titulo: Pedido entregado exitosamente!");
        System.out.println("   Mensaje: Orden #" + orden.getNumeroOrden() +
            " fue entregada. Califica tu experiencia.");
    }
}
