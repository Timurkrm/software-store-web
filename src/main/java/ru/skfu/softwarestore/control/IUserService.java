package ru.skfu.softwarestore.control;

import ru.skfu.softwarestore.dto.AuthResponse;
import ru.skfu.softwarestore.dto.LoginRequest;
import ru.skfu.softwarestore.dto.RegisterRequest;

import java.util.UUID;

public interface IUserService {
    void register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UUID findIdByEmail(String email);
}
