package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.DTO.AddressDTO;
import com.NewEmployeeManagement.DTO.EmployeeCreateDTO;
import com.NewEmployeeManagement.DTO.EmployeeFilterDTO;
import com.NewEmployeeManagement.DTO.EmployeeResponseDTO;
import com.NewEmployeeManagement.Entity.*;
import com.NewEmployeeManagement.Mapper.EmployeeMapper;
import com.NewEmployeeManagement.Pageination.EmployeeSpecification;
import com.NewEmployeeManagement.Repository.DepartmentRepository;
import com.NewEmployeeManagement.Repository.EmployeeCategoryRepository;
import com.NewEmployeeManagement.Repository.EmployeeRepository;
import com.NewEmployeeManagement.Service.EmployeeService;
import com.NewEmployeeManagement.Service.PermissionService;
import com.NewEmployeeManagement.Service.S3Service;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private RestTemplate restTemplate;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private S3Service s3Service;

    @Autowired
     EmployeeMapper employeeMapper;




    private String saveFileToStorageOrReturnName(MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            // For real project, save the file to disk or S3, etc.
            return file.getOriginalFilename();
        }
        return null;
    }

    @Override
    @Transactional(rollbackOn = Exception.class) // ensures atomic save or rollback
    public Employee createEmployee(EmployeeCreateDTO dto, String role, String email,
                                   Long departmentId, Long categoryId,
                                   MultipartFile idProof,
                                   MultipartFile employeePhoto,
                                   MultipartFile resume,
                                   MultipartFile addressProof,
                                   MultipartFile experienceLetter) {

        if (!permissionService.hasPermission(role, email, "POST")) {
            throw new AccessDeniedException("No permission");
        }

        Optional<Employee> existingEmp = repository.findByEmpEmail(dto.getEmpEmail());
        if (existingEmp.isPresent()) {
            throw new IllegalArgumentException("Employee already exists with email: " + dto.getEmpEmail());
        }

        Employee employee = new Employee();
        BeanUtils.copyProperties(dto, employee);

        EmployeeDepartment employeeDepartment = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found with ID: " + departmentId));

        EmployeeCategory category = employeeCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + categoryId));

        String branchCode = permissionService.fetchBranchCode(role, email);
        String systemName = "employee-sys";

        employee.setEmpRole("USER");
        employee.setSystemName("employee-sys");
        employee.setJoiningDate(LocalDate.now());
        employee.setBranchCode(branchCode);
        employee.setRole(role);
        employee.setCreatedByEmail(email);
        employee.setCreateAt(LocalDateTime.now());
        employee.setEmployeeDepartment(employeeDepartment);
        employee.setDepartment(employeeDepartment.getDepartment());
        employee.setEmployeeCategory(category);
        employee.setCategoryName(category.getCategoryName());
        employee.setPaidleaves(category.getTotalPaidLeave());
        employee.setUnpaidleaves(category.getTotalUnpaidLeave());
        employee.setDeleted(false);

        if (dto.getPassword() != null) {
            employee.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getAddress() != null) {
            EmployeeAddress address = new EmployeeAddress();
            BeanUtils.copyProperties(dto.getAddress(), address);
            address.setEmployee(employee);
            employee.setEmployeeAddress(address);
        }

        Employee savedEmployee = repository.save(employee);

        try {
            EmployeeDocument employeeDocument = new EmployeeDocument();

            if (idProof != null && !idProof.isEmpty()) {
                String url = s3Service.uploadEmployeeDocument(idProof, branchCode, systemName);
                employeeDocument.setIdProof(url);
            }

            if (resume != null && !resume.isEmpty()) {
                String url = s3Service.uploadEmployeeDocument(resume, branchCode, systemName);
                employeeDocument.setResume(url);
            }

            if (addressProof != null && !addressProof.isEmpty()) {
                String url = s3Service.uploadEmployeeDocument(addressProof, branchCode, systemName);
                employeeDocument.setAddressProof(url);
            }

            if (experienceLetter != null && !experienceLetter.isEmpty()) {
                String url = s3Service.uploadEmployeeDocument(experienceLetter, branchCode, systemName);
                employeeDocument.setExperienceLetter(url);
            }

            if (employeePhoto != null && !employeePhoto.isEmpty()) {
                String faceImageUrl = s3Service.uploadEmployeeFaceImage(employeePhoto, branchCode, savedEmployee.getId());
                employeeDocument.setEmployeePhoto(faceImageUrl);
            }

            employeeDocument.setEmployee(savedEmployee);
            savedEmployee.setEmployeeDocument(employeeDocument);

            savedEmployee = repository.save(savedEmployee);
            refreshCacheUpload("employee-sys", branchCode, savedEmployee.getId());

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload documents", e); // rollback will be triggered
        }

        return savedEmployee;

    }


    @Override
    public Page<Employee> getFilteredEmployees(String role, String email, EmployeeFilterDTO filter, String timeFrame,
                                               LocalDate startDate, LocalDate endDate,Pageable pageable)
    {
        if (!permissionService.hasPermission(role, email, "POST")) {
            throw new AccessDeniedException("No permission");
        }
        String branchCode = permissionService.fetchBranchCode(role, email);
        Specification<Employee> spec = EmployeeSpecification.build(filter, branchCode, timeFrame, startDate, endDate);
        return repository.findAll(spec, pageable);
    }


    @Override
    public EmployeeResponseDTO getEmployeeById(Long id, String role, String email) {
        if (!permissionService.hasPermission(role, email, "GET"))
            throw new AccessDeniedException("No permission");

        Employee employee = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Employee not found or has been deleted"));

        return employeeMapper.mapToDto(employee); // 👈 Single method call
    }


    @Override
    public Employee updateEmployee(
            Long id, EmployeeCreateDTO dto, String role, String email, Long departmentId, Long categoryId,
            MultipartFile idProof, MultipartFile employeePhoto, MultipartFile resume, MultipartFile addressProof, MultipartFile experienceLetter) {
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
            AddressDTO dtoAddress = dto.getAddress();

            updateIfNotNull(address::setCountry, dtoAddress.getCountry());
            updateIfNotNull(address::setState, dtoAddress.getState());
            updateIfNotNull(address::setDistrict, dtoAddress.getDistrict());
            updateIfNotNull(address::setTaluka, dtoAddress.getTaluka());
            updateIfNotNull(address::setCity, dtoAddress.getCity());
            if (dtoAddress.getPinCode() != 0) address.setPinCode(dtoAddress.getPinCode());
            updateIfNotNull(address::setLandmark, dtoAddress.getLandmark());
            updateIfNotNull(address::setCurrentAddress, dtoAddress.getCurrentAddress());
            updateIfNotNull(address::setPcountry, dtoAddress.getPcountry());
            updateIfNotNull(address::setPstate, dtoAddress.getPstate());
            updateIfNotNull(address::setPdistrict, dtoAddress.getPdistrict());
            updateIfNotNull(address::setPtaluka, dtoAddress.getPtaluka());
            updateIfNotNull(address::setPcity, dtoAddress.getPcity());
            if (dtoAddress.getPpinCode() != 0) address.setPpinCode(dtoAddress.getPpinCode());
            updateIfNotNull(address::setPlandmark, dtoAddress.getPlandmark());
            updateIfNotNull(address::setPaddress, dtoAddress.getPaddress());

            address.setEmployee(existing);
            existing.setEmployeeAddress(address);
        }

        try {
//            if (dto.getDocument() != null ) {
                EmployeeDocument document = existing.getEmployeeDocument() != null
                        ? existing.getEmployeeDocument()
                        : new EmployeeDocument();

                String branchCode = existing.getBranchCode();
                String systemName = String.valueOf(existing.getId());

                if (idProof != null && !idProof.isEmpty()) {
                    s3Service.deleteFile(idProof.getName());
                    String idProofUrl = s3Service.uploadEmployeeDocument(idProof, branchCode, systemName);
                    document.setIdProof(idProofUrl);
                }

                if (employeePhoto != null && !employeePhoto.isEmpty()) {
                    s3Service.deleteFile(employeePhoto.getName());
                    String photoUrl = s3Service.uploadEmployeeFaceImage(employeePhoto, branchCode, existing.getId());
                    document.setEmployeePhoto(photoUrl);
                    existing.setFaceEncoding(photoUrl); // optional
                }

                if (resume != null && !resume.isEmpty()) {
                    s3Service.deleteFile(resume.getName());
                    String resumeUrl = s3Service.uploadEmployeeDocument(resume, branchCode, systemName);
                    document.setResume(resumeUrl);
                }

                if (addressProof != null && !addressProof.isEmpty()) {
                    s3Service.deleteFile(addressProof.getName());
                    String addressProofUrl = s3Service.uploadEmployeeDocument(addressProof, branchCode, systemName);
                    document.setAddressProof(addressProofUrl);
                }

                if (experienceLetter != null && !experienceLetter.isEmpty()) {
                    s3Service.deleteFile(experienceLetter.getName());
                    String experienceLetterUrl = s3Service.uploadEmployeeDocument(experienceLetter, branchCode, systemName);
                    document.setExperienceLetter(experienceLetterUrl);
                }

                document.setEmployee(existing);
                existing.setEmployeeDocument(document);
                refreshCacheUpload("employee-sys", branchCode, existing.getId());

//            }
        } catch (IOException e) {
            throw new RuntimeException("File upload failed: " + e.getMessage(), e);
        }
     return repository.save(existing);
    }

    private <T> void updateIfNotNull(Consumer<T> setter, T value) {
        if (value != null) {
            setter.accept(value);
        }
    }


    @Override
    public void deleteEmployee(Long id, String role, String email) {
        if (!permissionService.hasPermission(role, email, "DELETE")) throw new AccessDeniedException("No permission");
        Employee emp = repository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        String branchCode = permissionService.fetchBranchCode(role, email);
        emp.setDeleted(true);
        refreshCacheUpload("employee-sys", branchCode, emp.getId());
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


//    @Override
//    public Map<String, Object> getCrudPermissionForEmployeeByEmail(String empEmail) {
//        Optional<Employee> employeeOpt = repository.findByEmpEmail(empEmail);
//        if (employeeOpt.isPresent()) {
//            Employee employee = employeeOpt.get();
//            Map<String, Object> permissions = new HashMap<>();
//            permissions.put("candGet", employee.isCandGet());
//            permissions.put("candPost", employee.isCandPost());
//            permissions.put("candPut", employee.isCandPut());
//            permissions.put("candDelete", employee.isCandDelete());
//            return permissions;
//        }
//        throw new EntityNotFoundException("Employee not found with email: " + empEmail);
//    }

    @Override
    public Employee updateStatus(Long id, String status){
        Employee employee=repository.findById(id).get();
        employee.setStatus(status);
        return repository.save(employee);
    }

    @Override
    public String getBranchCodeByEmail(String email) {
        Employee employee = repository.findByEmpEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found with email: " + email));

        if (employee.isDeleted()) {
            throw new RuntimeException("Employee is deleted with email: " + email);
        }
        return employee.getBranchCode();
    }

    private void refreshCacheUpload(String systemName, String branchCode, Long empId) {
        try {
            String url = "https://pjsofttech.in:51443/refresh-cache-upload";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            Map<String, Object> body = new HashMap<>();
            body.put("system_name", systemName);
            body.put("branch_code", branchCode);
            body.put("empid", String.valueOf(empId));
            body.put("token", "python-java-token-123");

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            System.out.println("✅ Python API response: " + response.getBody());
        } catch (Exception e) {
            System.out.println("❌ Error calling Python API: " + e.getMessage());
            e.printStackTrace();
        }
    }


}