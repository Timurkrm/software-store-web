package ru.skfu.softwarestore.dto; import jakarta.validation.constraints.*; public record RegisterRequest(@Email @NotBlank String email,@NotBlank @Size(min=6,max=72) String password){}
