package com.barberia.citas.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

// Documento de la colección "citas" en la base barberia_citas.
// Relaciones por referencia manual (se guarda el id, no el documento completo):
//  - clienteId apunta a la colección "clientes" de esta misma base.
//  - servicioId y barberoId apuntan a documentos de api-catalogo, que vive en otra base.
// Junto a cada referencia va una copia de los datos que se muestran (nombre, precio, duración):
// así la cita conserva lo que valía al agendarla aunque después cambie el catálogo.
@Document(collection = "citas")
@Data
@NoArgsConstructor
public class Cita {

    @Id
    private String id;
    // Al agendar se buscan las citas que se cruzan por fecha
    @Indexed
    private LocalDateTime fechaHora;
    private EstadoCita estado;
    private String observaciones;

    // null si el cliente se eliminó; la cita conserva su nombre
    @Indexed
    private String clienteId;
    private String clienteNombre;

    private String servicioId;
    private String servicioNombre;
    private int precio;
    private int duracionMinutos;

    private String barberoId;
    private String barberoNombre;

    // Momento en que termina la cita según la duración del servicio
    public LocalDateTime calcularFin() {
        return fechaHora.plusMinutes(duracionMinutos);
    }
}
