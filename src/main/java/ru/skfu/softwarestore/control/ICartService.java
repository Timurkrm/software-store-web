package ru.skfu.softwarestore.control;

import ru.skfu.softwarestore.dto.CartResponse;

import java.math.BigDecimal;
import java.util.UUID;

public interface ICartService {
    CartResponse getCart(UUID userId);

    CartResponse addItem(UUID userId, UUID productId, int quantity);

    CartResponse updateItem(UUID userId, UUID itemId, int quantity);

    void removeItem(UUID userId, UUID itemId);

    void clearCart(UUID userId);

    BigDecimal calculateTotal(UUID userId);
}
