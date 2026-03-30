package dev.artyx.finance_tracker.controller;

import dev.artyx.finance_tracker.entity.User;
import dev.artyx.finance_tracker.model.user.LoginUserRequest;
import dev.artyx.finance_tracker.model.TokenResponse;
import dev.artyx.finance_tracker.model.WebResponse;
import dev.artyx.finance_tracker.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping(path = "/api/auth/login", consumes = "application/json", produces = "application/json")
    public WebResponse<TokenResponse> login(@RequestBody LoginUserRequest request) {
        var response = authService.getToken(request);
        return WebResponse.<TokenResponse>builder()
                .data(response)
                .build();
    }
    @DeleteMapping(path = "/api/auth/logout", produces = "application/json")
    public WebResponse<String> logout(User user) {
        authService.logout(user);
        return WebResponse.<String>builder().data("Logout Successful").build();
    }
}
