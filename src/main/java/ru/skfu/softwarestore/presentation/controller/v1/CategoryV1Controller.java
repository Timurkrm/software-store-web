package ru.skfu.softwarestore.presentation.controller.v1;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.skfu.softwarestore.control.ICategoryService;
import ru.skfu.softwarestore.dto.CategoryRequest;
import ru.skfu.softwarestore.dto.CategoryResponse;
import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/categories") @RequiredArgsConstructor
public class CategoryV1Controller {
    private final ICategoryService categories;
    @GetMapping public List<CategoryResponse> all(){ return categories.all(); }
    @GetMapping("/{id}") public CategoryResponse get(@PathVariable UUID id){ return categories.get(id); }
    @PostMapping public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request){ return ResponseEntity.status(HttpStatus.CREATED).body(categories.create(request)); }
    @PutMapping("/{id}") public CategoryResponse update(@PathVariable UUID id,@Valid @RequestBody CategoryRequest request){ return categories.update(id,request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id){ categories.delete(id); return ResponseEntity.noContent().build(); }
}
