package com.NewEmployeeManagement.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String country;
    private String state;
    private String district;
    private String taluka;
    private String city;
    private int pinCode;
    private String landmark;
    private String currentAddress;

    private String pcountry;
    private String pstate;
    private String pdistrict;
    private String ptaluka;
    private String pcity;
    private int ppinCode;
    private String plandmark;
    private String paddress;

    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "employee_id")
    private Employee employee;
}