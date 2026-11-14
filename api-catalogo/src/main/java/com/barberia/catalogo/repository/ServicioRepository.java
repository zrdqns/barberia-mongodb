package com.barberia.catalogo.repository;

import com.barberia.catalogo.model.Servicio;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

// Spring Data MongoDB genera las consultas a partir del nombre de cada método
public interface ServicioRepository extends MongoRepository<Servicio, String> {

    // El ObjectId empieza con la fecha de creación: ordenar por id es ordenar por antigüedad
    List<Servicio> findAllByOrderByIdAsc();

    // ¿Otro servicio, distinto del que se edita, ya usa ese nombre? (sin importar mayúsculas)
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, String id);
}
