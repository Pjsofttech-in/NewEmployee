package com.NewEmployeeManagement.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeLeaveSummaryDTO {
    private Double totalPaidLeaves;
    private Double totalUnpaidLeaves;
    private Double totalAppliedLeaves;
    private Double paidLeave;
    private Double unpaidLeave;
    private Double remaingPaidLeave;
}