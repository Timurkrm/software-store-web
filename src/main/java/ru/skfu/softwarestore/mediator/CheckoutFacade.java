package ru.skfu.softwarestore.mediator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skfu.softwarestore.control.ILicenseService;
import ru.skfu.softwarestore.control.IOrderService;
import ru.skfu.softwarestore.control.IPaymentService;
import ru.skfu.softwarestore.dto.CheckoutRequest;
import ru.skfu.softwarestore.dto.OrderResponse;
import ru.skfu.softwarestore.entity.Order;
import ru.skfu.softwarestore.entity.OrderItem;
import ru.skfu.softwarestore.entity.OrderStatus;
import ru.skfu.softwarestore.entity.ProductStatus;
import ru.skfu.softwarestore.foundation.repository.OrderRepository;
import ru.skfu.softwarestore.foundation.repository.ProductRepository;
import ru.skfu.softwarestore.foundation.repository.UserRepository;
import ru.skfu.softwarestore.foundation.repository.CartRepository;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckoutFacade implements IOrderService {
    private final UserRepository users;
    private final ProductRepository products;
    private final OrderRepository orders;
    private final CartRepository carts;
    private final IPaymentService payments;
    private final ILicenseService licenses;

    @Override
    @Transactional
    public OrderResponse checkout(String email, CheckoutRequest request) {
        var user = users.findByEmailIgnoreCase(email).orElseThrow();
        if (request.items().isEmpty()) {
            throw new IllegalArgumentException("Корзина пуста");
        }

        var order = Order.builder()
                .user(user)
                .status(OrderStatus.CREATED)
                .currency("RUB")
                .totalAmount(BigDecimal.ZERO)
                .build();
        BigDecimal total = BigDecimal.ZERO;
        Set<java.util.UUID> productIds = new HashSet<>();

        for (var requestedItem : request.items()) {
            if (!productIds.add(requestedItem.productId())) {
                throw new IllegalArgumentException("Товар не может повторяться в заказе");
            }
            var product = products.findById(requestedItem.productId())
                    .filter(found -> found.getStatus() == ProductStatus.ACTIVE)
                    .orElseThrow(() -> new IllegalArgumentException("Товар недоступен"));
            order.getItems().add(OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(requestedItem.quantity())
                    .unitPrice(product.getPrice())
                    .build());
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(requestedItem.quantity())));
        }

        order.setTotalAmount(total);
        order.setStatus(OrderStatus.PAID);
        order = orders.save(order);
        payments.process(order, total);
        licenses.issueFor(order);
        order.setStatus(OrderStatus.COMPLETED);
        order = orders.save(order);
        return toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> history(String email) {
        var user = users.findByEmailIgnoreCase(email).orElseThrow();
        return orders.findByUserIdOrderByCreatedAtDesc(user.getId()).stream().map(this::toResponse).toList();
    }
    @Override @Transactional public OrderResponse createFromCart(String email){var user=users.findByEmailIgnoreCase(email).orElseThrow();var cart=carts.findByUserId(user.getId()).orElseThrow(()->new IllegalArgumentException("Cart is empty"));if(cart.getItems().isEmpty())throw new IllegalArgumentException("Cart is empty");var order=Order.builder().user(user).status(OrderStatus.PENDING_PAYMENT).currency("RUB").totalAmount(BigDecimal.ZERO).build();BigDecimal total=BigDecimal.ZERO;for(var ci:cart.getItems()){var p=ci.getProduct();if(p.getStatus()!=ProductStatus.ACTIVE)throw new IllegalArgumentException("Product unavailable");order.getItems().add(OrderItem.builder().order(order).product(p).quantity(ci.getQuantity()).unitPrice(p.getPrice()).build());total=total.add(p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));}order.setTotalAmount(total);order=orders.save(order);cart.clearItems();return toResponse(order);}
    @Override @Transactional(readOnly = true) public OrderResponse getOwn(String email,UUID id){var u=users.findByEmailIgnoreCase(email).orElseThrow();return toResponse(orders.findByIdAndUserId(id,u.getId()).orElseThrow());}
    @Override @Transactional public OrderResponse pay(String email,UUID id){var order=getOrder(email,id);if(order.getStatus()!=OrderStatus.PENDING_PAYMENT)throw new IllegalStateException("Order cannot be paid");order.setStatus(OrderStatus.PAID);payments.process(order,order.getTotalAmount());licenses.issueFor(order);order.setStatus(OrderStatus.COMPLETED);return toResponse(orders.save(order));}
    @Override @Transactional(readOnly = true) public List<OrderResponse> allForAdmin(){return orders.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();}
    @Override @Transactional public OrderResponse updateStatus(UUID id,OrderStatus status){var order=orders.findById(id).orElseThrow(()->new NoSuchElementException("Order not found"));if(!isAllowedTransition(order.getStatus(),status))throw new IllegalStateException("Order status transition is not allowed");order.setStatus(status);return toResponse(orders.save(order));}
    private boolean isAllowedTransition(OrderStatus from,OrderStatus to){if(from==to)return true;return switch(from){case CREATED->to==OrderStatus.PENDING_PAYMENT||to==OrderStatus.CANCELLED;case PENDING_PAYMENT->to==OrderStatus.CANCELLED;case PAID,COMPLETED,CANCELLED,FAILED->false;};}
    private Order getOrder(String email,UUID id){var u=users.findByEmailIgnoreCase(email).orElseThrow();return orders.findByIdAndUserId(id,u.getId()).orElseThrow();}

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(order.getId(), order.getStatus().name(), order.getTotalAmount(),
                order.getCurrency(), order.getCreatedAt(), order.getItems().stream()
                .map(item -> new OrderResponse.Line(item.getProduct().getId(), item.getProduct().getName(),
                        item.getQuantity(), item.getUnitPrice()))
                .toList());
    }
}
