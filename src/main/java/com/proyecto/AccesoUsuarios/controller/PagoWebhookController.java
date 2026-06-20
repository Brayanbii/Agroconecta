package com.proyecto.AccesoUsuarios.controller;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;
import com.proyecto.AccesoUsuarios.model.Orden;
import com.proyecto.AccesoUsuarios.repository.OrdenRepository;
import com.proyecto.AccesoUsuarios.service.OrdenEstadoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pagos")
public class PagoWebhookController {

    @Autowired
    private OrdenRepository ordenRepo;

    @Autowired
    private OrdenEstadoService ordenEstadoService;

    /**
     * Webhook de Mercado Pago para confirmar pagos.
     * Segun el tipo de envio:
     *   ECONOMICO → ESPERANDO_AGRUPACION (se acumulan pedidos)
     *   RAPIDO    → BUSCANDO_REPARTIDOR (busca inmediato)
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> recibirNotificacionMP(
            @RequestParam(value = "data.id", required = false) Long dataId,
            @RequestParam(value = "type", required = false) String type) {
        
        try {
            if ("payment".equals(type) && dataId != null) {
                PaymentClient client = new PaymentClient();
                Payment payment = client.get(dataId);
                
                String externalRef = payment.getExternalReference();
                String status = payment.getStatus();

                if (externalRef != null) {
                    Orden orden = ordenRepo.findById(Long.parseLong(externalRef)).orElse(null);
                    
                    if (orden != null) {
                        if ("approved".equals(status)) {
                            ordenEstadoService.procesarPagoAprobado(orden);
                        } else if ("rejected".equals(status)) {
                            orden.setEstado(OrdenEstadoService.RECHAZADO);
                            System.out.println("❌ Pago Rechazado - Orden #" + orden.getNumeroOrden());
                        } else if ("pending".equals(status) || "in_process".equals(status)) {
                            orden.setEstado(OrdenEstadoService.PENDIENTE);
                        }
                        
                        ordenRepo.save(orden);
                    }
                }
            }
            
            return ResponseEntity.status(HttpStatus.OK).body("OK");
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
        }
    }
}
