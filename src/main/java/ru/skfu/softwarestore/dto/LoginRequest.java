package ru.skfu.softwarestore.dto; import jakarta.validation.constraints.*; public record LoginRequest(@Email @NotBlank String email,@NotBlank String password){}
