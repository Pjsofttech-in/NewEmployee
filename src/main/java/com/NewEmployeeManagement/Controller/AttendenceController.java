package com.NewEmployeeManagement.Controller;

import com.NewEmployeeManagement.DTO.AttendanceLoginRequestDTO;
import com.NewEmployeeManagement.Entity.Attendence;
import com.NewEmployeeManagement.Service.AttendenceService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class AttendenceController {

    @Autowired
    private AttendenceService attendenceService;

    @PostMapping("/AttendanceLogin")
    public ResponseEntity<String> handleFaceRecognitionAttendance(
            @RequestParam("selfieImage") MultipartFile selfieImage,
            HttpServletRequest request) {

        try {
            String message = attendenceService.handleFaceRecognitionAndAttendance(selfieImage, request);
            return ResponseEntity.ok(message);
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("Invalid request: " + ex.getMessage());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + ex.getMessage());
        }
    }
}