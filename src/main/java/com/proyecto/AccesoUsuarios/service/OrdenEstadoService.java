package com.proyecto.AccesoUsuarios.service;

import com.proyecto.AccesoUsuarios.model.Orden;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class OrdenEstadoService {

    // Estados validos del ciclo de vida
    public static final String PENDIENTE = "PENDIENTE";
    public static final String RECHAZADO = "RECHAZADO";
    public static final String PAGADO = "PAGADO";
    public static final String PENDIENTE_CAMPESINO = "PENDIENTE_CAMPESINO"; // Esperando que el campesino acepte
    public static final String ESPERANDO_AGRUPACION = "ESPERANDO_AGRUPACION";
    public static final String AGRUPADO_EN_RUTA = "AGRUPADO_EN_RUTA";
    public static final String BUSCANDO_REPARTIDOR = "BUSCANDO_REPARTIDOR";
    public static final String ACEPTADO_POR_CAMPESINO = "ACEPTADO_POR_CAMPESINO";
    public static final String RUTA_ASIGNADA = "RUTA_ASIGNADA";
    public static final String EN_CAMINO = "EN_CAMINO";
    public static final String RECOGIDO = "RECOGIDO";          // Repartidor recogio en finca
    public static final String ENTREGADO = "ENTREGADO";
    public static final String CANCELADO = "CANCELADO";

    // Transiciones validas
    private static final Set<String> TRANSICIONES_DESDE_PENDIENTE = Set.of(PAGADO, RECHAZADO, CANCELADO);
    private static final Set<String> TRANSICIONES_DESDE_PAGADO = Set.of(PENDIENTE_CAMPESINO, BUSCANDO_REPARTIDOR);
    private static final Set<String> TRANSICIONES_DESDE_PENDIENTE_CAMPESINO = Set.of(ESPERANDO_AGRUPACION, CANCELADO);
    private static final Set<String> TRANSICIONES_DESDE_ESPERANDO_AGRUPACION = Set.of(AGRUPADO_EN_RUTA, CANCELADO);
    private static final Set<String> TRANSICIONES_DESDE_AGRUPADO_EN_RUTA = Set.of(ACEPTADO_POR_CAMPESINO);
    private static final Set<String> TRANSICIONES_DESDE_ACEPTADO_POR_CAMPESINO = Set.of(RUTA_ASIGNADA, CANCELADO);
    private static final Set<String> TRANSICIONES_DESDE_BUSCANDO_REPARTIDOR = Set.of(ACEPTADO_POR_CAMPESINO, CANCELADO);
    private static final Set<String> TRANSICIONES_DESDE_RUTA_ASIGNADA = Set.of(EN_CAMINO);
    private static final Set<String> TRANSICIONES_DESDE_EN_CAMINO = Set.of(RECOGIDO, ENTREGADO);
    private static final Set<String> TRANSICIONES_DESDE_RECOGIDO = Set.of(ENTREGADO);
    private static final Set<String> TRANSICIONES_DESDE_ENTREGADO = Set.of();

    /**
     * Procesa el pago aprobado de MercadoPago.
     * Si es ECONOMICO → ESPERANDO_AGRUPACION (espera juntar mas pedidos)
     * Si es RAPIDO → BUSCANDO_REPARTIDOR (busca conductor inmediato)
     */
    public void procesarPagoAprobado(Orden orden) {
        orden.setEstado(PAGADO);
        System.out.println("✅ Pago Aprobado - Orden #" + orden.getNumeroOrden());

        String tipo = orden.getTipoEnvio() != null ? orden.getTipoEnvio() : "ECONOMICO";

        if ("RAPIDO".equalsIgnoreCase(tipo)) {
            orden.setEstado(BUSCANDO_REPARTIDOR);
            System.out.println("⚡ Orden RAPIDA #" + orden.getNumeroOrden() + " → BUSCANDO_REPARTIDOR");
        } else {
            // ECONOMICO: primero notificar al campesino para que acepte
            orden.setEstado(PENDIENTE_CAMPESINO);
            System.out.println("📋 Orden ECONOMICA #" + orden.getNumeroOrden() + " → PENDIENTE_CAMPESINO");
            orden.setFechaLimiteEntrega(LocalDateTime.now().plusDays(3));
        }
    }

    /**
     * Verifica si una transicion de estado es valida
     */
    public boolean puedeTransicionar(Orden orden, String nuevoEstado) {
        String estadoActual = orden.getEstado();
        if (estadoActual == null) return true;

        Set<String> permitidos = switch (estadoActual) {
            case PENDIENTE -> TRANSICIONES_DESDE_PENDIENTE;
            case PAGADO -> TRANSICIONES_DESDE_PAGADO;
            case PENDIENTE_CAMPESINO -> TRANSICIONES_DESDE_PENDIENTE_CAMPESINO;
            case ESPERANDO_AGRUPACION -> TRANSICIONES_DESDE_ESPERANDO_AGRUPACION;
            case AGRUPADO_EN_RUTA -> TRANSICIONES_DESDE_AGRUPADO_EN_RUTA;
            case ACEPTADO_POR_CAMPESINO -> TRANSICIONES_DESDE_ACEPTADO_POR_CAMPESINO;
            case BUSCANDO_REPARTIDOR -> TRANSICIONES_DESDE_BUSCANDO_REPARTIDOR;
            case RUTA_ASIGNADA -> TRANSICIONES_DESDE_RUTA_ASIGNADA;
            case EN_CAMINO -> TRANSICIONES_DESDE_EN_CAMINO;
            case RECOGIDO -> TRANSICIONES_DESDE_RECOGIDO;
            case ENTREGADO -> TRANSICIONES_DESDE_ENTREGADO;
            default -> Set.of(); // Si es un estado desconocido, no permitir cambios
        };

        return permitidos.contains(nuevoEstado);
    }

    /**
     * Retorna todos los estados posibles para mostrarlos en el panel admin
     */
    public List<String> getTodosLosEstados() {
        return List.of(
            PENDIENTE, PAGADO, PENDIENTE_CAMPESINO,
            ESPERANDO_AGRUPACION, AGRUPADO_EN_RUTA,
            ACEPTADO_POR_CAMPESINO, BUSCANDO_REPARTIDOR,
            RUTA_ASIGNADA, EN_CAMINO, RECOGIDO, ENTREGADO,
            CANCELADO, RECHAZADO
        );
    }

    /**
     * Retorna el icono y color para cada estado (para UI)
     */
    public String getBadgeClases(String estado) {
        return switch (estado != null ? estado : "") {
            case PENDIENTE -> "bg-yellow-50 text-yellow-700 border-yellow-200";
            case PAGADO -> "bg-blue-50 text-blue-700 border-blue-200";
            case PENDIENTE_CAMPESINO -> "bg-rose-50 text-rose-700 border-rose-200";
            case ESPERANDO_AGRUPACION -> "bg-amber-50 text-amber-700 border-amber-200";
            case AGRUPADO_EN_RUTA -> "bg-orange-50 text-orange-700 border-orange-200";
            case ACEPTADO_POR_CAMPESINO -> "bg-emerald-50 text-emerald-700 border-emerald-200";
            case BUSCANDO_REPARTIDOR -> "bg-violet-50 text-violet-700 border-violet-200";
            case RUTA_ASIGNADA -> "bg-cyan-50 text-cyan-700 border-cyan-200";
            case EN_CAMINO -> "bg-sky-50 text-sky-700 border-sky-200";
            case RECOGIDO -> "bg-indigo-50 text-indigo-700 border-indigo-200";
            case ENTREGADO -> "bg-green-50 text-green-700 border-green-200";
            case CANCELADO, RECHAZADO -> "bg-red-50 text-red-700 border-red-200";
            default -> "bg-gray-50 text-gray-700 border-gray-200";
        };
    }

    public String getBadgeDotClase(String estado) {
        return switch (estado != null ? estado : "") {
            case PENDIENTE -> "bg-yellow-500";
            case PAGADO -> "bg-blue-500";
            case ESPERANDO_AGRUPACION -> "bg-amber-500";
            case AGRUPADO_EN_RUTA -> "bg-orange-500";
            case ACEPTADO_POR_CAMPESINO -> "bg-emerald-500";
            case BUSCANDO_REPARTIDOR -> "bg-violet-500";
            case RUTA_ASIGNADA -> "bg-cyan-500";
            case EN_CAMINO -> "bg-sky-500";
            case RECOGIDO -> "bg-indigo-500";
            case ENTREGADO -> "bg-green-500";
            case CANCELADO, RECHAZADO -> "bg-red-500";
            default -> "bg-gray-500";
        };
    }
}
