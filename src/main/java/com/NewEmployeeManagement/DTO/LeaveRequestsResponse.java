package com.NewEmployeeManagement.DTO;

import com.NewEmployeeManagement.Entity.EmployeeLeaveRequest;
import org.springframework.data.domain.Page;

import java.util.Map;

public class LeaveRequestsResponse
{
    private Page<EmployeeLeaveRequest> page;
    private Map<String, Long> statusCounts;

    public LeaveRequestsResponse(Page<EmployeeLeaveRequest> page, Map<String, Long> statusCounts) {
        this.page = page;
        this.statusCounts = statusCounts;
    }

    public Page<EmployeeLeaveRequest> getPage() { return page; }
    public void setPage(Page<EmployeeLeaveRequest> page) { this.page = page; }

    public Map<String, Long> getStatusCounts() { return statusCounts; }
    public void setStatusCounts(Map<String, Long> statusCounts) { this.statusCounts = statusCounts; }
}
