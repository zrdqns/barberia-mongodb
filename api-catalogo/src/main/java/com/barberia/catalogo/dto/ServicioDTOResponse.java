package com.barberia.catalogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Datos que la API devuelve de un servicio.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Un servicio de la barbería")
public class ServicioDTOResponse {

    @Schema(description = "ObjectId que asignó MongoDB", example = "6735f0c2a1b2c3d4e5f60718")
    private String id;
    @Schema(example = "Corte clásico")
    private String nombre;
    @Schema(example = "Corte a tijera o máquina con lavado")
    private String descripcion;
    @Schema(example = "25000")
    private int precio;
    @Schema(example = "30")
    private int duracionMinutos;
    @Schema(example = "true")
    private boolean activo;
}
