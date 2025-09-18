package com.NewEmployeeManagement.Mapper;


import com.NewEmployeeManagement.DTO.AddressDTO;
import com.NewEmployeeManagement.DTO.DocumentDTO;
import com.NewEmployeeManagement.DTO.EmployeeDTO;
import com.NewEmployeeManagement.DTO.EmployeeResponseDTO;
import com.NewEmployeeManagement.Entity.Employee;
import com.NewEmployeeManagement.Entity.EmployeeAddress;
import com.NewEmployeeManagement.Entity.EmployeeDocument;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    public static EmployeeResponseDTO mapToDto(Employee employee) {

        // Map basic employee fields
        EmployeeDTO employeeDTO = new EmployeeDTO(
                employee.getId(),
                employee.getFullName(),
                employee.getBloodGroup(),
                employee.getGender(),
                employee.getEmpEmail(),
                employee.getPassword(),
                employee.getDob(),
                employee.getAdharNo(),
                employee.getPanNo(),
                employee.getMobileNo(),
                employee.getOtp(),
                employee.getOtpExpiry(),
                employee.getParentNo(),
                employee.getJoiningDate(),
                employee.getDepartment(),
                employee.getWorkLocation(),
                employee.getDesignation(),
                employee.getDutyType(),
                employee.getEmployeeType(),
                employee.getSalary(),
                employee.getCpfNo(),
                employee.getEsicNo(),
                employee.getBasicQualification(),
                employee.getProfessionalQualification(),
                employee.getShift(),
                employee.getShiftStartTime(),
                employee.getShiftEndTime(),
                employee.getCategoryName(),
                employee.getStatus(),
                employee.getSystemName(),
                employee.getOsen(),
                employee.getToMail(),
                employee.getSubject(),
                employee.getBody(),
                employee.getCreateAt(),
                employee.getRejoiningData(),
                employee.getTerminatDate(),
                employee.getPaidleaves(),
                employee.getCarryForwardedLeaves(),
                employee.getUnpaidleaves(),
                employee.isDeleted(),
                employee.getFaceEncoding(),
                employee.getEmpRole(),
                employee.getCreatedByEmail(),
                employee.getRole(),
                employee.getBranchCode()
        );

        // Map document
        EmployeeDocument document = employee.getEmployeeDocument();
        DocumentDTO documentDTO = null;
        if (document != null) {
            documentDTO = new DocumentDTO(
                    document.getId(),
                    document.getIdProof(),
                    document.getEmployeePhoto(),
                    document.getResume(),
                    document.getAddressProof(),
                    document.getExperienceLetter()
            );
        }

        // Map address
        EmployeeAddress address = employee.getEmployeeAddress();
        AddressDTO addressDTO = null;
        if (address != null) {
            addressDTO = new AddressDTO(
                    address.getId(),
                    address.getCountry(),
                    address.getState(),
                    address.getDistrict(),
                    address.getTaluka(),
                    address.getCity(),
                    address.getPinCode(),
                    address.getLandmark(),
                    address.getCurrentAddress(),
                    address.getPcountry(),
                    address.getPstate(),
                    address.getPdistrict(),
                    address.getPtaluka(),
                    address.getPcity(),
                    address.getPpinCode(),
                    address.getPlandmark(),
                    address.getPaddress()
            );
        }

        return new EmployeeResponseDTO(employeeDTO, documentDTO, addressDTO);
    }
}