package com.NewEmployeeManagement.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeCountResponse
{
    private Map<String, Long> statusCounts;
    private Map<String, Long> departmentCounts;
    private Map<String, Long> categoryCounts;

}
