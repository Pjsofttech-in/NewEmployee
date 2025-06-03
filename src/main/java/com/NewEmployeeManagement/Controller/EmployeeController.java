package com.NewEmployeeManagement.Controller;

import com.NewEmployeeManagement.DTO.EmployeeCreateDTO;
import com.NewEmployeeManagement.Entity.Employee;
import com.NewEmployeeManagement.Pageination.SpecializationService;
import com.NewEmployeeManagement.Service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "https://pjsofttech.in")
public class EmployeeController {

    @Autowired
    private EmployeeService service;

    @Autowired
    private SpecializationService specializationService;

    @PostMapping(value = "/createEmployee", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Employee> createEmployee(
            @RequestPart("employeeDTO") String employeeDTOJson,
            @RequestParam("role") String role,
            @RequestParam("email") String email,
            @RequestParam("departmentId") int departmentId,
            @RequestParam("categoryId") Long categoryId,

            @RequestParam(value = "idProof", required = false) MultipartFile idProof,
            @RequestParam(value = "employeePhoto", required = false) MultipartFile employeePhoto,
            @RequestParam(value = "resume", required = false) MultipartFile resume,
            @RequestParam(value = "addressProof", required = false) MultipartFile addressProof,
            @RequestParam(value = "experienceLetter", required = false) MultipartFile experienceLetter
    ) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        EmployeeCreateDTO dto;
        try {
            dto = mapper.readValue(employeeDTOJson, EmployeeCreateDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON: " + e.getMessage());
        }

        Employee createdEmployee = service.createEmployee(
                dto, role, email, departmentId, categoryId,
                idProof, employeePhoto, resume, addressProof, experienceLetter
        );
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

    @PutMapping(value = "/updateEmployee/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Employee> updateEmployee(
            @PathVariable int id,
            @RequestPart("employeeDTO") String employeeDTOJson,
            @RequestParam("role") String role,
            @RequestParam("email") String email,
            @RequestParam(value = "departmentId",required = false) int departmentId,
            @RequestParam(value = "categoryId",required = false) Long categoryId,

            @RequestParam(value = "idProof", required = false) MultipartFile idProof,
            @RequestParam(value = "employeePhoto", required = false) MultipartFile employeePhoto,
            @RequestParam(value = "resume", required = false) MultipartFile resume,
            @RequestParam(value = "addressProof", required = false) MultipartFile addressProof,
            @RequestParam(value = "experienceLetter", required = false) MultipartFile experienceLetter
    ) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        EmployeeCreateDTO dto;
        try {
            dto = mapper.readValue(employeeDTOJson, EmployeeCreateDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON: " + e.getMessage());
        }

        Employee updatedEmployee = service.updateEmployee(
                id, dto, role, email, departmentId, categoryId,
                idProof, employeePhoto, resume, addressProof, experienceLetter
        );
        return new ResponseEntity<>(updatedEmployee, HttpStatus.OK);
    }


    @DeleteMapping("/deleteEmployee/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable int id,
                                         @RequestParam String role,
                                         @RequestParam String email) {
        service.deleteEmployee(id, role, email);
        return ResponseEntity.ok("Deleted successfully");
    }

    @PostMapping("/carryforward")
    public String carryForwardLeaves() {
        service.carryForwardLeavesForEligibleEmployees();
        return "Carry forward process completed for eligible employees.";
    }

    @GetMapping("/permissions")
    public ResponseEntity<Map<String, Object>> getCrudPermissions(@RequestParam String email) {
        Map<String, Object> permissions = service.getCrudPermissionForEmployeeByEmail(email);
        return ResponseEntity.ok(permissions);
    }
}