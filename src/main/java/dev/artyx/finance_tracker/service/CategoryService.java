package dev.artyx.finance_tracker.service;

import dev.artyx.finance_tracker.entity.Category;
import dev.artyx.finance_tracker.entity.User;
import dev.artyx.finance_tracker.model.category.CategoryResponse;
import dev.artyx.finance_tracker.model.category.CreateCategoryRequest;
import dev.artyx.finance_tracker.model.category.UpdateCategoryRequest;
import dev.artyx.finance_tracker.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final ValidationService validationService;
    private final CategoryRepository categoryRepository;

    private CategoryResponse toCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .name(category.getName())
                .type(category.getType())
                .build();
    }

    @Transactional
    public void createCategory(CreateCategoryRequest request) {
        validationService.validate(request);

        Category category = new Category();
        category.setName(request.getName());
        category.setType(request.getType());
        categoryRepository.save(category);
    }

    @Transactional
    public CategoryResponse editCategory(UpdateCategoryRequest request) {
        validationService.validate(request);

        Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        if (request.getName() != null) {
            category.setName(request.getName());
        }
        if (request.getType() != null) {
            category.setType(request.getType());
        }
        categoryRepository.save(category);
        return toCategoryResponse(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        categoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategory(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        return toCategoryResponse(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream().map(this::toCategoryResponse).toList();
    }
}
