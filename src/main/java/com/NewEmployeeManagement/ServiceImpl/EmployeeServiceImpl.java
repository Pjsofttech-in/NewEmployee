package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.DTO.EmployeeCreateDTO;
import com.NewEmployeeManagement.Entity.*;
import com.NewEmployeeManagement.Repository.DepartmentRepository;
import com.NewEmployeeManagement.Repository.EmployeeCategoryRepository;
import com.NewEmployeeManagement.Repository.EmployeeRepository;
import com.NewEmployeeManagement.Service.EmployeeService;
import com.NewEmployeeManagement.Service.PermissionService;
import com.NewEmployeeManagement.Service.S3Service;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

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

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private S3Service s3Service;

    private static final Logger logger = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private String saveFileToStorageOrReturnName(MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            // For real project, save the file to disk or S3, etc.
            return file.getOriginalFilename();
        }
        return null;
    }

    @Override
    public Employee createEmployee(EmployeeCreateDTO dto, String role, String email, int departmentId, Long categoryId,
                                   MultipartFile idProof,
                                   MultipartFile employeePhoto,
                                   MultipartFile resume,
                                   MultipartFile addressProof,
                                   MultipartFile experienceLetter) {

        if (!permissionService.hasPermission(role, email, "POST")) {
            throw new AccessDeniedException("No permission");
        }

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found with ID: " + departmentId));
        EmployeeCategory category = employeeCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + categoryId));

        Employee employee = new Employee();
        // set all basic fields
        BeanUtils.copyProperties(dto, employee);

        // Get and set branch/system
        String branchCode = permissionService.fetchBranchCode(role, email);
        String systemName = "NewEmployee";
        employee.setBranchCode(branchCode);
        employee.setRole(role);
        employee.setCreatedByEmail(email);
        employee.setJoiningDate(LocalDate.now());
        employee.setEmpRole("USER");
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));

        // Set address if present
        if (dto.getAddress() != null) {
            EmployeeAddress empAddress = new EmployeeAddress();
            BeanUtils.copyProperties(dto.getAddress(), empAddress);
            empAddress.setEmployee(employee);
            employee.setEmployeeAddress(empAddress);
        }

        // Upload documents to S3
        EmployeeDocument employeeDocument = new EmployeeDocument();

        try {
            if (idProof != null && !idProof.isEmpty()) {
                String idProofUrl = s3Service.uploadEmployeeDocument(idProof, branchCode, systemName);
                employeeDocument.setIdProof(idProofUrl);
            }
            if (employeePhoto != null && !employeePhoto.isEmpty()) {
                String photoUrl = s3Service.uploadEmployeeDocument(employeePhoto, branchCode, systemName);
                employeeDocument.setEmployeePhoto(photoUrl);
            }
            if (resume != null && !resume.isEmpty()) {
                String resumeUrl = s3Service.uploadEmployeeDocument(resume, branchCode, systemName);
                employeeDocument.setResume(resumeUrl);
            }
            if (addressProof != null && !addressProof.isEmpty()) {
                String addressProofUrl = s3Service.uploadEmployeeDocument(addressProof, branchCode, systemName);
                employeeDocument.setAddressProof(addressProofUrl);
            }
            if (experienceLetter != null && !experienceLetter.isEmpty()) {
                String experienceUrl = s3Service.uploadEmployeeDocument(experienceLetter, branchCode, systemName);
                employeeDocument.setExperienceLetter(experienceUrl);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error uploading employee documents to S3", e);
        }

        // Save document record
        employeeDocument.setEmployee(employee);
        employee.setEmployeeDocument(employeeDocument);
        // Set department and category
        employee.setDepartmentEntity(department);
        employee.setDepartment(department.getDepartment());
        employee.setEmployeeCategory(category);
        employee.setCategoryName(category.getCategoryName());
        employee.setPaidleaves(category.getTotalPaidLeave());
        employee.setUnpaidleaves(category.getTotalUnpaidLeave());
        // Save employee first to get ID
        Employee savedEmployee = repository.save(employee);

        // ✅ Call method to copy employee photo to attendance folder
        try {
            s3Service.copyImageToAttendanceFolderWithEmpId(savedEmployee.getId());
        } catch (Exception ex) {
            logger.error("Failed to copy image to attendance folder for employee ID: {}", savedEmployee.getId(), ex);
        }
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
    public Employee updateEmployee(
            int id,
            EmployeeCreateDTO dto,
            String role,
            String email,
            int departmentId,
            Long categoryId,
            MultipartFile idProof,
            MultipartFile employeePhoto,
            MultipartFile resume,
            MultipartFile addressProof,
            MultipartFile experienceLetter
    ) {
        if (!permissionService.hasPermission(role, email, "PUT")) {
            throw new AccessDeniedException("No permission");
        }

        Employee existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Update main employee fields
        updateIfNotNull(existing::setFullName, dto.getFullName());
        updateIfNotNull(existing::setEmpEmail, dto.getEmpEmail());
        updateIfNotNull(existing::setPassword, dto.getPassword());
        updateIfNotNull(existing::setDob, dto.getDob());
        updateIfNotNull(existing::setMobileNo, dto.getMobileNo());
        updateIfNotNull(existing::setParentNo, dto.getParentNo());
        updateIfNotNull(existing::setGender, dto.getGender());
        updateIfNotNull(existing::setBloodGroup, dto.getBloodGroup());
        updateIfNotNull(existing::setAdharNo, dto.getAdharNo());
        updateIfNotNull(existing::setPanNo, dto.getPanNo());
        updateIfNotNull(existing::setDepartment, dto.getDepartment());
        updateIfNotNull(existing::setWorkLocation, dto.getWorkLocation());
        updateIfNotNull(existing::setDesignation, dto.getDesignation());
        updateIfNotNull(existing::setDutyType, dto.getDutyType());
        updateIfNotNull(existing::setEmployeeType, dto.getEmployeeType());
        updateIfNotNull(existing::setSalary, dto.getSalary());
        updateIfNotNull(existing::setCpfNo, dto.getCpfNo());
        updateIfNotNull(existing::setEsicNo, dto.getEsicNo());
        updateIfNotNull(existing::setBasicQualification, dto.getBasicQualification());
        updateIfNotNull(existing::setProfessionalQualification, dto.getProfessionalQualification());
        updateIfNotNull(existing::setShift, dto.getShift());
        updateIfNotNull(existing::setShiftStartTime, dto.getShiftStartTime());
        updateIfNotNull(existing::setShiftEndTime, dto.getShiftEndTime());
        updateIfNotNull(existing::setCategoryName, dto.getCategoryName());
        updateIfNotNull(existing::setStatus, dto.getStatus());
        if (dto.getOsen() != 0) existing.setOsen(dto.getOsen());
        updateIfNotNull(existing::setToMail, dto.getToMail());
        updateIfNotNull(existing::setSubject, dto.getSubject());
        updateIfNotNull(existing::setBody, dto.getBody());
        updateIfNotNull(existing::setCreateAt, dto.getCreateAt());
        updateIfNotNull(existing::setPaidleaves, dto.getPaidleaves());
        updateIfNotNull(existing::setCarryForwardedLeaves, dto.getCarryForwardedLeaves());
        updateIfNotNull(existing::setUnpaidleaves, dto.getUnpaidleaves());
        updateIfNotNull(existing::setFaceEncoding, dto.getFaceEncoding());
        updateIfNotNull(existing::setCreatedByEmail, dto.getCreatedByEmail());
        updateIfNotNull(existing::setRole, dto.getRole());
        updateIfNotNull(existing::setBranchCode, dto.getBranchCode());
        existing.setDeleted(dto.isDeleted());

        // Update Address
        if (dto.getAddress() != null) {
            EmployeeAddress address = existing.getEmployeeAddress() != null ? existing.getEmployeeAddress() : new EmployeeAddress();
            EmployeeCreateDTO.AddressDTO dtoAddress = dto.getAddress();

            updateIfNotNull(address::setCountry, dtoAddress.getCountry());
            updateIfNotNull(address::setState, dtoAddress.getState());
            updateIfNotNull(address::setDistrict, dtoAddress.getDistrict());
            updateIfNotNull(address::setTaluka, dtoAddress.getTaluka());
            updateIfNotNull(address::setCity, dtoAddress.getCity());
            if (dtoAddress.getPinCode() != 0) address.setPinCode(dtoAddress.getPinCode());
            updateIfNotNull(address::setLandmark, dtoAddress.getLandmark());
            updateIfNotNull(address::setCurrentAddress, dtoAddress.getCurrentAddress());
            updateIfNotNull(address::setPCountry, dtoAddress.getPcountry());
            updateIfNotNull(address::setPState, dtoAddress.getPstate());
            updateIfNotNull(address::setPDistrict, dtoAddress.getPdistrict());
            updateIfNotNull(address::setPTaluka, dtoAddress.getPtaluka());
            updateIfNotNull(address::setPCity, dtoAddress.getPcity());
            if (dtoAddress.getPpinCode() != 0) address.setPPinCode(dtoAddress.getPpinCode());
            updateIfNotNull(address::setPLandmark, dtoAddress.getPlandmark());
            updateIfNotNull(address::setPAddress, dtoAddress.getPaddress());

            address.setEmployee(existing);
            existing.setEmployeeAddress(address);
        }

        // Update Document
        if (dto.getDocument() != null) {
            EmployeeDocument document = existing.getEmployeeDocument() != null ? existing.getEmployeeDocument() : new EmployeeDocument();
            EmployeeCreateDTO.DocumentDTO dtoDoc = dto.getDocument();

            updateIfNotNull(document::setIdProof, dtoDoc.getIdProof());
            updateIfNotNull(document::setEmployeePhoto, dtoDoc.getEmployeePhoto());
            updateIfNotNull(document::setResume, dtoDoc.getResume());
            updateIfNotNull(document::setAddressProof, dtoDoc.getAddressProof());
            updateIfNotNull(document::setExperienceLetter, dtoDoc.getExperienceLetter());

            document.setEmployee(existing);
            existing.setEmployeeDocument(document);
        }

        return repository.save(existing);
    }

    private <T> void updateIfNotNull(Consumer<T> setter, T value) {
        if (value != null) {
            setter.accept(value);
        }
    }


    @Override
    public void deleteEmployee(int id, String role, String email) {
        if (!permissionService.hasPermission(role, email, "DELETE")) throw new AccessDeniedException("No permission");
        Employee emp = repository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        emp.setDeleted(true);
        repository.save(emp);
    }

    @Override
    public void carryForwardLeavesForEligibleEmployees() {
        List<Employee> employees = repository.findByIsDeletedFalse();

        for (Employee employee : employees) {
            if (employee.getJoiningDate() == null || employee.getEmployeeCategory() == null) {
                continue;
            }

            LocalDate joiningDate = employee.getJoiningDate();
            long years = ChronoUnit.YEARS.between(joiningDate, LocalDate.now());

            if (years >= 1) {
                EmployeeCategory category = employee.getEmployeeCategory();
                Double totalLeave = category.getTotalPaidLeave(); // Assuming totalLeave is in EmployeeCategory

                if (totalLeave != null) {
                    Double remaining = employee.getPaidleaves() != null ? employee.getPaidleaves() : 0.0;
                    employee.setCarryForwardedLeaves(remaining);
                    employee.setPaidleaves(totalLeave+remaining);
                    repository.save(employee);
                }
            }
        }
    }


    @Override
    public Map<String, Object> getCrudPermissionForEmployeeByEmail(String empEmail) {
        Optional<Employee> employeeOpt = repository.findByEmpEmail(empEmail);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            Map<String, Object> permissions = new HashMap<>();
            permissions.put("candGet", employee.isCandGet());
            permissions.put("candPost", employee.isCandPost());
            permissions.put("candPut", employee.isCandPut());
            permissions.put("candDelete", employee.isCandDelete());
            return permissions;
        }
        throw new EntityNotFoundException("Employee not found with email: " + empEmail);
    }
}