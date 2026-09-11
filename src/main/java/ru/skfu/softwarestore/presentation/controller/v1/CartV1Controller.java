package ru.skfu.softwarestore.presentation.controller.v1;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.skfu.softwarestore.control.ICartService;
import ru.skfu.softwarestore.control.IUserService;
import ru.skfu.softwarestore.dto.CartItemQuantityRequest;
import ru.skfu.softwarestore.dto.CartItemRequest;
import ru.skfu.softwarestore.dto.CartResponse;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartV1Controller {
    private final ICartService carts;
    private final IUserService users;

    @GetMapping
    public CartResponse get(Authentication authentication) {
        return carts.getCart(userId(authentication));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> add(Authentication authentication, @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(carts.addItem(userId(authentication), request.productId(), request.quantity()));
    }

    @PutMapping("/items/{itemId}")
    public CartResponse update(Authentication authentication, @PathVariable UUID itemId,
                               @Valid @RequestBody CartItemQuantityRequest request) {
        return carts.updateItem(userId(authentication), itemId, request.quantity());
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> remove(Authentication authentication, @PathVariable UUID itemId) {
        carts.removeItem(userId(authentication), itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clear(Authentication authentication) {
        carts.clearCart(userId(authentication));
        return ResponseEntity.noContent().build();
    }

    private UUID userId(Authentication authentication) {
        return users.findIdByEmail(authentication.getName());
    }
}
