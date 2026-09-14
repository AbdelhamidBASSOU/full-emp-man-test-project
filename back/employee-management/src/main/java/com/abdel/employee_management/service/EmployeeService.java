package com.abdel.employee_management.service;

import com.abdel.employee_management.dto.EmployeeDTO;
import com.abdel.employee_management.dto.EmployeeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

public interface EmployeeService {

    EmployeeDTO createEmployee(EmployeeRequest request);

    EmployeeDTO updateEmployee(Long id, EmployeeRequest request);

    void deleteEmployee(Long id);

    EmployeeDTO getEmployeeById(Long id);

    Page<EmployeeDTO> searchEmployees(String search, Pageable pageable);

    EmployeeDTO uploadPhoto(Long employeeId, MultipartFile file);

    EmployeeDTO uploadCv(Long employeeId, MultipartFile file);

    InputStream downloadCv(Long employeeId);

    InputStream downloadPhoto(Long employeeId);
}