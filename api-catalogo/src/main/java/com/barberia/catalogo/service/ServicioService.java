package com.barberia.catalogo.service;

import com.barberia.catalogo.cliente.CitasCliente;
import com.barberia.catalogo.dto.ServicioDTORequest;
import com.barberia.catalogo.model.Servicio;
import com.barberia.catalogo.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ServicioService {

    // Los datos viven en la colección "servicios" de MongoDB.
    // El formato de cada campo ya lo revisó @Valid en el controller; aquí van las reglas de negocio.
    private final ServicioRepository servicioRepository;
    private final CitasCliente citasCliente;

    public Servicio crear(ServicioDTORequest request) {
        validarNombreLibre(request.getNombre(), null);

        Servicio servicio = new Servicio();
        copiarDatos(request, servicio);
        // Si el cliente no indica el estado, lo define la aplicación.
        servicio.setActivo(request.getActivo() == null || request.getActivo());

        return servicioRepository.save(servicio);
    }

    public List<Servicio> listar() {
        return servicioRepository.findAllByOrderByIdAsc();
    }

    public Servicio buscarPorId(String id) {
        return servicioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró un servicio con el ID " + id + "."));
    }

    public Servicio actualizar(String id, ServicioDTORequest request) {
        Servicio servicio = buscarPorId(id);
        validarNombreLibre(request.getNombre(), id);

        copiarDatos(request, servicio);
        if (request.getActivo() != null) {
            servicio.setActivo(request.getActivo());
        }
        return servicioRepository.save(servicio);
    }

    // Un servicio con citas programadas no se elimina: esas citas quedarían sin servicio.
    // Las citas viven en api-citas, así que se le pregunta antes.
    public void eliminar(String id) {
        Servicio servicio = buscarPorId(id);

        int programadas = citasCliente.contarProgramadas("servicioId", id);
        if (programadas > 0) {
            throw new IllegalStateException("El servicio " + servicio.getNombre() + " tiene " + (programadas == 1
                    ? "1 cita programada. Cancélela o cámbiela de servicio primero"
                    : programadas + " citas programadas. Cancélelas o cámbielas de servicio primero")
                    + ", o desactívelo para que no se agende más.");
        }

        servicioRepository.delete(servicio);
    }

    private void copiarDatos(ServicioDTORequest request, Servicio servicio) {
        servicio.setNombre(request.getNombre().trim());
        servicio.setDescripcion(request.getDescripcion() == null || request.getDescripcion().isBlank()
                ? null : request.getDescripcion().trim());
        servicio.setPrecio(request.getPrecio());
        servicio.setDuracionMinutos(request.getDuracionMinutos());
    }

    // idActual es el servicio que se está editando (null al crear),
    // para no compararlo consigo mismo al revisar nombres repetidos.
    private void validarNombreLibre(String nombre, String idActual) {
        if (servicioRepository.existsByNombreIgnoreCaseAndIdNot(nombre.trim(), idActual)) {
            throw new IllegalStateException("Ya existe un servicio con el nombre " + nombre.trim() + ".");
        }
    }
}
