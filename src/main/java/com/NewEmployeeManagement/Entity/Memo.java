package com.NewEmployeeManagement.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Memo
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String memoName;
    @Column(name = "memo_description", length = 5000)
    private String memoDescription;
    private LocalDate createdAt;
    @Email
    private String email;
    private String fullName;
    private boolean isDeleted = false;

    @Email
    private String createdByEmail;
    private String role;
    private String branchCode;
}