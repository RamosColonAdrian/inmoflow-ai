package com.inmoflow.backend.lead.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LeadQualificationServiceTest {

    private final LeadQualificationService service = new LeadQualificationService();

    @Test
    void detectsPetMismatch() {
        PropertyRuleEvaluation evaluation = service.evaluate("No mascotas", "Tengo perro y quiero visitarlo");

        assertThat(evaluation.result()).isEqualTo(PropertyRuleEvaluationResult.MISMATCH);
    }

    @Test
    void detectsStudentMismatch() {
        PropertyRuleEvaluation evaluation = service.evaluate("No estudiantes", "Somos estudiantes");

        assertThat(evaluation.result()).isEqualTo(PropertyRuleEvaluationResult.MISMATCH);
    }

    @Test
    void detectsShortStayMismatch() {
        PropertyRuleEvaluation evaluation = service.evaluate("Solo larga estancia", "Seria solo 2 meses");

        assertThat(evaluation.result()).isEqualTo(PropertyRuleEvaluationResult.MISMATCH);
    }

    @Test
    void asksForMissingPetInfo() {
        PropertyRuleEvaluation evaluation = service.evaluate("No mascotas", "Quiero visitarlo el jueves");

        assertThat(evaluation.result()).isEqualTo(PropertyRuleEvaluationResult.NEEDS_MORE_INFO);
        assertThat(evaluation.missingQuestions()).contains("si tienes mascotas");
    }

    @Test
    void asksForMissingStudentInfo() {
        PropertyRuleEvaluation evaluation = service.evaluate("No estudiantes", "Quiero visitarlo el jueves");

        assertThat(evaluation.result()).isEqualTo(PropertyRuleEvaluationResult.NEEDS_MORE_INFO);
        assertThat(evaluation.missingQuestions()).contains("si eres estudiante");
    }

    @Test
    void asksForMissingLongStayInfo() {
        PropertyRuleEvaluation evaluation = service.evaluate("Solo larga estancia", "Quiero visitarlo el jueves");

        assertThat(evaluation.result()).isEqualTo(PropertyRuleEvaluationResult.NEEDS_MORE_INFO);
        assertThat(evaluation.missingQuestions()).contains("si buscas una estancia de larga duracion");
    }

    @Test
    void asksForMissingEmploymentOrIncomeInfo() {
        PropertyRuleEvaluation evaluation = service.evaluate("Se pide contrato laboral y nominas", "Quiero visitarlo el jueves");

        assertThat(evaluation.result()).isEqualTo(PropertyRuleEvaluationResult.NEEDS_MORE_INFO);
        assertThat(evaluation.missingQuestions()).contains("si dispones de contrato laboral, nominas o ingresos demostrables");
    }

    @Test
    void matchesWhenLeadSatisfiesAllDetectedRules() {
        PropertyRuleEvaluation evaluation = service.evaluate(
                "No mascotas. No estudiantes. Solo larga estancia. Se pide contrato laboral y nominas.",
                "Sin mascotas, no soy estudiante, busco larga estancia y tengo contrato laboral"
        );

        assertThat(evaluation.result()).isEqualTo(PropertyRuleEvaluationResult.MATCH);
    }
}
