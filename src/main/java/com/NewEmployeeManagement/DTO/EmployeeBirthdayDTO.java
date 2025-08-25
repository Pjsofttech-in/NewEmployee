package com.NewEmployeeManagement.DTO;

import java.time.LocalDate;

public interface EmployeeBirthdayDTO
{
    String getFullName();
    LocalDate getDob();
    String getDepartment();
    String getCategoryName();
}
