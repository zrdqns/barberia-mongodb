package com.barberia.citas.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

// Documento de la colección "clientes" en la base barberia_citas
@Document(collection = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    private String id;
    private String nombre;
    // Índice único: no hay dos clientes con el mismo documento
    @Indexed(unique = true)
    private String documento;
    private String telefono;
    private String correo;
}
