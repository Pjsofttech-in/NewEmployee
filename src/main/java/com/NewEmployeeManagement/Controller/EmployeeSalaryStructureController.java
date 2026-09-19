package com.NewEmployeeManagement.Controller;

import com.NewEmployeeManagement.DTO.EmployeeSalaryStructureRequest;
import com.NewEmployeeManagement.Entity.EmployeeSalaryStructure;
import com.NewEmployeeManagement.Service.EmployeeSalaryStructureService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee-salary-structure")
@RequiredArgsConstructor
public class EmployeeSalaryStructureController {

    private final EmployeeSalaryStructureService salaryService;


    // ==========================================
    // CREATE
    // ==========================================

    @PostMapping("/create")
    public ResponseEntity<EmployeeSalaryStructure>
    createSalaryStructure(
            @Valid @RequestBody
            EmployeeSalaryStructureRequest request) {

        EmployeeSalaryStructure response =
                salaryService.createSalaryStructure(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // ==========================================
    // UPDATE
    // ==========================================

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeSalaryStructure>
    updateSalaryStructure(
            @PathVariable Long id,
            @Valid @RequestBody
            EmployeeSalaryStructureRequest request) {

        return ResponseEntity.ok(
                salaryService.updateSalaryStructure(
                        id,
                        request
                )
        );
    }


    // ==========================================
    // GET BY ID
    // ==========================================

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeSalaryStructure>
    getSalaryStructureById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                salaryService.getSalaryStructureById(id)
        );
    }


    // ==========================================
    // GET CURRENT SALARY
    // ==========================================

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<EmployeeSalaryStructure>
    getCurrentSalaryStructure(
            @PathVariable Long employeeId) {

        return ResponseEntity.ok(
                salaryService.getCurrentSalaryStructure(
                        employeeId
                )
        );
    }


    // ==========================================
    // GET SALARY HISTORY
    // ==========================================

    @GetMapping("/employee/{employeeId}/history")
    public ResponseEntity<List<EmployeeSalaryStructure>>
    getSalaryHistory(
            @PathVariable Long employeeId) {

        return ResponseEntity.ok(
                salaryService.getSalaryHistory(
                        employeeId
                )
        );
    }


    // ==========================================
    // DELETE
    // ==========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<String>
    deleteSalaryStructure(
            @PathVariable Long id) {

        salaryService.deleteSalaryStructure(id);

        return ResponseEntity.ok(
                "Salary structure deleted successfully"
        );
    }
}