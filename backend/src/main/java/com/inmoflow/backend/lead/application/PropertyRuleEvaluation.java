package com.inmoflow.backend.lead.application;

import java.util.List;

public record PropertyRuleEvaluation(
        PropertyRuleEvaluationResult result,
        List<String> missingQuestions,
        String mismatchMessage
) {

    public static PropertyRuleEvaluation match() {
        return new PropertyRuleEvaluation(PropertyRuleEvaluationResult.MATCH, List.of(), null);
    }

    public static PropertyRuleEvaluation mismatch(String mismatchMessage) {
        return new PropertyRuleEvaluation(PropertyRuleEvaluationResult.MISMATCH, List.of(), mismatchMessage);
    }

    public static PropertyRuleEvaluation needsMoreInfo(List<String> missingQuestions) {
        return new PropertyRuleEvaluation(PropertyRuleEvaluationResult.NEEDS_MORE_INFO, missingQuestions, null);
    }
}
