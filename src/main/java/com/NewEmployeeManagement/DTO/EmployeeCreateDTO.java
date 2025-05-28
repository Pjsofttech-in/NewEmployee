package com.NewEmployeeManagement.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class EmployeeCreateDTO {
    private String fullName;
    private String empEmail;
    private String password;
    private LocalDate dob;
    private String mobileNo;
    private String parentNo;
    private String gender;
    private String bloodGroup;
    private Long adharNo;
    private String panNo;
    private Date joiningDate;
    private String department;
    private String workLocation;
    private String designation;
    private String dutyType;
    private String employeeType;
    private BigDecimal salary;
    private String cpfNo;
    private Long esicNo;
    private String basicQualification;
    private String professionalQualification;
    private String shift;
    private String shiftStartTime;
    private String shiftEndTime;
    private String categoryName;
    private String status = "Joined";
    //extra Felid
    private int osen;

    private String toMail;
    private String subject;
    private String body;

    private String systemName;

    private LocalDateTime createAt;

    // Leave information
    private Double paidleaves;
    private Double carryForwardedLeaves =0.0;
    private Double unpaidleaves;

    @JsonProperty("isDeleted")
    private boolean isDeleted = false;

    private String faceEncoding;

    @Email
    private String createdByEmail;
    private String role;
    private String branchCode;

    private AddressDTO address;
    private DocumentDTO document;

    // other employee fields...

    @Data
    public static class AddressDTO {
        private String country;
        private String state;
        private String district;
        private String taluka;
        private String city;
        private int pinCode;
        private String landmark;
        private String currentAddress;
        @JsonProperty("pCountry")
        private String pcountry;

        @JsonProperty("pState")
        private String pstate;

        @JsonProperty("pDistrict")
        private String pdistrict;

        @JsonProperty("pTaluka")
        private String ptaluka;

        @JsonProperty("pCity")
        private String pcity;

        @JsonProperty("pPinCode")
        private int ppinCode;

        @JsonProperty("pLandmark")
        private String plandmark;

        @JsonProperty("pAddress")
        private String paddress;
    }

    @Data
    public static class DocumentDTO {
        private String idProof;
        private String employeePhoto;
        private String resume;
        private String addressProof;
        private String experienceLetter;
    }
}
