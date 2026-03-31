package dev.artyx.finance_tracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.artyx.finance_tracker.entity.User;
import dev.artyx.finance_tracker.model.WebResponse;
import dev.artyx.finance_tracker.model.user.LoginUserRequest;
import dev.artyx.finance_tracker.model.user.RegisterUserRequest;
import dev.artyx.finance_tracker.model.wallet.CreateWalletRequest;
import dev.artyx.finance_tracker.model.wallet.UpdateWalletRequest;
import dev.artyx.finance_tracker.repository.UserRepository;
import dev.artyx.finance_tracker.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        walletRepository.deleteAll();
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

    private UUID createWallet(String token, String name, Long balance) throws Exception {
        var createRequest = new CreateWalletRequest();
        createRequest.setName(name);
        createRequest.setBalance(balance);

        String response = mockMvc.perform(post("/api/wallet")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Get the created wallet from repository
        var wallets = walletRepository.findAll();
        return (UUID) wallets.getLast().getId();
    }

    // ===== CREATE WALLET TESTS =====

    @Test
    void testCreateWalletUnauthorizedNoToken() throws Exception {
        var request = new CreateWalletRequest();
        request.setName("Main Wallet");
        request.setBalance(10000L);

        mockMvc.perform(post("/api/wallet")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateWalletUnauthorizedInvalidToken() throws Exception {
        var request = new CreateWalletRequest();
        request.setName("Main Wallet");
        request.setBalance(10000L);

        mockMvc.perform(post("/api/wallet")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", "invalid-token")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateWalletSuccess() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        var request = new CreateWalletRequest();
        request.setName("Main Wallet");
        request.setBalance(10000L);

        mockMvc.perform(post("/api/wallet")
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
                    assertEquals("Wallet created successfully", response.getData());
                    System.out.println(response);
                });
    }

    @Test
    void testCreateWalletValidationErrorNameBlank() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        var request = new CreateWalletRequest();
        request.setName("");
        request.setBalance(10000L);

        mockMvc.perform(post("/api/wallet")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateWalletValidationErrorNameTooLong() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        var request = new CreateWalletRequest();
        request.setName("a".repeat(256));
        request.setBalance(10000L);

        mockMvc.perform(post("/api/wallet")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateWalletValidationErrorBalanceNull() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        var request = new CreateWalletRequest();
        request.setName("Main Wallet");
        request.setBalance(null);

        mockMvc.perform(post("/api/wallet")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateWalletSuccessNameExactlyMaxLength() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        var request = new CreateWalletRequest();
        request.setName("a".repeat(255));
        request.setBalance(10000L);

        mockMvc.perform(post("/api/wallet")
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
                    assertEquals("Wallet created successfully", response.getData());
                    System.out.println(response);
                });
    }

    @Test
    void testCreateWalletUnauthorizedExpiredToken() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        // Set token expiry to past (expired)
        User user = userRepository.findByUsername("testuser");
        user.setTokenExpiry(System.currentTimeMillis() - 10000);
        userRepository.save(user);

        var request = new CreateWalletRequest();
        request.setName("Main Wallet");
        request.setBalance(10000L);

        mockMvc.perform(post("/api/wallet")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ===== UPDATE WALLET TESTS =====

    @Test
    void testUpdateWalletUnauthorizedNoToken() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        UUID walletId = createWallet(token, "Main Wallet", 10000L);

        var request = new UpdateWalletRequest();
        request.setName("Updated Wallet");

        mockMvc.perform(put("/api/wallet/{walletId}", walletId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateWalletUnauthorizedInvalidToken() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        UUID walletId = createWallet(token, "Main Wallet", 10000L);

        var request = new UpdateWalletRequest();
        request.setName("Updated Wallet");

        mockMvc.perform(put("/api/wallet/{walletId}", walletId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", "invalid-token")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateWalletSuccessNameOnly() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        UUID walletId = createWallet(token, "Main Wallet", 10000L);

        var request = new UpdateWalletRequest();
        request.setName("Updated Wallet");

        mockMvc.perform(put("/api/wallet/{walletId}", walletId)
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
                    assertEquals("Wallet updated successfully", response.getData());
                    System.out.println(response);
                });
    }

    @Test
    void testUpdateWalletSuccessBalanceOnly() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        UUID walletId = createWallet(token, "Main Wallet", 10000L);

        var request = new UpdateWalletRequest();
        request.setBalance(20000L);

        mockMvc.perform(put("/api/wallet/{walletId}", walletId)
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
                    assertEquals("Wallet updated successfully", response.getData());
                    System.out.println(response);
                });
    }

    @Test
    void testUpdateWalletSuccessAllFields() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        UUID walletId = createWallet(token, "Main Wallet", 10000L);

        var request = new UpdateWalletRequest();
        request.setName("Savings Account");
        request.setBalance(50000L);

        mockMvc.perform(put("/api/wallet/{walletId}", walletId)
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
                    assertEquals("Wallet updated successfully", response.getData());
                    System.out.println(response);
                });
    }

    @Test
    void testUpdateWalletValidationErrorNameTooLong() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        UUID walletId = createWallet(token, "Main Wallet", 10000L);

        var request = new UpdateWalletRequest();
        request.setName("a".repeat(256));

        mockMvc.perform(put("/api/wallet/{walletId}", walletId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateWalletUnauthorizedExpiredToken() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        UUID walletId = createWallet(token, "Main Wallet", 10000L);

        // Set token expiry to past (expired)
        User user = userRepository.findByUsername("testuser");
        user.setTokenExpiry(System.currentTimeMillis() - 10000);
        userRepository.save(user);

        var request = new UpdateWalletRequest();
        request.setName("Updated Wallet");

        mockMvc.perform(put("/api/wallet/{walletId}", walletId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateWalletSuccessEmptyBody() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        UUID walletId = createWallet(token, "Main Wallet", 10000L);

        var request = new UpdateWalletRequest();

        mockMvc.perform(put("/api/wallet/{walletId}", walletId)
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

    // ===== DELETE WALLET TESTS =====

    @Test
    void testDeleteWalletUnauthorizedNoToken() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        UUID walletId = createWallet(token, "Main Wallet", 10000L);

        mockMvc.perform(delete("/api/wallet/{walletId}", walletId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteWalletUnauthorizedInvalidToken() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        UUID walletId = createWallet(token, "Main Wallet", 10000L);

        mockMvc.perform(delete("/api/wallet/{walletId}", walletId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", "invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteWalletSuccess() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        UUID walletId = createWallet(token, "Main Wallet", 10000L);

        mockMvc.perform(delete("/api/wallet/{walletId}", walletId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token))
                .andExpect(status().isOk())
                .andDo(result -> {
                    var response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            WebResponse.class
                    );
                    assertEquals("Wallet deleted successfully", response.getData());
                    System.out.println(response);
                });

        // Verify wallet is deleted
        var wallet = walletRepository.findById(walletId);
        assertFalse(wallet.isPresent());
    }

    @Test
    void testDeleteWalletUnauthorizedExpiredToken() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        UUID walletId = createWallet(token, "Main Wallet", 10000L);

        // Set token expiry to past (expired)
        User user = userRepository.findByUsername("testuser");
        user.setTokenExpiry(System.currentTimeMillis() - 10000);
        userRepository.save(user);

        mockMvc.perform(delete("/api/wallet/{walletId}", walletId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", token))
                .andExpect(status().isUnauthorized());
    }

    // ===== GET WALLET TESTS =====

    @Test
    void testGetWalletByIdSuccess() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        UUID walletId = createWallet(token, "Main Wallet", 10000L);

        mockMvc.perform(get("/api/wallet/{walletId}", walletId)
                        .accept(MediaType.APPLICATION_JSON).header("X-API-TOKEN", token))
                .andExpect(status().isOk())
                .andDo(result -> {
                    var response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            WebResponse.class
                    );
                    var dataMap = objectMapper.convertValue(response.getData(), Map.class);
                    assertEquals("Main Wallet", dataMap.get("name"));
                    assertEquals(10000L, ((Number) dataMap.get("balance")).longValue());
                    System.out.println(response);
                });
    }

    @Test
    void testGetAllWalletsSuccess() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        createWallet(token, "Main Wallet", 10000L);
        createWallet(token, "Savings", 50000L);
        createWallet(token, "Cash", 5000L);

        mockMvc.perform(get("/api/wallets")
                        .accept(MediaType.APPLICATION_JSON).header("X-API-TOKEN", token))
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
}
