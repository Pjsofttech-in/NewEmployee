package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.Entity.Employee;
import com.NewEmployeeManagement.Entity.EmployeeHolidays;
import com.NewEmployeeManagement.Repository.EmployeeRepository;
import com.NewEmployeeManagement.Repository.HolidaysRepository;
import com.NewEmployeeManagement.Service.HolidaysService;
import com.NewEmployeeManagement.Service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HolidaysServiceImpl implements HolidaysService {

    @Autowired
    HolidaysRepository repository;

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private PermissionService permissionService;

    @Override
    public EmployeeHolidays createHoliday(EmployeeHolidays holiday, String role, String email) {
        if (!permissionService.hasPermission(role, email, "POST")) {
            throw new AccessDeniedException("No permission to create holiday");
        }

        String branchCode = permissionService.fetchBranchCode(role, email);
        holiday.setCreatedByEmail(email);
        holiday.setBranchCode(branchCode);
        holiday.setRole(role);

        // Fetch employee and set to holiday
        holiday.setEmployee(null);

        return repository.save(holiday);
    }


    @Override
    public List<EmployeeHolidays> getAllHolidays(String role, String email) {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view holidays");
        }
        String branchCode = permissionService.fetchBranchCode(role, email);
        return repository.findAllByBranchCode(branchCode);
    }

    @Override
    public EmployeeHolidays updateHoliday(Long id, EmployeeHolidays holiday, String role, String email) {
        if (!permissionService.hasPermission(role, email, "PUT")) {
            throw new AccessDeniedException("No permission to update holiday");
        }

        EmployeeHolidays existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Holiday not found"));

        existing.setHolidayName(holiday.getHolidayName() != null ? holiday.getHolidayName() : existing.getHolidayName());
        existing.setDate(holiday.getDate() != null ? holiday.getDate() : existing.getDate());
        existing.setDay(holiday.getDay() != null ? holiday.getDay() : existing.getDay());
        existing.setPaidHoliday(holiday.isPaidHoliday() != existing.isPaidHoliday() ? holiday.isPaidHoliday() : existing.isPaidHoliday());

        return repository.save(existing);
    }


    @Override
    public void deleteHoliday(Long id, String role, String email) {
        if (!permissionService.hasPermission(role, email, "DELETE")) {
            throw new AccessDeniedException("No permission to delete holiday");
        }

        repository.findById(id).orElseThrow(() -> new RuntimeException("Holiday not found"));
        repository.deleteById(id);
    }

    @Override
    public EmployeeHolidays getHolidayById(Long id, String role, String email) {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view holiday");
        }

        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Holiday not found"));
    }
}