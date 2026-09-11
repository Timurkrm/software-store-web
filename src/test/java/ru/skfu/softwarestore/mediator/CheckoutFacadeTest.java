package ru.skfu.softwarestore.mediator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.skfu.softwarestore.control.ILicenseService;
import ru.skfu.softwarestore.control.IPaymentService;
import ru.skfu.softwarestore.entity.Cart;
import ru.skfu.softwarestore.entity.CartItem;
import ru.skfu.softwarestore.entity.Order;
import ru.skfu.softwarestore.entity.OrderItem;
import ru.skfu.softwarestore.entity.OrderStatus;
import ru.skfu.softwarestore.entity.ProductStatus;
import ru.skfu.softwarestore.entity.SoftwareProduct;
import ru.skfu.softwarestore.entity.User;
import ru.skfu.softwarestore.foundation.repository.CartRepository;
import ru.skfu.softwarestore.foundation.repository.OrderRepository;
import ru.skfu.softwarestore.foundation.repository.ProductRepository;
import ru.skfu.softwarestore.foundation.repository.UserRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CheckoutFacadeTest {
    private final UUID userId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private UserRepository users;
    private CartRepository carts;
    private OrderRepository orders;
    private IPaymentService payments;
    private ILicenseService licenses;
    private CheckoutFacade facade;
    private User user;
    private Cart cart;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        carts = mock(CartRepository.class);
        orders = mock(OrderRepository.class);
        payments = mock(IPaymentService.class);
        licenses = mock(ILicenseService.class);
        facade = new CheckoutFacade(users, mock(ProductRepository.class), orders, carts, payments, licenses);

        user = User.builder().email("user@example.test").build();
        user.setId(userId);
        cart = Cart.forUser(user);
        SoftwareProduct product = SoftwareProduct.builder()
                .name("Product").vendor("Vendor").description("Description")
                .price(new BigDecimal("50.00")).version("1.0")
                .status(ProductStatus.ACTIVE).build();
        product.setId(UUID.randomUUID());
        cart.addItem(CartItem.create(cart, product, 2));

        when(users.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(carts.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(orders.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsPendingOrderFromCartUsingCurrentServerPrice() {
        var response = facade.createFromCart(user.getEmail());

        assertEquals("PENDING_PAYMENT", response.status());
        assertEquals(new BigDecimal("100.00"), response.totalAmount());
        assertEquals(0, cart.getItems().size());
        verify(orders).save(any(Order.class));
    }

    @Test
    void rejectsEmptyCart() {
        cart.clearItems();

        assertThrows(IllegalArgumentException.class, () -> facade.createFromCart(user.getEmail()));
    }

    @Test
    void paysOnlyPendingOrderAndIssuesLicenses() {
        Order order = Order.builder().id(orderId).user(user).status(OrderStatus.PENDING_PAYMENT)
                .totalAmount(new BigDecimal("100.00")).build();
        when(orders.findByIdAndUserId(orderId, userId)).thenReturn(Optional.of(order));

        var response = facade.pay(user.getEmail(), orderId);

        assertEquals("COMPLETED", response.status());
        verify(payments, times(1)).process(order, new BigDecimal("100.00"));
        verify(licenses, times(1)).issueFor(order);
    }

    @Test
    void rejectsDuplicateOrCancelledPayment() {
        Order order = Order.builder().id(orderId).user(user).status(OrderStatus.CANCELLED)
                .totalAmount(new BigDecimal("100.00")).build();
        when(orders.findByIdAndUserId(orderId, userId)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> facade.pay(user.getEmail(), orderId));

        verify(payments, never()).process(any(), any());
        verify(licenses, never()).issueFor(any());
    }

    @Test
    void historyMapsOrderItemsAndProducts() {
        Order order = orderWithLine(orderId);
        when(orders.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(order));

        var history = facade.history(user.getEmail());

        assertEquals(1, history.size());
        assertEquals("Product", history.get(0).items().get(0).productName());
        verify(orders).findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    void ownOrderDetailMapsOrderItemsAndProducts() {
        Order order = orderWithLine(orderId);
        when(orders.findByIdAndUserId(orderId, userId)).thenReturn(Optional.of(order));

        var response = facade.getOwn(user.getEmail(), orderId);

        assertEquals(orderId, response.id());
        assertEquals("Product", response.items().get(0).productName());
        verify(orders).findByIdAndUserId(orderId, userId);
    }

    @Test
    void adminCannotMarkPendingOrderAsPaidWithoutPaymentFlow() {
        Order order = Order.builder().id(orderId).user(user).status(OrderStatus.PENDING_PAYMENT)
                .totalAmount(new BigDecimal("100.00")).build();
        when(orders.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> facade.updateStatus(orderId, OrderStatus.PAID));

        verify(orders, never()).save(order);
    }

    private Order orderWithLine(UUID id) {
        SoftwareProduct product = SoftwareProduct.builder()
                .id(UUID.randomUUID()).name("Product").vendor("Vendor").description("Description")
                .price(new BigDecimal("50.00")).version("1.0").status(ProductStatus.ACTIVE).build();
        Order order = Order.builder().id(id).user(user).status(OrderStatus.PENDING_PAYMENT)
                .totalAmount(new BigDecimal("50.00")).items(new ArrayList<>()).build();
        order.getItems().add(OrderItem.builder().order(order).product(product)
                .quantity(1).unitPrice(new BigDecimal("50.00")).build());
        return order;
    }
}
