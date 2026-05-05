package com.proyecto.AccesoUsuarios.controller;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;
import com.proyecto.AccesoUsuarios.model.Orden;
import com.proyecto.AccesoUsuarios.repository.OrdenRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pagos")
public class PagoWebhookController {

    @Autowired
    private OrdenRepository ordenRepo;

    /**
     * Fase 4: Webhook para confirmar pagos asincrónicamente
     * Mercado Pago enviará un POST a esta URL cada que un pago cambie de estado.
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> recibirNotificacionMP(
            @RequestParam(value = "data.id", required = false) Long dataId,
            @RequestParam(value = "type", required = false) String type) {
        
        try {
            if ("payment".equals(type) && dataId != null) {
                // Instanciar cliente de MP para consultar el pago real por seguridad
                PaymentClient client = new PaymentClient();
                Payment payment = client.get(dataId);
                
                String externalRef = payment.getExternalReference();
                String status = payment.getStatus();

                if (externalRef != null) {
                    Orden orden = ordenRepo.findById(Long.parseLong(externalRef)).orElse(null);
                    
                    if (orden != null) {
                        // Manejo de Estados
                        if ("approved".equals(status)) {
                            orden.setEstado("Aprobado");
                            System.out.println("✅ Pago Aprobado. Dinero recibido para la Orden #" + orden.getNumeroOrden());
                            
                            // Aquí en la vida real es donde se manda el correo de confirmación final
                            // y donde se manda la orden a la cocina/campesino.
                            
                        } else if ("rejected".equals(status)) {
                            orden.setEstado("Rechazado");
                        } else if ("pending".equals(status) || "in_process".equals(status)) {
                            orden.setEstado("Pendiente");
                        }
                        
                        ordenRepo.save(orden);
                    }
                }
            }
            
            // Siempre se debe responder 200 OK rápido para que MP no reintente
            return ResponseEntity.status(HttpStatus.OK).body("Notificación procesada");
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error procesando webhook");
        }
    }
}
