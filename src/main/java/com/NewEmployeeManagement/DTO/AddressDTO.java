package com.NewEmployeeManagement.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO
{
    private Long id;

    private String country;
    private String state;
    private String district;
    private String taluka;
    private String city;
    private int pinCode;
    private String landmark;
    private String currentAddress;

    private String pCountry;
    private String pState;
    private String pDistrict;
    private String pTaluka;
    private String pCity;
    private int pPinCode;
    private String pLandmark;
    private String pAddress;
}
