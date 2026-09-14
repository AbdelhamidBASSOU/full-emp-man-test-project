package com.abdel.employee_management.service;

import com.abdel.employee_management.dto.UserDTO;
import com.abdel.employee_management.dto.UserCreateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserDTO createUser(UserCreateRequest request);

    UserDTO updateUser(Long id, UserCreateRequest request);

    void deleteUser(Long id);

    UserDTO getUserById(Long id);

    Page<UserDTO> searchUsers(String search, Pageable pageable);

    UserDTO updatePermissions(Long id, boolean canCreate, boolean canRead, boolean canUpdate, boolean canDelete);
}