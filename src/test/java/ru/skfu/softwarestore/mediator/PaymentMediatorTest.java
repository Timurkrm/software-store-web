package ru.skfu.softwarestore.mediator;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.skfu.softwarestore.entity.Order;
import ru.skfu.softwarestore.entity.Payment;
import ru.skfu.softwarestore.entity.PaymentStatus;
import ru.skfu.softwarestore.foundation.repository.PaymentRepository;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentMediatorTest {
    @Test
    void savesSuccessfulPaymentForOrderAmount() {
        PaymentRepository payments = mock(PaymentRepository.class);
        PaymentMediator mediator = new PaymentMediator(payments);
        Order order = Order.builder().build();
        BigDecimal amount = new BigDecimal("199.00");
        when(payments.save(org.mockito.ArgumentMatchers.any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mediator.process(order, amount);

        ArgumentCaptor<Payment> payment = ArgumentCaptor.forClass(Payment.class);
        verify(payments).save(payment.capture());
        assertEquals(order, payment.getValue().getOrder());
        assertEquals(amount, payment.getValue().getAmount());
        assertEquals(PaymentStatus.SUCCESS, payment.getValue().getStatus());
        assertTrue(payment.getValue().getTransactionId().startsWith("TX-"));
    }
}
