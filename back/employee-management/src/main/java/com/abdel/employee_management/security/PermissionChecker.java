package com.abdel.employee_management.security;

import com.abdel.employee_management.model.User;
import com.abdel.employee_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class PermissionChecker {

    @Autowired
    private UserRepository userRepository;

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

    public void checkCreate() {
        User user = getCurrentUser();
        if (user.getUserType() == User.UserType.SUPER_ADMIN) return;
        if (!user.isCanCreate()) throw new org.springframework.security.access.AccessDeniedException("No create permission");
    }

    public void checkRead() {
        User user = getCurrentUser();
        if (user.getUserType() == User.UserType.SUPER_ADMIN) return;
        if (!user.isCanRead()) throw new org.springframework.security.access.AccessDeniedException("No read permission");
    }

    public void checkUpdate() {
        User user = getCurrentUser();
        if (user.getUserType() == User.UserType.SUPER_ADMIN) return;
        if (!user.isCanUpdate()) throw new org.springframework.security.access.AccessDeniedException("No update permission");
    }

    public void checkDelete() {
        User user = getCurrentUser();
        if (user.getUserType() == User.UserType.SUPER_ADMIN) return;
        if (!user.isCanDelete()) throw new org.springframework.security.access.AccessDeniedException("No delete permission");
    }
}