package ru.skfu.softwarestore.presentation.controller.v1;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.skfu.softwarestore.control.IOrderService;
import ru.skfu.softwarestore.dto.OrderResponse;
import ru.skfu.softwarestore.dto.OrderStatusUpdateRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
public class AdminOrderV1Controller {
    private final IOrderService orders;

    @GetMapping
    public List<OrderResponse> all() { return orders.allForAdmin(); }

    @PutMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable UUID id,
                                      @Valid @RequestBody OrderStatusUpdateRequest request) {
        return orders.updateStatus(id, request.status());
    }
}
