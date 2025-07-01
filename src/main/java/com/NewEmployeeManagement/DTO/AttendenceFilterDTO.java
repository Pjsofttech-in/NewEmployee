package com.NewEmployeeManagement.DTO;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AttendenceFilterDTO {
    private String status; // present, absent, late, on time
    private String todaysDateFilter; // today, yesterday, last7days, last30days, last365days, custom
    private LocalDate startDate;
    private LocalDate endDate;
    private String branchCode;
    private String role;
    private String email;
    private int page = 0;
    private int size = 10;
}