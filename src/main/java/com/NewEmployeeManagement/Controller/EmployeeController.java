package com.NewEmployeeManagement.Controller;

import com.NewEmployeeManagement.Entity.Employee;
import com.NewEmployeeManagement.Pageination.SpecializationService;
import com.NewEmployeeManagement.Service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin(origins = "https://pjsofttech.in")
public class EmployeeController {

    @Autowired
    private EmployeeService service;

    @Autowired
    private SpecializationService specializationService;

    @PostMapping("createEmployee")
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee,
                                                   @RequestParam String role,
                                                   @RequestParam String email,
                                                   @RequestParam int departmentId,
                                                   @RequestParam Long categoryId) {
        Employee createdEmployee = service.createEmployee(employee, role, email, departmentId, categoryId);
        return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
    }

    @PostMapping("/getAllEmployee")
    public Page<Employee> filterEmployees(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String designation,
            @RequestParam(required = false) String status,
            @RequestParam String branchCode,
            @RequestParam String role,
            @RequestParam String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return specializationService.filterEmployees(department, categoryName, designation, status,
                branchCode, role, email, page, size);
    }

    @GetMapping("/getEmployeeById/{id}")
    public ResponseEntity<Employee> getById(@PathVariable int id,
                                            @RequestParam String role,
                                            @RequestParam String email) {
        return ResponseEntity.ok(service.getEmployeeById(id, role, email));
    }

    @PutMapping("/updateEmployee/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable int id,
                                           @RequestBody Employee employee,
                                           @RequestParam String role,
                                           @RequestParam String email) {
        return ResponseEntity.ok(service.updateEmployee(id, employee, role, email));
    }

    @DeleteMapping("/deleteEmployee/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable int id,
                                         @RequestParam String role,
                                         @RequestParam String email) {
        service.deleteEmployee(id, role, email);
        return ResponseEntity.ok("Deleted successfully");
    }

    @PostMapping("/uploadDocuments/{id}")
    public ResponseEntity<Employee> uploadDocuments(@PathVariable int id,
                                                    @RequestParam MultipartFile idProof,
                                                    @RequestParam MultipartFile photo,
                                                    @RequestParam MultipartFile resume,
                                                    @RequestParam MultipartFile addressProof,
                                                    @RequestParam MultipartFile experienceLetter,
                                                    @RequestParam String role,
                                                    @RequestParam String email) {
        return ResponseEntity.ok(service.uploadDocuments(id, idProof, photo, resume, addressProof, experienceLetter, role, email));
    }
}