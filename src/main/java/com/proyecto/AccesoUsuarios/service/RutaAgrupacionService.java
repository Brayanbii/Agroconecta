package com.proyecto.AccesoUsuarios.service;

import com.proyecto.AccesoUsuarios.model.Orden;
import com.proyecto.AccesoUsuarios.model.Ruta;
import com.proyecto.AccesoUsuarios.repository.OrdenRepository;
import com.proyecto.AccesoUsuarios.repository.RutaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 🤖 Robot Agrupador de AgroConecta
 * Se ejecuta automaticamente cada 10 minutos.
 * Agrupa pedidos ECONOMICOS en rutas optimizadas por zona destino.
 */
@Service
public class RutaAgrupacionService {

    @Autowired
    private OrdenRepository ordenRepo;

    @Autowired
    private RutaRepository rutaRepo;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UnidadConversionService conversionService;

    // Umbrales de salida
    private static final double UMBRAL_PESO_KG = 100.0;    // Salir si hay >= 100kg
    private static final double UMBRAL_PESO_TARDE = 30.0;  // Salir tarde si hay >= 30kg
    private static final int HORA_SALIDA_TARDE = 18;        // 6 PM
    private static final int HORAS_MAX_ESPERA = 24;        // Maximo 24h para forzar salida

    /**
     * 🔄 Robot principal: se ejecuta cada 10 minutos
     */
    @Scheduled(fixedRate = 600000)
    @Transactional
    public void agruparPedidos() {
        System.out.println("═══════════════════════════════════════");
        System.out.println("🤖 [ROBOT AGRUPADOR] Iniciando ciclo...");
        System.out.println("═══════════════════════════════════════");

        // 1. Buscar pedidos ECONOMICOS que esten ESPERANDO_AGRUPACION
        List<Orden> pedidosPendientes = ordenRepo.findByEstado(OrdenEstadoService.ESPERANDO_AGRUPACION);
        pedidosPendientes = pedidosPendientes.stream()
            .filter(o -> "ECONOMICO".equalsIgnoreCase(o.getTipoEnvio()))
            .collect(Collectors.toList());

        if (pedidosPendientes.isEmpty()) {
            System.out.println("🤖 No hay pedidos esperando agrupacion.");
            System.out.println("═══════════════════════════════════════\n");
            return;
        }

        System.out.println("🤖 Pedidos encontrados: " + pedidosPendientes.size());

        // 2. Agrupar por zona de RECOGIDA (origen) + DESTINO
        Map<String, List<Orden>> pedidosPorZona = pedidosPendientes.stream()
            .filter(o -> o.getDireccionEnvio() != null)
            .collect(Collectors.groupingBy(o ->
                // Clave compuesta: "Origen>Destino"
                extraerZonaOrigenSimple(o) + ">" + extraerZonaDestino(o)
            ));

        System.out.println("🤖 Zonas de recogida+entrega detectadas: " + pedidosPorZona.size());

        // 3. Para cada zona (origen>destino), crear o actualizar una Ruta
        int rutasCreadas = 0;
        int rutasActualizadas = 0;
        int rutasListas = 0;

        for (Map.Entry<String, List<Orden>> entry : pedidosPorZona.entrySet()) {
            String zonaCompleta = entry.getKey();
            String[] partes = zonaCompleta.split(">", 2);
            String zonaOrigen = partes.length > 0 ? partes[0] : "Sin zona";
            String zonaDestino = partes.length > 1 ? partes[1] : zonaCompleta;
            List<Orden> pedidosZona = entry.getValue();

            // Buscar si ya existe una ruta FORMANDOSE para esta zona de origen+destino
            List<Ruta> rutasExistentes = rutaRepo.findByEstadoAndZonaDestino("FORMANDOSE", zonaDestino);
            Ruta ruta = rutasExistentes.stream()
                .filter(rt -> zonaOrigen.equals(rt.getZonaOrigen()))
                .findFirst().orElse(null);

            if (ruta != null) {
                rutasActualizadas++;
                System.out.println("🤖 Ruta existente #" + ruta.getCodigoRuta() + " - Agregando " + pedidosZona.size() + " pedidos");
            } else {
                ruta = new Ruta();
                ruta.setCodigoRuta("RUTA-" + LocalDateTime.now().getYear() + "-" +
                    String.format("%03d", rutaRepo.count() + 1));
                ruta.setZonaDestino(zonaDestino);
                ruta.setZonaOrigen(zonaOrigen);
                ruta.setEstado("FORMANDOSE");
                ruta.setFechaCreacion(LocalDateTime.now());
                ruta.setFechaLimite(LocalDateTime.now().plusHours(HORAS_MAX_ESPERA));
                ruta.setForzarSalida(false);
                rutaRepo.save(ruta);
                rutasCreadas++;
                System.out.println("🤖 Nueva ruta: #" + ruta.getCodigoRuta() +
                    " | Recoger: " + zonaOrigen + " | Entregar: " + zonaDestino);
            }

            // Agregar pedidos a la ruta (con conversion de unidades a kg)
            for (Orden orden : pedidosZona) {
                if (orden.getRuta() == null) {
                    // Recalcular peso real usando conversion de unidades
                    double pesoRealKg = 0;
                    if (orden.getDetalles() != null) {
                        for (com.proyecto.AccesoUsuarios.model.DetalleOrden d : orden.getDetalles()) {
                            pesoRealKg += conversionService.calcularPesoDetalle(d);
                        }
                    }
                    // Si no se pudo calcular, usar el peso guardado o 5kg default
                    if (pesoRealKg <= 0) {
                        pesoRealKg = orden.getPesoTotalKg() != null ? orden.getPesoTotalKg() : 5.0;
                    }
                    orden.setPesoTotalKg(pesoRealKg);
                    
                    ruta.agregarPedido(orden);
                    orden.setEstado(OrdenEstadoService.AGRUPADO_EN_RUTA);
                    notificationService.notificarCampesinoPedidoPendiente(orden);
                }
            }

            // Establecer coordenadas del centro de destino (usando el primer pedido)
            if (ruta.getLatitudCentroDestino() == null && !pedidosZona.isEmpty()) {
                Orden primerPedido = pedidosZona.get(0);
                ruta.setLatitudCentroDestino(primerPedido.getLatitudEnvio());
                ruta.setLongitudCentroDestino(primerPedido.getLongitudEnvio());
            }
            if (ruta.getLatitudCentroOrigen() == null && !pedidosZona.isEmpty()) {
                Orden primerPedido = pedidosZona.get(0);
                ruta.setLatitudCentroOrigen(primerPedido.getLatitudOrigen());
                ruta.setLongitudCentroOrigen(primerPedido.getLongitudOrigen());
            }

            ruta.recalcularVehiculo(); // Actualizar tipo de vehiculo segun peso
            rutaRepo.save(ruta);

            // 4. Verificar reglas de salida (con umbrales por tipo de vehiculo)
            boolean debeSalir = evaluarReglasSalida(ruta);

            if (debeSalir && "FORMANDOSE".equals(ruta.getEstado())) {
                ruta.setEstado("LISTA_PARA_SALIR");
                rutaRepo.save(ruta);
                rutasListas++;
                System.out.println("🚛 RUTA LISTA: #" + ruta.getCodigoRuta() +
                    " | " + ruta.getPedidosCount() + " pedidos | " +
                    String.format("%.1f", ruta.getPesoTotalKg()) + "kg | $" +
                    String.format("%.0f", ruta.getPagoTotalEstimado()) +
                    " | Vehiculo: " + ruta.getTipoVehiculoRequerido());

                // Notificar repartidores cercanos
                notificationService.notificarRepartidoresRutaDisponible(ruta);
            }
        }

        // 5. Revisar rutas antiguas que deban forzarse
        evaluarRutasVencidas();

        System.out.println("🤖 RESULTADO: Creadas=" + rutasCreadas +
            " | Actualizadas=" + rutasActualizadas +
            " | Listas=" + rutasListas);
        System.out.println("═══════════════════════════════════════\n");
    }

    /**
     * Evalua las reglas de salida usando umbrales POR TIPO DE VEHICULO
     */
    private boolean evaluarReglasSalida(Ruta ruta) {
        double peso = ruta.getPesoTotalKg() != null ? ruta.getPesoTotalKg() : 0;
        double capacidad = ruta.getCapacidadMaximaKg() != null ? ruta.getCapacidadMaximaKg() : 50;
        double minimo = ruta.getPesoMinimoSalidaKg() != null ? ruta.getPesoMinimoSalidaKg() : 10;
        int hora = LocalTime.now().getHour();

        // Regla 0: La ruta esta LLENA (alcanzo capacidad maxima) → salir ya
        if (peso >= capacidad) {
            System.out.println("🤖 Regla 0: RUTA LLENA " + ruta.getTipoVehiculoRequerido() +
                " (" + String.format("%.1f", peso) + "kg de " + String.format("%.0f", capacidad) + "kg)");
            return true;
        }

        // Regla 1: Alcanzo el peso minimo para este tipo de vehiculo
        if (peso >= minimo) {
            System.out.println("🤖 Regla 1: Peso suficiente para " + ruta.getTipoVehiculoRequerido() +
                " (" + String.format("%.1f", peso) + "kg >= " + String.format("%.0f", minimo) + "kg)");
            return true;
        }

        // Regla 2: Ya es tarde y hay algo de carga
        double pesoMinimoTarde = minimo * 0.5; // mitad del minimo en la tarde
        if (hora >= HORA_SALIDA_TARDE && peso >= pesoMinimoTarde) {
            System.out.println("🤖 Regla 2: Hora tarde (" + hora + "h) + carga parcial");
            return true;
        }

        // Regla 3: Pedido urgente
        LocalDateTime ahora = LocalDateTime.now();
        boolean pedidoUrgente = ruta.getPedidos().stream()
            .anyMatch(o -> o.getFechaLimiteEntrega() != null &&
                o.getFechaLimiteEntrega().isBefore(ahora.plusHours(3)));
        if (pedidoUrgente) {
            System.out.println("🤖 Regla 3: Pedido urgente");
            ruta.setForzarSalida(true);
            return true;
        }

        return false;
    }

    /**
     * Revisa rutas que llevan mas de 24h formandose y las fuerza a salir
     */
    private void evaluarRutasVencidas() {
        List<Ruta> rutasFormandose = rutaRepo.findByEstadoOrderByFechaCreacionAsc("FORMANDOSE");
        LocalDateTime ahora = LocalDateTime.now();

        for (Ruta ruta : rutasFormandose) {
            if (ruta.getFechaCreacion() != null &&
                ruta.getFechaCreacion().isBefore(ahora.minusHours(HORAS_MAX_ESPERA))) {

                double peso = ruta.getPesoTotalKg() != null ? ruta.getPesoTotalKg() : 0;
                if (peso >= UMBRAL_PESO_TARDE) {
                    System.out.println("🤖 Regla 4: RUTA VENCIDA #" + ruta.getCodigoRuta() +
                        " (" + HORAS_MAX_ESPERA + "h en espera) - Forzando salida");
                    ruta.setEstado("LISTA_PARA_SALIR");
                    ruta.setForzarSalida(true);
                    rutaRepo.save(ruta);
                    notificationService.notificarRepartidoresRutaDisponible(ruta);
                } else {
                    // Cancelar ruta si no alcanzo peso minimo en 24h
                    System.out.println("🤖 Ruta #" + ruta.getCodigoRuta() +
                        " cancelada: no alcanzo peso minimo en " + HORAS_MAX_ESPERA + "h");
                    ruta.setEstado("CANCELADA");
                    // Devolver pedidos a ESPERANDO_AGRUPACION (se reintentara en otra ruta)
                    List<Orden> pedidos = new ArrayList<>(ruta.getPedidos());
                    for (Orden o : pedidos) {
                        o.setRuta(null);
                        o.setEstado(OrdenEstadoService.ESPERANDO_AGRUPACION);
                    }
                    ruta.getPedidos().clear();
                    rutaRepo.save(ruta);
                }
            }
        }
    }

    private String extraerZonaDestino(Orden orden) {
        if (orden.getDireccionEnvio() == null) return "Sin zona";
        String[] partes = orden.getDireccionEnvio().split("[,\\-]");
        if (partes.length >= 2) {
            return partes[partes.length - 1].trim();
        }
        return orden.getDireccionEnvio().trim();
    }

    private String extraerZonaOrigenSimple(Orden orden) {
        if (orden.getMunicipioOrigen() != null && !orden.getMunicipioOrigen().isBlank()) {
            return orden.getMunicipioOrigen().trim();
        }
        // Intentar extraer municipio del campesino dueño del producto
        if (orden.getDetalles() != null) {
            for (com.proyecto.AccesoUsuarios.model.DetalleOrden d : orden.getDetalles()) {
                if (d.getProducto() != null && d.getProducto().getUsuario() != null) {
                    String municipio = d.getProducto().getUsuario().getMunicipioOrigen();
                    if (municipio != null && !municipio.isBlank()) {
                        return municipio.trim();
                    }
                    break;
                }
            }
        }
        return "Sin origen";
    }

    private String extraerZonaOrigen(List<Orden> pedidos) {
        return pedidos.stream()
            .map(Orden::getMunicipioOrigen)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse("Zona rural");
    }

    /**
     * Ejecucion manual (para pruebas o admin)
     */
    public Map<String, Object> ejecutarManual() {
        Map<String, Object> result = new HashMap<>();
        try {
            agruparPedidos();
            result.put("success", true);
            result.put("message", "Robot ejecutado correctamente");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
}
