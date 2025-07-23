package com.NewEmployeeManagement.Controller;

import com.NewEmployeeManagement.DTO.EmployeeCountResponse;
import com.NewEmployeeManagement.Service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class DashboardController
{
    @Autowired
    DashboardService dashboardService;

    @GetMapping("/getEmployeeCountForCardsAndGraph")
    public ResponseEntity<EmployeeCountResponse> getEmployeeCounts(
           @RequestParam String role,
           @RequestParam String email,
            @RequestParam String filter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        EmployeeCountResponse response = dashboardService.getEmployeeCounts(role, email,filter, startDate, endDate);
        return ResponseEntity.ok(response);
    }


}
