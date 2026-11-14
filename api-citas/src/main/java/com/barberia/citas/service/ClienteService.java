package com.barberia.citas.service;

import com.barberia.citas.dto.ClienteDTORequest;
import com.barberia.citas.model.Cita;
import com.barberia.citas.model.Cliente;
import com.barberia.citas.model.EstadoCita;
import com.barberia.citas.repository.CitaRepository;
import com.barberia.citas.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ClienteService {

    // Los datos viven en la colección "clientes" de MongoDB.
    // Las citas guardan una copia del nombre del cliente, así que se mantiene al día desde aquí.
    private final ClienteRepository clienteRepository;
    private final CitaRepository citaRepository;

    public Cliente crear(ClienteDTORequest request) {
        validarDocumentoLibre(request.getDocumento(), null);

        Cliente cliente = new Cliente();
        copiarDatos(request, cliente);

        return clienteRepository.save(cliente);
    }

    public List<Cliente> listar() {
        return clienteRepository.findAllByOrderByIdAsc();
    }

    public Cliente buscarPorId(String id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró un cliente con el ID " + id + "."));
    }

    public Cliente actualizar(String id, ClienteDTORequest request) {
        Cliente cliente = buscarPorId(id);
        validarDocumentoLibre(request.getDocumento(), id);

        String nombreAnterior = cliente.getNombre();
        copiarDatos(request, cliente);
        Cliente guardado = clienteRepository.save(cliente);

        // Si cambió el nombre, sus citas muestran el nuevo
        if (!guardado.getNombre().equals(nombreAnterior)) {
            List<Cita> suyas = citaRepository.findByClienteId(id);
            suyas.forEach(cita -> cita.setClienteNombre(guardado.getNombre()));
            citaRepository.saveAll(suyas);
        }
        return guardado;
    }

    // Un cliente con citas pendientes no se elimina. Sus citas anteriores se conservan con
    // su nombre y dejan de apuntarle (clienteId = null) antes de borrarlo.
    public void eliminar(String id) {
        Cliente cliente = buscarPorId(id);

        if (citaRepository.existsByClienteIdAndEstado(id, EstadoCita.PROGRAMADA)) {
            throw new IllegalStateException("El cliente tiene citas programadas. Cancélelas o elimínelas primero.");
        }

        List<Cita> suyas = citaRepository.findByClienteId(id);
        suyas.forEach(cita -> cita.setClienteId(null));
        citaRepository.saveAll(suyas);
        clienteRepository.delete(cliente);
    }

    private void copiarDatos(ClienteDTORequest request, Cliente cliente) {
        cliente.setNombre(request.getNombre().trim());
        cliente.setDocumento(request.getDocumento().trim());
        cliente.setTelefono(request.getTelefono().trim());
        cliente.setCorreo(request.getCorreo() == null || request.getCorreo().isBlank() ? null : request.getCorreo().trim());
    }

    // idActual es el cliente que se está editando (null al crear),
    // para no compararlo consigo mismo al revisar documentos repetidos.
    private void validarDocumentoLibre(String documento, String idActual) {
        if (clienteRepository.existsByDocumentoAndIdNot(documento.trim(), idActual)) {
            throw new IllegalStateException("Ya existe un cliente con el documento " + documento.trim() + ".");
        }
    }
}
