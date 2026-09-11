package ru.skfu.softwarestore.foundation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skfu.softwarestore.entity.Cart;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findByUserId(UUID userId);
}
