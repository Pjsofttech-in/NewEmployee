package com.NewEmployeeManagement.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class BirthdayDTO
{
    private String fullName;
    private LocalDate dob;
    private String department;
    private String category;

    public BirthdayDTO(String fullName, LocalDate dob, String department, String category) {
        this.fullName = fullName;
        this.dob = dob;
        this.department = department;
        this.category = category;
    }
}
