package ru.skfu.softwarestore.presentation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.skfu.softwarestore.config.SecurityConfig;
import ru.skfu.softwarestore.control.ICartService;
import ru.skfu.softwarestore.control.ICategoryService;
import ru.skfu.softwarestore.control.ILicenseService;
import ru.skfu.softwarestore.control.IOrderService;
import ru.skfu.softwarestore.control.IProductService;
import ru.skfu.softwarestore.control.IUserService;
import ru.skfu.softwarestore.dto.CartResponse;
import ru.skfu.softwarestore.entity.Role;
import ru.skfu.softwarestore.entity.User;
import ru.skfu.softwarestore.foundation.repository.UserRepository;
import ru.skfu.softwarestore.presentation.controller.ExceptionHandlerController;
import ru.skfu.softwarestore.presentation.controller.v1.AdminOrderV1Controller;
import ru.skfu.softwarestore.presentation.controller.v1.CartV1Controller;
import ru.skfu.softwarestore.presentation.controller.v1.CategoryV1Controller;
import ru.skfu.softwarestore.presentation.controller.v1.LicenseV1Controller;
import ru.skfu.softwarestore.presentation.controller.v1.OrderV1Controller;
import ru.skfu.softwarestore.presentation.controller.v1.ProductV1Controller;
import ru.skfu.softwarestore.security.JwtAuthFilter;
import ru.skfu.softwarestore.security.JwtService;
import ru.skfu.softwarestore.security.RestAccessDeniedHandler;
import ru.skfu.softwarestore.security.RestAuthenticationEntryPoint;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        ProductV1Controller.class, CategoryV1Controller.class, CartV1Controller.class,
        OrderV1Controller.class, LicenseV1Controller.class, AdminOrderV1Controller.class
}, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
@Import({SecurityConfig.class, JwtAuthFilter.class, RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class, ExceptionHandlerController.class})
class ApiSecurityMvcTest {
    @Autowired
    private MockMvc mvc;

    @MockBean private IProductService products;
    @MockBean private ICategoryService categories;
    @MockBean private ICartService carts;
    @MockBean private IUserService users;
    @MockBean private IOrderService orders;
    @MockBean private ILicenseService licenses;
    @MockBean private JwtService jwtService;
    @MockBean private UserRepository userRepository;

    @Test
    void publicProductsAreAvailable() throws Exception {
        when(products.all(null)).thenReturn(List.of());

        mvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk());
    }

    @Test
    void publicCategoriesAreAvailable() throws Exception {
        when(categories.all()).thenReturn(List.of());

        mvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk());
    }

    @Test
    void unauthenticatedCartReturnsJson401() throws Exception {
        mvc.perform(get("/api/v1/cart"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.path").value("/api/v1/cart"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCannotCreateProductAndReceivesJson403() throws Exception {
        mvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void invalidProductRequestReturns400() throws Exception {
        mvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void unknownProductReturns404() throws Exception {
        when(products.get(any(UUID.class))).thenThrow(new NoSuchElementException("Product not found"));

        mvc.perform(get("/api/v1/products/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(username = "user@example.test", roles = "USER")
    void authenticatedUserCanReadCartAndOrders() throws Exception {
        UUID userId = UUID.randomUUID();
        when(users.findIdByEmail("user@example.test")).thenReturn(userId);
        when(carts.getCart(userId)).thenReturn(new CartResponse(UUID.randomUUID(), List.of(),
                BigDecimal.ZERO, "RUB", LocalDateTime.now()));
        when(orders.history("user@example.test")).thenReturn(List.of());

        mvc.perform(get("/api/v1/cart")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/orders")).andExpect(status().isOk());
    }

    @Test
    void validJwtAllowsProtectedCartRequest() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).email("user@example.test")
                .passwordHash("hash").role(Role.USER).build();
        when(jwtService.valid("valid-token")).thenReturn(true);
        when(jwtService.extractEmail("valid-token")).thenReturn(user.getEmail());
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(users.findIdByEmail(user.getEmail())).thenReturn(userId);
        when(carts.getCart(userId)).thenReturn(new CartResponse(UUID.randomUUID(), List.of(),
                BigDecimal.ZERO, "RUB", LocalDateTime.now()));

        mvc.perform(get("/api/v1/cart").header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCannotAccessAdminOrders() throws Exception {
        mvc.perform(get("/api/v1/admin/orders"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }
}
