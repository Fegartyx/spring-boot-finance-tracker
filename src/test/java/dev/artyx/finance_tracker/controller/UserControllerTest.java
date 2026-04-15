package dev.artyx.finance_tracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.artyx.finance_tracker.model.user.LoginUserRequest;
import dev.artyx.finance_tracker.model.user.RegisterUserRequest;
import dev.artyx.finance_tracker.model.user.UpdateUserRequest;
import dev.artyx.finance_tracker.model.WebResponse;
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
class UserControllerTest {
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

    // ===== REGISTER TESTS =====

    @Test
    void testRegisterFailedBlankFields() throws Exception {
        var request = new RegisterUserRequest();

        mockMvc.perform(post("/api/users").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpectAll(status().isBadRequest());
    }

    @Test
    void testRegisterFailedDuplicateUsername() throws Exception {
        var request = new RegisterUserRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setEmail("test@gmail.com");

        mockMvc.perform(post("/api/users").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/users").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterSuccess() throws Exception {
        var request = new RegisterUserRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setEmail("test@gmail.com");

        mockMvc.perform(post("/api/users").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpectAll(status().isOk())
                .andDo(result -> {
                    var response = objectMapper.readValue(result.getResponse().getContentAsString(), WebResponse.class);
                    System.out.println(response);
                    assertEquals("User registered successfully", response.getData());
                });
    }

    // ===== GET CURRENT USER TESTS =====

    @Test
    void testGetCurrentUserUnauthorizedNoToken() throws Exception {
        mockMvc.perform(get("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetCurrentUserUnauthorizedInvalidToken() throws Exception {
        mockMvc.perform(get("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer invalid-token-12345"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetCurrentUserSuccess() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        mockMvc.perform(get("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(result -> {
                    var response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            WebResponse.class
                    );
                    System.out.println(response);
                });
    }

    // ===== UPDATE USER TESTS =====

    @Test
    void testUpdateUserUnauthorizedNoToken() throws Exception {
        var updateRequest = new UpdateUserRequest();
        updateRequest.setUsername("newusername");

        mockMvc.perform(patch("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateUserUnauthorizedInvalidToken() throws Exception {
        var updateRequest = new UpdateUserRequest();
        updateRequest.setUsername("newusername");

        mockMvc.perform(patch("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateUserFailedUsernameTooLong() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        var updateRequest = new UpdateUserRequest();
        updateRequest.setUsername("a".repeat(101));

        mockMvc.perform(patch("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateUserFailedPasswordTooLong() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        var updateRequest = new UpdateUserRequest();
        updateRequest.setPassword("a".repeat(101));

        mockMvc.perform(patch("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateUserFailedEmailTooLong() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        var updateRequest = new UpdateUserRequest();
        updateRequest.setEmail("a".repeat(256) + "@gmail.com");

        mockMvc.perform(patch("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateUserSuccessUsernameOnly() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        var updateRequest = new UpdateUserRequest();
        updateRequest.setUsername("newusername");

        mockMvc.perform(patch("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(result -> {
                    var response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            WebResponse.class
                    );
                    var dataMap = objectMapper.convertValue(response.getData(), java.util.Map.class);
                    assertEquals("newusername", dataMap.get("username"));
                    assertEquals("test@gmail.com", dataMap.get("email"));
                    System.out.println(response);
                });
    }

    @Test
    void testUpdateUserSuccessEmailOnly() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        var updateRequest = new UpdateUserRequest();
        updateRequest.setEmail("newemail@gmail.com");

        mockMvc.perform(patch("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(result -> {
                    var response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            WebResponse.class
                    );
                    var dataMap = objectMapper.convertValue(response.getData(), java.util.Map.class);
                    assertEquals("testuser", dataMap.get("username"));
                    assertEquals("newemail@gmail.com", dataMap.get("email"));
                    System.out.println(response);
                });
    }

    @Test
    void testUpdateUserSuccessPasswordOnly() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        var updateRequest = new UpdateUserRequest();
        updateRequest.setPassword("newpassword456");

        mockMvc.perform(patch("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(result -> {
                    var response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            WebResponse.class
                    );
                    var dataMap = objectMapper.convertValue(response.getData(), java.util.Map.class);
                    assertEquals("testuser", dataMap.get("username"));
                    assertEquals("test@gmail.com", dataMap.get("email"));
                    assertNull(dataMap.get("password"));
                    System.out.println(response);
                });
    }

    @Test
    void testUpdateUserSuccessAllFields() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        var updateRequest = new UpdateUserRequest();
        updateRequest.setUsername("newusername");
        updateRequest.setEmail("newemail@gmail.com");
        updateRequest.setPassword("newpassword456");

        mockMvc.perform(patch("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(result -> {
                    var response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            WebResponse.class
                    );
                    var dataMap = objectMapper.convertValue(response.getData(), java.util.Map.class);
                    assertEquals("newusername", dataMap.get("username"));
                    assertEquals("newemail@gmail.com", dataMap.get("email"));
                    assertNull(dataMap.get("password"));
                    System.out.println(response);
                });
    }

    @Test
    void testUpdateUserSuccessEmptyBody() throws Exception {
        registerUser("testuser", "password123", "test@gmail.com");
        String token = loginAndGetToken("testuser", "password123");

        var updateRequest = new UpdateUserRequest();

        mockMvc.perform(patch("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(result -> {
                    var response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            WebResponse.class
                    );
                    var dataMap = objectMapper.convertValue(response.getData(), java.util.Map.class);
                    assertEquals("testuser", dataMap.get("username"));
                    assertEquals("test@gmail.com", dataMap.get("email"));
                    System.out.println(response);
                });
    }
}
