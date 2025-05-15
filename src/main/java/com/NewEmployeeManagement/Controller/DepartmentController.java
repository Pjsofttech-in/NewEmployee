package com.NewEmployeeManagement.Controller;

import com.NewEmployeeManagement.Entity.Department;
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
    public ResponseEntity<Department> createDepartment(@RequestBody Department department,
                                                       @RequestParam String role,
                                                       @RequestParam String email) {
        return ResponseEntity.ok(service.createDepartment(department, role, email));
    }

    @GetMapping("/getAllDepartments")
    public ResponseEntity<List<Department>> getAllDepartments(@RequestParam String role,
                                                              @RequestParam String email) {
        return ResponseEntity.ok(service.getAllDepartments(role, email));
    }

    @GetMapping("/getDepartmentById/{id}")
    public ResponseEntity<Department> getDepartmentById(@PathVariable int id,
                                                        @RequestParam String role,
                                                        @RequestParam String email) {
        return ResponseEntity.ok(service.getDepartmentById(id, role, email));
    }

    @PutMapping("/updateDepartment/{id}")
    public ResponseEntity<Department> updateDepartment(@PathVariable int id,
                                                       @RequestBody Department department,
                                                       @RequestParam String role,
                                                       @RequestParam String email) {
        return ResponseEntity.ok(service.updateDepartment(id, department, role, email));
    }

    @DeleteMapping("/deleteDepartment/{id}")
    public ResponseEntity<String> deleteDepartment(@PathVariable int id,
                                                   @RequestParam String role,
                                                   @RequestParam String email) {
        service.deleteDepartment(id, role, email);
        return ResponseEntity.ok("Department deleted successfully");
    }
}