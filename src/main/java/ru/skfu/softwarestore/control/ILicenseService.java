package ru.skfu.softwarestore.control;

import ru.skfu.softwarestore.dto.LicenseResponse;
import ru.skfu.softwarestore.entity.Order;

import java.util.List;

public interface ILicenseService {
    void issueFor(Order order);

    List<LicenseResponse> findByUserEmail(String email);
}
