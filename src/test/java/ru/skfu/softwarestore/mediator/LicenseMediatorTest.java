package ru.skfu.softwarestore.mediator;

import org.junit.jupiter.api.Test;
import ru.skfu.softwarestore.entity.License;
import ru.skfu.softwarestore.entity.Order;
import ru.skfu.softwarestore.entity.OrderItem;
import ru.skfu.softwarestore.entity.SoftwareProduct;
import ru.skfu.softwarestore.entity.User;
import ru.skfu.softwarestore.foundation.repository.LicenseRepository;
import ru.skfu.softwarestore.foundation.repository.UserRepository;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class LicenseMediatorTest {
    @Test
    void issuesOneLicenseForEachPurchasedUnit() {
        LicenseRepository licenses = mock(LicenseRepository.class);
        LicenseMediator mediator = new LicenseMediator(mock(UserRepository.class), licenses);
        User user = User.builder().email("user@example.test").build();
        SoftwareProduct product = SoftwareProduct.builder()
                .name("Product").vendor("Vendor").description("Description")
                .price(BigDecimal.ONE).version("1.0").build();
        Order order = Order.builder().user(user).items(new ArrayList<>()).build();
        order.getItems().add(OrderItem.builder().order(order).product(product)
                .quantity(3).unitPrice(BigDecimal.ONE).build());

        mediator.issueFor(order);

        verify(licenses, times(3)).save(any(License.class));
    }
}
