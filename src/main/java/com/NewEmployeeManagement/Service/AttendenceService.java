package com.NewEmployeeManagement.Service;

import com.NewEmployeeManagement.DTO.AttendanceLoginRequestDTO;
import com.NewEmployeeManagement.Entity.Attendence;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AttendenceService {

    String handleFaceRecognitionAndAttendance(MultipartFile selfieImage, HttpServletRequest request);

}