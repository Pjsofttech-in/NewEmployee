package com.NewEmployeeManagement.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //1st page form
    private String fullName;
    private String bloodGroup;
    private String gender;
    @Email
    @Column(name = "emp_email", unique = true, nullable = false)
    private String empEmail;
    private String password;
    @Past(message = "Date of birth must be a past date")
    private LocalDate dob;

    private Long adharNo;
    private String panNo;
    @Column(name = "mobile_no", length = 10)
    @Size(min = 10, max = 10, message = "Mobile number must be exactly 10 digits")
    @Pattern(regexp = "\\d{10}", message = "Mobile number must be 10 digits")
    private String mobileNo;
    private int otp;
    private Date otpExpiry;
    @Column(name = "pMobile_no", length = 10)
    @Pattern(regexp = "\\d{10}", message = "Mobile number must be 10 digits")
    private String parentNo;



    //2nd page form
    @Column(name = "joining_date")
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

    // Leave information
    private Double paidleaves;
    private Double carryForwardedLeaves =0.0;
    private Double unpaidleaves;

    private boolean isDeleted = false;

    private String faceEncoding;
    private String empRole;

    @Email
    private String createdByEmail;
    private String role;
    private String branchCode;

    @ManyToOne
    @JoinColumn(name = "department_id")
    @JsonIgnore
    private EmployeeDepartment employeeDepartment;

    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonIgnore
    private EmployeeCategory employeeCategory;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<EmployeeQuery> empQueries;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<EmployeeLeaveRequest> employeeLeaveRequests = new ArrayList<>();

    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL)
    @JsonIgnore
    private EmployeeAddress employeeAddress;

    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL)
    @JsonIgnore
    private EmployeeDocument employeeDocument;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<EmployeeAttendence> employeeAttendences = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<EmployeeHolidays> holidays = new ArrayList<>();

    private boolean candGet;
    private boolean candPost;
    private boolean candPut;
    private boolean candDelete;
}
