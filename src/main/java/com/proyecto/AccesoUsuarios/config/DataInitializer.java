package com.proyecto.AccesoUsuarios.config;

import com.proyecto.AccesoUsuarios.model.Producto;
import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.ProductoRepository;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initDatabase(UsuarioRepository userRepo, ProductoRepository prodRepo, PasswordEncoder encoder) {
        return args -> {

            // 1. Crear ADMIN
            if (userRepo.findByEmail("admin@agroconecta.com").isEmpty()) {
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

            // 2. Crear CAMPESINO
            Usuario campesino = userRepo.findByEmail("pepe@finca.com").orElse(null);
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

                // Productos de prueba
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

            // 3. Crear CLIENTE
            if (userRepo.findByEmail("maria@gmail.com").isEmpty()) {
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
        };
    }
}