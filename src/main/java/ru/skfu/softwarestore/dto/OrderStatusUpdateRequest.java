package ru.skfu.softwarestore.dto;

import jakarta.validation.constraints.NotNull;
import ru.skfu.softwarestore.entity.OrderStatus;

public record OrderStatusUpdateRequest(@NotNull OrderStatus status) {}
