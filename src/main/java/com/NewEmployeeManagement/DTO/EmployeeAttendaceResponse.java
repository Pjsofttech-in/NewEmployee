package com.NewEmployeeManagement.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeAttendaceResponse
{

    private Long id;
    private String fullName;
    private String bloodGroup;
    private String gender;
    private String empEmail;
    private LocalDate dob;
    private String mobileNo;
    private LocalDate joiningDate;
    private String department;
    private String workLocation;
    private String designation;
    private String dutyType;
    private String employeeType;
    private String shift;
    private String shiftStartTime;
    private String shiftEndTime;
    private String categoryName;
    private String empRole;

}
