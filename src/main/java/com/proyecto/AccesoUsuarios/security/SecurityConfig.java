package com.proyecto.AccesoUsuarios.security;

import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Necesario para que funcione el botón "Cerrar Sesión"
            .authorizeHttpRequests(auth -> auth
                // AGREGAMOS "/" AQUÍ AL PRINCIPIO
                .requestMatchers("/", "/login", "/registro", "/registro/guardar", "/css/**", "/js/**", "/img/**", "/images/**").permitAll()
                // Rutas protegidas por ROL
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/campesino/**").hasRole("CAMPESINO")
                .requestMatchers("/tienda/**").hasRole("CLIENTE")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(agroConectaSuccessHandler()) // Handler inteligente de redirección
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .exceptionHandling(exception -> exception
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.sendRedirect("/login?denied");
                })
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Este Handler decide a dónde te manda después de iniciar sesión
    @Bean
    public AuthenticationSuccessHandler agroConectaSuccessHandler() {
        return (request, response, authentication) -> {
            Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

            if (roles.contains("ROLE_ADMIN")) {
                response.sendRedirect("/admin/dashboard");
            } else if (roles.contains("ROLE_CAMPESINO")) {
                response.sendRedirect("/campesino/productos");
            } else if (roles.contains("ROLE_CLIENTE")) {
                response.sendRedirect("/tienda");
            } else {
                response.sendRedirect("/home"); // Ruta por defecto
            }
        };
    }
}
