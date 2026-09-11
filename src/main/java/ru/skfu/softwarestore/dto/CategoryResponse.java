package ru.skfu.softwarestore.dto;

import java.util.UUID;

public record CategoryResponse(UUID id, String name, String description) {
}
