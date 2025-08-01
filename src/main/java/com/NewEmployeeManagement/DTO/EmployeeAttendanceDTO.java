package com.NewEmployeeManagement.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeAttendanceDTO
{
    private Long empId;
    private String name;
    private String email;
    private LocalDate date;
    private String status; // Present / Absent / Sunday
    private LocalTime loginTime;
    private LocalTime logoutTime;
    private LocalTime breakIn;
    private LocalTime breakOut;
    private Long breakMinutes;
}
