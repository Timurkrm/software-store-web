package ru.skfu.softwarestore.mediator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skfu.softwarestore.control.ILicenseService;
import ru.skfu.softwarestore.dto.LicenseResponse;
import ru.skfu.softwarestore.entity.License;
import ru.skfu.softwarestore.entity.Order;
import ru.skfu.softwarestore.foundation.repository.LicenseRepository;
import ru.skfu.softwarestore.foundation.repository.UserRepository;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LicenseMediator implements ILicenseService {
    private final UserRepository users;
    private final LicenseRepository licenses;
    private final SecureRandom random = new SecureRandom();

    @Override
    public void issueFor(Order order) {
        for (var item : order.getItems()) {
            for (int issued = 0; issued < item.getQuantity(); issued++) {
                licenses.save(License.builder()
                        .user(order.getUser())
                        .product(item.getProduct())
                        .order(order)
                        .licenseKey(generateKey())
                        .build());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<LicenseResponse> findByUserEmail(String email) {
        var user = users.findByEmailIgnoreCase(email).orElseThrow();
        return licenses.findByUserIdOrderByIssuedAtDesc(user.getId()).stream()
                .map(license -> new LicenseResponse(license.getId(), license.getProduct().getId(),
                        license.getProduct().getName(), license.getLicenseKey(), license.getIssuedAt()))
                .toList();
    }

    private String generateKey() {
        byte[] bytes = new byte[18];
        random.nextBytes(bytes);
        StringBuilder key = new StringBuilder();
        for (int index = 0; index < bytes.length; index++) {
            if (index > 0 && index % 6 == 0) {
                key.append('-');
            }
            key.append(String.format("%02X", bytes[index]));
        }
        return key.toString();
    }
}
