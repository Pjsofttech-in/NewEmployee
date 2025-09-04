package com.NewEmployeeManagement.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EmployeeLeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long empId;

    @NotNull(message = "From date cannot be null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fromDate;

    private LocalDate toDate;
    private Double totalleavecount;

    private String categoryName;
    private String status;
    private String fullName;
    private String reasondescription;
    private Double paidleave;
    private Double unpaidleave;
    @NotNull(message = "Leave Required cannot be null")
    private Double leaveRequired;
    private Double appliedPaidLeaves;
    private LocalDate leaveRequestDate;
    private String LeaveType;
    private boolean isDeleted = false;
    private String duration;


    @Email
    private String createdByEmail;
    private String role;
    private String branchCode;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    @JsonIgnore
    private Employee employee;


    public void calculateToDateAndLeaveRequestDate() {
        if (this.fromDate != null && this.leaveRequired != null) {
            if (leaveRequired == 0.5) {
                // Half-day leave ends on the same day
                this.toDate = fromDate;
            } else {
                // For full-day or multi-day leave
                this.toDate = fromDate.plusDays((long) (leaveRequired - 1));
            }
        }

        // Automatically set today's date
        this.leaveRequestDate = LocalDate.now();

        // Calculate total leave count from paid + unpaid
        double paid = this.paidleave != null ? this.paidleave : 0.0;
        double unpaid = this.unpaidleave != null ? this.unpaidleave : 0.0;
        this.totalleavecount = paid + unpaid;
    }


}