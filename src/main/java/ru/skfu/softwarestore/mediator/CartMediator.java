package ru.skfu.softwarestore.mediator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skfu.softwarestore.control.ICartService;
import ru.skfu.softwarestore.dto.CartResponse;
import ru.skfu.softwarestore.entity.Cart;
import ru.skfu.softwarestore.entity.CartItem;
import ru.skfu.softwarestore.entity.ProductStatus;
import ru.skfu.softwarestore.entity.SoftwareProduct;
import ru.skfu.softwarestore.foundation.repository.CartItemRepository;
import ru.skfu.softwarestore.foundation.repository.CartRepository;
import ru.skfu.softwarestore.foundation.repository.ProductRepository;
import ru.skfu.softwarestore.foundation.repository.UserRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartMediator implements ICartService {
    private final UserRepository users;
    private final ProductRepository products;
    private final CartRepository carts;
    private final CartItemRepository cartItems;

    @Override
    @Transactional
    public CartResponse getCart(UUID userId) {
        return toResponse(findOrCreateCart(userId));
    }

    @Override
    @Transactional
    public CartResponse addItem(UUID userId, UUID productId, int quantity) {
        validateQuantity(quantity);
        Cart cart = findOrCreateCart(userId);
        SoftwareProduct product = products.findById(productId)
                .filter(found -> found.getStatus() == ProductStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("Товар недоступен"));

        cartItems.findByCartIdAndProductId(cart.getId(), productId).ifPresentOrElse(
                item -> item.increaseQuantity(quantity),
                () -> {
                    CartItem item = CartItem.create(cart, product, quantity);
                    cart.addItem(item);
                    cartItems.save(item);
                });
        cart.touch();
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateItem(UUID userId, UUID itemId, int quantity) {
        validateQuantity(quantity);
        CartItem item = findOwnedItem(userId, itemId);
        item.changeQuantity(quantity);
        item.getCart().touch();
        return toResponse(item.getCart());
    }

    @Override
    @Transactional
    public void removeItem(UUID userId, UUID itemId) {
        CartItem item = findOwnedItem(userId, itemId);
        item.getCart().removeItem(item);
    }

    @Override
    @Transactional
    public void clearCart(UUID userId) {
        findOrCreateCart(userId).clearItems();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTotal(UUID userId) {
        return carts.findByUserId(userId)
                .map(this::calculateTotal)
                .orElseGet(() -> {
                    users.findById(userId)
                            .orElseThrow(() -> new java.util.NoSuchElementException("Пользователь не найден"));
                    return BigDecimal.ZERO;
                });
    }

    private Cart findOrCreateCart(UUID userId) {
        return carts.findByUserId(userId).orElseGet(() -> carts.save(Cart.forUser(
                users.findById(userId).orElseThrow(() -> new java.util.NoSuchElementException("Пользователь не найден")))));
    }

    private CartItem findOwnedItem(UUID userId, UUID itemId) {
        return cartItems.findByIdAndCartUserId(itemId, userId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Позиция корзины не найдена"));
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0 || quantity > CartItem.MAX_QUANTITY) {
            throw new IllegalArgumentException("Количество товара должно быть от 1 до " + CartItem.MAX_QUANTITY);
        }
    }

    private BigDecimal calculateTotal(Cart cart) {
        return cart.getItems().stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private CartResponse toResponse(Cart cart) {
        var items = cart.getItems().stream().map(item -> new CartResponse.Item(item.getId(),
                item.getProduct().getId(), item.getProduct().getName(), item.getQuantity(),
                item.getProduct().getPrice(), item.getProduct().getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity())))).toList();
        return new CartResponse(cart.getId(), items, calculateTotal(cart), "RUB", cart.getUpdatedAt());
    }
}
