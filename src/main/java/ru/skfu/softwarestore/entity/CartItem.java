package ru.skfu.softwarestore.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cart_items", uniqueConstraints = @UniqueConstraint(
        name = "uk_cart_item_cart_product", columnNames = {"cart_id", "product_id"}))
@Check(constraints = "quantity > 0 AND quantity <= 99")
@Getter
@NoArgsConstructor
public class CartItem {
    public static final int MAX_QUANTITY = 99;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private SoftwareProduct product;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private CartItem(Cart cart, SoftwareProduct product, int quantity) {
        this.cart = cart;
        this.product = product;
        changeQuantity(quantity);
    }

    public static CartItem create(Cart cart, SoftwareProduct product, int quantity) {
        return new CartItem(cart, product, quantity);
    }

    public void increaseQuantity(int amount) {
        changeQuantity(quantity + amount);
    }

    public void changeQuantity(int quantity) {
        if (quantity <= 0 || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException("Количество товара должно быть от 1 до " + MAX_QUANTITY);
        }
        this.quantity = quantity;
        updatedAt = LocalDateTime.now();
    }

    @PrePersist
    @PreUpdate
    void validateAndUpdateTimestamp() {
        changeQuantity(quantity);
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
