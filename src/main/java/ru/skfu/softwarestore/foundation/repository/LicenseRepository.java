package ru.skfu.softwarestore.foundation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skfu.softwarestore.entity.License;

import java.util.List;
import java.util.UUID;

public interface LicenseRepository extends JpaRepository<License, UUID> {
    List<License> findByUserIdOrderByIssuedAtDesc(UUID userId);
}
