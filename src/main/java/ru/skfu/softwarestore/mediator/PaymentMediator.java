package ru.skfu.softwarestore.mediator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.skfu.softwarestore.control.IPaymentService;
import ru.skfu.softwarestore.entity.Order;
import ru.skfu.softwarestore.entity.Payment;
import ru.skfu.softwarestore.entity.PaymentStatus;
import ru.skfu.softwarestore.foundation.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentMediator implements IPaymentService {
    private final PaymentRepository payments;

    @Override
    public Payment process(Order order, BigDecimal amount) {
        return payments.save(Payment.builder()
                .order(order)
                .amount(amount)
                .status(PaymentStatus.SUCCESS)
                .transactionId("TX-" + UUID.randomUUID())
                .build());
    }
}
