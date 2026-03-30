package dev.artyx.finance_tracker.service;

import dev.artyx.finance_tracker.entity.Category;
import dev.artyx.finance_tracker.entity.User;
import dev.artyx.finance_tracker.model.category.CategoryResponse;
import dev.artyx.finance_tracker.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    public CategoryResponse createCategory(User user) {}

    @Transactional
    public CategoryResponse editCategory() {}

    @Transactional
    public void deleteCategory() {}

    @Transactional
    public CategoryResponse getCategory() {}

    @Transactional
    public List<CategoryResponse> getAllCategories() {}
}
