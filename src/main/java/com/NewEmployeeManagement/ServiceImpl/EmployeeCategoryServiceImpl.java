package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.Entity.EmployeeCategory;
import com.NewEmployeeManagement.Repository.EmployeeCategoryRepository;
import com.NewEmployeeManagement.Service.EmployeeCategoryService;
import com.NewEmployeeManagement.Service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EmployeeCategoryServiceImpl implements EmployeeCategoryService {

    private final EmployeeCategoryRepository repository;
    private final WebClient webClient;
    private final StaffService staffService;

    @Autowired
    private PermissionService permissionService;


    @Value("${client.superadmin.base-url}")
    private String superAdminBaseUrl;

    @Autowired
    public EmployeeCategoryServiceImpl(EmployeeCategoryRepository repository,
                                       WebClient webClient,
                                       StaffService staffService) {
        this.repository = repository;
        this.webClient = webClient;
        this.staffService = staffService;
    }

    private boolean hasPermission(String role, String email, String action) {
        if ("BRANCH".equalsIgnoreCase(role)) {
            try {
                Boolean exists = webClient.get()
                        .uri(uriBuilder -> uriBuilder.path("/existBranchbyemail").queryParam("email", email).build())
                        .retrieve().bodyToMono(Boolean.class).block();
                return Boolean.TRUE.equals(exists);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        return switch (role.toUpperCase()) {
            case "STAFF" -> {
                Map<String, Boolean> perms = staffService.getPermissionsByEmail(email);
                yield switch (action.toUpperCase()) {
                    case "GET" -> Boolean.TRUE.equals(perms.get("cansGet"));
                    case "POST" -> Boolean.TRUE.equals(perms.get("cansPost"));
                    case "PUT" -> Boolean.TRUE.equals(perms.get("cansPut"));
                    case "DELETE" -> Boolean.TRUE.equals(perms.get("cansDelete"));
                    default -> false;
                };
            }
            case "DEPARTMENT" -> {
                Map<String, Object> perms = staffService.getCrudPermissionForDepartmentByEmail(email);
                yield switch (action.toUpperCase()) {
                    case "GET" -> Boolean.TRUE.equals(perms.get("candGet"));
                    case "POST" -> Boolean.TRUE.equals(perms.get("candPost"));
                    case "PUT" -> Boolean.TRUE.equals(perms.get("candPut"));
                    case "DELETE" -> Boolean.TRUE.equals(perms.get("candDelete"));
                    default -> false;
                };
            }
            default -> false;
        };
    }

    private String fetchBranchCodeByRole(String role, String email) {
        String endpoint = switch (role.toLowerCase()) {
            case "branch" -> "/branch/getbranchcode";
            case "department" -> "/department/getbranchcode";
            case "staff" -> "/staff/getbranchcode";
            default -> throw new IllegalArgumentException("Invalid role: " + role);
        };

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path(endpoint).queryParam("email", email).build())
                .retrieve().bodyToMono(String.class).block();
    }

    @Override
    public EmployeeCategory createCategory(EmployeeCategory category, String role, String email) {
        if (!hasPermission(role, email, "POST")) {
            throw new AccessDeniedException("No permission to create category");
        }
        String branchCode = fetchBranchCodeByRole(role, email);
        category.setRole(role);
        category.setCreatedByEmail(email);
        category.setBranchCode(branchCode);

        convertToPercentage(category);
        return repository.save(category);
    }

    @Override
    public List<EmployeeCategory> getAllCategories(String role, String email, String branchCode) {
        if (!hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view categories");
        }

        List<EmployeeCategory> list = repository.findAllByBranchCode(branchCode);
        list.forEach(this::convertToIntegerValues);
        return list;
    }

    @Override
    public EmployeeCategory updateCategory(Long id, EmployeeCategory category, String role, String email) {
        if (!hasPermission(role, email, "PUT")) {
            throw new AccessDeniedException("No permission to update category");
        }

        EmployeeCategory existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        existing.setCategoryName(category.getCategoryName() != null ? category.getCategoryName() : existing.getCategoryName());
        existing.setHraPercentage(category.getHraPercentage() != null ? category.getHraPercentage() : existing.getHraPercentage());
        existing.setMedicalAllowancePercentage(category.getMedicalAllowancePercentage() != null ? category.getMedicalAllowancePercentage() : existing.getMedicalAllowancePercentage());
        existing.setPfPercentage(category.getPfPercentage() != null ? category.getPfPercentage() : existing.getPfPercentage());
        existing.setEsicPercentage(category.getEsicPercentage() != null ? category.getEsicPercentage() : existing.getEsicPercentage());
        existing.setProfessionalTaxPercentage(category.getProfessionalTaxPercentage() != null ? category.getProfessionalTaxPercentage() : existing.getProfessionalTaxPercentage());
        existing.setIncomeTaxPercentage(category.getIncomeTaxPercentage() != null ? category.getIncomeTaxPercentage() : existing.getIncomeTaxPercentage());
        existing.setTotalPaidLeave(category.getTotalPaidLeave() != null ? category.getTotalPaidLeave() : existing.getTotalPaidLeave());
        existing.setTotalUnpaidLeave(category.getTotalUnpaidLeave() != null ? category.getTotalUnpaidLeave() : existing.getTotalUnpaidLeave());

        convertToPercentageIfNeeded(existing);
        return repository.save(existing);
    }


    @Override
    public void deleteCategory(Long id, String role, String email) {
        if (!hasPermission(role, email, "DELETE")) {
            throw new AccessDeniedException("No permission to delete category");
        }

        EmployeeCategory category = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        category.setDeleted(true);
        repository.deleteById(id);
    }

    @Override
    public EmployeeCategory getCategoryById(Long id, String role, String email) {
        if (!hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view category");
        }

        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    @Override
    public BigDecimal convertToPercent(BigDecimal value) {
        return value != null ? value.divide(BigDecimal.valueOf(100)) : null;
    }

    @Override
    public BigDecimal convertIfNeeded(BigDecimal value) {
        if (value != null && value.stripTrailingZeros().scale() <= 0) {
            return value.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return value;
    }

    @Override
    public BigDecimal convertToInteger(BigDecimal value) {
        return value != null ? value.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP) : null;
    }

    @Override
    public void convertToPercentage(EmployeeCategory category) {
        category.setHraPercentage(convertToPercent(category.getHraPercentage()));
        category.setMedicalAllowancePercentage(convertToPercent(category.getMedicalAllowancePercentage()));
        category.setPfPercentage(convertToPercent(category.getPfPercentage()));
        category.setEsicPercentage(convertToPercent(category.getEsicPercentage()));
        category.setProfessionalTaxPercentage(convertToPercent(category.getProfessionalTaxPercentage()));
        category.setIncomeTaxPercentage(convertToPercent(category.getIncomeTaxPercentage()));
    }

    @Override
    public void convertToPercentageIfNeeded(EmployeeCategory category) {
        category.setHraPercentage(convertIfNeeded(category.getHraPercentage()));
        category.setMedicalAllowancePercentage(convertIfNeeded(category.getMedicalAllowancePercentage()));
        category.setPfPercentage(convertIfNeeded(category.getPfPercentage()));
        category.setEsicPercentage(convertIfNeeded(category.getEsicPercentage()));
        category.setProfessionalTaxPercentage(convertIfNeeded(category.getProfessionalTaxPercentage()));
        category.setIncomeTaxPercentage(convertIfNeeded(category.getIncomeTaxPercentage()));
    }

    @Override
    public void convertToIntegerValues(EmployeeCategory category) {
        category.setHraPercentage(convertToInteger(category.getHraPercentage()));
        category.setMedicalAllowancePercentage(convertToInteger(category.getMedicalAllowancePercentage()));
        category.setPfPercentage(convertToInteger(category.getPfPercentage()));
        category.setEsicPercentage(convertToInteger(category.getEsicPercentage()));
        category.setProfessionalTaxPercentage(convertToInteger(category.getProfessionalTaxPercentage()));
        category.setIncomeTaxPercentage(convertToInteger(category.getIncomeTaxPercentage()));
    }

    @Override
    public Optional<EmployeeCategory> getCategoryByName(String categoryName, String branchCode) {
        return repository.findByCategoryNameAndBranchCode(categoryName, branchCode);
    }

    @Override
    public long getTotalCategory(String branchCode) {
        return repository.countByBranchCode(branchCode);
    }

    @Override
    public List<EmployeeCategory> getNonDeletedCategories(String branchCode) {
        return repository.findByIsDeletedFalseAndBranchCode(branchCode);
    }

//    @Override
//    public void softDeleteCategory(Long id, String role, String email) {
//        if (!permissionService.hasPermission("employee_category", "delete", role, email)) {
//            throw new RuntimeException("Access Denied: You do not have permission to delete EmployeeCategory.");
//        }
//
//        EmployeeCategory category = repository.findById(id)
//                .orElseThrow(() -> new RuntimeException("EmployeeCategory not found with id: " + id));
//
//        // Map to archive entity
//        EmployeeCategoryArchive archive = new EmployeeCategoryArchive();
//        archive.setId(category.getId());
//        archive.setCategoryName(category.getCategoryName());
//        archive.setDeleted(true);
//
//        // Save to archive table
//        employeeCategoryArchiveRepository.save(archive);
//
//        // Delete from main table
//        repository.deleteById(id);
//    }

}