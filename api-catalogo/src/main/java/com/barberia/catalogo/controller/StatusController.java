package com.barberia.catalogo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Estado", description = "Comprobación de que el microservicio está encendido")
public class StatusController {

    @GetMapping("/api/status")
    @Operation(summary = "Estado del microservicio", description = "Público. Responde texto plano.")
    public String check() {
        return "api-catalogo funcionando correctamente";
    }
}
