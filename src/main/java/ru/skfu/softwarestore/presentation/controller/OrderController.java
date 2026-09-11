package ru.skfu.softwarestore.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.skfu.softwarestore.control.ILicenseService;
import ru.skfu.softwarestore.control.IOrderService;
import ru.skfu.softwarestore.dto.CheckoutRequest;
import ru.skfu.softwarestore.dto.LicenseResponse;
import ru.skfu.softwarestore.dto.OrderResponse;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final IOrderService orders;
    private final ILicenseService licenses;

    @PostMapping("/checkout")
    public OrderResponse checkout(Authentication authentication, @Valid @RequestBody CheckoutRequest request) {
        return orders.checkout(authentication.getName(), request);
    }

    @GetMapping
    public List<OrderResponse> history(Authentication authentication) {
        return orders.history(authentication.getName());
    }

    @GetMapping("/licenses")
    public List<LicenseResponse> licenses(Authentication authentication) {
        return licenses.findByUserEmail(authentication.getName());
    }
}
