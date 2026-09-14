package com.abdel.employee_management.controller;

import com.abdel.employee_management.dto.EmployeeDTO;
import com.abdel.employee_management.dto.EmployeeRequest;
import com.abdel.employee_management.security.PermissionChecker;
import com.abdel.employee_management.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private PermissionChecker permissionChecker;

    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        permissionChecker.checkCreate();
        return ResponseEntity.ok(employeeService.createEmployee(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployee(@PathVariable Long id) {
        permissionChecker.checkRead();
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeDTO>> searchEmployees(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        permissionChecker.checkRead();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(employeeService.searchEmployees(search, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        permissionChecker.checkUpdate();
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        permissionChecker.checkDelete();
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/photo")
    public ResponseEntity<EmployeeDTO> uploadPhoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        permissionChecker.checkUpdate();
        return ResponseEntity.ok(employeeService.uploadPhoto(id, file));
    }

    @PostMapping("/{id}/cv")
    public ResponseEntity<EmployeeDTO> uploadCv(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        permissionChecker.checkUpdate();
        return ResponseEntity.ok(employeeService.uploadCv(id, file));
    }

    @GetMapping("/{id}/cv")
    public ResponseEntity<byte[]> downloadCv(@PathVariable Long id) throws IOException {
        permissionChecker.checkRead();
        InputStream fileStream = employeeService.downloadCv(id);
        byte[] fileBytes = fileStream.readAllBytes();

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=cv.pdf")
                .header("Content-Type", "application/pdf")
                .body(fileBytes);
    }

    @GetMapping("/{id}/photo")
    public ResponseEntity<byte[]> downloadPhoto(@PathVariable Long id) throws IOException {
        InputStream fileStream = employeeService.downloadPhoto(id);
        byte[] fileBytes = fileStream.readAllBytes();

        return ResponseEntity.ok()
                .header("Content-Type", "image/jpeg")
                .body(fileBytes);
    }
}