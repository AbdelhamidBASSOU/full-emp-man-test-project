package com.abdel.employee_management.security;

import com.abdel.employee_management.model.User;
import com.abdel.employee_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class PermissionChecker {

    @Autowired
    private UserRepository userRepository;

    public User getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            throw new AccessDeniedException("No valid JWT authentication");
        }

        String keycloakId = jwtAuth.getToken().getSubject();
        String email = jwtAuth.getToken().getClaimAsString("email");
        if (email == null || email.isBlank()) {
            email = jwtAuth.getToken().getClaimAsString("preferred_username");
        }

        // 1. Try finding by keycloakId
        Optional<User> userOpt = userRepository.findByKeycloakId(keycloakId);
        if (userOpt.isPresent()) {
            return userOpt.get();
        }

        // 2. Fallback: match by email and link keycloakId
        if (email != null && !email.isBlank()) {
            userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setKeycloakId(keycloakId);
                return userRepository.save(user);
            }
        }

        // 3. Fallback: auto-provision user record for valid Keycloak token
        boolean isSuperAdmin = false;
        Map<String, Object> realmAccess = jwtAuth.getToken().getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.get("roles");
            if (roles != null && roles.contains("SUPER_ADMIN")) {
                isSuperAdmin = true;
            }
        }

        User newUser = new User();
        newUser.setKeycloakId(keycloakId);
        newUser.setEmail(email != null ? email : keycloakId);
        String name = jwtAuth.getToken().getClaimAsString("name");
        newUser.setName(name != null && !name.isBlank() ? name : (email != null ? email : "User"));
        newUser.setUserType(isSuperAdmin ? User.UserType.SUPER_ADMIN : User.UserType.NORMAL_USER);
        newUser.setCanCreate(isSuperAdmin);
        newUser.setCanRead(true);
        newUser.setCanUpdate(isSuperAdmin);
        newUser.setCanDelete(isSuperAdmin);
        newUser.setEnabled(true);

        return userRepository.save(newUser);
    }

    public void checkCreate() {
        User user = getCurrentUser();
        if (user.getUserType() == User.UserType.SUPER_ADMIN) return;
        if (!user.isCanCreate()) throw new AccessDeniedException("No create permission");
    }

    public void checkRead() {
        User user = getCurrentUser();
        if (user.getUserType() == User.UserType.SUPER_ADMIN) return;
        if (!user.isCanRead()) throw new AccessDeniedException("No read permission");
    }

    public void checkUpdate() {
        User user = getCurrentUser();
        if (user.getUserType() == User.UserType.SUPER_ADMIN) return;
        if (!user.isCanUpdate()) throw new AccessDeniedException("No update permission");
    }

    public void checkDelete() {
        User user = getCurrentUser();
        if (user.getUserType() == User.UserType.SUPER_ADMIN) return;
        if (!user.isCanDelete()) throw new AccessDeniedException("No delete permission");
    }
}