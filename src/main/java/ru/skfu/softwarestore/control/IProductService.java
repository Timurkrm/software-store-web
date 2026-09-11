package ru.skfu.softwarestore.control;

import ru.skfu.softwarestore.dto.ProductRequest;
import ru.skfu.softwarestore.dto.ProductResponse;

import java.util.List;
import java.util.UUID;

public interface IProductService {
    List<ProductResponse> all(String query);

    ProductResponse get(UUID id);

    ProductResponse create(ProductRequest request);

    ProductResponse update(UUID id, ProductRequest request);

    void archive(UUID id);
}
