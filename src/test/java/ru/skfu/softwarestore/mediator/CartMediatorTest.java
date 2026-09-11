package ru.skfu.softwarestore.mediator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.skfu.softwarestore.entity.Cart;
import ru.skfu.softwarestore.entity.CartItem;
import ru.skfu.softwarestore.entity.ProductStatus;
import ru.skfu.softwarestore.entity.SoftwareProduct;
import ru.skfu.softwarestore.entity.User;
import ru.skfu.softwarestore.foundation.repository.CartItemRepository;
import ru.skfu.softwarestore.foundation.repository.CartRepository;
import ru.skfu.softwarestore.foundation.repository.ProductRepository;
import ru.skfu.softwarestore.foundation.repository.UserRepository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CartMediatorTest {
    private final UUID userId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final UUID itemId = UUID.randomUUID();

    private UserRepository users;
    private ProductRepository products;
    private CartRepository carts;
    private CartItemRepository cartItems;
    private CartMediator mediator;
    private User user;
    private SoftwareProduct product;
    private Cart cart;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        products = mock(ProductRepository.class);
        carts = mock(CartRepository.class);
        cartItems = mock(CartItemRepository.class);
        mediator = new CartMediator(users, products, carts, cartItems);

        user = User.builder().email("user@example.test").build();
        user.setId(userId);
        product = SoftwareProduct.builder()
                .name("Product")
                .vendor("Vendor")
                .description("Description")
                .price(new BigDecimal("100.00"))
                .version("1.0")
                .status(ProductStatus.ACTIVE)
                .build();
        product.setId(productId);
        cart = Cart.forUser(user);

        when(carts.findByUserId(userId)).thenReturn(Optional.of(cart));
    }

    @Test
    void addsActiveProductToCart() {
        when(products.findById(productId)).thenReturn(Optional.of(product));
        when(cartItems.findByCartIdAndProductId(null, productId)).thenReturn(Optional.empty());

        mediator.addItem(userId, productId, 2);

        assertEquals(1, cart.getItems().size());
        assertEquals(2, cart.getItems().get(0).getQuantity());
        verify(cartItems).save(any(CartItem.class));
    }

    @Test
    void addsQuantityWhenProductAlreadyExistsInCart() {
        CartItem item = CartItem.create(cart, product, 2);
        cart.addItem(item);
        when(products.findById(productId)).thenReturn(Optional.of(product));
        when(cartItems.findByCartIdAndProductId(null, productId)).thenReturn(Optional.of(item));

        mediator.addItem(userId, productId, 3);

        assertEquals(5, item.getQuantity());
        assertEquals(1, cart.getItems().size());
        verify(cartItems, never()).save(any(CartItem.class));
    }

    @Test
    void rejectsInvalidQuantity() {
        assertThrows(IllegalArgumentException.class, () -> mediator.addItem(userId, productId, 0));
        assertThrows(IllegalArgumentException.class, () -> mediator.addItem(userId, productId, 100));
    }

    @Test
    void rejectsInactiveProduct() {
        product.setStatus(ProductStatus.ARCHIVED);
        when(products.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(IllegalArgumentException.class, () -> mediator.addItem(userId, productId, 1));
    }

    @Test
    void updatesOwnedCartItemQuantity() {
        CartItem item = CartItem.create(cart, product, 1);
        when(cartItems.findByIdAndCartUserId(itemId, userId)).thenReturn(Optional.of(item));

        mediator.updateItem(userId, itemId, 7);

        assertEquals(7, item.getQuantity());
    }

    @Test
    void removesOwnedCartItem() {
        CartItem item = CartItem.create(cart, product, 1);
        cart.addItem(item);
        when(cartItems.findByIdAndCartUserId(itemId, userId)).thenReturn(Optional.of(item));

        mediator.removeItem(userId, itemId);

        assertEquals(0, cart.getItems().size());
    }

    @Test
    void clearsOnlyCurrentUsersCart() {
        cart.addItem(CartItem.create(cart, product, 1));

        mediator.clearCart(userId);

        assertEquals(0, cart.getItems().size());
    }

    @Test
    void rejectsAccessToAnotherUsersCartItem() {
        when(cartItems.findByIdAndCartUserId(itemId, userId)).thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class,
                () -> mediator.updateItem(userId, itemId, 1));
        verify(cartItems).findByIdAndCartUserId(eq(itemId), eq(userId));
    }

    @Test
    void calculatesTotalFromCurrentProductPrices() {
        cart.addItem(CartItem.create(cart, product, 2));

        assertDoesNotThrow(() -> mediator.calculateTotal(userId));
        assertEquals(new BigDecimal("200.00"), mediator.calculateTotal(userId));
    }
}
