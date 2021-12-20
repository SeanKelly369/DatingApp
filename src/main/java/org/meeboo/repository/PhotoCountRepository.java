package org.meeboo.repository;

import org.meeboo.entity.PhotoCountEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PhotoCountRepository extends MongoRepository<PhotoCountEntity, String> {
    List<PhotoCountEntity> findAll();
}
