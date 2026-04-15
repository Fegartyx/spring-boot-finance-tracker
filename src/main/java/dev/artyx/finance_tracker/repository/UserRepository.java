package dev.artyx.finance_tracker.repository;

import dev.artyx.finance_tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsUserByEmail(String email);

    boolean existsUserByUsername(String username);

    User findByUsername(String username);

}
