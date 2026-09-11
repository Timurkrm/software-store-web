package ru.skfu.softwarestore.foundation.bootstrap;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.skfu.softwarestore.entity.Category;
import ru.skfu.softwarestore.entity.ProductStatus;
import ru.skfu.softwarestore.entity.Role;
import ru.skfu.softwarestore.entity.SoftwareProduct;
import ru.skfu.softwarestore.entity.User;
import ru.skfu.softwarestore.foundation.repository.CategoryRepository;
import ru.skfu.softwarestore.foundation.repository.ProductRepository;
import ru.skfu.softwarestore.foundation.repository.UserRepository;

import java.math.BigDecimal;

@Profile("dev")
@Configuration
@RequiredArgsConstructor
public class DataInitializer {
    private final UserRepository users;
    private final CategoryRepository categories;
    private final ProductRepository products;
    private final PasswordEncoder encoder;

    @Value("${app.seed.admin-email:}") private String adminEmail;
    @Value("${app.seed.admin-password:}") private String adminPassword;

    @Bean
    CommandLineRunner seed() {
        return args -> {
            seedAdmin();
            Category ide = category("IDE и разработка", "Среды разработки и профессиональные IDE.");
            Category antivirus = category("Антивирусы", "Защита устройств и данных.");
            Category design = category("Графика и дизайн", "Инструменты для графики и иллюстраций.");
            Category office = category("Офисные программы", "Документы, таблицы и презентации.");
            Category utilities = category("Утилиты", "Полезные инструменты для работы с файлами.");
            Category softwareDevelopment = category("Разработка ПО", "Инструменты для сборки и поставки ПО.");

            product("IntelliJ IDEA Ultimate", "JetBrains", "Коммерческая IDE для Java и Kotlin.", "2026.1", "14990", ide, "Коммерческая", "Windows 10+, macOS 12+, Linux");
            product("Visual Studio Professional", "Microsoft", "IDE для .NET, C++ и облачных приложений.", "2022", "18990", ide, "Коммерческая", "Windows 10/11, 8 ГБ RAM");
            product("JetBrains WebStorm", "JetBrains", "IDE для JavaScript, TypeScript и frontend-разработки.", "2026.1", "9990", ide, "Коммерческая", "Windows 10+, macOS 12+, Linux");
            product("DataGrip", "JetBrains", "Инструмент для работы с SQL и базами данных.", "2026.1", "10990", ide, "Коммерческая", "Windows 10+, macOS 12+, Linux");
            product("PyCharm Professional", "JetBrains", "Профессиональная IDE для Python-разработки.", "2026.1", "9990", ide, "Коммерческая", "Windows 10+, macOS 12+, Linux");
            product("Microsoft Office Professional", "Microsoft", "Набор программ для документов, таблиц и презентаций.", "2024", "9990", office, "Коммерческая", "Windows 10/11, 4 ГБ RAM");
            product("Adobe Photoshop", "Adobe", "Профессиональный редактор растровой графики.", "2025", "12990", design, "Подписка", "Windows 10+, macOS 12+, 8 ГБ RAM");
            product("Adobe Illustrator", "Adobe", "Векторный редактор для иллюстраций и макетов.", "2025", "11990", design, "Подписка", "Windows 10+, macOS 12+, 8 ГБ RAM");
            product("ESET NOD32", "ESET", "Антивирусная защита для персональных компьютеров.", "18", "1990", antivirus, "На 1 устройство", "Windows 10/11, 1 ГБ RAM");
            product("WinRAR", "RARLAB", "Архиватор с поддержкой популярных форматов.", "7.10", "1490", utilities, "Бессрочная", "Windows 10/11");
            product("Total Commander", "Ghisler Software", "Двухпанельный файловый менеджер.", "11", "2190", utilities, "Бессрочная", "Windows 10/11");
            product("Docker Desktop Pro", "Docker", "Среда контейнерной разработки для команд.", "4.40", "14900", softwareDevelopment, "Годовая", "Windows 11 или macOS, 8 ГБ RAM");
        };
    }

    private void seedAdmin() {
        if (!adminEmail.isBlank() && !adminPassword.isBlank() && users.findByEmailIgnoreCase(adminEmail).isEmpty()) {
            users.save(User.builder().email(adminEmail.trim().toLowerCase())
                    .passwordHash(encoder.encode(adminPassword)).role(Role.ADMIN).build());
        }
    }

    private Category category(String name, String description) {
        return categories.findByNameIgnoreCase(name)
                .orElseGet(() -> categories.save(Category.builder().name(name).description(description).build()));
    }

    private void product(String name, String vendor, String description, String version, String price,
                         Category category, String licenseType, String requirements) {
        if (!products.existsByNameIgnoreCase(name)) {
            products.save(SoftwareProduct.builder().name(name).vendor(vendor).description(description)
                    .version(version).price(new BigDecimal(price)).category(category).licenseType(licenseType)
                    .systemRequirements(requirements).status(ProductStatus.ACTIVE).build());
        }
    }
}
