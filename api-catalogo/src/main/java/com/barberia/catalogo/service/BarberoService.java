package com.barberia.catalogo.service;

import com.barberia.catalogo.cliente.CitasCliente;
import com.barberia.catalogo.dto.BarberoDTORequest;
import com.barberia.catalogo.model.Barbero;
import com.barberia.catalogo.repository.BarberoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class BarberoService {

    // Los datos viven en la colección "barberos" de MongoDB
    private final BarberoRepository barberoRepository;
    private final CitasCliente citasCliente;

    public Barbero crear(BarberoDTORequest request) {
        validarDocumentoLibre(request.getDocumento(), null);

        Barbero barbero = new Barbero();
        copiarDatos(request, barbero);
        barbero.setActivo(request.getActivo() == null || request.getActivo());

        return barberoRepository.save(barbero);
    }

    public List<Barbero> listar() {
        return barberoRepository.findAllByOrderByIdAsc();
    }

    public Barbero buscarPorId(String id) {
        return barberoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró un barbero con el ID " + id + "."));
    }

    public Barbero actualizar(String id, BarberoDTORequest request) {
        Barbero barbero = buscarPorId(id);
        validarDocumentoLibre(request.getDocumento(), id);

        copiarDatos(request, barbero);
        if (request.getActivo() != null) {
            barbero.setActivo(request.getActivo());
        }
        return barberoRepository.save(barbero);
    }

    // Un barbero con citas programadas no se elimina: esos clientes llegarían y nadie los atendería.
    // Las citas viven en api-citas, así que se le pregunta antes.
    public void eliminar(String id) {
        Barbero barbero = buscarPorId(id);

        int programadas = citasCliente.contarProgramadas("barberoId", id);
        if (programadas > 0) {
            throw new IllegalStateException(barbero.getNombre() + " tiene " + (programadas == 1
                    ? "1 cita programada. Pásela a otro barbero o cancélela primero"
                    : programadas + " citas programadas. Páselas a otro barbero o cancélelas primero")
                    + ", o márquelo como inactivo.");
        }

        barberoRepository.delete(barbero);
    }

    private void copiarDatos(BarberoDTORequest request, Barbero barbero) {
        barbero.setNombre(request.getNombre().trim());
        barbero.setDocumento(request.getDocumento().trim());
        barbero.setTelefono(request.getTelefono() == null || request.getTelefono().isBlank() ? null : request.getTelefono().trim());
        barbero.setEspecialidad(request.getEspecialidad() == null || request.getEspecialidad().isBlank()
                ? null : request.getEspecialidad().trim());
    }

    private void validarDocumentoLibre(String documento, String idActual) {
        if (barberoRepository.existsByDocumentoAndIdNot(documento.trim(), idActual)) {
            throw new IllegalStateException("Ya existe un barbero con el documento " + documento.trim() + ".");
        }
    }
}
