package com.NewEmployeeManagement.Controller;

import com.NewEmployeeManagement.Entity.EmployeeDepartment;
import com.NewEmployeeManagement.Service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "https://pjsofttech.in")
public class DepartmentController {

    @Autowired
    private DepartmentService service;

    @PostMapping("/createDepartment")
    public ResponseEntity<EmployeeDepartment> createDepartment(@RequestBody EmployeeDepartment employeeDepartment,
                                                               @RequestParam String role,
                                                               @RequestParam String email) {
        return ResponseEntity.ok(service.createDepartment(employeeDepartment, role, email));
    }

    @GetMapping("/getAllDepartments")
    public ResponseEntity<List<EmployeeDepartment>> getAllDepartments(@RequestParam String role,
                                                                      @RequestParam String email) {
        return ResponseEntity.ok(service.getAllDepartments(role, email));
    }

    @GetMapping("/getDepartmentById/{id}")
    public ResponseEntity<EmployeeDepartment> getDepartmentById(@PathVariable Long id,
                                                                @RequestParam String role,
                                                                @RequestParam String email) {
        return ResponseEntity.ok(service.getDepartmentById(id, role, email));
    }

    @PutMapping("/updateDepartment/{id}")
    public ResponseEntity<EmployeeDepartment> updateDepartment(@PathVariable Long id,
                                                               @RequestBody EmployeeDepartment employeeDepartment,
                                                               @RequestParam String role,
                                                               @RequestParam String email) {
        return ResponseEntity.ok(service.updateDepartment(id, employeeDepartment, role, email));
    }

    @DeleteMapping("/deleteDepartment/{id}")
    public ResponseEntity<String> deleteDepartment(@PathVariable Long id,
                                                   @RequestParam String role,
                                                   @RequestParam String email) {
        service.deleteDepartment(id, role, email);
        return ResponseEntity.ok("Department deleted successfully");
    }
}