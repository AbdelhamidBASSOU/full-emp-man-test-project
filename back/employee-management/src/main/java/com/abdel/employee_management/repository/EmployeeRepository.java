package com.abdel.employee_management.repository;

import com.abdel.employee_management.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("SELECT e FROM Employee e WHERE LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Employee> search(@Param("search") String search, Pageable pageable);
}