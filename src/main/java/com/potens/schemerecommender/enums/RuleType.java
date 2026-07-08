package com.potens.schemerecommender.enums;

/**
 * The 5 rule types for scheme eligibility evaluation.
 *
 * <ul>
 *   <li>ENUM_MATCH — value must equal rule value (case-insensitive)</li>
 *   <li>RANGE_CHECK — numeric value must be within [minValue, maxValue]</li>
 *   <li>BOOLEAN_CHECK — boolean must match rule value</li>
 *   <li>MIN_VALUE — numeric value must be >= rule value</li>
 *   <li>MAX_VALUE — numeric value must be <= rule value</li>
 * </ul>
 */
public enum RuleType {
    ENUM_MATCH,
    RANGE_CHECK,
    BOOLEAN_CHECK,
    MIN_VALUE,
    MAX_VALUE
}
