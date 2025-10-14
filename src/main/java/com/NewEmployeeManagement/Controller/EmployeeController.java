package com.NewEmployeeManagement.Controller;

import com.NewEmployeeManagement.DTO.*;
import com.NewEmployeeManagement.Entity.Employee;
import com.NewEmployeeManagement.Pageination.EmployeeSpecification;
import com.NewEmployeeManagement.Service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "https://pjsofttech.in")
public class EmployeeController {

    @Autowired
    private EmployeeService service;


    @PostMapping(value = "/createEmployee", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Employee> createEmployee(
            @RequestPart("employeeDTO") String employeeDTOJson,
            @RequestParam("role") String role,
            @RequestParam("email") String email,
            @RequestParam("departmentId") Long departmentId,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam(value = "idProof", required = false) MultipartFile idProof,
            @RequestParam(value = "employeePhoto", required = false) MultipartFile employeePhoto,
            @RequestParam(value = "resume", required = false) MultipartFile resume,
            @RequestParam(value = "addressProof", required = false) MultipartFile addressProof,
            @RequestParam(value = "experienceLetter", required = false) MultipartFile experienceLetter,
            @RequestParam(value = "slipImage", required = false) MultipartFile slipImage
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
                idProof, employeePhoto, resume, addressProof, experienceLetter,slipImage);

        return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
    }

    @PostMapping("/getAllEmployee")
    public ResponseEntity<Page<Employee>> getFilteredEmployees(
            @RequestParam String role,
            @RequestParam String email,
            @RequestParam(required = false) String timeFrame,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestBody(required = false) EmployeeFilterDTO filter)
    {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        if (timeFrame == null || timeFrame.isBlank()) {
            timeFrame = "all";
        }

        Page<Employee> result = service.getFilteredEmployees(
                role, email,filter, timeFrame, startDate, endDate, pageable);

        return ResponseEntity.ok(result);
    }


    @GetMapping("/getEmployeeById/{id}")
    public ResponseEntity<EmployeeResponseDTO> getById(@PathVariable Long id,
                                                       @RequestParam String role,
                                                       @RequestParam String email) {
        return ResponseEntity.ok(service.getEmployeeById(id, role, email));
    }

    @PutMapping(value = "/updateEmployee/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Employee> updateEmployee(
            @PathVariable Long id,
            @RequestPart("employeeDTO") String employeeDTOJson,
            @RequestParam("role") String role,
            @RequestParam("email") String email,
            @RequestParam(value = "departmentId",required = false) Long departmentId,
            @RequestParam(value = "categoryId",required = false) Long categoryId,
            @RequestParam(value = "idProof", required = false) MultipartFile idProof,
            @RequestParam(value = "employeePhoto", required = false) MultipartFile employeePhoto,
            @RequestParam(value = "resume", required = false) MultipartFile resume,
            @RequestParam(value = "addressProof", required = false) MultipartFile addressProof,
            @RequestParam(value = "experienceLetter", required = false) MultipartFile experienceLetter,
            @RequestParam(value = "slipImage", required = false) MultipartFile slipImage
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
                idProof, employeePhoto, resume, addressProof, experienceLetter,slipImage
        );
        return new ResponseEntity<>(updatedEmployee, HttpStatus.OK);
    }


    @DeleteMapping("/deleteEmployee/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable Long id,
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

//    @GetMapping("/permissions")
//    public ResponseEntity<Map<String, Object>> getCrudPermissions(@RequestParam String email) {
//        Map<String, Object> permissions = service.getCrudPermissionForEmployeeByEmail(email);
//        return ResponseEntity.ok(permissions);
//    }

    @PutMapping("updateStatus/{id}")
    public ResponseEntity<Employee> updateStus(@PathVariable Long id,
                                               @RequestParam  String role,
                                               @RequestParam String email,
                                               @RequestParam String Status,
                                               @RequestParam LocalDate date){
        return ResponseEntity.ok(service.updateStatus(role, email,id, Status,date));
    }

    @GetMapping("/upcomingEmployeeBirthdays")
    public List<EmployeeBirthdayDTO> getUpcomingBirthdays(@RequestParam String branchCode) {
        return service.getUpcomingBirthdays(branchCode);
    }


    @GetMapping("/getEmployeeAttendaceResponesId/{id}")
    public ResponseEntity<EmployeeAttendaceResponse> getEmployeeById(@PathVariable Long id,
                                                       @RequestParam String role,
                                                       @RequestParam String email) {
        return ResponseEntity.ok(service.getEmployeeAttendaceById(id, role, email));
    }
}