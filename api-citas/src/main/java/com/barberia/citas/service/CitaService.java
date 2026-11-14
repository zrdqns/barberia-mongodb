package com.barberia.citas.service;

import com.barberia.citas.cliente.CatalogoCliente;
import com.barberia.citas.dto.CitaDTORequest;
import com.barberia.citas.model.Barbero;
import com.barberia.citas.model.Cita;
import com.barberia.citas.model.Cliente;
import com.barberia.citas.model.EstadoCita;
import com.barberia.citas.model.Servicio;
import com.barberia.citas.repository.CitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CitaService {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("H:mm");
    // Ningún servicio dura más que esto (api-catalogo lo valida)
    private static final int DURACION_MAXIMA = 240;

    // Los datos viven en la colección "citas" de MongoDB.
    // Crear, editar y cambiar de estado son synchronized: primero se revisan las citas guardadas y
    // después se guarda. Así dos peticiones al mismo tiempo no pueden quedarse con el mismo horario.
    private final CitaRepository citaRepository;
    private final MongoTemplate mongoTemplate;
    private final ClienteService clienteService;
    private final HorarioService horarioService;
    private final CatalogoCliente catalogoCliente;

    public synchronized Cita crear(CitaDTORequest request) {
        Cita cita = new Cita();
        asignarDatos(cita, request);

        // El estado inicial lo define la aplicación, no el cliente.
        cita.setEstado(EstadoCita.PROGRAMADA);

        return citaRepository.save(cita);
    }

    // Todos los filtros son opcionales: sin ninguno devuelve todas las citas, de la más próxima a la más lejana.
    // Los filtros van dentro de la consulta a MongoDB, que devuelve solo las citas que cumplen,
    // en vez de traerlas todas y filtrarlas en Java.
    public List<Cita> listar(String clienteId, String barberoId, String servicioId, EstadoCita estado, LocalDate fecha) {
        Query consulta = new Query();
        if (clienteId != null) consulta.addCriteria(Criteria.where("clienteId").is(clienteId));
        if (barberoId != null) consulta.addCriteria(Criteria.where("barberoId").is(barberoId));
        if (servicioId != null) consulta.addCriteria(Criteria.where("servicioId").is(servicioId));
        if (estado != null) consulta.addCriteria(Criteria.where("estado").is(estado));
        if (fecha != null) {
            consulta.addCriteria(Criteria.where("fechaHora")
                    .gte(fecha.atStartOfDay())
                    .lt(fecha.plusDays(1).atStartOfDay()));
        }
        consulta.with(Sort.by("fechaHora", "id"));

        return mongoTemplate.find(consulta, Cita.class);
    }

    public Cita buscarPorId(String id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró una cita con el ID " + id + "."));
    }

    public synchronized Cita actualizar(String id, CitaDTORequest request) {
        Cita cita = buscarPorId(id);

        if (cita.getEstado() != EstadoCita.PROGRAMADA) {
            throw new IllegalStateException("Solo se pueden modificar las citas programadas.");
        }

        asignarDatos(cita, request);
        return citaRepository.save(cita);
    }

    public synchronized Cita cambiarEstado(String id, EstadoCita nuevoEstado) {
        Cita cita = buscarPorId(id);

        if (nuevoEstado == null || nuevoEstado == EstadoCita.PROGRAMADA) {
            throw new IllegalArgumentException("El estado debe ser COMPLETADA o CANCELADA.");
        }
        if (cita.getEstado() != EstadoCita.PROGRAMADA) {
            throw new IllegalStateException("La cita ya está " + cita.getEstado().name().toLowerCase()
                    + " y no puede cambiar de estado.");
        }
        // Una cita se completa cuando el cliente ya fue atendido, no antes de que llegue su hora
        if (nuevoEstado == EstadoCita.COMPLETADA && cita.getFechaHora().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("La cita todavía no ha empezado. Se puede completar desde las "
                    + cita.getFechaHora().format(HORA) + " del día de la cita.");
        }

        cita.setEstado(nuevoEstado);
        return citaRepository.save(cita);
    }

    public void eliminar(String id) {
        citaRepository.delete(buscarPorId(id));
    }

    // Revisa las reglas de negocio y, solo si todo es correcto, aplica la solicitud sobre la cita.
    // El formato de los campos ya lo revisó @Valid en el controller.
    private void asignarDatos(Cita cita, CitaDTORequest request) {
        // Las citas van en minutos exactos: los segundos que lleguen se descartan
        LocalDateTime inicio = request.getFechaHora().truncatedTo(ChronoUnit.MINUTES);
        if (inicio.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La cita no puede agendarse en una fecha pasada.");
        }

        // El cliente está en esta API
        Cliente cliente = clienteService.buscarPorId(request.getClienteId());

        // El servicio y el barbero están en api-catalogo. Al editar, los que la cita ya tenía se
        // conservan como se agendaron (mismo precio y duración) y no se vuelven a consultar, así que
        // se puede cambiar la hora aunque después se hayan desactivado en el catálogo.
        boolean nueva = cita.getId() == null;

        String servicioNombre = cita.getServicioNombre();
        int precio = cita.getPrecio();
        int duracion = cita.getDuracionMinutos();
        if (nueva || !request.getServicioId().equals(cita.getServicioId())) {
            Servicio servicio = catalogoCliente.buscarServicio(request.getServicioId());
            if (servicio == null) {
                throw new NoSuchElementException("El servicio con ID " + request.getServicioId() + " no existe en el catálogo.");
            }
            if (!servicio.isActivo()) {
                throw new IllegalStateException("El servicio " + servicio.getNombre() + " no está activo.");
            }
            servicioNombre = servicio.getNombre();
            precio = servicio.getPrecio();
            duracion = servicio.getDuracionMinutos();
        }

        String barberoNombre = cita.getBarberoNombre();
        if (nueva || !request.getBarberoId().equals(cita.getBarberoId())) {
            Barbero barbero = catalogoCliente.buscarBarbero(request.getBarberoId());
            if (barbero == null) {
                throw new NoSuchElementException("El barbero con ID " + request.getBarberoId() + " no existe en el catálogo.");
            }
            if (!barbero.isActivo()) {
                throw new IllegalStateException("El barbero " + barbero.getNombre() + " no está activo.");
            }
            barberoNombre = barbero.getNombre();
        }

        LocalDateTime fin = inicio.plusMinutes(duracion);
        horarioService.validarAtencion(inicio, fin);
        validarCruces(cita.getId(), request.getBarberoId(), barberoNombre, cliente, inicio, fin);

        cita.setFechaHora(inicio);
        cita.setObservaciones(request.getObservaciones() == null || request.getObservaciones().isBlank()
                ? null : request.getObservaciones().trim());
        cita.setClienteId(cliente.getId());
        cita.setClienteNombre(cliente.getNombre());
        cita.setServicioId(request.getServicioId());
        cita.setServicioNombre(servicioNombre);
        cita.setPrecio(precio);
        cita.setDuracionMinutos(duracion);
        cita.setBarberoId(request.getBarberoId());
        cita.setBarberoNombre(barberoNombre);
    }

    // Ni el barbero ni el cliente pueden tener dos citas programadas que se crucen en el tiempo.
    // idActual es la cita que se está editando (null al crear).
    private void validarCruces(String idActual, String barberoId, String barberoNombre, Cliente cliente,
                               LocalDateTime inicio, LocalDateTime fin) {
        // Solo pueden cruzarse las que empiezan antes del fin y no más de una duración máxima antes del inicio
        List<Cita> cercanas = citaRepository.findByEstadoAndFechaHoraBetween(
                EstadoCita.PROGRAMADA, inicio.minusMinutes(DURACION_MAXIMA), fin);

        for (Cita otra : cercanas) {
            boolean seCruzan = inicio.isBefore(otra.calcularFin()) && otra.getFechaHora().isBefore(fin);
            if (Objects.equals(otra.getId(), idActual) || !seCruzan) {
                continue;
            }
            String tramo = otra.getFechaHora().format(HORA) + " a " + otra.calcularFin().format(HORA);

            if (barberoId.equals(otra.getBarberoId())) {
                throw new IllegalStateException("El barbero " + barberoNombre + " ya tiene una cita de " + tramo + ".");
            }
            if (cliente.getId().equals(otra.getClienteId())) {
                throw new IllegalStateException(cliente.getNombre() + " ya tiene una cita de " + tramo
                        + " con " + otra.getBarberoNombre() + ".");
            }
        }
    }
}
