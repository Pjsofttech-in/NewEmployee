package com.NewEmployeeManagement.Service;

import com.NewEmployeeManagement.DTO.EmployeeSalaryStructureRequest;
import com.NewEmployeeManagement.Entity.EmployeeSalaryStructure;

import java.util.List;

public interface EmployeeSalaryStructureService {

    EmployeeSalaryStructure createSalaryStructure(
            EmployeeSalaryStructureRequest request
    );

    EmployeeSalaryStructure updateSalaryStructure(
            Long id,
            EmployeeSalaryStructureRequest request
    );

    EmployeeSalaryStructure getSalaryStructureById(
            Long id
    );

    EmployeeSalaryStructure getCurrentSalaryStructure(
            Long employeeId
    );

    List<EmployeeSalaryStructure> getSalaryHistory(
            Long employeeId
    );

    void deleteSalaryStructure(Long id);
}