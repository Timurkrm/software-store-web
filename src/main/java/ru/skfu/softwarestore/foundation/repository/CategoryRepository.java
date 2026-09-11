package ru.skfu.softwarestore.foundation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skfu.softwarestore.entity.Category;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findByNameIgnoreCase(String name);
}
