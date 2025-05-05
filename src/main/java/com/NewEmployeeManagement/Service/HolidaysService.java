package com.NewEmployeeManagement.Service;

import com.NewEmployeeManagement.Entity.Holidays;

import java.util.List;

public interface HolidaysService {
    Holidays createHoliday(Holidays holiday, String role, String email);
    List<Holidays> getAllHolidays(String role, String email, String branchCode);
    Holidays updateHoliday(Long id, Holidays holiday, String role, String email);
    void deleteHoliday(Long id, String role, String email);
    Holidays getHolidayById(Long id, String role, String email);
}