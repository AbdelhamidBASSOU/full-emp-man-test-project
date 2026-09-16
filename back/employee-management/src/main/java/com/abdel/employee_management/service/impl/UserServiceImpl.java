package com.abdel.employee_management.service.impl;

import com.abdel.employee_management.dto.UserDTO;
import com.abdel.employee_management.dto.UserCreateRequest;
import com.abdel.employee_management.model.User;
import com.abdel.employee_management.repository.UserRepository;
import com.abdel.employee_management.service.KeycloakAdminService;
import com.abdel.employee_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KeycloakAdminService keycloakAdminService;

    @Override
    public UserDTO createUser(UserCreateRequest request) {
        // Provision in Keycloak first — Keycloak owns credentials
        String keycloakId = keycloakAdminService.createUser(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getUserType()
        );

        // Save the permissions row in our database
        User user = new User();
        user.setKeycloakId(keycloakId);
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setUserType(User.UserType.valueOf(request.getUserType()));
        user.setEnabled(true);
        user.setCanCreate(false);
        user.setCanRead(false);
        user.setCanUpdate(false);
        user.setCanDelete(false);

        return toDTO(userRepository.save(user));
    }

    @Override
    public UserDTO updateUser(Long id, UserCreateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user exists in Keycloak
        String keycloakId = keycloakAdminService.findUserIdByEmail(user.getEmail());
        if (keycloakId == null) {
            // User doesn't exist in Keycloak yet (legacy user); create them in Keycloak
            keycloakId = keycloakAdminService.createUser(
                    request.getName(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getUserType()
            );
        } else {
            // Update name, email, role, and password in Keycloak
            keycloakAdminService.updateUser(
                    keycloakId,
                    request.getName(),
                    request.getEmail(),
                    request.getUserType(),
                    request.getPassword()
            );
        }

        user.setKeycloakId(keycloakId);
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setUserType(User.UserType.valueOf(request.getUserType()));

        return toDTO(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getKeycloakId() != null) {
            keycloakAdminService.deleteUser(user.getKeycloakId());
        } else {
            String foundId = keycloakAdminService.findUserIdByEmail(user.getEmail());
            if (foundId != null) {
                keycloakAdminService.deleteUser(foundId);
            }
        }
        userRepository.delete(user);
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toDTO(user);
    }

    @Override
    public Page<UserDTO> searchUsers(String search, Pageable pageable) {
        return userRepository.search(search, pageable)
                .map(this::toDTO);
    }

    @Override
    public UserDTO updatePermissions(
            Long id,
            boolean canCreate,
            boolean canRead,
            boolean canUpdate,
            boolean canDelete) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setCanCreate(canCreate);
        user.setCanRead(canRead);
        user.setCanUpdate(canUpdate);
        user.setCanDelete(canDelete);

        return toDTO(userRepository.save(user));
    }

    private UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setUserType(user.getUserType().name());
        dto.setCanCreate(user.isCanCreate());
        dto.setCanRead(user.isCanRead());
        dto.setCanUpdate(user.isCanUpdate());
        dto.setCanDelete(user.isCanDelete());
        dto.setEnabled(user.isEnabled());
        return dto;
    }
}