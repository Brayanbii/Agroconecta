package com.proyecto.AccesoUsuarios.service;

import org.springframework.stereotype.Service;

@Service
public class EnvioService {

    // Constantes de calculo de envio
    private static final double BASE_ECONOMICO = 3500.0;
    private static final double FACTOR_KM_ECONOMICO = 50.0;
    private static final double FACTOR_KG_ECONOMICO = 30.0;

    private static final double BASE_RAPIDO = 8000.0;
    private static final double FACTOR_KM_RAPIDO = 100.0;
    private static final double FACTOR_KG_RAPIDO = 80.0;

    // Tarifas de la plataforma segun la "Matematica del Exito"
    private static final double TARIFA_PLATAFORMA = 0.10; // 10%
    private static final double TARIFA_PASARELA = 0.05;   // 5%

    /**
     * Calcula el costo de envio basado en distancia, peso y tipo de entrega
     */
    public double calcularCostoEnvio(double distanciaKm, double pesoKg, String tipoEnvio) {
        if ("RAPIDO".equalsIgnoreCase(tipoEnvio)) {
            return BASE_RAPIDO + (distanciaKm * FACTOR_KM_RAPIDO) + (pesoKg * FACTOR_KG_RAPIDO);
        }
        // ECONOMICO por defecto
        return BASE_ECONOMICO + (distanciaKm * FACTOR_KM_ECONOMICO) + (pesoKg * FACTOR_KG_ECONOMICO);
    }

    /**
     * Calcula la distancia en km entre dos coordenadas GPS (Formula de Haversine)
     */
    public double calcularDistanciaKm(double lat1, double lon1, double lat2, double lon2) {
        final int RADIO_TIERRA_KM = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distancia = RADIO_TIERRA_KM * c;
        return Math.round(distancia * 10.0) / 10.0; // redondear a 1 decimal
    }

    /**
     * Calcula el desglose completo de la orden segun la "Matematica del Exito"
     * Campesino 75% | Delivery 10% | Plataforma 10% | Pasarela 5%
     */
    public DesglosePago calcularDesglose(double subtotalProductos, double costoEnvio) {
        double tarifaPlataforma = Math.round(subtotalProductos * TARIFA_PLATAFORMA * 100.0) / 100.0;
        double costoPasarela = Math.round(subtotalProductos * TARIFA_PASARELA * 100.0) / 100.0;
        double total = subtotalProductos + costoEnvio + tarifaPlataforma + costoPasarela;

        double pagoCampesino = Math.round(subtotalProductos * 0.75 * 100.0) / 100.0;
        double pagoDelivery = Math.round(costoEnvio * 100.0) / 100.0;
        double gananciaPlataforma = Math.round(tarifaPlataforma * 100.0) / 100.0;

        return new DesglosePago(
            total, subtotalProductos, costoEnvio,
            tarifaPlataforma, costoPasarela,
            pagoCampesino, pagoDelivery, gananciaPlataforma
        );
    }

    public static class DesglosePago {
        public final double total;
        public final double subtotalProductos;
        public final double costoEnvio;
        public final double tarifaPlataforma;
        public final double costoPasarela;
        public final double pagoCampesino;
        public final double pagoDelivery;
        public final double gananciaPlataforma;

        public DesglosePago(double total, double subtotal, double envio,
                           double tarifa, double pasarela,
                           double campesino, double delivery, double plataforma) {
            this.total = total;
            this.subtotalProductos = subtotal;
            this.costoEnvio = envio;
            this.tarifaPlataforma = tarifa;
            this.costoPasarela = pasarela;
            this.pagoCampesino = campesino;
            this.pagoDelivery = delivery;
            this.gananciaPlataforma = plataforma;
        }
    }
}
