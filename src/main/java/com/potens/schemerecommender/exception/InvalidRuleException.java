package com.potens.schemerecommender.exception;

import com.potens.schemerecommender.enums.RuleType;

public class InvalidRuleException extends RuntimeException {

    public InvalidRuleException(RuleType ruleType, String reason) {
        super(String.format("Invalid rule configuration for %s: %s", ruleType.name(), reason));
    }
}
