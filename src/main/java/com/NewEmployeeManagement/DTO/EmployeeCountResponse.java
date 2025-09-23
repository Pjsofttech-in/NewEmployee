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
    private Map<String, Long> statusCounts;      // e.g., { "Joined": 6, "Terminated": 3 }
    private Map<String, Long> departmentCounts;  // e.g., { "IT": 6 }
    private Map<String, Long> categoryCounts;    // e.g., { "Developer": 2, "Tester": 4 }

}
