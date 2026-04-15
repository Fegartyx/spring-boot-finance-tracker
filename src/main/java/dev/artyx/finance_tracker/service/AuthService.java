package dev.artyx.finance_tracker.service;


import dev.artyx.finance_tracker.config.JwtUtil;
import dev.artyx.finance_tracker.entity.User;
import dev.artyx.finance_tracker.model.user.LoginUserRequest;
import dev.artyx.finance_tracker.model.TokenResponse;
import dev.artyx.finance_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final ValidationService validator;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public TokenResponse getToken(LoginUserRequest request) {
        validator.validate(request);

        User user = userRepository.findByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        return TokenResponse.builder()
                .token(jwtUtil.generateToken(user.getUsername()))
                .build();
    }

    public void logout(User user) {
        // JWT is stateless, logout is handled client-side
    }
}
