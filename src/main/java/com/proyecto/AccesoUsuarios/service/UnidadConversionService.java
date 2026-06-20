package com.proyecto.AccesoUsuarios.service;

import com.proyecto.AccesoUsuarios.model.DetalleOrden;
import org.springframework.stereotype.Service;

/**
 * Convierte las unidades agricolas colombianas a kilogramos.
 * Las conversiones son aproximadas basadas en el mercado colombiano real.
 */
@Service
public class UnidadConversionService {

    /**
     * Convierte una cantidad en su unidad a kilogramos.
     * @param cantidad - la cantidad numerica (ej: 5)
     * @param unidad - la unidad (ej: "Bulto", "Libra", "Kg", "Arroba")
     * @param categoriaProducto - ayuda a ajustar el peso para ciertas unidades variables (ej: Bulto de papa vs Bulto de cebolla)
     * @return peso en kilogramos
     */
    public double convertirAKg(double cantidad, String unidad, String categoriaProducto) {
        if (unidad == null) return cantidad;
        
        return switch (unidad.toLowerCase().trim()) {
            case "kg", "kilo", "kilogramo", "kilogramos" -> cantidad;
            
            case "libra", "libras", "lb" -> cantidad * 0.5;        // Libra colombiana = 500g
            
            case "bulto", "bultos" -> cantidad * pesoBulto(categoriaProducto);
            
            case "arroba", "arrobas" -> cantidad * 12.5;           // Arroba colombiana ≈ 12.5 kg
            
            case "quintal", "quintales" -> cantidad * 50.0;        // Quintal = 50 kg
            
            case "tonelada", "toneladas", "ton" -> cantidad * 1000.0;
            
            case "gramo", "gramos", "gr", "g" -> cantidad * 0.001;
            
            case "canasta", "canastas", "canastilla" -> cantidad * 20.0;  // Canasta agricola ≈ 20 kg
            
            case "caja", "cajas" -> cantidad * 15.0;               // Caja de productos ≈ 15 kg
            
            case "racimo", "racimos" -> cantidad * 25.0;           // Racimo de plátano ≈ 25 kg
            
            case "unidad", "unidades", "ud", "uds", "pieza", "piezas" -> 
                cantidad * pesoPorUnidad(categoriaProducto);
            
            case "carga", "cargas" -> cantidad * 125.0;            // Carga ≈ 125 kg (2.5 bultos)
            
            case "manojo", "manojos" -> cantidad * 1.0;            // Manojo ≈ 1 kg
            
            case "mazo", "mazos" -> cantidad * 0.5;               // Mazo ≈ 500g
            
            case "atado", "atados" -> cantidad * 1.5;             // Atado ≈ 1.5 kg
            
            default -> cantidad; // Si no reconoce, asumir que ya esta en kg
        };
    }

    /**
     * Peso aproximado de un bulto segun el tipo de producto.
     * En Colombia los bultos varian: papa=62.5kg, cebolla=50kg, arroz=50kg, etc.
     */
    private double pesoBulto(String categoria) {
        if (categoria == null) return 50.0;
        String cat = categoria.toLowerCase();
        if (cat.contains("papa") || cat.contains("tuberculo") || cat.contains("yuca")) return 62.5;
        if (cat.contains("cebolla") || cat.contains("ajo")) return 50.0;
        if (cat.contains("arroz") || cat.contains("cereal")) return 50.0;
        if (cat.contains("maiz") || cat.contains("frijol") || cat.contains("grano")) return 50.0;
        if (cat.contains("cafe") || cat.contains("cacao")) return 60.0;
        if (cat.contains("platano") || cat.contains("banano")) return 50.0;
        if (cat.contains("tomate") || cat.contains("verdura")) return 25.0;
        if (cat.contains("fruta") || cat.contains("naranja") || cat.contains("mango")) return 25.0;
        if (cat.contains("panela")) return 50.0;
        return 50.0; // default
    }

    /**
     * Peso promedio por unidad segun el tipo de producto
     */
    private double pesoPorUnidad(String categoria) {
        if (categoria == null) return 1.0;
        String cat = categoria.toLowerCase();
        if (cat.contains("papa") || cat.contains("yuca") || cat.contains("name")) return 0.2;
        if (cat.contains("cebolla") || cat.contains("tomate")) return 0.15;
        if (cat.contains("platano") || cat.contains("banano")) return 0.3;
        if (cat.contains("naranja") || cat.contains("limon") || cat.contains("mandarina")) return 0.2;
        if (cat.contains("mango") || cat.contains("manzana") || cat.contains("pera")) return 0.25;
        if (cat.contains("aguacate")) return 0.3;
        if (cat.contains("huevo")) return 0.06;
        if (cat.contains("lechuga") || cat.contains("repollo")) return 0.5;
        if (cat.contains("mazorca")) return 0.3;
        return 1.0;
    }

    /**
     * Calcula el peso total en kg de un detalle de orden, usando su cantidad, unidad y categoria
     */
    public double calcularPesoDetalle(DetalleOrden detalle) {
        String unidad = null;
        String categoria = null;
        if (detalle.getProducto() != null) {
            unidad = detalle.getProducto().getUnidad();
            categoria = detalle.getProducto().getCategoria();
        }
        if (unidad == null || unidad.isBlank()) unidad = "Kg";
        double cantidad = detalle.getCantidad() != null ? detalle.getCantidad() : 1;
        return convertirAKg(cantidad, unidad, categoria);
    }

    /**
     * Formatea la unidad para mostrar (ej: "5 Bultos ≈ 312.5 kg")
     */
    public String formatearConversion(double cantidad, String unidad, String categoria) {
        double kg = convertirAKg(cantidad, unidad, categoria);
        if (Math.abs(kg - cantidad) < 0.01) {
            return String.format("%.0f %s", cantidad, unidad);
        }
        return String.format("%.0f %s ≈ %.1f kg", cantidad, unidad, kg);
    }
}
