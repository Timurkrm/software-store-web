package ru.skfu.softwarestore.entity;

public enum OrderStatus {
    CREATED,
    PENDING_PAYMENT,
    PAID,
    COMPLETED,
    CANCELLED,
    FAILED
}
