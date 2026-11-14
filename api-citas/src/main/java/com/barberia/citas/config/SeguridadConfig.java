package com.barberia.citas.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

// Quién puede llamar a cada endpoint. Clientes y citas son datos privados: los maneja la recepción
// (USER) y el administrador (ADMIN). Eliminar clientes y cambiar el horario es solo del ADMIN.
@Configuration
public class SeguridadConfig {

    // Las dos interfaces web, que corren en otros puertos (CORS)
    @Value("${frontend.url}")
    private String[] frontendUrl;

    @Bean
    public SecurityFilterChain cadenaDeSeguridad(HttpSecurity http, JwtAuthenticationConverter rolesDelToken) throws Exception {
        http
                // API sin sesiones ni formularios: cada petición trae su token, así que CSRF no aplica
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(reglas -> reglas
                        .requestMatchers("/api/auth/login", "/api/status", "/error").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Comprobación de la llamada a api-catalogo, para las luces de las interfaces
                        .requestMatchers(HttpMethod.GET, "/api/conexion/**").permitAll()
                        // El horario se muestra en la agenda antes de iniciar sesión
                        .requestMatchers(HttpMethod.GET, "/api/horario").permitAll()
                        .requestMatchers("/api/horario/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/clientes/**").hasRole("ADMIN")
                        .requestMatchers("/api/clientes/**", "/api/citas/**").hasAnyRole("ADMIN", "USER")
                        .anyRequest().authenticated())
                // Valida el JWT del header Authorization: Bearer <token> (firma y vencimiento)
                .oauth2ResourceServer(servidor -> servidor
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(rolesDelToken))
                        .authenticationEntryPoint(SIN_SESION)
                        .accessDeniedHandler(SIN_PERMISO))
                .exceptionHandling(errores -> errores
                        .authenticationEntryPoint(SIN_SESION)
                        .accessDeniedHandler(SIN_PERMISO));

        return http.build();
    }

    // Usuarios de prueba en memoria. Spring Security agrega el prefijo ROLE_ a cada rol.
    @Bean
    public UserDetailsService usuarios(PasswordEncoder passwordEncoder) {
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("123456"))
                .roles("ADMIN")
                .build();
        UserDetails recepcion = User.builder()
                .username("recepcion")
                .password(passwordEncoder.encode("123456"))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(admin, recepcion);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(List.of(frontendUrl));
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/**", cors);
        return fuente;
    }

    // Sin token, o con uno vencido o alterado -> 401
    private static final AuthenticationEntryPoint SIN_SESION = (peticion, respuesta, error) ->
            responder(respuesta, HttpServletResponse.SC_UNAUTHORIZED,
                    "Inicie sesión: la petición no trae un token válido o ya venció.");

    // Token válido, pero el rol no alcanza -> 403
    private static final AccessDeniedHandler SIN_PERMISO = (peticion, respuesta, error) ->
            responder(respuesta, HttpServletResponse.SC_FORBIDDEN,
                    "Su usuario no tiene permiso para esta operación. Solo el administrador puede eliminar clientes o cambiar el horario.");

    private static void responder(HttpServletResponse respuesta, int codigo, String mensaje) throws IOException {
        respuesta.setStatus(codigo);
        respuesta.setContentType("application/json;charset=UTF-8");
        respuesta.getWriter().write("{\"mensaje\":\"" + mensaje + "\"}");
    }
}
