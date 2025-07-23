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
    private String dutyType;
    private String shift;

}
