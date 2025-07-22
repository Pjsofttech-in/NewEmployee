package com.NewEmployeeManagement.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO
{
    private Long id;

    private String fullName;
    private String bloodGroup;
    private String gender;
    private String empEmail;
    private String password;
    private LocalDate dob;
    private Long adharNo;
    private String panNo;
    private String mobileNo;
    private int otp;
    private Date otpExpiry;
    private String parentNo;
    private LocalDate joiningDate;
    private String department;
    private String workLocation;
    private String designation;
    private String dutyType;
    private String employeeType;
    private BigDecimal salary;
    private String cpfNo;
    private Long esicNo;
    private String basicQualification;
    private String professionalQualification;
    private String shift;
    private String shiftStartTime;
    private String shiftEndTime;
    private String categoryName;
    private String status = "Joined";
    private String systemName;
    private int osen;
    private String toMail;
    private String subject;
    private String body;
    private LocalDateTime createAt;
    private Double paidleaves;
    private Double carryForwardedLeaves =0.0;
    private Double unpaidleaves;
    private boolean isDeleted = false;
    private String faceEncoding;
    private String empRole;

    private String createdByEmail;
    private String role;
    private String branchCode;
}
