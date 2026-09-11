package ru.skfu.softwarestore.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record CartItemQuantityRequest(@Min(1) @Max(99) int quantity) {
}
