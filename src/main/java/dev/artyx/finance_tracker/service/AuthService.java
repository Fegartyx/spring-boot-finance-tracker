package dev.artyx.finance_tracker.service;


import dev.artyx.finance_tracker.entity.User;
import dev.artyx.finance_tracker.model.user.LoginUserRequest;
import dev.artyx.finance_tracker.model.TokenResponse;
import dev.artyx.finance_tracker.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final ValidationService validator;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public TokenResponse getToken(LoginUserRequest request) {
        validator.validate(request);

        User user = userRepository.findByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        user.setToken(UUID.randomUUID().toString());
        user.setTokenExpiry(next30Days());
        userRepository.save(user);
        return TokenResponse.builder()
                .token(user.getToken())
                .expiredAt(user.getTokenExpiry())
                .build();
    }

    @Transactional
    public void logout(User user) {
        user.setToken(null);
        user.setTokenExpiry(null);
        userRepository.save(user);
    }

    private Long next30Days() {
        return System.currentTimeMillis() + (1000 * 16 * 24 *30);
    }
}
