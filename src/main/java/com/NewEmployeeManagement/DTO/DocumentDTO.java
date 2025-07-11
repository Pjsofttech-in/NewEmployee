package com.NewEmployeeManagement.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentDTO
{
    private Long id;
    private String idProof;
    private String employeePhoto;
    private String resume;
    private String addressProof;
    private String experienceLetter;
}
