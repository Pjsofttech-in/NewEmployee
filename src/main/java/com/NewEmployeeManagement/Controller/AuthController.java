package com.NewEmployeeManagement.Controller;

import com.NewEmployeeManagement.Entity.Employee;
import com.NewEmployeeManagement.JWT.JwtUtil;
import com.NewEmployeeManagement.JWT.LoginRequest;
import com.NewEmployeeManagement.JWT.LoginResponse;
import com.NewEmployeeManagement.Repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final EmployeeRepository employeeRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/userLogin")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<Employee> optionalEmployee = employeeRepository.findByEmpEmail(request.getEmail());

        if (optionalEmployee.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }

        Employee employee = optionalEmployee.get();

        if (!passwordEncoder.matches(request.getPassword(), employee.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }

        String token = jwtUtil.generateToken(employee.getEmpEmail());

        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("id", employee.getId());
        userDetails.put("fullName", employee.getFullName());
        userDetails.put("empEmail", employee.getEmpEmail());
        userDetails.put("role", employee.getEmpRole());
        userDetails.put("branchCode", employee.getBranchCode());

        return ResponseEntity.ok(new LoginResponse(token, userDetails));
    }
}