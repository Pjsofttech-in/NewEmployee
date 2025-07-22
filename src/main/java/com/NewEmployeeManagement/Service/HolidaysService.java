package com.NewEmployeeManagement.Service;

import com.NewEmployeeManagement.Entity.EmployeeHolidays;

import java.util.List;

public interface HolidaysService {
    EmployeeHolidays createHoliday(EmployeeHolidays holiday, String role, String email);
    List<EmployeeHolidays> getAllHolidays(String role, String email);
    EmployeeHolidays updateHoliday(Long id, EmployeeHolidays holiday, String role, String email);
    void deleteHoliday(Long id, String role, String email);
    EmployeeHolidays getHolidayById(Long id, String role, String email);
}