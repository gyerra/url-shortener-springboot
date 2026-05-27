package com.urlshortener.config;

import com.urlshortener.entity.Role;
import com.urlshortener.entity.User;
import com.urlshortener.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:Admin@12345}")
    private String adminPassword;

    @Value("${app.admin.email:admin@urlshortener.local}")
    private String adminEmail;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername(adminUsername)) {
            User admin = User.builder()
                    .username(adminUsername)
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Default admin user created: {}", adminUsername);
        }
    }
}
