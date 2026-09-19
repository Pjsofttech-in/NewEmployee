package com.NewEmployeeManagement.Repository;

import com.NewEmployeeManagement.Entity.EmployeeSalaryStructure;
import com.NewEmployeeManagement.Entity.EmployeeSalaryStructure.SalaryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeSalaryStructureRepository
        extends JpaRepository<EmployeeSalaryStructure, Long> {

    List<EmployeeSalaryStructure> findByEmployeeId(
            Long employeeId
    );

    Optional<EmployeeSalaryStructure>
    findByEmployeeIdAndStatus(
            Long employeeId,
            SalaryStatus status
    );

    List<EmployeeSalaryStructure>
    findByEmployeeIdOrderByEffectiveFromDesc(
            Long employeeId
    );

    boolean existsByEmployeeIdAndStatus(
            Long employeeId,
            SalaryStatus status
    );
}