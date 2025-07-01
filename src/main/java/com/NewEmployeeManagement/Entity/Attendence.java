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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String email;
    private LocalDate todaysDate;
    private String name;
    private LocalTime LoginTime;
    private LocalTime LogoutTime;
    private String status;
    private String systemName;
    private String systemIP;
    private String IP;       // Field for Wi-Fi IP
    private String logoutIP;
    private String shift;
    private LocalTime breakIn;
    private LocalTime breakOut;
    private Long breakMinutes;
    private Integer shiftMinutes;
    private String workType;
    private String shiftStartTime;
    private Double day;
    private String shiftEndTime;
    private Long overTime;
    private String branchCode;

    @ManyToOne
    @JoinColumn(name = "empid")
    @JsonIgnore
    private Employee employee;
}