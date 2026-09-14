package com.abdel.employee_management.service.implementation;

import com.abdel.employee_management.dto.UserDTO;
import com.abdel.employee_management.dto.UserCreateRequest;
import com.abdel.employee_management.model.User;
import com.abdel.employee_management.repository.UserRepository;
import com.abdel.employee_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDTO createUser(UserCreateRequest request) {
        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserType(User.UserType.valueOf(request.getUserType()));

        // New users are enabled by default
        user.setEnabled(true);

        // Permissions are managed separately through updatePermissions()
        user.setCanCreate(false);
        user.setCanRead(false);
        user.setCanUpdate(false);
        user.setCanDelete(false);

        User saved = userRepository.save(user);

        return toDTO(saved);
    }

    @Override
    public UserDTO updateUser(Long id, UserCreateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setUserType(User.UserType.valueOf(request.getUserType()));

        // Only update the password if a new password was provided
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Permissions are intentionally preserved here.
        // They are managed through updatePermissions().

        return toDTO(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
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