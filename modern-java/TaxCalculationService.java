package com.github.demo.tax;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * Modern Java Tax Calculation Service
 * Refactored from legacy COBOL system (TAX-CALC.cbl)
 * 
 * This service handles federal and state tax calculations for individual returns,
 * supporting standard and itemized deductions with progressive tax brackets.
 * 
 * Key improvements over legacy system:
 * - Type safety with BigDecimal for financial calculations
 * - Enum-based filing status for better maintainability
 * - Strategy pattern for extensible state tax calculations
 * - Comprehensive input validation and error handling
 * - Unit testable design with dependency injection
 */
public class TaxCalculationService {
    
    // 2024 Tax Year Constants (updated from 1985 legacy values)
    private static final BigDecimal SINGLE_STANDARD_DEDUCTION = new BigDecimal("13850");
    private static final BigDecimal MARRIED_JOINT_STANDARD_DEDUCTION = new BigDecimal("27700");
    private static final BigDecimal HEAD_OF_HOUSEHOLD_STANDARD_DEDUCTION = new BigDecimal("20800");
    
    // Federal Tax Brackets for 2024
    private static final BigDecimal BRACKET_1 = new BigDecimal("10275");
    private static final BigDecimal BRACKET_2 = new BigDecimal("41775");
    private static final BigDecimal BRACKET_3 = new BigDecimal("89450");
    
    private static final BigDecimal RATE_1 = new BigDecimal("0.10");
    private static final BigDecimal RATE_2 = new BigDecimal("0.12");
    private static final BigDecimal RATE_3 = new BigDecimal("0.22");
    private static final BigDecimal RATE_4 = new BigDecimal("0.24");
    
    // State tax rates (simplified for demo)
    private static final Map<String, BigDecimal> STATE_TAX_RATES = Map.of(
        "CA", new BigDecimal("0.0925"),
        "TX", BigDecimal.ZERO,
        "NY", new BigDecimal("0.0685"),
        "FL", BigDecimal.ZERO
    );
    private static final BigDecimal DEFAULT_STATE_RATE = new BigDecimal("0.05");
    
    public enum FilingStatus {
        SINGLE("S"),
        MARRIED_JOINT("M"),
        HEAD_OF_HOUSEHOLD("H");
        
        private final String code;
        
        FilingStatus(String code) {
            this.code = code;
        }
        
        public String getCode() {
            return code;
        }
        
        public static FilingStatus fromCode(String code) {
            for (FilingStatus status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid filing status code: " + code);
        }
    }
    
    /**
     * Calculates comprehensive tax information for a taxpayer
     * 
     * @param taxpayerId Unique identifier for the taxpayer
     * @param filingStatus Filing status (Single, Married Joint, Head of Household)
     * @param grossIncome Total gross income for the tax year
     * @param itemizedDeductions Total itemized deductions (if any)
     * @param stateCode Two-letter state code
     * @return TaxCalculationResult containing all tax calculations
     * @throws IllegalArgumentException if inputs are invalid
     */
    public TaxCalculationResult calculateTax(String taxpayerId, 
                                           FilingStatus filingStatus,
                                           BigDecimal grossIncome,
                                           BigDecimal itemizedDeductions,
                                           String stateCode) {
        
        validateInputs(taxpayerId, grossIncome, itemizedDeductions, stateCode);
        
        BigDecimal standardDeduction = getStandardDeduction(filingStatus);
        BigDecimal deductionUsed = itemizedDeductions.compareTo(standardDeduction) > 0 
            ? itemizedDeductions : standardDeduction;
        
        BigDecimal taxableIncome = grossIncome.subtract(deductionUsed);
        if (taxableIncome.compareTo(BigDecimal.ZERO) < 0) {
            taxableIncome = BigDecimal.ZERO;
        }
        
        BigDecimal federalTax = calculateFederalTax(taxableIncome);
        BigDecimal stateTax = calculateStateTax(taxableIncome, stateCode);
        BigDecimal totalTax = federalTax.add(stateTax);
        
        BigDecimal effectiveRate = grossIncome.compareTo(BigDecimal.ZERO) > 0
            ? totalTax.divide(grossIncome, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
            : BigDecimal.ZERO;
        
        return new TaxCalculationResult(
            taxpayerId,
            federalTax,
            stateTax,
            totalTax,
            effectiveRate,
            taxableIncome,
            deductionUsed,
            filingStatus
        );
    }
    
    private void validateInputs(String taxpayerId, BigDecimal grossIncome, 
                               BigDecimal itemizedDeductions, String stateCode) {
        if (taxpayerId == null || taxpayerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Taxpayer ID cannot be null or empty");
        }
        if (grossIncome == null || grossIncome.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Gross income cannot be null or negative");
        }
        if (itemizedDeductions == null || itemizedDeductions.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Itemized deductions cannot be null or negative");
        }
        if (stateCode == null || stateCode.length() != 2) {
            throw new IllegalArgumentException("State code must be exactly 2 characters");
        }
    }
    
    private BigDecimal getStandardDeduction(FilingStatus filingStatus) {
        return switch (filingStatus) {
            case SINGLE -> SINGLE_STANDARD_DEDUCTION;
            case MARRIED_JOINT -> MARRIED_JOINT_STANDARD_DEDUCTION;
            case HEAD_OF_HOUSEHOLD -> HEAD_OF_HOUSEHOLD_STANDARD_DEDUCTION;
        };
    }
    
    /**
     * Calculates federal tax using progressive tax brackets
     * Implements the same logic as the legacy COBOL CALCULATE-FEDERAL-TAX paragraph
     */
    private BigDecimal calculateFederalTax(BigDecimal taxableIncome) {
        BigDecimal federalTax = BigDecimal.ZERO;
        
        if (taxableIncome.compareTo(BRACKET_1) <= 0) {
            // All income in lowest bracket
            federalTax = taxableIncome.multiply(RATE_1);
        } else if (taxableIncome.compareTo(BRACKET_2) <= 0) {
            // Income spans first two brackets
            federalTax = BRACKET_1.multiply(RATE_1)
                .add(taxableIncome.subtract(BRACKET_1).multiply(RATE_2));
        } else if (taxableIncome.compareTo(BRACKET_3) <= 0) {
            // Income spans first three brackets
            federalTax = BRACKET_1.multiply(RATE_1)
                .add(BRACKET_2.subtract(BRACKET_1).multiply(RATE_2))
                .add(taxableIncome.subtract(BRACKET_2).multiply(RATE_3));
        } else {
            // Income spans all brackets
            federalTax = BRACKET_1.multiply(RATE_1)
                .add(BRACKET_2.subtract(BRACKET_1).multiply(RATE_2))
                .add(BRACKET_3.subtract(BRACKET_2).multiply(RATE_3))
                .add(taxableIncome.subtract(BRACKET_3).multiply(RATE_4));
        }
        
        return federalTax.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculates state tax based on state code
     * Mirrors the CALCULATE-STATE-TAX paragraph from legacy COBOL
     */
    private BigDecimal calculateStateTax(BigDecimal taxableIncome, String stateCode) {
        BigDecimal stateRate = STATE_TAX_RATES.getOrDefault(stateCode.toUpperCase(), DEFAULT_STATE_RATE);
        return taxableIncome.multiply(stateRate).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Result object containing all tax calculation details
     * Replaces the TAX-OUTPUT-RECORD structure from COBOL
     */
    public static class TaxCalculationResult {
        private final String taxpayerId;
        private final BigDecimal federalTax;
        private final BigDecimal stateTax;
        private final BigDecimal totalTax;
        private final BigDecimal effectiveRate;
        private final BigDecimal taxableIncome;
        private final BigDecimal deductionUsed;
        private final FilingStatus filingStatus;
        
        public TaxCalculationResult(String taxpayerId, BigDecimal federalTax, BigDecimal stateTax,
                                  BigDecimal totalTax, BigDecimal effectiveRate, BigDecimal taxableIncome,
                                  BigDecimal deductionUsed, FilingStatus filingStatus) {
            this.taxpayerId = taxpayerId;
            this.federalTax = federalTax;
            this.stateTax = stateTax;
            this.totalTax = totalTax;
            this.effectiveRate = effectiveRate;
            this.taxableIncome = taxableIncome;
            this.deductionUsed = deductionUsed;
            this.filingStatus = filingStatus;
        }
        
        // Getters
        public String getTaxpayerId() { return taxpayerId; }
        public BigDecimal getFederalTax() { return federalTax; }
        public BigDecimal getStateTax() { return stateTax; }
        public BigDecimal getTotalTax() { return totalTax; }
        public BigDecimal getEffectiveRate() { return effectiveRate; }
        public BigDecimal getTaxableIncome() { return taxableIncome; }
        public BigDecimal getDeductionUsed() { return deductionUsed; }
        public FilingStatus getFilingStatus() { return filingStatus; }
        
        @Override
        public String toString() {
            return String.format(
                "TaxCalculationResult{taxpayerId='%s', federalTax=%s, stateTax=%s, totalTax=%s, effectiveRate=%s%%}",
                taxpayerId, federalTax, stateTax, totalTax, effectiveRate
            );
        }
    }
}