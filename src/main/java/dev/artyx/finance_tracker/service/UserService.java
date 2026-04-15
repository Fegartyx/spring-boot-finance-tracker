package dev.artyx.finance_tracker.service;

import dev.artyx.finance_tracker.entity.User;
import dev.artyx.finance_tracker.model.user.RegisterUserRequest;
import dev.artyx.finance_tracker.model.user.UpdateUserRequest;
import dev.artyx.finance_tracker.model.user.UserResponse;
import dev.artyx.finance_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ValidationService validationService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegisterUserRequest request) {
        validationService.validate(request);
        if (userRepository.existsUserByUsername(request.getUsername()) || userRepository.existsUserByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username or email already exists");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(encodedPassword);
        user.setEmail(request.getEmail());

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserResponse get(User user) {
        return UserResponse.builder().username(user.getUsername()).email(user.getEmail()).build();
    }

    @Transactional
    public UserResponse update(User user, UpdateUserRequest request) {
        validationService.validate(request);

        if (Objects.nonNull(request.getUsername())) {
            user.setUsername(request.getUsername());
        }

        if (Objects.nonNull(request.getEmail())) {
            user.setEmail(request.getEmail());
        }

        if (Objects.nonNull(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(user);

        return UserResponse.builder().username(user.getUsername()).email(user.getEmail()).build();
    }
}
