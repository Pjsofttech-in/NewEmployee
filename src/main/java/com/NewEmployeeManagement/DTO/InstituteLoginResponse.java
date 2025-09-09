package com.NewEmployeeManagement.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InstituteLoginResponse
{
    private String instituteEmail;
    private String instituteName;
    private Long phoneNumber;
    private String instituteImage;
    private String address;
    private String city;
    private String state;
    private String district;

}
