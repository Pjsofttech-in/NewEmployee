package com.NewEmployeeManagement.Controller;

import com.NewEmployeeManagement.DTO.EmployeeSalaryFilterDTO;
import com.NewEmployeeManagement.Entity.EmployeeSalary;
import com.NewEmployeeManagement.Service.SalaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SalaryController
{

    @Autowired
    SalaryService salaryService;


    @PostMapping("/calculateEmployeeSalary")
    public EmployeeSalary calculateAndSave(
            @RequestParam Long empId,
            @RequestParam String role,
            @RequestParam String email,
            @RequestBody EmployeeSalary salary
    ) {
        return salaryService.saveSalary(salary, empId, role, email);
    }

    @GetMapping("/getEmployeeSalaryByEmpIdMonthYear")
    public EmployeeSalary getSalary(@RequestParam String role, @RequestParam String email,
                                    @RequestParam Long empId, @RequestParam int month, @RequestParam int year)
    {
        return salaryService.getSalaryByEmpIdMonthYear(role,email,empId, month, year);
    }

    @PostMapping("/getAllSalary")
    public ResponseEntity<Page<EmployeeSalary>> getFilteredSalaries(
            @RequestParam String role,
            @RequestParam String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestBody EmployeeSalaryFilterDTO filter) {

        Page<EmployeeSalary> salaries = salaryService.getFilteredSalaries(filter, page, size, role, email);
        return ResponseEntity.ok(salaries);
    }

    @GetMapping("/getAllSalariesByEmpId")
    public ResponseEntity<List<EmployeeSalary>> getAllSalariesByEmpId(@RequestParam String role, @RequestParam String email, @RequestParam Long empId)
    {
        List<EmployeeSalary> salaries = salaryService.getAllSalaryByEmpId(role, email, empId);
        return ResponseEntity.ok(salaries);
    }

    @PutMapping("/updateSalaryStatus")
    public ResponseEntity<String> updateSalaryStatus(
            @RequestParam String role,
            @RequestParam String email,
            @RequestParam Long salaryId,
            @RequestParam String status) {

        String result = salaryService.updateSalaryStatus(salaryId, status, role, email);
        return ResponseEntity.ok(result);
    }

}
