package com.barberia.catalogo.repository;

import com.barberia.catalogo.model.Barbero;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

// Spring Data MongoDB genera las consultas a partir del nombre de cada método
public interface BarberoRepository extends MongoRepository<Barbero, String> {

    List<Barbero> findAllByOrderByIdAsc();

    // ¿Otro barbero, distinto del que se edita, ya tiene ese documento?
    boolean existsByDocumentoAndIdNot(String documento, String id);
}
