package ru.skfu.softwarestore.presentation.controller.v1;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.skfu.softwarestore.control.IProductService;
import ru.skfu.softwarestore.dto.ProductRequest;
import ru.skfu.softwarestore.dto.ProductResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductV1Controller {
    private final IProductService products;

    @GetMapping
    public List<ProductResponse> list(@RequestParam(required = false) String q) { return products.all(q); }
    @GetMapping("/search")
    public List<ProductResponse> search(@RequestParam String q) { return products.all(q); }
    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable UUID id) { return products.get(id); }
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(products.create(request)); }
    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable UUID id, @Valid @RequestBody ProductRequest request) { return products.update(id, request); }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) { products.archive(id); return ResponseEntity.noContent().build(); }
}
