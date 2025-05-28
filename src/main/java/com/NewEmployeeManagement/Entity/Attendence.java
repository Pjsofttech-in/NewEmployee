package com.NewEmployeeManagement.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Attendence {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private int empID;
    private LocalDate todaysDate;
    private String name;
    private LocalTime LoginTime;
    private LocalTime LogoutTime;
    private Long minutes;
    private String status;
    private String systemIP;
    private String IP;       // Field for Wi-Fi IP
    private String logoutIP;
    private String shift;
    private LocalTime breakIn;
    private LocalTime breakOut;
    private Long breakMinutes;
    private Long shiftMinutes;
    private String workType;
    private String lateMark;
    private String shiftStartTime;
    private String shiftEndTime;

    @Email
    private String createdByEmail;
    private String role;
    private String branchCode;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    @JsonIgnore
    private Employee employee;

}