package com.barberia.citas.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

// Datos generales de la documentación en /swagger-ui.html y el esquema de seguridad:
// el botón "Authorize" de Swagger pide el token que devuelve POST /api/auth/login.
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "api-citas",
        version = "3.0",
        description = "Microservicio de citas de la barbería: clientes, citas y horario de atención, guardados en "
                + "MongoDB (base barberia_citas). Para agendar consulta el servicio y el barbero en api-catalogo. "
                + "Clientes y citas exigen un token de ADMIN o USER; eliminar clientes y cambiar el horario, de ADMIN."))
@SecurityScheme(name = "bearer", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}
