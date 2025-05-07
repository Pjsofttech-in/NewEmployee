package com.NewEmployeeManagement.DTO;

import lombok.Data;

@Data
public class EmployeeFilterDTO {
    private String department;
    private String categoryName;
    private String designation;
    private String status;
    private String branchCode;
    private String role;
    private String email;
    private int page = 0;
    private int size = 10;
}