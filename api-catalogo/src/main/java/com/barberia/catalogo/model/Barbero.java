package com.barberia.catalogo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

// Documento de la colección "barberos" en la base barberia_catalogo
@Document(collection = "barberos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Barbero {

    @Id
    private String id;
    private String nombre;
    // Índice único: no hay dos barberos con el mismo documento
    @Indexed(unique = true)
    private String documento;
    private String telefono;
    private String especialidad;
    private boolean activo;
}
