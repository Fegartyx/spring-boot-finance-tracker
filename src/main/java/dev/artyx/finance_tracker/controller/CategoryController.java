package dev.artyx.finance_tracker.controller;

import dev.artyx.finance_tracker.entity.User;
import dev.artyx.finance_tracker.model.WebResponse;
import dev.artyx.finance_tracker.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    public WebResponse<String> createCategory(User user) {
        categoryService.createCategory(user);
        return WebResponse.<String>builder()
                .data("Category created successfully")
                .build();
    }
}
