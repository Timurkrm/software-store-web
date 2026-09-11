package ru.skfu.softwarestore.control;

import ru.skfu.softwarestore.dto.CheckoutRequest;
import ru.skfu.softwarestore.dto.OrderResponse;
import ru.skfu.softwarestore.entity.OrderStatus;

import java.util.List;
import java.util.UUID;

public interface IOrderService {
    OrderResponse checkout(String email, CheckoutRequest request);

    List<OrderResponse> history(String email);
    OrderResponse createFromCart(String email);
    OrderResponse getOwn(String email, UUID id);
    OrderResponse pay(String email, UUID id);
    List<OrderResponse> allForAdmin();
    OrderResponse updateStatus(UUID id, OrderStatus status);
}
