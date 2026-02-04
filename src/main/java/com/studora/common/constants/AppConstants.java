package com.studora.common.constants;

/**
 * Centralized application constants for validation and configuration.
 * 
 * All magic numbers and repeated values should be defined here
 * to maintain consistency and enable easy configuration changes.
 */
public final class AppConstants {
    
    // Prevent instantiation
    private AppConstants() {
        throw new AssertionError("Cannot instantiate constants class");
    }
    
    // ========================================
    // Pagination Defaults
    // ========================================
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final String DEFAULT_PAGE_SIZE_STR = "20";
    public static final int DEFAULT_PAGE_NUMBER = 0;
    public static final String DEFAULT_PAGE_NUMBER_STR = "0";
    
    // ========================================
    // General Validation
    // ========================================
    public static final int MAX_NAME_LENGTH = 255;
    
    // ========================================
    // Question Validation
    // ========================================
    
    /**
     * Minimum number of alternatives required per question.
     * Standard multiple-choice requires at least 2 options.
     */
    public static final int MIN_ALTERNATIVAS = 2;
    
    /**
     * Required number of correct alternatives for non-annulled questions.
     * Standard exams have exactly one correct answer.
     */
    public static final int REQUIRED_CORRECT_ALTERNATIVAS = 1;
    
    /**
     * Minimum number of cargo associations required per question.
     * Every question must apply to at least one position.
     */
    public static final int MIN_CARGO_ASSOCIATIONS = 1;
    
    // ========================================
    // Simulado Validation
    // ========================================
    
    /**
     * Minimum number of questions required for a simulado.
     * Exams with fewer questions aren't representative of real exams.
     */
    public static final int MIN_SIMULADO_QUESTIONS = 20;
    
    // ========================================
    // Date Validation
    // ========================================
    
    /**
     * Minimum valid year for concursos.
     * Public exams in Brazil began in modern format around 1900.
     */
    public static final int MIN_YEAR = 1900;
    
    /**
     * Maximum valid year for concursos.
     * Prevents accidental future dates far beyond reasonable range.
     */
    public static final int MAX_YEAR = 2100;
    
    /**
     * Minimum valid month (January).
     */
    public static final int MIN_MONTH = 1;
    
    /**
     * Maximum valid month (December).
     */
    public static final int MAX_MONTH = 12;
}
