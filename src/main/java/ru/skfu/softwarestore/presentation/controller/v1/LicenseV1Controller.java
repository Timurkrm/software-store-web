package ru.skfu.softwarestore.presentation.controller.v1;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.skfu.softwarestore.control.ILicenseService;
import ru.skfu.softwarestore.dto.LicenseResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/licenses")
@RequiredArgsConstructor
public class LicenseV1Controller {
    private final ILicenseService licenses;

    @GetMapping
    public List<LicenseResponse> own(Authentication authentication) {
        return licenses.findByUserEmail(authentication.getName());
    }
}
