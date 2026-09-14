package com.abdel.employee_management.config;

import com.abdel.employee_management.model.User;
import com.abdel.employee_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdmin("Admin One", "admin1@company.com", "Admin123!");
        seedAdmin("Admin Two", "admin2@company.com", "Admin123!");
    }

    private void seedAdmin(String name, String email, String rawPassword) {
        if (userRepository.findByEmail(email).isEmpty()) {
            User admin = new User();
            admin.setName(name);
            admin.setEmail(email);
            admin.setPassword(passwordEncoder.encode(rawPassword));
            admin.setUserType(User.UserType.SUPER_ADMIN);
            admin.setCanCreate(true);
            admin.setCanRead(true);
            admin.setCanUpdate(true);
            admin.setCanDelete(true);
            admin.setEnabled(true);
            userRepository.save(admin);
            System.out.println("Seeded super admin: " + email);
        }
    }
}