package com.barberia.citas.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

// Un día del horario. No es un documento propio: va embebido dentro de HorarioSemanal.
// Las horas se guardan como texto "09:00" (ver MongoConfig).
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiaHorario {

    // 1 = lunes ... 7 = domingo, la misma numeración de java.time.DayOfWeek
    private int diaSemana;
    // Las dos en null: ese día no se atiende
    private LocalTime apertura;
    private LocalTime cierre;

    public boolean abierto() {
        return apertura != null && cierre != null;
    }
}
