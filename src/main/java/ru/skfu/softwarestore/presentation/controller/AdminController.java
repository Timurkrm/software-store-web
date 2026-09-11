package ru.skfu.softwarestore.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.skfu.softwarestore.control.IProductService;
import ru.skfu.softwarestore.dto.ProductRequest;
import ru.skfu.softwarestore.dto.ProductResponse;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final IProductService products;

    @PostMapping("/products")
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return products.create(request);
    }
}
