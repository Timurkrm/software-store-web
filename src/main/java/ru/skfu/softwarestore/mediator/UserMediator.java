package ru.skfu.softwarestore.mediator;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skfu.softwarestore.control.IUserService;
import ru.skfu.softwarestore.dto.AuthResponse;
import ru.skfu.softwarestore.dto.LoginRequest;
import ru.skfu.softwarestore.dto.RegisterRequest;
import ru.skfu.softwarestore.entity.Role;
import ru.skfu.softwarestore.entity.User;
import ru.skfu.softwarestore.foundation.repository.UserRepository;
import ru.skfu.softwarestore.security.JwtService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserMediator implements IUserService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (users.findByEmailIgnoreCase(request.email()).isPresent()) {
            throw new IllegalArgumentException("Пользователь уже существует");
        }
        users.save(User.builder()
                .email(request.email().trim().toLowerCase())
                .passwordHash(encoder.encode(request.password()))
                .role(Role.USER)
                .build());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        var user = users.findByEmailIgnoreCase(request.email().trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Неверный email или пароль"));
        if (!encoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Неверный email или пароль");
        }
        return new AuthResponse(jwt.generate(user.getEmail(), user.getRole().name()),
                user.getId(), user.getEmail(), user.getRole().name());
    }

    @Override
    public UUID findIdByEmail(String email) {
        return users.findByEmailIgnoreCase(email).orElseThrow().getId();
    }
}
