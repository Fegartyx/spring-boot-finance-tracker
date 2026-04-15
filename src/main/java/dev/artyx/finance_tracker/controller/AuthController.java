package dev.artyx.finance_tracker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import dev.artyx.finance_tracker.entity.User;
import dev.artyx.finance_tracker.model.TokenResponse;
import dev.artyx.finance_tracker.model.WebResponse;
import dev.artyx.finance_tracker.model.user.LoginUserRequest;
import dev.artyx.finance_tracker.service.AuthService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping(path = "/api/auth/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginUserRequest request) {
        var response = authService.getToken(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(path = "/api/auth/logout", produces = "application/json")
    public WebResponse<String> logout(@AuthenticationPrincipal User user) {
        authService.logout(user);
        return WebResponse.<String>builder().data("Logout Successful").build();
    }
}
