package com.NewEmployeeManagement.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceLoginRequestDTO {
    private Long employeeId;
    private String systemIP;
    private String wifiIP;
}