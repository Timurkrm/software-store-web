package ru.skfu.softwarestore.control;

import ru.skfu.softwarestore.entity.Category;
import ru.skfu.softwarestore.dto.CategoryRequest;
import ru.skfu.softwarestore.dto.CategoryResponse;
import java.util.List;
import java.util.UUID;

public interface ICategoryService {
    Category findOrCreate(String name);
    List<CategoryResponse> all();
    CategoryResponse get(UUID id);
    CategoryResponse create(CategoryRequest request);
    CategoryResponse update(UUID id, CategoryRequest request);
    void delete(UUID id);
}
