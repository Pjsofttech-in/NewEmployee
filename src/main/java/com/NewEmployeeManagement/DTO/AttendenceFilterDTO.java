package com.NewEmployeeManagement.DTO;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AttendenceFilterDTO {

    private String status; // present, absent, late, on time
    private String name;
    private String branchCode;
}