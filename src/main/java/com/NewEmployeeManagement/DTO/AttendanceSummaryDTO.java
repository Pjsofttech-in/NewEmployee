package com.NewEmployeeManagement.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSummaryDTO {
    private String branchCode;
    private int presentCount;
    private List<String> presentEmployees;
    private int absentCount;
    private List<String> absentEmployees;
}