package ru.skfu.softwarestore.mediator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.skfu.softwarestore.control.ICategoryService;
import ru.skfu.softwarestore.entity.Category;
import ru.skfu.softwarestore.foundation.repository.CategoryRepository;
import ru.skfu.softwarestore.foundation.repository.ProductRepository;
import ru.skfu.softwarestore.dto.CategoryRequest;
import ru.skfu.softwarestore.dto.CategoryResponse;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CategoryMediator implements ICategoryService {
    private final CategoryRepository categories;
    private final ProductRepository products;

    @Override
    public Category findOrCreate(String name) {
        return categories.findByNameIgnoreCase(name)
                .orElseGet(() -> categories.save(Category.builder().name(name).build()));
    }
    @Override public List<CategoryResponse> all(){ return categories.findAll().stream().map(this::response).toList(); }
    @Override public CategoryResponse get(UUID id){ return response(categories.findById(id).orElseThrow(() -> new NoSuchElementException("Category not found"))); }
    @Override public CategoryResponse create(CategoryRequest request){ if(categories.findByNameIgnoreCase(request.name()).isPresent()) throw new IllegalStateException("Category name already exists"); return response(categories.save(Category.builder().name(request.name().trim()).description(request.description()).build())); }
    @Override public CategoryResponse update(UUID id,CategoryRequest request){ var category=categories.findById(id).orElseThrow(() -> new NoSuchElementException("Category not found")); categories.findByNameIgnoreCase(request.name()).filter(found -> !found.getId().equals(id)).ifPresent(found -> {throw new IllegalStateException("Category name already exists");}); category.setName(request.name().trim()); category.setDescription(request.description()); return response(categories.save(category)); }
    @Override public void delete(UUID id){ var category=categories.findById(id).orElseThrow(() -> new NoSuchElementException("Category not found")); if(products.existsByCategoryId(id)) throw new IllegalStateException("Category is used by products"); categories.delete(category); }
    private CategoryResponse response(Category c){ return new CategoryResponse(c.getId(),c.getName(),c.getDescription()); }
}
