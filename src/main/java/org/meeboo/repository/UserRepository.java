package org.meeboo.repository;

import org.meeboo.entity.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends MongoRepository<UserEntity, String> {
    UserEntity findByUserId (Long userId);

    UserEntity findUserEntityByUsername(String username);

    UserEntity findUserByEmail(String email);
}
