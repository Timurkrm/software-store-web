package ru.skfu.softwarestore.dto; import java.util.UUID; public record AuthResponse(String token,UUID userId,String email,String role){}
