package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.DTO.EmployeeCreateDTO;
import com.NewEmployeeManagement.Entity.*;
import com.NewEmployeeManagement.Repository.DepartmentRepository;
import com.NewEmployeeManagement.Repository.EmployeeCategoryRepository;
import com.NewEmployeeManagement.Repository.EmployeeRepository;
import com.NewEmployeeManagement.Service.EmployeeService;
import com.NewEmployeeManagement.Service.PermissionService;
import com.NewEmployeeManagement.Service.S3Service;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
    private S3Service s3Service;


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
        employee.setFullName(dto.getFullName());
        employee.setEmpEmail(dto.getEmpEmail());
        employee.setPassword(dto.getPassword());
        employee.setDob(dto.getDob());
        employee.setMobileNo(dto.getMobileNo());
        employee.setParentNo(dto.getParentNo());
        employee.setGender(dto.getGender());
        employee.setBloodGroup(dto.getBloodGroup());
        employee.setAdharNo(dto.getAdharNo());
        employee.setPanNo(dto.getPanNo());
        employee.setJoiningDate(dto.getJoiningDate());
        employee.setDepartment(dto.getDepartment());
        employee.setWorkLocation(dto.getWorkLocation());
        employee.setDesignation(dto.getDesignation());
        employee.setDutyType(dto.getDutyType());
        employee.setEmployeeType(dto.getEmployeeType());
        employee.setSalary(dto.getSalary());
        employee.setCpfNo(dto.getCpfNo());
        employee.setEsicNo(dto.getEsicNo());
        employee.setBasicQualification(dto.getBasicQualification());
        employee.setProfessionalQualification(dto.getProfessionalQualification());
        employee.setShift(dto.getShift());
        employee.setShiftStartTime(dto.getShiftStartTime());
        employee.setShiftEndTime(dto.getShiftEndTime());
        employee.setCategoryName(dto.getCategoryName());
        employee.setStatus(dto.getStatus());
        employee.setOsen(dto.getOsen());
        employee.setToMail(dto.getToMail());
        employee.setSubject(dto.getSubject());
        employee.setBody(dto.getBody());
        employee.setCreateAt(dto.getCreateAt());
        employee.setPaidleaves(dto.getPaidleaves());
        employee.setCarryForwardedLeaves(dto.getCarryForwardedLeaves());
        employee.setUnpaidleaves(dto.getUnpaidleaves());
        employee.setDeleted(dto.isDeleted());
        employee.setFaceEncoding(dto.getFaceEncoding());
        employee.setCreatedByEmail(dto.getCreatedByEmail());
        employee.setRole(dto.getRole());

        // Fetch branch code and define system name for document upload paths
        String branchCode = permissionService.fetchBranchCode(role, email);
        String systemName = "NewEmployee";

        employee.setBranchCode(branchCode);

        // Set address if available
        if (dto.getAddress() != null) {
            EmployeeAddress empAddress = new EmployeeAddress();
            BeanUtils.copyProperties(dto.getAddress(), empAddress);
            empAddress.setEmployee(employee);
            employee.setEmployeeAddress(empAddress);
        }

        EmployeeCreateDTO.DocumentDTO documentDTO = new EmployeeCreateDTO.DocumentDTO();
        try {
            if (idProof != null && !idProof.isEmpty()) {
                documentDTO.setIdProof(s3Service.uploadEmployeeDocument(idProof, branchCode, systemName));
            }
            if (employeePhoto != null && !employeePhoto.isEmpty()) {
                documentDTO.setEmployeePhoto(s3Service.uploadEmployeeDocument(employeePhoto, branchCode, systemName));
            }
            if (resume != null && !resume.isEmpty()) {
                documentDTO.setResume(s3Service.uploadEmployeeDocument(resume, branchCode, systemName));
            }
            if (addressProof != null && !addressProof.isEmpty()) {
                documentDTO.setAddressProof(s3Service.uploadEmployeeDocument(addressProof, branchCode, systemName));
            }
            if (experienceLetter != null && !experienceLetter.isEmpty()) {
                documentDTO.setExperienceLetter(s3Service.uploadEmployeeDocument(experienceLetter, branchCode, systemName));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error uploading employee documents to S3", e);
        }

        // Set uploaded document URLs in DTO
        dto.setDocument(documentDTO);

        // Set department and category
        employee.setDepartmentEntity(department);
        employee.setEmployeeCategory(category);

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
        updateIfNotNull(existing::setJoiningDate, dto.getJoiningDate());
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

            LocalDate joiningDate = new java.sql.Date(employee.getJoiningDate().getTime()).toLocalDate();
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

}