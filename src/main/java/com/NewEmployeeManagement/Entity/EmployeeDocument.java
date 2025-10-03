package com.NewEmployeeManagement.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.UniqueElements;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String idProof;
    private String employeePhoto;
    private String resume;
    private String addressProof;
    private String experienceLetter;
    private String slipImage;

    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "employee_id")
    private Employee employee;
}