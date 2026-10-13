package com.vocabularyai.auth.config;

import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initDefaultAdmin() {
        return args -> {

            // Kiểm tra admin đã tồn tại chưa
            if (!userRepository.existsByUsername("admin")) {

                User admin = User.builder()
                        .username("admin")
                        .email("admin@gmail.com")
                        .password(passwordEncoder.encode("Admin@123"))
                        .fullName("Administrator")
                        .avatarUrl(null)
                        .role(User.UserRole.ADMIN)
                        .status(User.UserStatus.ACTIVE)
                        .build();

                userRepository.save(admin);

                System.out.println("========================================");
                System.out.println(" DEFAULT ADMIN ACCOUNT CREATED");
                System.out.println(" Username : admin");
                System.out.println(" Password : Admin@123");
                System.out.println(" Role     : ADMIN");
                System.out.println(" Status   : ACTIVE");
                System.out.println("========================================");

            } else {
                System.out.println("Default admin account already exists.");
            }
        };
    }
}