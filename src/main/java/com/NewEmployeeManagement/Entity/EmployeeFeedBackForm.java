package com.NewEmployeeManagement.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class EmployeeFeedBackForm
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private String name;
//    private String email;
    private String subject;
    private String department;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String status;
    @Column(columnDefinition = "TEXT")
    private String remark;

    @Email
    private String createdByEmail;
    private String role;
    private String branchCode;


}
