package com.barberia.citas.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Datos que la API devuelve de un cliente.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Un cliente de la barbería")
public class ClienteDTOResponse {

    @Schema(description = "ObjectId que asignó MongoDB", example = "6735f0c2a1b2c3d4e5f60730")
    private String id;
    @Schema(example = "Carlos Ramírez")
    private String nombre;
    @Schema(example = "1098765432")
    private String documento;
    @Schema(example = "3001234567")
    private String telefono;
    @Schema(example = "carlos.ramirez@correo.com")
    private String correo;
}
