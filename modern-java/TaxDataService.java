package com.github.demo.tax;

import java.sql.*;
import java.util.logging.Logger;
import java.math.BigDecimal;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Tax Data Service - DEMO VERSION WITH INTENTIONAL SECURITY ISSUES
 * 
 * WARNING: This class contains intentional security vulnerabilities for 
 * demonstration purposes. DO NOT use in production!
 * 
 * This demonstrates how GitHub Advanced Security can identify:
 * - SQL Injection vulnerabilities
 * - Hard-coded credentials
 * - Weak cryptography
 * - Information disclosure through logs
 */
public class TaxDataService {
    
    private static final Logger logger = Logger.getLogger(TaxDataService.class.getName());
    
    // SECURITY ISSUE: Hard-coded database credentials
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/taxdb";
    private static final String DB_USER = "admin";
    private static final String DB_PASSWORD = "password123"; // Hard-coded password!
    
    // SECURITY ISSUE: Hard-coded encryption key
    private static final String ENCRYPTION_KEY = "MySecretKey12345"; // Weak encryption key!
    
    private Connection connection;
    
    public TaxDataService() throws SQLException {
        // SECURITY ISSUE: Database connection without proper error handling
        this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    
    /**
     * Retrieves taxpayer information by ID
     * SECURITY ISSUE: SQL Injection vulnerability
     */
    public TaxpayerInfo getTaxpayerById(String taxpayerId) throws SQLException {
        // VULNERABLE: Direct string concatenation allows SQL injection
        String query = "SELECT * FROM taxpayers WHERE taxpayer_id = '" + taxpayerId + "'";
        
        // SECURITY ISSUE: Logging sensitive query with potential PII
        logger.info("Executing query: " + query);
        
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        
        if (rs.next()) {
            return new TaxpayerInfo(
                rs.getString("taxpayer_id"),
                rs.getString("ssn"), // SECURITY ISSUE: Handling SSN without encryption
                rs.getString("name"),
                rs.getBigDecimal("income"),
                rs.getString("state")
            );
        }
        return null;
    }
    
    /**
     * Updates taxpayer income
     * SECURITY ISSUE: Another SQL injection point
     */
    public void updateTaxpayerIncome(String taxpayerId, BigDecimal newIncome) throws SQLException {
        // VULNERABLE: String concatenation in UPDATE statement
        String updateQuery = "UPDATE taxpayers SET income = " + newIncome + 
                           " WHERE taxpayer_id = '" + taxpayerId + "'";
        
        // SECURITY ISSUE: Logging sensitive financial data
        logger.info("Updating income for taxpayer " + taxpayerId + " to $" + newIncome);
        
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(updateQuery);
    }
    
    /**
     * Encrypts sensitive data using weak encryption
     * SECURITY ISSUE: Weak cryptographic practices
     */
    public String encryptSensitiveData(String data) {
        try {
            // SECURITY ISSUE: Using DES (weak encryption algorithm)
            Cipher cipher = Cipher.getInstance("DES");
            SecretKeySpec keySpec = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), "DES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            
            byte[] encrypted = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
            
        } catch (Exception e) {
            // SECURITY ISSUE: Exposing sensitive information in exception logs
            logger.severe("Failed to encrypt data: " + data + " - Error: " + e.getMessage());
            return data; // SECURITY ISSUE: Returning unencrypted data on failure
        }
    }
    
    /**
     * Processes tax calculation with logging
     * SECURITY ISSUE: Information disclosure through logs
     */
    public void processTaxCalculation(TaxpayerInfo taxpayer, BigDecimal calculatedTax) {
        // SECURITY ISSUE: Logging PII and financial information
        logger.info(String.format(
            "Processing tax for SSN: %s, Name: %s, Income: $%s, Tax: $%s",
            taxpayer.getSsn(), 
            taxpayer.getName(), 
            taxpayer.getIncome(), 
            calculatedTax
        ));
        
        // Simulate processing...
        System.out.println("Tax calculation processed for " + taxpayer.getName());
    }
    
    /**
     * Administrative function with privilege escalation risk
     * SECURITY ISSUE: No access control validation
     */
    public void deleteAllTaxRecords(String adminToken) throws SQLException {
        // SECURITY ISSUE: Weak token validation
        if ("admin123".equals(adminToken)) {
            String deleteQuery = "DELETE FROM taxpayers"; // DANGEROUS!
            
            logger.warning("ADMIN ACTION: Deleting all tax records with token: " + adminToken);
            
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(deleteQuery);
        }
    }
    
    /**
     * Data class for taxpayer information
     */
    public static class TaxpayerInfo {
        private String taxpayerId;
        private String ssn;
        private String name;
        private BigDecimal income;
        private String state;
        
        public TaxpayerInfo(String taxpayerId, String ssn, String name, BigDecimal income, String state) {
            this.taxpayerId = taxpayerId;
            this.ssn = ssn;
            this.name = name;
            this.income = income;
            this.state = state;
        }
        
        // Getters
        public String getTaxpayerId() { return taxpayerId; }
        public String getSsn() { return ssn; }
        public String getName() { return name; }
        public BigDecimal getIncome() { return income; }
        public String getState() { return state; }
        
        @Override
        public String toString() {
            // SECURITY ISSUE: toString exposes sensitive data
            return String.format("TaxpayerInfo{id='%s', ssn='%s', name='%s', income=%s, state='%s'}", 
                               taxpayerId, ssn, name, income, state);
        }
    }
}