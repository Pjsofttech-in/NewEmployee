package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.Entity.Employee;
import com.NewEmployeeManagement.Entity.Holidays;
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
    public Holidays createHoliday(Holidays holiday, int employeeId, String role, String email) {
        if (!permissionService.hasPermission(role, email, "POST")) {
            throw new AccessDeniedException("No permission to create holiday");
        }

        String branchCode = permissionService.fetchBranchCode(role, email);
        holiday.setCreatedByEmail(email);
        holiday.setBranchCode(branchCode);
        holiday.setRole(role);

        // Fetch employee and set to holiday
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(()-> new RuntimeException("employee not found by id:"+employeeId));
        holiday.setEmployee(employee);

        return repository.save(holiday);
    }


    @Override
    public List<Holidays> getAllHolidays(String role, String email) {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view holidays");
        }
        String branchCode = permissionService.fetchBranchCode(role, email);
        return repository.findAllByBranchCode(branchCode);
    }

    @Override
    public Holidays updateHoliday(Long id, Holidays holiday, String role, String email) {
        if (!permissionService.hasPermission(role, email, "PUT")) {
            throw new AccessDeniedException("No permission to update holiday");
        }

        Holidays existing = repository.findById(id)
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
    public Holidays getHolidayById(Long id, String role, String email) {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view holiday");
        }

        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Holiday not found"));
    }
}