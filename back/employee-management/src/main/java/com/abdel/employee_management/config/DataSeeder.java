package com.abdel.employee_management.config;

import com.abdel.employee_management.model.User;
import com.abdel.employee_management.repository.UserRepository;
import com.abdel.employee_management.service.KeycloakAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KeycloakAdminService keycloakAdminService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        cleanLegacyConstraints();
        seedAdmin("Admin One", "admin1@company.com");
        seedAdmin("Admin Two", "admin2@company.com");
    }

    private void cleanLegacyConstraints() {
        try {
            jdbcTemplate.execute("DROP TABLE IF EXISTS refresh_tokens CASCADE");
            jdbcTemplate.execute("DROP TABLE IF EXISTS refresh_token CASCADE");
            jdbcTemplate.execute("DROP TABLE IF EXISTS password_reset_tokens CASCADE");
            jdbcTemplate.execute("DROP TABLE IF EXISTS password_reset_token CASCADE");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN password DROP NOT NULL");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN enabled DROP NOT NULL");
            jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN enabled SET DEFAULT true");
            jdbcTemplate.execute("UPDATE users SET enabled = true WHERE enabled IS NULL");
        } catch (Exception ignored) {}
    }

    private void seedAdmin(String name, String email) {
        String keycloakId = keycloakAdminService.findUserIdByEmail(email);
        if (keycloakId == null) {
            System.out.println("Keycloak user not found for " + email + " — skipping seed. Import the realm first.");
            return;
        }

        Optional<User> existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            User admin = existing.get();
            if (admin.getKeycloakId() == null || !admin.getKeycloakId().equals(keycloakId)) {
                admin.setKeycloakId(keycloakId);
                userRepository.save(admin);
                System.out.println("Updated keycloakId for existing admin: " + email);
            }
            return;
        }

        User admin = new User();
        admin.setKeycloakId(keycloakId);
        admin.setName(name);
        admin.setEmail(email);
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