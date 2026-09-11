package ru.skfu.softwarestore.control;

import ru.skfu.softwarestore.entity.Order;
import ru.skfu.softwarestore.entity.Payment;

import java.math.BigDecimal;

public interface IPaymentService {
    Payment process(Order order, BigDecimal amount);
}
