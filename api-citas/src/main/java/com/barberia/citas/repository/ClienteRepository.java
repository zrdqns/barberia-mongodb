package com.barberia.citas.repository;

import com.barberia.citas.model.Cliente;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

// Spring Data MongoDB genera las consultas a partir del nombre de cada método
public interface ClienteRepository extends MongoRepository<Cliente, String> {

    List<Cliente> findAllByOrderByIdAsc();

    // ¿Otro cliente, distinto del que se edita, ya tiene ese documento?
    boolean existsByDocumentoAndIdNot(String documento, String id);
}
