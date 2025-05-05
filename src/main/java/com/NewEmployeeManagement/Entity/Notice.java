package com.NewEmployeeManagement.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String noticeName;
    private String noticeDescription;
    private LocalDate createdAt = LocalDate.now();
    private boolean isDeleted = false;
    @Email(message = "Email should be valid")
    private String email;

    @Email
    private String createdByEmail;
    private String role;
    private String branchCode;
}