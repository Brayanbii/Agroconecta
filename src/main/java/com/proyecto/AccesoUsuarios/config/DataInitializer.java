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
                // Agregamos 'null, null' al final para las listas de productos y órdenes
                Usuario admin = new Usuario(null, "admin", encoder.encode("123"), "ADMIN", 
                        "Administrador Principal", "admin@agroconecta.com", "3001234567", null, null);
                userRepo.save(admin);
                System.out.println("✅ Usuario ADMIN creado");
            }

            // 2. Crear CAMPESINO
            Usuario campesino = userRepo.findByEmail("pepe@finca.com").orElse(null);
            if (campesino == null) {
                // Agregamos 'null, null' al final
                campesino = new Usuario(null, "pepe_campesino", encoder.encode("123"), "CAMPESINO",
                        "Pepe Grillo", "pepe@finca.com", "3109876543", null, null);
                userRepo.save(campesino);
                System.out.println("✅ Usuario CAMPESINO creado");

                // Productos de prueba
                Producto p1 = new Producto(null, "Papa Pastusa", 2500.0, "Papa fresca lavada", 
                        "https://th.bing.com/th/id/OIP.K7V6kXj5f1Zk8w9qXj5f1gHaHa?pid=ImgDet&rs=1", 
                        "Verduras", 100, "Kg", campesino);
                
                Producto p2 = new Producto(null, "Tomate Chonto", 3000.0, "Tomate rojo maduro", 
                        "https://th.bing.com/th/id/R.2964576717714902141771490?pid=ImgDet&rs=1", 
                        "Verduras", 50, "Libra", campesino);
                
                prodRepo.save(p1);
                prodRepo.save(p2);
            }

            // 3. Crear CLIENTE
            if (userRepo.findByEmail("maria@gmail.com").isEmpty()) {
                // Agregamos 'null, null' al final
                Usuario cliente = new Usuario(null, "maria_cliente", encoder.encode("123"), "CLIENTE",
                        "María López", "maria@gmail.com", "3201112233", null, null);
                userRepo.save(cliente);
                System.out.println("✅ Usuario CLIENTE creado");
            }
        };
    }
}