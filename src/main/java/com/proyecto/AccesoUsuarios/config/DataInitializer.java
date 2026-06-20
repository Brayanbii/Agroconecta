package com.proyecto.AccesoUsuarios.config;

import com.proyecto.AccesoUsuarios.model.Producto;
import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.ProductoRepository;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initDatabase(UsuarioRepository userRepo, ProductoRepository prodRepo, PasswordEncoder encoder) {
        return args -> {

            // === 1. CREAR ADMINISTRADOR ===
            if (userRepo.findFirstByEmail("admin@agroconecta.com").isEmpty()) {
                Usuario admin = new Usuario();
                admin.setUserName("admin");
                admin.setPassword(encoder.encode("123"));
                admin.setRol("ADMIN");
                admin.setNombreCompleto("Administrador Principal");
                admin.setEmail("admin@agroconecta.com");
                admin.setTelefono("3001234567");
                userRepo.save(admin);
                System.out.println("✅ Usuario ADMIN creado");
            }

            // === 2. CREAR CAMPESINO Y PRODUCTOS DE PRUEBA ===
            Usuario campesino = userRepo.findFirstByEmail("pepe@finca.com").orElse(null);
            if (campesino == null) {
                campesino = new Usuario();
                campesino.setUserName("pepe_campesino");
                campesino.setPassword(encoder.encode("123"));
                campesino.setRol("CAMPESINO");
                campesino.setNombreCompleto("Pepe Grillo");
                campesino.setEmail("pepe@finca.com");
                campesino.setTelefono("3109876543");
                // Localidad finca predeterminada — Barbosa, Santander (zona agrícola real)
                campesino.setLatitud(5.9317);
                campesino.setLongitud(-73.6147);
                userRepo.save(campesino);
                System.out.println("✅ Usuario CAMPESINO creado");

                // Productos de prueba iniciales
                Producto p1 = new Producto();
                p1.setNombre("Papa Pastusa");
                p1.setPrecio(2500.0);
                p1.setDescripcion("Papa fresca lavada de la región santandereana");
                p1.setImagenUrl("https://th.bing.com/th/id/OIP.K7V6kXj5f1Zk8w9qXj5f1gHaHa?pid=ImgDet&rs=1");
                p1.setCategoria("Tubérculos");
                p1.setStock(100);
                p1.setUnidad("Kg");
                p1.setUsuario(campesino);
                p1.setLatitudOrigen(5.9317);
                p1.setLongitudOrigen(-73.6147);
                p1.setMunicipioOrigen("Barbosa, Santander");

                Producto p2 = new Producto();
                p2.setNombre("Tomate Chonto");
                p2.setPrecio(3000.0);
                p2.setDescripcion("Tomate rojo maduro de cultivo artesanal");
                p2.setImagenUrl("https://th.bing.com/th/id/R.2964576717714902141771490?pid=ImgDet&rs=1");
                p2.setCategoria("Verduras");
                p2.setStock(50);
                p2.setUnidad("Libra");
                p2.setUsuario(campesino);
                p2.setLatitudOrigen(5.9317);
                p2.setLongitudOrigen(-73.6147);
                p2.setMunicipioOrigen("Barbosa, Santander");

                prodRepo.save(p1);
                prodRepo.save(p2);
            }

            // === 3. CREAR CLIENTE ===
            if (userRepo.findFirstByEmail("maria@gmail.com").isEmpty()) {
                Usuario cliente = new Usuario();
                cliente.setUserName("maria_cliente");
                cliente.setPassword(encoder.encode("123"));
                cliente.setRol("CLIENTE");
                cliente.setNombreCompleto("María López");
                cliente.setEmail("maria@gmail.com");
                cliente.setTelefono("3201112233");
                userRepo.save(cliente);
                System.out.println("✅ Usuario CLIENTE creado");
            }

            // === 3.5 CREAR REPARTIDOR DE PRUEBA ===
            if (userRepo.findFirstByEmail("repartidor@agroconecta.com").isEmpty()) {
                Usuario repartidor = new Usuario();
                repartidor.setUserName("carlos_delivery");
                repartidor.setPassword(encoder.encode("123"));
                repartidor.setRol("REPARTIDOR");
                repartidor.setNombreCompleto("Carlos Moto");
                repartidor.setEmail("repartidor@agroconecta.com");
                repartidor.setTelefono("3154445566");
                repartidor.setLatitud(5.7000);
                repartidor.setLongitud(-73.6200);
                repartidor.setMunicipioOrigen("Barbosa, Santander");
                repartidor.setEstadoVerificacion("APROBADO");
                userRepo.save(repartidor);
                System.out.println("✅ Usuario REPARTIDOR creado");
            }

            // === 4. SISTEMA DE AUTOREPARACIÓN DE BASE DE DATOS ===
            System.out.println("🔧 [SISTEMA DE AUTOREPARACIÓN] Buscando productos corruptos con valores NULL...");
            List<Producto> todosLosProductos = prodRepo.findAll();
            int productosReparados = 0;

            // Buscamos un campesino de respaldo por si el producto no tiene vendedor
            Usuario campesinoDeRespaldo = userRepo.findFirstByEmail("pepe@finca.com").orElse(null);

            for (Producto p : todosLosProductos) {
                boolean necesitaReparacion = false;

                // Reparamos categoría nula
                if (p.getCategoria() == null || p.getCategoria().trim().isEmpty()) {
                    p.setCategoria("Verduras");
                    necesitaReparacion = true;
                }

                // Reparamos imagenUrl nula
                if (p.getImagenUrl() == null || p.getImagenUrl().trim().isEmpty()) {
                    p.setImagenUrl("default.png"); // Nombre seguro para Thymeleaf
                    necesitaReparacion = true;
                }

                // Reparamos descripción nula
                if (p.getDescripcion() == null || p.getDescripcion().trim().isEmpty()) {
                    p.setDescripcion("Producto cosechado fresco en AgroConecta");
                    necesitaReparacion = true;
                }

                // Reparamos unidad nula
                if (p.getUnidad() == null || p.getUnidad().trim().isEmpty()) {
                    p.setUnidad("Kg");
                    necesitaReparacion = true;
                }

                // Reparamos geolocalización de origen nula
                if (p.getLatitudOrigen() == null) {
                    p.setLatitudOrigen(5.9317);
                    necesitaReparacion = true;
                }
                if (p.getLongitudOrigen() == null) {
                    p.setLongitudOrigen(-73.6147);
                    necesitaReparacion = true;
                }
                if (p.getMunicipioOrigen() == null || p.getMunicipioOrigen().trim().isEmpty()) {
                    p.setMunicipioOrigen("Barbosa, Santander");
                    necesitaReparacion = true;
                }

                // Reparamos relación de usuario (vendedor) nula
                if (p.getUsuario() == null && campesinoDeRespaldo != null) {
                    p.setUsuario(campesinoDeRespaldo);
                    necesitaReparacion = true;
                }

                // Guardamos los cambios en MySQL solo si el producto fue curado
                if (necesitaReparacion) {
                    prodRepo.save(p);
                    productosReparados++;
                }
            }

            if (productosReparados > 0) {
                System.out.println("✅ [SISTEMA DE AUTOREPARACIÓN] ¡Se repararon con éxito " + productosReparados + " productos corruptos!");
            } else {
                System.out.println("☀️ [SISTEMA DE AUTOREPARACIÓN] No se encontraron productos corruptos. Todo en orden.");
            }
        };
    }
}