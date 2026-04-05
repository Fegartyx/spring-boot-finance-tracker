package dev.artyx.finance_tracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.artyx.finance_tracker.entity.TransactionType;
import dev.artyx.finance_tracker.entity.User;
import dev.artyx.finance_tracker.model.WebResponse;
import dev.artyx.finance_tracker.model.transaction.CreateTransactionRequest;
import dev.artyx.finance_tracker.model.transaction.UpdateTransactionRequest;
import dev.artyx.finance_tracker.model.user.LoginUserRequest;
import dev.artyx.finance_tracker.model.user.RegisterUserRequest;
import dev.artyx.finance_tracker.model.wallet.CreateWalletRequest;
import dev.artyx.finance_tracker.repository.CategoryRepository;
import dev.artyx.finance_tracker.repository.TransactionRepository;
import dev.artyx.finance_tracker.repository.UserRepository;
import dev.artyx.finance_tracker.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        categoryRepository.deleteAll();
        walletRepository.deleteAll();
        transactionRepository.deleteAll();
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
        var dataMap = objectMapper.convertValue(loginResponse.getData(), Map.class);
        return (String) dataMap.get("token");
    }

    private Long createCategory(String token, String name, TransactionType type) throws Exception {
        dev.artyx.finance_tracker.model.category.CreateCategoryRequest createRequest =
            new dev.artyx.finance_tracker.model.category.CreateCategoryRequest();
        createRequest.setName(name);
        createRequest.setType(type);

        mockMvc.perform(post("/api/category")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk());

        var categories = categoryRepository.findAll();
        return (long) categories.getLast().getId();
    }

    private UUID createWallet(String token, String name, Long balance) throws Exception {
        var createWalletRequest = new CreateWalletRequest();
        createWalletRequest.setName(name);
        createWalletRequest.setBalance(balance);

        mockMvc.perform(post("/api/wallet")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(createWalletRequest)))
                .andExpect(status().isOk());

        var wallets = walletRepository.findAll();
        return (UUID) wallets.getLast().getId();
    }

    private UUID createTransaction(String token, String description, Long amount,
                                   LocalDateTime date, Long categoryId, UUID walletId) throws Exception {
        var request = new CreateTransactionRequest();
        request.setDescription(description);
        request.setAmount(amount);
        request.setDate(date);

        // Set category and wallet
        var category = categoryRepository.findById(categoryId).orElseThrow();
        var wallet = walletRepository.findById(walletId).orElseThrow();
        request.setCategory(category);
        request.setWallet(wallet);

        mockMvc.perform(post("/api/transaction")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        var transactions = transactionRepository.findAll();
        return (UUID) transactions.getLast().getId();
    }

    // ===== CREATE TRANSACTION TESTS =====

    @Test
    void testCreateTransactionUnauthorizedNoToken() throws Exception {
        var request = new CreateTransactionRequest();
        request.setDescription("Grocery shopping");
        request.setAmount(50000L);
        request.setDate(LocalDateTime.now());

        mockMvc.perform(post("/api/transaction")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateTransactionUnauthorizedInvalidToken() throws Exception {
        var request = new CreateTransactionRequest();
        request.setDescription("Grocery shopping");
        request.setAmount(50000L);
        request.setDate(LocalDateTime.now());

        mockMvc.perform(post("/api/transaction")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", "invalid-token")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateTransactionUnauthorizedExpiredToken() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        // Set token expiry to past (expired)
        User user = userRepository.findByUsername("testuser");
        user.setTokenExpiry(System.currentTimeMillis() - 10000);
        userRepository.save(user);

        var request = new CreateTransactionRequest();
        request.setDescription("Grocery shopping");
        request.setAmount(50000L);
        request.setDate(LocalDateTime.now());

        mockMvc.perform(post("/api/transaction")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateTransactionSuccess() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);
        UUID walletId = createWallet(token, "Main Wallet", 1000000L);

        var request = new CreateTransactionRequest();
        request.setDescription("Grocery shopping");
        request.setAmount(50000L);
        request.setDate(LocalDateTime.now());

        var category = categoryRepository.findById(categoryId).orElseThrow();
        var wallet = walletRepository.findById(walletId).orElseThrow();
        request.setCategory(category);
        request.setWallet(wallet);

        mockMvc.perform(post("/api/transaction")
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
                    assertEquals("Transaction created successfully", response.getData());
                    System.out.println(response);
                });
    }

    @Test
    void testCreateTransactionValidationErrorAmountNull() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);
        UUID walletId = createWallet(token, "Main Wallet", 1000000L);

        var request = new CreateTransactionRequest();
        request.setDescription("Grocery shopping");
        request.setAmount(null);
        request.setDate(LocalDateTime.now());

        var category = categoryRepository.findById(categoryId).orElseThrow();
        var wallet = walletRepository.findById(walletId).orElseThrow();
        request.setCategory(category);
        request.setWallet(wallet);

        mockMvc.perform(post("/api/transaction")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTransactionValidationErrorDateNull() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);
        UUID walletId = createWallet(token, "Main Wallet", 1000000L);

        var request = new CreateTransactionRequest();
        request.setDescription("Grocery shopping");
        request.setAmount(50000L);
        request.setDate(null);

        var category = categoryRepository.findById(categoryId).orElseThrow();
        var wallet = walletRepository.findById(walletId).orElseThrow();
        request.setCategory(category);
        request.setWallet(wallet);

        mockMvc.perform(post("/api/transaction")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTransactionValidationErrorDescriptionTooLong() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);
        UUID walletId = createWallet(token, "Main Wallet", 1000000L);

        var request = new CreateTransactionRequest();
        request.setDescription("a".repeat(256));
        request.setAmount(50000L);
        request.setDate(LocalDateTime.now());

        var category = categoryRepository.findById(categoryId).orElseThrow();
        var wallet = walletRepository.findById(walletId).orElseThrow();
        request.setCategory(category);
        request.setWallet(wallet);

        mockMvc.perform(post("/api/transaction")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTransactionValidationErrorCategoryNull() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        UUID walletId = createWallet(token, "Main Wallet", 1000000L);

        var request = new CreateTransactionRequest();
        request.setDescription("Grocery shopping");
        request.setAmount(50000L);
        request.setDate(LocalDateTime.now());
        request.setCategory(null);

        var wallet = walletRepository.findById(walletId).orElseThrow();
        request.setWallet(wallet);

        mockMvc.perform(post("/api/transaction")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTransactionValidationErrorWalletNull() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);

        var request = new CreateTransactionRequest();
        request.setDescription("Grocery shopping");
        request.setAmount(50000L);
        request.setDate(LocalDateTime.now());

        var category = categoryRepository.findById(categoryId).orElseThrow();
        request.setCategory(category);
        request.setWallet(null);

        mockMvc.perform(post("/api/transaction")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTransactionWithIncomeType() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Salary", TransactionType.income);
        UUID walletId = createWallet(token, "Main Wallet", 1000000L);

        var request = new CreateTransactionRequest();
        request.setDescription("Monthly salary");
        request.setAmount(5000000L);
        request.setDate(LocalDateTime.now());

        var category = categoryRepository.findById(categoryId).orElseThrow();
        var wallet = walletRepository.findById(walletId).orElseThrow();
        request.setCategory(category);
        request.setWallet(wallet);

        mockMvc.perform(post("/api/transaction")
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
                    assertEquals("Transaction created successfully", response.getData());
                    System.out.println(response);
                });
    }

    // ===== UPDATE TRANSACTION TESTS =====

    @Test
    void testUpdateTransactionUnauthorizedNoToken() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);
        UUID walletId = createWallet(token, "Main Wallet", 1000000L);
        UUID transactionId = createTransaction(token, "Grocery shopping", 50000L,
                LocalDateTime.now(), categoryId, walletId);

        var request = new UpdateTransactionRequest();
        request.setTransactionId(transactionId);
        request.setDescription("Updated grocery shopping");
        request.setAmount(75000L);
        request.setDate(LocalDateTime.now());

        var category = categoryRepository.findById(categoryId).orElseThrow();
        var wallet = walletRepository.findById(walletId).orElseThrow();
        request.setCategory(category);
        request.setWallet(wallet);

        mockMvc.perform(put("/api/transaction/{transactionId}", transactionId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateTransactionUnauthorizedInvalidToken() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);
        UUID walletId = createWallet(token, "Main Wallet", 1000000L);
        UUID transactionId = createTransaction(token, "Grocery shopping", 50000L,
                LocalDateTime.now(), categoryId, walletId);

        var request = new UpdateTransactionRequest();
        request.setTransactionId(transactionId);
        request.setDescription("Updated grocery shopping");
        request.setAmount(75000L);
        request.setDate(LocalDateTime.now());

        var category = categoryRepository.findById(categoryId).orElseThrow();
        var wallet = walletRepository.findById(walletId).orElseThrow();
        request.setCategory(category);
        request.setWallet(wallet);

        mockMvc.perform(put("/api/transaction/{transactionId}", transactionId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", "invalid-token")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateTransactionUnauthorizedExpiredToken() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);
        UUID walletId = createWallet(token, "Main Wallet", 1000000L);
        UUID transactionId = createTransaction(token, "Grocery shopping", 50000L,
                LocalDateTime.now(), categoryId, walletId);

        // Set token expiry to past (expired)
        User user = userRepository.findByUsername("testuser");
        user.setTokenExpiry(System.currentTimeMillis() - 10000);
        userRepository.save(user);

        var request = new UpdateTransactionRequest();
        request.setTransactionId(transactionId);
        request.setDescription("Updated grocery shopping");
        request.setAmount(75000L);
        request.setDate(LocalDateTime.now());

        var category = categoryRepository.findById(categoryId).orElseThrow();
        var wallet = walletRepository.findById(walletId).orElseThrow();
        request.setCategory(category);
        request.setWallet(wallet);

        mockMvc.perform(put("/api/transaction/{transactionId}", transactionId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateTransactionSuccessDescriptionOnly() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);
        UUID walletId = createWallet(token, "Main Wallet", 1000000L);
        UUID transactionId = createTransaction(token, "Grocery shopping", 50000L,
                LocalDateTime.now(), categoryId, walletId);

        var request = new UpdateTransactionRequest();
        request.setTransactionId(transactionId);
        request.setDescription("Updated grocery shopping");
        request.setDate(LocalDateTime.now());

        var category = categoryRepository.findById(categoryId).orElseThrow();
        var wallet = walletRepository.findById(walletId).orElseThrow();
        request.setCategory(category);
        request.setWallet(wallet);

        mockMvc.perform(put("/api/transaction/{transactionId}", transactionId)
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
                    assertEquals("Transaction updated successfully", response.getData());
                    System.out.println(response);
                });
    }

    @Test
    void testUpdateTransactionSuccessAmountOnly() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);
        UUID walletId = createWallet(token, "Main Wallet", 1000000L);
        UUID transactionId = createTransaction(token, "Grocery shopping", 50000L,
                LocalDateTime.now(), categoryId, walletId);

        var request = new UpdateTransactionRequest();
        request.setTransactionId(transactionId);
        request.setAmount(75000L);
        request.setDate(LocalDateTime.now());

        var category = categoryRepository.findById(categoryId).orElseThrow();
        var wallet = walletRepository.findById(walletId).orElseThrow();
        request.setCategory(category);
        request.setWallet(wallet);

        mockMvc.perform(put("/api/transaction/{transactionId}", transactionId)
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
                    assertEquals("Transaction updated successfully", response.getData());
                    System.out.println(response);
                });
    }

    @Test
    void testUpdateTransactionSuccessAllFields() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);
        UUID walletId = createWallet(token, "Main Wallet", 1000000L);
        UUID transactionId = createTransaction(token, "Grocery shopping", 50000L,
                LocalDateTime.now(), categoryId, walletId);

        var request = new UpdateTransactionRequest();
        request.setTransactionId(transactionId);
        request.setDescription("Updated grocery shopping with more items");
        request.setAmount(100000L);
        request.setDate(LocalDateTime.now());

        var category = categoryRepository.findById(categoryId).orElseThrow();
        var wallet = walletRepository.findById(walletId).orElseThrow();
        request.setCategory(category);
        request.setWallet(wallet);

        mockMvc.perform(put("/api/transaction/{transactionId}", transactionId)
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
                    assertEquals("Transaction updated successfully", response.getData());
                    System.out.println(response);
                });
    }

    @Test
    void testUpdateTransactionValidationErrorDescriptionTooLong() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);
        UUID walletId = createWallet(token, "Main Wallet", 1000000L);
        UUID transactionId = createTransaction(token, "Grocery shopping", 50000L,
                LocalDateTime.now(), categoryId, walletId);

        var request = new UpdateTransactionRequest();
        request.setTransactionId(transactionId);
        request.setDescription("a".repeat(256));
        request.setDate(LocalDateTime.now());

        var category = categoryRepository.findById(categoryId).orElseThrow();
        var wallet = walletRepository.findById(walletId).orElseThrow();
        request.setCategory(category);
        request.setWallet(wallet);

        mockMvc.perform(put("/api/transaction/{transactionId}", transactionId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateTransactionValidationErrorDateNull() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);
        UUID walletId = createWallet(token, "Main Wallet", 1000000L);
        UUID transactionId = createTransaction(token, "Grocery shopping", 50000L,
                LocalDateTime.now(), categoryId, walletId);

        var request = new UpdateTransactionRequest();
        request.setTransactionId(transactionId);
        request.setDescription("Updated description");
        request.setDate(null);

        var category = categoryRepository.findById(categoryId).orElseThrow();
        var wallet = walletRepository.findById(walletId).orElseThrow();
        request.setCategory(category);
        request.setWallet(wallet);

        mockMvc.perform(put("/api/transaction/{transactionId}", transactionId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ===== DELETE TRANSACTION TESTS =====

    @Test
    void testDeleteTransactionUnauthorizedNoToken() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);
        UUID walletId = createWallet(token, "Main Wallet", 1000000L);
        UUID transactionId = createTransaction(token, "Grocery shopping", 50000L,
                LocalDateTime.now(), categoryId, walletId);

        mockMvc.perform(delete("/api/transaction/{transactionId}", transactionId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteTransactionUnauthorizedInvalidToken() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);
        UUID walletId = createWallet(token, "Main Wallet", 1000000L);
        UUID transactionId = createTransaction(token, "Grocery shopping", 50000L,
                LocalDateTime.now(), categoryId, walletId);

        mockMvc.perform(delete("/api/transaction/{transactionId}", transactionId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", "invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteTransactionUnauthorizedExpiredToken() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);
        UUID walletId = createWallet(token, "Main Wallet", 1000000L);
        UUID transactionId = createTransaction(token, "Grocery shopping", 50000L,
                LocalDateTime.now(), categoryId, walletId);

        // Set token expiry to past (expired)
        User user = userRepository.findByUsername("testuser");
        user.setTokenExpiry(System.currentTimeMillis() - 10000);
        userRepository.save(user);

        mockMvc.perform(delete("/api/transaction/{transactionId}", transactionId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteTransactionSuccess() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);
        UUID walletId = createWallet(token, "Main Wallet", 1000000L);
        UUID transactionId = createTransaction(token, "Grocery shopping", 50000L,
                LocalDateTime.now(), categoryId, walletId);

        mockMvc.perform(delete("/api/transaction/{transactionId}", transactionId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token))
                .andExpect(status().isOk())
                .andDo(result -> {
                    var response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            WebResponse.class
                    );
                    assertEquals("Transaction deleted successfully", response.getData());
                    System.out.println(response);
                });

        // Verify transaction is deleted
        var transaction = transactionRepository.findById(transactionId);
        assertFalse(transaction.isPresent());
    }

    // ===== GET TRANSACTION TESTS =====

    @Test
    void testGetTransactionByIdSuccess() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);
        UUID walletId = createWallet(token, "Main Wallet", 1000000L);
        LocalDateTime transactionDate = LocalDateTime.now();
        String description = "Grocery shopping";
        Long amount = 50000L;
        UUID transactionId = createTransaction(token, description, amount,
                transactionDate, categoryId, walletId);

        mockMvc.perform(get("/api/transaction/{transactionId}", transactionId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token))
                .andExpect(status().isOk())
                .andDo(result -> {
                    var response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            WebResponse.class
                    );
                    var dataMap = objectMapper.convertValue(response.getData(), Map.class);
                    assertEquals(description, dataMap.get("description"));
                    assertEquals(amount, ((Number) dataMap.get("amount")).longValue());
                    System.out.println(response);
                });
    }

    @Test
    void testGetTransactionUnauthorizedNoToken() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);
        UUID walletId = createWallet(token, "Main Wallet", 1000000L);
        UUID transactionId = createTransaction(token, "Grocery shopping", 50000L,
                LocalDateTime.now(), categoryId, walletId);

        mockMvc.perform(get("/api/transaction/{transactionId}", transactionId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetTransactionUnauthorizedInvalidToken() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);
        UUID walletId = createWallet(token, "Main Wallet", 1000000L);
        UUID transactionId = createTransaction(token, "Grocery shopping", 50000L,
                LocalDateTime.now(), categoryId, walletId);

        mockMvc.perform(get("/api/transaction/{transactionId}", transactionId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", "invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetAllTransactionsSuccess() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);
        UUID walletId = createWallet(token, "Main Wallet", 1000000L);

        createTransaction(token, "Grocery shopping", 50000L, LocalDateTime.now(), categoryId, walletId);
        createTransaction(token, "Monthly salary", 5000000L, LocalDateTime.now(),
                createCategory(token, "Salary", TransactionType.income), walletId);
        createTransaction(token, "Electric bill", 200000L, LocalDateTime.now(),
                createCategory(token, "Utilities", TransactionType.expense), walletId);

        mockMvc.perform(get("/api/transactions")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token))
                .andExpect(status().isOk())
                .andDo(result -> {
                    var response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            WebResponse.class
                    );
                    var dataMap = objectMapper.convertValue(response.getData(), List.class);
                    assertEquals(3, dataMap.size());
                    System.out.println(response);
                });
    }

    @Test
    void testGetAllTransactionsUnauthorizedNoToken() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);
        UUID walletId = createWallet(token, "Main Wallet", 1000000L);

        createTransaction(token, "Grocery shopping", 50000L, LocalDateTime.now(), categoryId, walletId);

        mockMvc.perform(get("/api/transactions")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetAllTransactionsUnauthorizedInvalidToken() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        Long categoryId = createCategory(token, "Groceries", TransactionType.expense);
        UUID walletId = createWallet(token, "Main Wallet", 1000000L);

        createTransaction(token, "Grocery shopping", 50000L, LocalDateTime.now(), categoryId, walletId);

        mockMvc.perform(get("/api/transactions")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", "invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetAllTransactionsEmpty() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        mockMvc.perform(get("/api/transactions")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token))
                .andExpect(status().isOk())
                .andDo(result -> {
                    var response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            WebResponse.class
                    );
                    var dataMap = objectMapper.convertValue(response.getData(), List.class);
                    assertEquals(0, dataMap.size());
                    System.out.println(response);
                });
    }
}
