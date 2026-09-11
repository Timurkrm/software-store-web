package ru.skfu.softwarestore.mediator;

import org.junit.jupiter.api.Test;
import ru.skfu.softwarestore.dto.CategoryRequest;
import ru.skfu.softwarestore.entity.Category;
import ru.skfu.softwarestore.foundation.repository.CategoryRepository;
import ru.skfu.softwarestore.foundation.repository.ProductRepository;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CategoryMediatorTest {
    private final CategoryRepository categories=mock(CategoryRepository.class);
    private final ProductRepository products=mock(ProductRepository.class);
    private final CategoryMediator mediator=new CategoryMediator(categories,products);
    @Test void createsCategory(){ when(categories.findByNameIgnoreCase("Office")).thenReturn(Optional.empty()); when(categories.save(any())).thenAnswer(i->i.getArgument(0)); assertEquals("Office",mediator.create(new CategoryRequest("Office",null)).name()); }
    @Test void rejectsDuplicate(){ when(categories.findByNameIgnoreCase("Office")).thenReturn(Optional.of(Category.builder().name("Office").build())); assertThrows(IllegalStateException.class,()->mediator.create(new CategoryRequest("Office",null))); }
    @Test void updatesCategory(){ UUID id=UUID.randomUUID(); Category category=Category.builder().id(id).name("Old").build(); when(categories.findById(id)).thenReturn(Optional.of(category)); when(categories.findByNameIgnoreCase("New")).thenReturn(Optional.empty()); when(categories.save(category)).thenReturn(category); assertEquals("New",mediator.update(id,new CategoryRequest("New","Description")).name()); }
    @Test void reportsMissingCategory(){ assertThrows(java.util.NoSuchElementException.class,()->mediator.get(UUID.randomUUID())); }
    @Test void rejectsDeleteWhenProductsExist(){ UUID id=UUID.randomUUID(); when(categories.findById(id)).thenReturn(Optional.of(Category.builder().id(id).name("Office").build())); when(products.existsByCategoryId(id)).thenReturn(true); assertThrows(IllegalStateException.class,()->mediator.delete(id)); }
}
