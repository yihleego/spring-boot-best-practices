package io.leego.example.repository;

import io.leego.example.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Leego Yih
 */
public interface UserRepository extends MongoRepository<User, String> {

    boolean existsByUsername(String username);

    int deleteByUsername(String username);

}
