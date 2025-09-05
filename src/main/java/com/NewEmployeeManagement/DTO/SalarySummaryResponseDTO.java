package com.NewEmployeeManagement.DTO;

import com.NewEmployeeManagement.Entity.EmployeeSalary;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SalarySummaryResponseDTO
{
    private Page<EmployeeSalary> salaries;
    private long totalCount;
    private BigDecimal totalSum;
}
