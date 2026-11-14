package com.barberia.catalogo.config;

import com.barberia.catalogo.model.Barbero;
import com.barberia.catalogo.model.Servicio;
import com.barberia.catalogo.repository.BarberoRepository;
import com.barberia.catalogo.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

// Datos de demostración. Cada colección se llena solo si está vacía, para no repetir
// documentos ni revivir los que se hayan eliminado.
@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private final ServicioRepository servicioRepository;
    private final BarberoRepository barberoRepository;

    @Override
    public void run(String... args) {
        if (servicioRepository.count() == 0) {
            servicioRepository.saveAll(List.of(
                    new Servicio(null, "Corte clásico", "Corte a tijera o máquina con lavado", 25000, 30, true),
                    new Servicio(null, "Arreglo de barba", "Perfilado y afeitado con navaja", 18000, 20, true),
                    new Servicio(null, "Corte y barba", "Corte clásico más arreglo de barba", 38000, 50, true),
                    new Servicio(null, "Tinte", "Aplicación de color completo", 60000, 90, false)));
        }
        if (barberoRepository.count() == 0) {
            barberoRepository.saveAll(List.of(
                    new Barbero(null, "Andrés Rojas", "1012345678", "3104567890", "Cortes clásicos", true),
                    new Barbero(null, "Julián Mora", "1023456789", "3115678901", "Barba y afeitado", true),
                    new Barbero(null, "Felipe Cárdenas", "1034567890", "3126789012", "Degradados", false)));
        }
    }
}
