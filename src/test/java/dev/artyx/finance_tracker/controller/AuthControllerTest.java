package dev.artyx.finance_tracker.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.artyx.finance_tracker.model.WebResponse;
import dev.artyx.finance_tracker.model.user.LoginUserRequest;
import dev.artyx.finance_tracker.model.user.RegisterUserRequest;
import dev.artyx.finance_tracker.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
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

    // ===== LOGIN TESTS =====

    @Test
    void testLoginFailedBlankFields() throws Exception {
        var request = new LoginUserRequest();

        mockMvc.perform(post("/api/auth/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginFailedWrongUsername() throws Exception {
        var request = new LoginUserRequest();
        request.setUsername("nonexistent");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLoginFailedWrongPassword() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");

        var loginRequest = new LoginUserRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLoginSuccess() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");

        var loginRequest = new LoginUserRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andDo(result -> {
                    var response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            WebResponse.class);
                    assertNotNull(response.getData());
                    System.out.println(response);
                });
    }

    // ===== LOGOUT TESTS =====

    @Test
    void testLogoutSuccess() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        mockMvc.perform(delete("/api/auth/logout")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(result -> {
                    var response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            WebResponse.class);
                    assertEquals("Logout Successful", response.getData());
                    System.out.println(response);
                });
    }

    @Test
    void testLogoutThenTryAccessProtectedEndpoint() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        mockMvc.perform(delete("/api/auth/logout")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // JWT is still valid after logout (stateless), but this tests the flow
        mockMvc.perform(get("/api/users/current")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
