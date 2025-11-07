package com.NewEmployeeManagement.Controller;

import com.NewEmployeeManagement.Entity.EmployeeNotice;
import com.NewEmployeeManagement.Service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@CrossOrigin(origins = "https://pjsofttech.in")
public class NoticeController {

    @Autowired
    private NoticeService service;

    @PostMapping("/createNotice")
    public ResponseEntity<EmployeeNotice> createNotice(@RequestBody EmployeeNotice employeeNotice,
                                                       @RequestParam String role,
                                                       @RequestParam String email) {
        return ResponseEntity.ok(service.createNotice(employeeNotice, role, email));
    }

    @GetMapping("/getAllNotices")
    public ResponseEntity<Page<EmployeeNotice>> getAllNotices(
            @RequestParam String role,
            @RequestParam String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String branchCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<EmployeeNotice> notices = service.getAllNotices(role, email, startDate, endDate, branchCode, page, size);
        return ResponseEntity.ok(notices);
    }


    @GetMapping("/getNoticeById/{id}")
    public ResponseEntity<EmployeeNotice> getNoticeById(@PathVariable Long id,
                                                        @RequestParam String role,
                                                        @RequestParam String email) {
        return ResponseEntity.ok(service.getNoticeById(id, role, email));
    }

    @PutMapping("/updateNotice/{id}")
    public ResponseEntity<EmployeeNotice> updateNotice(@PathVariable Long id,
                                                       @RequestBody EmployeeNotice employeeNotice,
                                                       @RequestParam String role,
                                                       @RequestParam String email) {
        return ResponseEntity.ok(service.updateNotice(id, employeeNotice, role, email));
    }

    @DeleteMapping("/deleteNotice/{id}")
    public ResponseEntity<String> deleteNotice(@PathVariable Long id,
                                               @RequestParam String role,
                                               @RequestParam String email) {
        service.deleteNotice(id, role, email);
        return ResponseEntity.ok("Notice deleted successfully");
    }


}