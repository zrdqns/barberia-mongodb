package com.barberia.citas.repository;

import com.barberia.citas.model.HorarioSemanal;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HorarioRepository extends MongoRepository<HorarioSemanal, String> {
}
