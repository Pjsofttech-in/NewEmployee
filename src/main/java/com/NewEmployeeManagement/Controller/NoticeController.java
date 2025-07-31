package com.NewEmployeeManagement.Controller;

import com.NewEmployeeManagement.Entity.EmployeeNotice;
import com.NewEmployeeManagement.Service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<EmployeeNotice>> getAllNotices(@RequestParam String role,
                                                              @RequestParam String email) {
        return ResponseEntity.ok(service.getAllNotices(role, email));
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


    @GetMapping("/getNoticeByEmail")
    public List<EmployeeNotice> getNoticesByEmail(@RequestParam String role,@RequestParam String email)
    {
        return service.getNoticesByEmail(role,email);
    }
}