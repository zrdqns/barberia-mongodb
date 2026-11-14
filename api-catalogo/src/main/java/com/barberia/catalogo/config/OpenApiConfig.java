package com.barberia.catalogo.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

// Datos generales de la documentación en /swagger-ui.html y el esquema de seguridad:
// el botón "Authorize" de Swagger pide el token que devuelve POST /api/auth/login.
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "api-catalogo",
        version = "3.0",
        description = "Microservicio de catálogo de la barbería: servicios y barberos, guardados en MongoDB "
                + "(base barberia_catalogo). Consultar es público; crear, editar y eliminar exige un token de ADMIN."))
@SecurityScheme(name = "bearer", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}
