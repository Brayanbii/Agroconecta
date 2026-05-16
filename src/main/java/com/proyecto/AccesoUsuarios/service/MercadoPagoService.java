package com.proyecto.AccesoUsuarios.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import com.proyecto.AccesoUsuarios.model.ItemCarrito;

import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class MercadoPagoService {

    // Token simulado entregado para el sandbox
    private String accessToken = "TEST-858847077141020-041322-81b6fc80e7d9503c2a5b2b4616aaaffb-741531223";

    @PostConstruct
    public void init() {
        System.out.println("⏳ Configurando Mercado Pago SDK v2...");
        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            System.out.println("✅ Mercado Pago SDK inicializado con Token Sandbox");
        } catch (Exception e) {
            System.err.println("❌ Error al inicializar Mercado Pago: " + e.getMessage());
        }
    }

    /**
     * Fase 2: Construir la Preferencia (Intención de cobro)
     * En este paso simulamos la lógica Marketplace: El total se cobra, 
     * y especificamos un Marketplace Fee simulado.
     */
    public Preference crearPreferenciaDePago(List<ItemCarrito> itemsCarrito, String serverUrl, String ordenId, Double tarifaServicio, Double propina) throws Exception {
        
        List<PreferenceItemRequest> itemsRequest = new ArrayList<>();
        
        // 1. Mapear los CarritoItems de AgroConecta a PreferenceItems de Mercado Pago
        for(ItemCarrito item : itemsCarrito) {
            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                .id(item.getProducto().getId().toString())
                .title(item.getProducto().getNombre())
                .description("Producto del campo colombiano")
                .quantity(item.getCantidad())
                .currencyId("COP")
                .unitPrice(new BigDecimal(item.getProducto().getPrecio().toString()))
                .build();
            itemsRequest.add(itemRequest);
        }

        // Agregar envío (Simulado como un item extra logístico)
        PreferenceItemRequest envioRequest = PreferenceItemRequest.builder()
            .id("ENVIO-001")
            .title("Costo de Envío")
            .quantity(1)
            .currencyId("COP")
            .unitPrice(new BigDecimal("3500"))
            .build();
        itemsRequest.add(envioRequest);

        if (tarifaServicio != null && tarifaServicio > 0) {
            itemsRequest.add(PreferenceItemRequest.builder()
                .id("TARIFA-001")
                .title("Tarifa de Servicio")
                .quantity(1)
                .currencyId("COP")
                .unitPrice(new BigDecimal(tarifaServicio.intValue()))
                .build());
        }

        if (propina != null && propina > 0) {
            itemsRequest.add(PreferenceItemRequest.builder()
                .id("PROPINA-001")
                .title("Propina Campesino")
                .quantity(1)
                .currencyId("COP")
                .unitPrice(new BigDecimal(propina.intValue()))
                .build());
        }

        // 2. Determinar URLs de Retorno (Redirección FrontEnd Fase 3)
        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
            .success(serverUrl + "/orden/success")
            .pending(serverUrl + "/orden/pending")
            .failure(serverUrl + "/orden/failure")
            .build();

        // 3. Simular Marketplace Fee
        // En una app real de MP, marketplace_fee define cuánto nos quedamos y el resto va al vendor.
        // Aquí tomamos $2200 COP de comisión + propinas si las hubiera.
        // NOTA: Comentado porque en cuentas Sandbox sin configurar Marketplace explícitamente, lanza error 400.
        // BigDecimal comisionAgroconecta = new BigDecimal("2200"); 

        // Ensamblar Petición y mandar a Servidores de Mercado Pago
        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
            .items(itemsRequest)
            .backUrls(backUrls)
            // .autoReturn("approved") // Autoredireccionar si fue aprobado (Falla localmente con localhost)
            // .marketplaceFee(comisionAgroconecta) 
            .externalReference(ordenId) // <--- CONEXIÓN CON NUESTRA BASE DE DATOS
            .statementDescriptor("AGROCONECTA")
            .build();

        PreferenceClient client = new PreferenceClient();
        return client.create(preferenceRequest);
    }
}
