package ru.skfu.softwarestore.foundation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skfu.softwarestore.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailIgnoreCase(String email);
}
