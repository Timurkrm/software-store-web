package ru.skfu.softwarestore.mediator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.skfu.softwarestore.dto.LoginRequest;
import ru.skfu.softwarestore.dto.RegisterRequest;
import ru.skfu.softwarestore.entity.Role;
import ru.skfu.softwarestore.entity.User;
import ru.skfu.softwarestore.foundation.repository.UserRepository;
import ru.skfu.softwarestore.security.JwtService;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserMediatorTest {
    private UserRepository users;
    private PasswordEncoder encoder;
    private JwtService jwt;
    private UserMediator mediator;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        encoder = mock(PasswordEncoder.class);
        jwt = mock(JwtService.class);
        mediator = new UserMediator(users, encoder, jwt);
    }

    @Test
    void registersNewUserWithEncodedPassword() {
        when(users.findByEmailIgnoreCase("user@example.test")).thenReturn(Optional.empty());
        when(encoder.encode("secret123")).thenReturn("encoded-password");

        mediator.register(new RegisterRequest(" User@Example.Test ", "secret123"));

        ArgumentCaptor<User> user = ArgumentCaptor.forClass(User.class);
        verify(users).save(user.capture());
        assertEquals("user@example.test", user.getValue().getEmail());
        assertEquals("encoded-password", user.getValue().getPasswordHash());
        assertEquals(Role.USER, user.getValue().getRole());
    }

    @Test
    void rejectsDuplicateEmailDuringRegistration() {
        when(users.findByEmailIgnoreCase("user@example.test")).thenReturn(Optional.of(User.builder().build()));

        assertThrows(IllegalArgumentException.class,
                () -> mediator.register(new RegisterRequest("user@example.test", "secret123")));
    }

    @Test
    void logsInWithValidCredentials() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).email("user@example.test")
                .passwordHash("encoded-password").role(Role.USER).build();
        when(users.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(encoder.matches("secret123", "encoded-password")).thenReturn(true);
        when(jwt.generate(user.getEmail(), "USER")).thenReturn("jwt-token");

        var response = mediator.login(new LoginRequest(user.getEmail(), "secret123"));

        assertEquals("jwt-token", response.token());
        assertEquals(userId, response.userId());
        assertEquals("USER", response.role());
    }

    @Test
    void rejectsInvalidCredentials() {
        User user = User.builder().email("user@example.test").passwordHash("encoded-password").build();
        when(users.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(encoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> mediator.login(new LoginRequest(user.getEmail(), "wrong-password")));
    }
}
