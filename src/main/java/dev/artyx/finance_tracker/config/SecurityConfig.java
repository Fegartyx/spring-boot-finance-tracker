package dev.artyx.finance_tracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                // 1. Matikan CSRF untuk SEMUA request
                                .csrf(AbstractHttpConfigurer::disable)
                                .exceptionHandling(exception -> exception.authenticationEntryPoint((request, response,
                                                authException) -> {
                                        System.out.println("LOG SECURITY: Request Ke : "
                                                        + request.getRequestURI() + " Ditolak Karena : "
                                                        + authException.getMessage());
                                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                }))

                                // 2. Atur siapa yang boleh lewat
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api/users/**", "/api/auth/**", "/api/category/**",
                                                                "/api/wallet/**", "/error")
                                                .permitAll())

                                // 3. Karena pakai JWT, buat session jadi STATELESS
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
