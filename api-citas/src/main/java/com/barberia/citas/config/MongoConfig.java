package com.barberia.citas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Cómo se guardan las fechas y las horas en MongoDB.
@Configuration
public class MongoConfig {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return MongoCustomConversions.create(conversiones -> {
            // La barbería trabaja en una sola zona horaria: la fecha de la cita se guarda tal cual,
            // sin pasarla a UTC, y en Compass se ve con la misma hora con que se agendó.
            conversiones.useNativeDriverJavaTimeCodecs();
            // Las horas del horario se guardan como texto ("09:00"), fáciles de leer en Compass
            conversiones.registerConverters(List.of(new HoraATexto(), new TextoAHora()));
        });
    }

    @WritingConverter
    static class HoraATexto implements Converter<LocalTime, String> {
        @Override
        public String convert(LocalTime hora) {
            return hora.format(HORA);
        }
    }

    @ReadingConverter
    static class TextoAHora implements Converter<String, LocalTime> {
        @Override
        public LocalTime convert(String texto) {
            return LocalTime.parse(texto, HORA);
        }
    }
}
