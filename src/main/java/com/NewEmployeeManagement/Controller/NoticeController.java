package com.NewEmployeeManagement.Controller;

import com.NewEmployeeManagement.Entity.Notice;
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
    public ResponseEntity<Notice> createNotice(@RequestBody Notice notice,
                                               @RequestParam String role,
                                               @RequestParam String email) {
        return ResponseEntity.ok(service.createNotice(notice, role, email));
    }

    @GetMapping("/getAllNotices")
    public ResponseEntity<List<Notice>> getAllNotices(@RequestParam String role,
                                                      @RequestParam String email,
                                                      @RequestParam String branchCode) {
        return ResponseEntity.ok(service.getAllNotices(role, email, branchCode));
    }

    @GetMapping("/getNoticeById/{id}")
    public ResponseEntity<Notice> getNoticeById(@PathVariable int id,
                                                @RequestParam String role,
                                                @RequestParam String email) {
        return ResponseEntity.ok(service.getNoticeById(id, role, email));
    }

    @PutMapping("/updateNotice/{id}")
    public ResponseEntity<Notice> updateNotice(@PathVariable int id,
                                               @RequestBody Notice notice,
                                               @RequestParam String role,
                                               @RequestParam String email) {
        return ResponseEntity.ok(service.updateNotice(id, notice, role, email));
    }

    @DeleteMapping("/deleteNotice/{id}")
    public ResponseEntity<String> deleteNotice(@PathVariable int id,
                                               @RequestParam String role,
                                               @RequestParam String email) {
        service.deleteNotice(id, role, email);
        return ResponseEntity.ok("Notice deleted successfully");
    }
}