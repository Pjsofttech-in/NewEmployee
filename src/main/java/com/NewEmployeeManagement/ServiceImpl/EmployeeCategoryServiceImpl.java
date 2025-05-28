package com.NewEmployeeManagement.ServiceImpl;

import com.NewEmployeeManagement.Entity.EmployeeCategory;
import com.NewEmployeeManagement.Repository.EmployeeCategoryRepository;
import com.NewEmployeeManagement.Service.EmployeeCategoryService;
import com.NewEmployeeManagement.Service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EmployeeCategoryServiceImpl implements EmployeeCategoryService {

    @Autowired
    private EmployeeCategoryRepository employeeCategoryRepository;

    @Autowired
    private PermissionService permissionService;

    @Override
    public EmployeeCategory createCategory(EmployeeCategory category, String role, String email) {
        if (!permissionService.hasPermission(role, email, "POST")) {
            throw new AccessDeniedException("No permission to create category");
        }
        String branchCode = permissionService.fetchBranchCode(role, email);
        category.setRole(role);
        category.setCreatedByEmail(email);
        category.setBranchCode(branchCode);

        convertToPercentage(category);
        return employeeCategoryRepository.save(category);
    }

    @Override
    public List<EmployeeCategory> getAllCategories(String role, String email) {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view categories");
        }
        String branchCode = permissionService.fetchBranchCode(role, email);
        List<EmployeeCategory> list = employeeCategoryRepository.findAllByBranchCode(branchCode);
        list.forEach(this::convertToIntegerValues);
        return list;
    }

    @Override
    public EmployeeCategory updateCategory(Long id, EmployeeCategory category, String role, String email) {
        if (!permissionService.hasPermission(role, email, "PUT")) {
            throw new AccessDeniedException("No permission to update category");
        }

        EmployeeCategory existing = employeeCategoryRepository.findById(id)
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
        existing.setInsentive(category.getInsentive() !=null? category.getInsentive() : existing.getInsentive());
        existing.setPt(category.getPt() !=null? category.getPt() : existing.getPt());
        existing.setTds(category.getTds() !=null? category.getTds() : existing.getTds());
        existing.setTa(category.getTa() !=null? category.getTa() : existing.getTa());
        existing.setCompanyFund(category.getCompanyFund() !=null? category.getCompanyFund() : existing.getCompanyFund());
        convertToPercentageIfNeeded(existing);
        return employeeCategoryRepository.save(existing);
    }


    @Override
    public void deleteCategory(Long id, String role, String email) {
        if (!permissionService.hasPermission(role, email, "DELETE")) {
            throw new AccessDeniedException("No permission to delete category");
        }

        EmployeeCategory category = employeeCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        category.setDeleted(true);
        employeeCategoryRepository.deleteById(id);
    }

    @Override
    public EmployeeCategory getCategoryById(Long id, String role, String email) {
        if (!permissionService.hasPermission(role, email, "GET")) {
            throw new AccessDeniedException("No permission to view category");
        }

        return employeeCategoryRepository.findById(id)
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
//        category.setMedicalAllowancePercentage(convertToPercent(category.getMedicalAllowancePercentage()));
        category.setPfPercentage(convertToPercent(category.getPfPercentage()));
        category.setEsicPercentage(convertToPercent(category.getEsicPercentage()));
//        category.setProfessionalTaxPercentage(convertToPercent(category.getProfessionalTaxPercentage()));
//        category.setIncomeTaxPercentage(convertToPercent(category.getIncomeTaxPercentage()));
    }

    @Override
    public void convertToPercentageIfNeeded(EmployeeCategory category) {
        category.setHraPercentage(convertIfNeeded(category.getHraPercentage()));
//        category.setMedicalAllowancePercentage(convertIfNeeded(category.getMedicalAllowancePercentage()));
        category.setPfPercentage(convertIfNeeded(category.getPfPercentage()));
        category.setEsicPercentage(convertIfNeeded(category.getEsicPercentage()));
//        category.setProfessionalTaxPercentage(convertIfNeeded(category.getProfessionalTaxPercentage()));
//        category.setIncomeTaxPercentage(convertIfNeeded(category.getIncomeTaxPercentage()));
    }

    @Override
    public void convertToIntegerValues(EmployeeCategory category) {
        category.setHraPercentage(convertToInteger(category.getHraPercentage()));
//        category.setMedicalAllowancePercentage(convertToInteger(category.getMedicalAllowancePercentage()));
        category.setPfPercentage(convertToInteger(category.getPfPercentage()));
        category.setEsicPercentage(convertToInteger(category.getEsicPercentage()));
//        category.setProfessionalTaxPercentage(convertToInteger(category.getProfessionalTaxPercentage()));
//        category.setIncomeTaxPercentage(convertToInteger(category.getIncomeTaxPercentage()));
    }

    @Override
    public Optional<EmployeeCategory> getCategoryByName(String categoryName, String branchCode) {
        return employeeCategoryRepository.findByCategoryNameAndBranchCode(categoryName, branchCode);
    }

    @Override
    public long getTotalCategory(String branchCode) {
        return employeeCategoryRepository.countByBranchCode(branchCode);
    }

    @Override
    public List<EmployeeCategory> getNonDeletedCategories(String branchCode) {
        return employeeCategoryRepository.findByIsDeletedFalseAndBranchCode(branchCode);
    }

}