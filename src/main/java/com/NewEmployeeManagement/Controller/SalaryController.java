package com.NewEmployeeManagement.Controller;

import com.NewEmployeeManagement.Entity.EmployeeSalary;
import com.NewEmployeeManagement.Service.SalaryService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public EmployeeSalary getSalary(
            @RequestParam String role, @RequestParam String email, @RequestParam Long empId,
            @RequestParam int month, @RequestParam int year)
    {
        return salaryService.getSalaryByEmpIdMonthYear(role,email,empId, month, year);
    }

    @GetMapping("/getAllSalary")
    public ResponseEntity<List<EmployeeSalary>> getAllSalary(@RequestParam String role, @RequestParam String email)
    {
        List<EmployeeSalary> salaries = salaryService.getAllSalary(role, email);
        return ResponseEntity.ok(salaries);
    }
}
