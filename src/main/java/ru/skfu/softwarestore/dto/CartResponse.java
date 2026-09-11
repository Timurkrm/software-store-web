package ru.skfu.softwarestore.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CartResponse(UUID id, List<Item> items, BigDecimal totalAmount, String currency,
                           LocalDateTime updatedAt) {
    public record Item(UUID id, UUID productId, String productName, int quantity,
                       BigDecimal unitPrice, BigDecimal lineTotal) {
    }
}
