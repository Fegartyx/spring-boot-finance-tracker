package dev.artyx.finance_tracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.artyx.finance_tracker.entity.TransactionType;
import dev.artyx.finance_tracker.entity.User;
import dev.artyx.finance_tracker.model.WebResponse;
import dev.artyx.finance_tracker.model.category.CreateCategoryRequest;
import dev.artyx.finance_tracker.model.category.UpdateCategoryRequest;
import dev.artyx.finance_tracker.model.user.LoginUserRequest;
import dev.artyx.finance_tracker.model.user.RegisterUserRequest;
import dev.artyx.finance_tracker.repository.CategoryRepository;
import dev.artyx.finance_tracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ===== HELPER METHODS =====

    private void registerUser(String username, String password, String email) throws Exception {
        var registerRequest = new RegisterUserRequest();
        registerRequest.setUsername(username);
        registerRequest.setPassword(password);
        registerRequest.setEmail(email);

        mockMvc.perform(post("/api/users")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        var loginRequest = new LoginUserRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        String tokenResponse = mockMvc.perform(post("/api/auth/login")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var loginResponse = objectMapper.readValue(tokenResponse, WebResponse.class);
        var dataMap = objectMapper.convertValue(loginResponse.getData(), java.util.Map.class);
        return (String) dataMap.get("token");
    }

    private Long createCategory(String token, String name, TransactionType type) throws Exception {
        var createRequest = new CreateCategoryRequest();
        createRequest.setName(name);
        createRequest.setType(type);

        String response = mockMvc.perform(post("/api/category")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Get the created category from repository
        var categories = categoryRepository.findAll();
        return (long) categories.getLast().getId();
    }

    // ===== CREATE CATEGORY TESTS =====

    @Test
    void testCreateCategoryUnauthorizedNoToken() throws Exception {
        var request = new CreateCategoryRequest();
        request.setName("Groceries");
        request.setType(TransactionType.expense);

        mockMvc.perform(post("/api/category")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateCategoryUnauthorizedInvalidToken() throws Exception {
        var request = new CreateCategoryRequest();
        request.setName("Groceries");
        request.setType(TransactionType.expense);

        mockMvc.perform(post("/api/category")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", "invalid-token")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateCategorySuccessWithExpenseType() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        var request = new CreateCategoryRequest();
        request.setName("Groceries");
        request.setType(TransactionType.expense);

        mockMvc.perform(post("/api/category")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(result -> {
                    var response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            WebResponse.class
                    );
                    assertEquals("Category created successfully", response.getData());
                    System.out.println(response);
                });
    }

    @Test
    void testCreateCategorySuccessWithIncomeType() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        var request = new CreateCategoryRequest();
        request.setName("Salary");
        request.setType(TransactionType.income);

        mockMvc.perform(post("/api/category")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(result -> {
                    var response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            WebResponse.class
                    );
                    assertEquals("Category created successfully", response.getData());
                    System.out.println(response);
                });
    }

    @Test
    void testCreateCategoryValidationErrorNameBlank() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        var request = new CreateCategoryRequest();
        request.setName("");
        request.setType(TransactionType.expense);

        mockMvc.perform(post("/api/category")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateCategoryValidationErrorNameTooLong() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        var request = new CreateCategoryRequest();
        request.setName("a".repeat(201));
        request.setType(TransactionType.expense);

        mockMvc.perform(post("/api/category")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateCategoryValidationErrorTypeBlank() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        var request = new CreateCategoryRequest();
        request.setName("Valid Category Name");
        request.setType(null);

        mockMvc.perform(post("/api/category")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateCategorySuccessNameExactlyMinLength() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        var request = new CreateCategoryRequest();
        request.setName("1234567890");
        request.setType(TransactionType.expense);

        mockMvc.perform(post("/api/category")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(result -> {
                    var response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            WebResponse.class
                    );
                    assertEquals("Category created successfully", response.getData());
                    System.out.println(response);
                });
    }

    @Test
    void testCreateCategorySuccessNameExactlyMaxLength() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        var request = new CreateCategoryRequest();
        request.setName("a".repeat(200));
        request.setType(TransactionType.expense);

        mockMvc.perform(post("/api/category")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(result -> {
                    var response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            WebResponse.class
                    );
                    assertEquals("Category created successfully", response.getData());
                    System.out.println(response);
                });
    }

    @Test
    void testCreateCategoryUnauthorizedExpiredToken() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        // Set token expiry to past (expired)
        User user = userRepository.findByUsername("testuser");
        user.setTokenExpiry(System.currentTimeMillis() - 10000);
        userRepository.save(user);

        var request = new CreateCategoryRequest();
        request.setName("Groceries");
        request.setType(TransactionType.expense);

        mockMvc.perform(post("/api/category")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ===== UPDATE CATEGORY TESTS =====

    @Test
    void testUpdateCategoryUnauthorizedNoToken() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);

        var request = new UpdateCategoryRequest();
        request.setName("Updated Groceries");

        mockMvc.perform(put("/api/category/{categoryId}", categoryId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateCategoryUnauthorizedInvalidToken() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);

        var request = new UpdateCategoryRequest();
        request.setName("Updated Groceries");

        mockMvc.perform(put("/api/category/{categoryId}", categoryId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", "invalid-token")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateCategorySuccessNameOnly() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);

        var request = new UpdateCategoryRequest();
        request.setName("Updated Groceries");

        mockMvc.perform(put("/api/category/{categoryId}", categoryId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(result -> {
                    var response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            WebResponse.class
                    );
                    var dataMap = objectMapper.convertValue(response.getData(), java.util.Map.class);
                    assertEquals("Updated Groceries", dataMap.get("name"));
                    System.out.println(response);
                });
    }

    @Test
    void testUpdateCategorySuccessTypeOnly() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);

        var request = new UpdateCategoryRequest();
        request.setType(TransactionType.income);

        mockMvc.perform(put("/api/category/{categoryId}", categoryId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(result -> {
                    var response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            WebResponse.class
                    );
                    var dataMap = objectMapper.convertValue(response.getData(), java.util.Map.class);
                    assertEquals("income", dataMap.get("type"));
                    System.out.println(response);
                });
    }

    @Test
    void testUpdateCategorySuccessAllFields() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);

        var request = new UpdateCategoryRequest();
        request.setName("Updated Salary");
        request.setType(TransactionType.income);

        mockMvc.perform(put("/api/category/{categoryId}", categoryId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(result -> {
                    var response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            WebResponse.class
                    );
                    var dataMap = objectMapper.convertValue(response.getData(), java.util.Map.class);
                    assertEquals("Updated Salary", dataMap.get("name"));
                    assertEquals("income", dataMap.get("type"));
                    System.out.println(response);
                });
    }

    @Test
    void testUpdateCategoryValidationErrorNameTooLong() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);

        var request = new UpdateCategoryRequest();
        request.setName("a".repeat(201));

        mockMvc.perform(put("/api/category/{categoryId}", categoryId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateCategoryUnauthorizedExpiredToken() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);

        // Set token expiry to past (expired)
        User user = userRepository.findByUsername("testuser");
        user.setTokenExpiry(System.currentTimeMillis() - 10000);
        userRepository.save(user);

        var request = new UpdateCategoryRequest();
        request.setName("Updated Groceries");

        mockMvc.perform(put("/api/category/{categoryId}", categoryId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateCategorySuccessEmptyBody() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);

        var request = new UpdateCategoryRequest();

        mockMvc.perform(put("/api/category/{categoryId}", categoryId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(result -> {
                    var response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            WebResponse.class
                    );
                    System.out.println(response);
                });
    }

    // ===== DELETE CATEGORY TESTS =====

    @Test
    void testDeleteCategoryUnauthorizedNoToken() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);

        mockMvc.perform(delete("/api/category/{categoryId}", categoryId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteCategoryUnauthorizedInvalidToken() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);

        mockMvc.perform(delete("/api/category/{categoryId}", categoryId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", "invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteCategorySuccess() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);

        mockMvc.perform(delete("/api/category/{categoryId}", categoryId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token))
                .andExpect(status().isOk())
                .andDo(result -> {
                    var response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            WebResponse.class
                    );
                    assertEquals("Category deleted successfully", response.getData());
                    System.out.println(response);
                });

        // Verify category is deleted
        var category = categoryRepository.findById(categoryId);
        assertFalse(category.isPresent());
    }

    @Test
    void testDeleteCategoryUnauthorizedExpiredToken() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);

        // Set token expiry to past (expired)
        User user = userRepository.findByUsername("testuser");
        user.setTokenExpiry(System.currentTimeMillis() - 10000);
        userRepository.save(user);

        mockMvc.perform(delete("/api/category/{categoryId}", categoryId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token))
                .andExpect(status().isUnauthorized());
    }

    // ===== GET CATEGORY TESTS =====

    @Test
    void testGetCategoryByIdExpense() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);

        mockMvc.perform(get("/api/category/{categoryId}", categoryId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(result -> {
                    var response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            WebResponse.class
                    );
                    var dataMap = objectMapper.convertValue(response.getData(), java.util.Map.class);
                    assertEquals("Groceries", dataMap.get("name"));
                    assertEquals("expense", dataMap.get("type"));
                    System.out.println(response);
                });
    }

    @Test
    void testGetCategoryByIdIncome() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Salary", TransactionType.income);

        mockMvc.perform(get("/api/category/{categoryId}", categoryId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(result -> {
                    var response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            WebResponse.class
                    );
                    var dataMap = objectMapper.convertValue(response.getData(), java.util.Map.class);
                    assertEquals("Salary", dataMap.get("name"));
                    assertEquals("income", dataMap.get("type"));
                    System.out.println(response);
                });
    }

    @Test
    void testGetAllCategoriesSuccess() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        createCategory(token, "Groceries", TransactionType.expense);
        createCategory(token, "Salary", TransactionType.income);
        createCategory(token, "Utilities", TransactionType.expense);

        mockMvc.perform(get("/api/categories")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(result -> {
                    var response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            WebResponse.class
                    );
                    System.out.println(response);
                });
    }

    @Test
    void testGetAllCategoriesEmpty() throws Exception {
        mockMvc.perform(get("/api/categories")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(result -> {
                    var response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            WebResponse.class
                    );
                    System.out.println(response);
                });
    }
}
