package com.barberia.citas.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

// Horario de atención de la barbería: un solo documento en la colección "horario"
// con los siete días embebidos. Es una relación 1:N acotada (siempre son 7 días) y los
// días se leen y se cambian siempre junto con la semana, así que se embeben en vez de
// guardarse en otra colección.
@Document(collection = "horario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorarioSemanal {

    // Siempre hay uno solo
    public static final String ID = "semanal";

    @Id
    private String id;
    private List<DiaHorario> dias;
}
