package ru.skfu.softwarestore.foundation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skfu.softwarestore.entity.Payment;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
}
