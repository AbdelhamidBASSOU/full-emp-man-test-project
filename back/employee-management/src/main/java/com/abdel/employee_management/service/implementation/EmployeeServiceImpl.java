package com.abdel.employee_management.service.implementation;

import com.abdel.employee_management.dto.EmployeeDTO;
import com.abdel.employee_management.dto.EmployeeRequest;
import com.abdel.employee_management.model.Employee;
import com.abdel.employee_management.repository.EmployeeRepository;
import com.abdel.employee_management.security.PermissionChecker;
import com.abdel.employee_management.service.EmployeeService;
import com.abdel.employee_management.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private PermissionChecker permissionChecker;

    @Override
    public EmployeeDTO createEmployee(EmployeeRequest request) {
        permissionChecker.checkCreate();

        Employee employee = new Employee();
        mapRequestToEntity(request, employee);

        Employee saved = employeeRepository.save(employee);
        return toDTO(saved);
    }

    @Override
    public EmployeeDTO updateEmployee(Long id, EmployeeRequest request) {
        permissionChecker.checkUpdate();

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        mapRequestToEntity(request, employee);

        return toDTO(employeeRepository.save(employee));
    }

    @Override
    public void deleteEmployee(Long id) {
        permissionChecker.checkDelete();
        employeeRepository.deleteById(id);
    }

    @Override
    public EmployeeDTO getEmployeeById(Long id) {
        permissionChecker.checkRead();

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return toDTO(employee);
    }

    @Override
    public Page<EmployeeDTO> searchEmployees(String search, Pageable pageable) {
        permissionChecker.checkRead();
        return employeeRepository.search(search, pageable).map(this::toDTO);
    }

    @Override
    public EmployeeDTO uploadPhoto(Long employeeId, MultipartFile file) {
        permissionChecker.checkUpdate();

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        String objectKey = "employees/" + employeeId + "/photo-" + file.getOriginalFilename();
        String storedKey = fileStorageService.uploadFile(file, objectKey);

        employee.setPhotoUrl(storedKey);
        return toDTO(employeeRepository.save(employee));
    }

    @Override
    public EmployeeDTO uploadCv(Long employeeId, MultipartFile file) {
        permissionChecker.checkUpdate();

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // replace: delete old CV first if one exists
        if (employee.getCvUrl() != null) {
            fileStorageService.deleteFile(employee.getCvUrl());
        }

        String objectKey = "employees/" + employeeId + "/cv-" + file.getOriginalFilename();
        String storedKey = fileStorageService.uploadFile(file, objectKey);

        employee.setCvUrl(storedKey);
        return toDTO(employeeRepository.save(employee));
    }

    @Override
    public InputStream downloadCv(Long employeeId) {
        permissionChecker.checkRead();

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (employee.getCvUrl() == null) {
            throw new RuntimeException("No CV uploaded for this employee");
        }

        return fileStorageService.getFile(employee.getCvUrl());
    }

    @Override
    public InputStream downloadPhoto(Long employeeId) {
        permissionChecker.checkRead();

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (employee.getPhotoUrl() == null) {
            throw new RuntimeException("No photo uploaded for this employee");
        }

        return fileStorageService.getFile(employee.getPhotoUrl());
    }

    private void mapRequestToEntity(EmployeeRequest request, Employee employee) {
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhoneNumber(request.getPhoneNumber());
        employee.setJobTitle(request.getJobTitle());
        employee.setDepartment(request.getDepartment());
        employee.setHireDate(request.getHireDate());
        employee.setSalary(request.getSalary());
    }

    private EmployeeDTO toDTO(Employee employee) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(employee.getId());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setEmail(employee.getEmail());
        dto.setPhoneNumber(employee.getPhoneNumber());
        dto.setJobTitle(employee.getJobTitle());
        dto.setDepartment(employee.getDepartment());
        dto.setHireDate(employee.getHireDate());
        dto.setSalary(employee.getSalary());
        dto.setPhotoUrl(employee.getPhotoUrl());
        dto.setCvUrl(employee.getCvUrl());
        return dto;
    }
}