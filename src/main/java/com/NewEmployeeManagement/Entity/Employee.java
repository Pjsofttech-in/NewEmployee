package com.NewEmployeeManagement.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String password;
    private String fullName;

    @Column(name = "mobile_no", length = 10)
    private String mobileNo;
    private int otp;
    private Long parentNo;

    @Email
    @Column(unique = true, nullable = false)
    private String email;
    private LocalDate dob;
}
