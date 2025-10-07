package com.NewEmployeeManagement.DTO;

import java.math.BigDecimal;

public class MonthSalaryResponse
{
    private BigDecimal paidAmount;
    private BigDecimal pendingAmount;
    private long paidCount;
    private long pendingCount;

    public MonthSalaryResponse(BigDecimal paidAmount, BigDecimal pendingAmount, long paidCount, long pendingCount) {
        this.paidAmount = paidAmount;
        this.pendingAmount = pendingAmount;
        this.paidCount = paidCount;
        this.pendingCount = pendingCount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public BigDecimal getPendingAmount() {
        return pendingAmount;
    }

    public long getPaidCount() {
        return paidCount;
    }

    public long getPendingCount() {
        return pendingCount;
    }
}
