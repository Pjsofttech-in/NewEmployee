package com.NewEmployeeManagement.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeSalaryFilterDTO
{
    private Long empId;
    private String fullName;
    private String department;
    private String employeecategory;
    private Integer month;
    private Integer year;
    private String status;
}
