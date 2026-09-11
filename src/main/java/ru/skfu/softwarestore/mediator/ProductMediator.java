package ru.skfu.softwarestore.mediator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skfu.softwarestore.control.ICategoryService;
import ru.skfu.softwarestore.control.IProductService;
import ru.skfu.softwarestore.dto.ProductRequest;
import ru.skfu.softwarestore.dto.ProductResponse;
import ru.skfu.softwarestore.entity.ProductStatus;
import ru.skfu.softwarestore.entity.SoftwareProduct;
import ru.skfu.softwarestore.foundation.repository.ProductRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductMediator implements IProductService {
    private final ProductRepository products;
    private final ICategoryService categories;

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> all(String query) {
        var productsFound = query == null || query.isBlank()
                ? products.findByStatusOrderByCreatedAtDesc(ProductStatus.ACTIVE)
                : products.findByStatusAndNameContainingIgnoreCaseOrderByNameAsc(ProductStatus.ACTIVE, query);
        return productsFound.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse get(UUID id) {
        return toResponse(products.findById(id)
                .filter(product -> product.getStatus() == ProductStatus.ACTIVE)
                .orElseThrow(() -> new NoSuchElementException("Товар не найден")));
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        var product = SoftwareProduct.builder()
                .name(request.name())
                .vendor(request.vendor())
                .description(request.description())
                .price(request.price())
                .version(request.version())
                .imageUrl(request.imageUrl())
                .category(categories.findOrCreate(request.category()))
                .status(ProductStatus.ACTIVE)
                .build();
        return toResponse(products.save(product));
    }

    @Override
    @Transactional
    public ProductResponse update(UUID id, ProductRequest request) {
        var product = products.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Товар не найден"));
        product.setName(request.name());
        product.setVendor(request.vendor());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setVersion(request.version());
        product.setImageUrl(request.imageUrl());
        product.setCategory(categories.findOrCreate(request.category()));
        return toResponse(products.save(product));
    }

    @Override
    @Transactional
    public void archive(UUID id) {
        var product = products.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Товар не найден"));
        product.setStatus(ProductStatus.ARCHIVED);
        products.save(product);
    }

    private ProductResponse toResponse(SoftwareProduct product) {
        return new ProductResponse(product.getId(), product.getName(), product.getVendor(),
                product.getDescription(), product.getPrice(), product.getCurrency(),
                product.getVersion(), product.getImageUrl(), product.getCategory().getName());
    }
}
