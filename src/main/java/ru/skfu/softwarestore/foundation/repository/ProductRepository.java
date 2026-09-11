package ru.skfu.softwarestore.foundation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skfu.softwarestore.entity.ProductStatus;
import ru.skfu.softwarestore.entity.SoftwareProduct;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<SoftwareProduct, UUID> {
    List<SoftwareProduct> findByStatusOrderByCreatedAtDesc(ProductStatus status);

    List<SoftwareProduct> findByStatusAndNameContainingIgnoreCaseOrderByNameAsc(ProductStatus status, String name);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByCategoryId(UUID categoryId);
}
