package io.leego.example.repository;

import io.leego.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Leego Yih
 */
public interface UserRepository extends JpaRepository<User, Long>, DeletableRepository<User, Long> {

    boolean existsByUsername(String username);

}
