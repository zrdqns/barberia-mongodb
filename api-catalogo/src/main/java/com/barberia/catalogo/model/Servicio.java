package com.barberia.catalogo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

// Documento de la colección "servicios" en la base barberia_catalogo
@Document(collection = "servicios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Servicio {

    // MongoDB genera el _id (un ObjectId) y Spring lo entrega como texto
    @Id
    private String id;
    // Índice único: dos servicios no pueden llamarse igual
    @Indexed(unique = true)
    private String nombre;
    private String descripcion;
    // Precio en pesos colombianos, sin decimales
    private int precio;
    private int duracionMinutos;
    private boolean activo;
}
