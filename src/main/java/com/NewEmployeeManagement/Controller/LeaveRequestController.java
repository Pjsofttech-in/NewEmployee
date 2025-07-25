package com.NewEmployeeManagement.Controller;

import com.NewEmployeeManagement.DTO.EmployeeLeaveSummaryDTO;
import com.NewEmployeeManagement.Entity.EmployeeLeaveRequest;
import com.NewEmployeeManagement.Service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
//@CrossOrigin(origins = "http://localhost:3000")
@CrossOrigin(origins = "https://pjsofttech.in")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @Autowired
    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @PostMapping("/createLeaveRequest")
    public EmployeeLeaveRequest createLeaveRequest(@RequestBody EmployeeLeaveRequest employeeLeaveRequest,
                                                   @RequestParam Long employeeId,
                                                   @RequestParam String role,
                                                   @RequestParam String email) {
        return leaveRequestService.createLeaveRequest(employeeLeaveRequest, employeeId, role, email);
    }

    @GetMapping("/getAllLeaveRequests")
    public List<EmployeeLeaveRequest> getAllLeaveRequests(@RequestParam String role,
                                                          @RequestParam String email) {
        return leaveRequestService.getAllLeaveRequests(role, email);
    }

    @GetMapping("/getLeaveRequestById/{id}")
    public EmployeeLeaveRequest getLeaveRequestById(@PathVariable Long id,
                                                    @RequestParam String role, @RequestParam String email) {
        return leaveRequestService.getLeaveRequestById(id, role, email);
    }

    @PutMapping("/updateLeaveRequest/{id}")
    public EmployeeLeaveRequest updateLeaveRequest(@PathVariable Long id,
                                                   @RequestBody EmployeeLeaveRequest employeeLeaveRequest,
                                                   @RequestParam String role, @RequestParam String email) {
        return leaveRequestService.updateLeaveRequest(id, employeeLeaveRequest, role, email);
    }

    @DeleteMapping("/deleteLeaveRequest/{id}")
    public void deleteLeaveRequest(@PathVariable Long id, @RequestParam String role, @RequestParam String email) {
        leaveRequestService.deleteLeaveRequest(id, role, email);
    }

    @PostMapping("/leaveStatus/{id}")
    public EmployeeLeaveRequest approveOrRejectLeave(@PathVariable Long id,
                                                     @RequestParam String action,
                                                     @RequestParam String role,
                                                     @RequestParam String email) {
        return leaveRequestService.approveOrRejectLeave(id, action, role, email);
    }

    @GetMapping("/getLeaveSummary/{employeeId}")
    public ResponseEntity<EmployeeLeaveSummaryDTO> getLeaveSummary(@RequestParam String role, @RequestParam String email,@PathVariable Long employeeId) {
        EmployeeLeaveSummaryDTO summary = leaveRequestService.getLeaveSummary(role,email,employeeId);
        return ResponseEntity.ok(summary);
    }

}