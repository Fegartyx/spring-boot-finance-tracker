package dev.artyx.finance_tracker.controller;

import dev.artyx.finance_tracker.entity.User;
import dev.artyx.finance_tracker.model.WebResponse;
import dev.artyx.finance_tracker.model.category.CategoryResponse;
import dev.artyx.finance_tracker.model.category.CreateCategoryRequest;
import dev.artyx.finance_tracker.model.category.UpdateCategoryRequest;
import dev.artyx.finance_tracker.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping(path = "/api/category", consumes = "application/json", produces = "application/json")
    public WebResponse<String> createCategory(User user, @RequestBody CreateCategoryRequest request) {
        categoryService.createCategory(user, request);
        return WebResponse.<String>builder()
                .data("Category created successfully")
                .build();
    }

    @PutMapping(path = "/api/category/{categoryId}", consumes = "application/json", produces = "application/json")
    public WebResponse<CategoryResponse> updateCategory(User user, @PathVariable Long categoryId, @RequestBody UpdateCategoryRequest request) {
        request.setCategoryId(categoryId);
        CategoryResponse categoryResponse = categoryService.editCategory(user, request);
        return WebResponse.<CategoryResponse>builder()
                .data(categoryResponse)
                .build();
    }

    @DeleteMapping(path = "/api/category/{categoryId}", produces = "application/json")
    public WebResponse<String> deleteCategory(User user, @PathVariable Long categoryId) {
        categoryService.deleteCategory(user, categoryId);
        return WebResponse.<String>builder()
                .data("Category deleted successfully")
                .build();
    }

    @GetMapping(path = "/api/category/{categoryId}", produces = "application/json")
    public WebResponse<CategoryResponse> getCategory(@PathVariable Long categoryId) {
        CategoryResponse category = categoryService.getCategory(categoryId);
        return WebResponse.<CategoryResponse>builder()
                .data(category)
                .build();
    }

    @GetMapping(path = "/api/categories", produces = "application/json")
    public WebResponse<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return WebResponse.<List<CategoryResponse>>builder()
                .data(categories)
                .build();
    }
}
