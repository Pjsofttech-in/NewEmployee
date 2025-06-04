package com.NewEmployeeManagement.DTO;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeFilterDTO {
    private String department;
    private String categoryName;
    private String designation;
    private String status;
    private String fullName;
    private String joiningDateFilter; // today, last7days, last30days, last365days, custom
    private LocalDate startDate;
    private LocalDate endDate;
    private String branchCode;
    private String role;
    private String email;
    private int page = 0;
    private int size = 10;
}
