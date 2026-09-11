package ru.skfu.softwarestore.mediator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.skfu.softwarestore.control.ICategoryService;
import ru.skfu.softwarestore.dto.ProductRequest;
import ru.skfu.softwarestore.entity.Category;
import ru.skfu.softwarestore.entity.ProductStatus;
import ru.skfu.softwarestore.entity.SoftwareProduct;
import ru.skfu.softwarestore.foundation.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductMediatorTest {
    private ProductRepository products;
    private ICategoryService categories;
    private ProductMediator mediator;
    private Category category;

    @BeforeEach
    void setUp() {
        products = mock(ProductRepository.class);
        categories = mock(ICategoryService.class);
        mediator = new ProductMediator(products, categories);
        category = Category.builder().id(UUID.randomUUID()).name("Development").build();
        when(categories.findOrCreate("Development")).thenReturn(category);
    }

    @Test
    void createsActiveProduct() {
        when(products.save(org.mockito.ArgumentMatchers.any(SoftwareProduct.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = mediator.create(request());

        assertEquals("IDE", response.name());
        assertEquals("Development", response.category());
        verify(products).save(org.mockito.ArgumentMatchers.any(SoftwareProduct.class));
    }

    @Test
    void updatesExistingProduct() {
        UUID id = UUID.randomUUID();
        SoftwareProduct product = product(id);
        when(products.findById(id)).thenReturn(Optional.of(product));
        when(products.save(product)).thenReturn(product);
        ProductRequest updated = new ProductRequest("Updated IDE", "Vendor", "Description",
                new BigDecimal("249.00"), "2.0", null, "Development");

        var response = mediator.update(id, updated);

        assertEquals("Updated IDE", response.name());
        assertEquals(new BigDecimal("249.00"), response.price());
    }

    @Test
    void archivesExistingProduct() {
        UUID id = UUID.randomUUID();
        SoftwareProduct product = product(id);
        when(products.findById(id)).thenReturn(Optional.of(product));

        mediator.archive(id);

        assertEquals(ProductStatus.ARCHIVED, product.getStatus());
        verify(products).save(product);
    }

    private ProductRequest request() {
        return new ProductRequest("IDE", "Vendor", "Description", new BigDecimal("199.00"),
                "1.0", null, "Development");
    }

    private SoftwareProduct product(UUID id) {
        return SoftwareProduct.builder().id(id).name("IDE").vendor("Vendor")
                .description("Description").price(new BigDecimal("199.00")).version("1.0")
                .category(category).status(ProductStatus.ACTIVE).build();
    }
}
