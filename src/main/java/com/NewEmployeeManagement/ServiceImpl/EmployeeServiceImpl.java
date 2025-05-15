package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.Entity.Department;
import com.NewEmployeeManagement.Entity.Employee;
import com.NewEmployeeManagement.Entity.EmployeeCategory;
import com.NewEmployeeManagement.Repository.DepartmentRepository;
import com.NewEmployeeManagement.Repository.EmployeeCategoryRepository;
import com.NewEmployeeManagement.Repository.EmployeeRepository;
import com.NewEmployeeManagement.Service.EmployeeService;
import com.NewEmployeeManagement.Service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepository repository;

    @Autowired
    DepartmentRepository departmentRepository;
    @Autowired
    EmployeeCategoryRepository employeeCategoryRepository;

    @Autowired
    private PermissionService permissionService;

    @Override
    public Employee createEmployee(Employee employee, String role, String email, int departmentId, Long categoryId) {
        if (!permissionService.hasPermission(role, email, "POST")) throw new AccessDeniedException("No permission");

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found with ID: " + departmentId));

        EmployeeCategory category = employeeCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + categoryId));

        // Set string values
        employee.setDepartment(department.getDepartment());
        employee.setCategoryName(category.getCategoryName());

        // Set entity associations
        employee.setDepartmentEntity(department);
        employee.setEmployeeCategory(category);

        employee.setCreatedByEmail(email);
        employee.setRole(role);
        employee.setBranchCode(permissionService.fetchBranchCode(role, email));
        return repository.save(employee);
    }



    @Override
    public List<Employee> getAllEmployees(String role, String email) {
        if (!permissionService.hasPermission(role, email, "GET")) throw new AccessDeniedException("No permission");
       String branchCode = permissionService.fetchBranchCode(role, email);
        return repository.findAllByBranchCodeAndIsDeletedFalse(branchCode);
    }

    @Override
    public Employee getEmployeeById(int id, String role, String email) {
        if (!permissionService.hasPermission(role, email, "GET")) throw new AccessDeniedException("No permission");
        return repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Employee not found or has been deleted"));
    }

    @Override
    public Employee updateEmployee(int id, Employee employee, String role, String email) {
        if (!permissionService.hasPermission(role, email, "PUT")) throw new AccessDeniedException("No permission");

        Employee existing = repository.findById(id).orElseThrow(() -> new RuntimeException("Employee not found"));

        existing.setFullName(employee.getFullName() != null ? employee.getFullName() : existing.getFullName());
        existing.setBloodGroup(employee.getBloodGroup() != null ? employee.getBloodGroup() : existing.getBloodGroup());
        existing.setGender(employee.getGender() != null ? employee.getGender() : existing.getGender());
        existing.setEmpEmail(employee.getEmpEmail() != null ? employee.getEmpEmail() : existing.getEmpEmail());
        existing.setPassword(employee.getPassword() != null ? employee.getPassword() : existing.getPassword());
        existing.setDob(employee.getDob() != null ? employee.getDob() : existing.getDob());
        existing.setAdharNo(employee.getAdharNo() != null ? employee.getAdharNo() : existing.getAdharNo());
        existing.setPanNo(employee.getPanNo() != null ? employee.getPanNo() : existing.getPanNo());
        existing.setMobileNo(employee.getMobileNo() != null ? employee.getMobileNo() : existing.getMobileNo());
        existing.setOtp(employee.getOtp() != 0 ? employee.getOtp() : existing.getOtp());
        existing.setOtpExpiry(employee.getOtpExpiry() != null ? employee.getOtpExpiry() : existing.getOtpExpiry());
        existing.setParentNo(employee.getParentNo() != null ? employee.getParentNo() : existing.getParentNo());
        existing.setCountry(employee.getCountry() != null ? employee.getCountry() : existing.getCountry());
        existing.setState(employee.getState() != null ? employee.getState() : existing.getState());
        existing.setDistrict(employee.getDistrict() != null ? employee.getDistrict() : existing.getDistrict());
        existing.setTaluka(employee.getTaluka() != null ? employee.getTaluka() : existing.getTaluka());
        existing.setCity(employee.getCity() != null ? employee.getCity() : existing.getCity());
        existing.setPinCode(employee.getPinCode() != 0 ? employee.getPinCode() : existing.getPinCode());
        existing.setLandmark(employee.getLandmark() != null ? employee.getLandmark() : existing.getLandmark());
        existing.setCurrentAddress(employee.getCurrentAddress() != null ? employee.getCurrentAddress() : existing.getCurrentAddress());
        existing.setPAddress(employee.getPAddress() != null ? employee.getPAddress() : existing.getPAddress());
        existing.setPCountry(employee.getPCountry() != null ? employee.getPCountry() : existing.getPCountry());
        existing.setPState(employee.getPState() != null ? employee.getPState() : existing.getPState());
        existing.setPDistrict(employee.getPDistrict() != null ? employee.getPDistrict() : existing.getPDistrict());
        existing.setPTaluka(employee.getPTaluka() != null ? employee.getPTaluka() : existing.getPTaluka());
        existing.setPCity(employee.getPCity() != null ? employee.getPCity() : existing.getPCity());
        existing.setPPinCode(employee.getPPinCode() != 0 ? employee.getPPinCode() : existing.getPPinCode());
        existing.setPLandmark(employee.getPLandmark() != null ? employee.getPLandmark() : existing.getPLandmark());
        existing.setJoiningDate(employee.getJoiningDate() != null ? employee.getJoiningDate() : existing.getJoiningDate());
        existing.setDepartment(employee.getDepartment() != null ? employee.getDepartment() : existing.getDepartment());
        existing.setWorkLocation(employee.getWorkLocation() != null ? employee.getWorkLocation() : existing.getWorkLocation());
        existing.setDesignation(employee.getDesignation() != null ? employee.getDesignation() : existing.getDesignation());
        existing.setDutyType(employee.getDutyType() != null ? employee.getDutyType() : existing.getDutyType());
        existing.setEmployeeType(employee.getEmployeeType() != null ? employee.getEmployeeType() : existing.getEmployeeType());
        existing.setSalary(employee.getSalary() != null ? employee.getSalary() : existing.getSalary());
        existing.setCpfNo(employee.getCpfNo() != null ? employee.getCpfNo() : existing.getCpfNo());
        existing.setEsicNo(employee.getEsicNo() != null ? employee.getEsicNo() : existing.getEsicNo());
        existing.setBasicQualification(employee.getBasicQualification() != null ? employee.getBasicQualification() : existing.getBasicQualification());
        existing.setProfessionalQualification(employee.getProfessionalQualification() != null ? employee.getProfessionalQualification() : existing.getProfessionalQualification());
        existing.setShift(employee.getShift() != null ? employee.getShift() : existing.getShift());
        existing.setShiftStartTime(employee.getShiftStartTime() != null ? employee.getShiftStartTime() : existing.getShiftStartTime());
        existing.setShiftEndTime(employee.getShiftEndTime() != null ? employee.getShiftEndTime() : existing.getShiftEndTime());
        existing.setCategoryName(employee.getCategoryName() != null ? employee.getCategoryName() : existing.getCategoryName());
        existing.setStatus(employee.getStatus() != null ? employee.getStatus() : existing.getStatus());
        existing.setIdProof(employee.getIdProof() != null ? employee.getIdProof() : existing.getIdProof());
        existing.setEmployeePhoto(employee.getEmployeePhoto() != null ? employee.getEmployeePhoto() : existing.getEmployeePhoto());
        existing.setResume(employee.getResume() != null ? employee.getResume() : existing.getResume());
        existing.setAddressProof(employee.getAddressProof() != null ? employee.getAddressProof() : existing.getAddressProof());
        existing.setExperienceLetter(employee.getExperienceLetter() != null ? employee.getExperienceLetter() : existing.getExperienceLetter());
        existing.setOsen(employee.getOsen() != 0 ? employee.getOsen() : existing.getOsen());
        existing.setToMail(employee.getToMail() != null ? employee.getToMail() : existing.getToMail());
        existing.setSubject(employee.getSubject() != null ? employee.getSubject() : existing.getSubject());
        existing.setBody(employee.getBody() != null ? employee.getBody() : existing.getBody());
        existing.setCreateAt(employee.getCreateAt() != null ? employee.getCreateAt() : existing.getCreateAt());
        existing.setPaidleaves(employee.getPaidleaves() != null ? employee.getPaidleaves() : existing.getPaidleaves());
        existing.setCarryForwardedLeaves(employee.getCarryForwardedLeaves() != null ? employee.getCarryForwardedLeaves() : existing.getCarryForwardedLeaves());
        existing.setUnpaidleaves(employee.getUnpaidleaves() != null ? employee.getUnpaidleaves() : existing.getUnpaidleaves());
        existing.setFaceEncoding(employee.getFaceEncoding() != null ? employee.getFaceEncoding() : existing.getFaceEncoding());
        existing.setCreatedByEmail(employee.getCreatedByEmail() != null ? employee.getCreatedByEmail() : existing.getCreatedByEmail());
        existing.setRole(employee.getRole() != null ? employee.getRole() : existing.getRole());
        existing.setBranchCode(employee.getBranchCode() != null ? employee.getBranchCode() : existing.getBranchCode());
        existing.setDepartmentEntity(employee.getDepartmentEntity() != null ? employee.getDepartmentEntity() : existing.getDepartmentEntity());
        existing.setEmployeeCategory(employee.getEmployeeCategory() != null ? employee.getEmployeeCategory() : existing.getEmployeeCategory());

        return repository.save(existing);
    }


    @Override
    public void deleteEmployee(int id, String role, String email) {
        if (!permissionService.hasPermission(role, email, "DELETE")) throw new AccessDeniedException("No permission");
        Employee emp = repository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        emp.setDeleted(true);
        repository.save(emp);
    }

    @Override
    public Employee uploadDocuments(int id, MultipartFile idProof, MultipartFile photo,
                                    MultipartFile resume, MultipartFile addressProof,
                                    MultipartFile experienceLetter, String role, String email) {
        if (!permissionService.hasPermission(role, email, "PUT")) throw new AccessDeniedException("No permission");

        Employee emp = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        try {
            if (idProof != null && !idProof.isEmpty()) {
                emp.setIdProof(idProof.getOriginalFilename());
            }

            if (photo != null && !photo.isEmpty()) {
                // Extract file extension
                String originalFilename = photo.getOriginalFilename();
                String extension = "";
                int dotIndex = originalFilename.lastIndexOf('.');
                if (dotIndex >= 0) {
                    extension = originalFilename.substring(dotIndex);
                }

                String customFileName = "employee_" + emp.getId() + "_" + emp.getBranchCode() + extension;

                emp.setEmployeePhoto(customFileName);
            }

            if (resume != null && !resume.isEmpty()) {
                emp.setResume(resume.getOriginalFilename());
            }

            if (addressProof != null && !addressProof.isEmpty()) {
                emp.setAddressProof(addressProof.getOriginalFilename());
            }

            if (experienceLetter != null && !experienceLetter.isEmpty()) {
                emp.setExperienceLetter(experienceLetter.getOriginalFilename());
            }

        } catch (Exception e) {
            throw new RuntimeException("Error saving documents", e);
        }

        return repository.save(emp);
    }

}