package com.inmoflow.backend.lead.application;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

@Service
public class LeadQualificationService {

    public PropertyRuleEvaluation evaluate(String qualificationRulesText, String leadMessage) {
        String rules = normalize(qualificationRulesText);
        if (rules.isBlank()) {
            return PropertyRuleEvaluation.match();
        }

        String message = normalize(leadMessage);
        List<String> missingQuestions = new ArrayList<>();

        if (hasPetsRule(rules)) {
            RuleStatus status = evaluatePets(message);
            if (status == RuleStatus.MISMATCH) {
                return PropertyRuleEvaluation.mismatch("no se aceptarian mascotas");
            }
            if (status == RuleStatus.UNKNOWN) {
                missingQuestions.add("si tienes mascotas");
            }
        }

        if (hasStudentsRule(rules)) {
            RuleStatus status = evaluateStudents(message);
            if (status == RuleStatus.MISMATCH) {
                return PropertyRuleEvaluation.mismatch("no se aceptarian estudiantes");
            }
            if (status == RuleStatus.UNKNOWN) {
                missingQuestions.add("si eres estudiante");
            }
        }

        if (hasLongStayRule(rules)) {
            RuleStatus status = evaluateLongStay(message);
            if (status == RuleStatus.MISMATCH) {
                return PropertyRuleEvaluation.mismatch("se busca un perfil de larga estancia");
            }
            if (status == RuleStatus.UNKNOWN) {
                missingQuestions.add("si buscas una estancia de larga duracion");
            }
        }

        if (hasEmploymentOrIncomeRule(rules)) {
            RuleStatus status = evaluateEmploymentOrIncome(message);
            if (status == RuleStatus.MISMATCH) {
                return PropertyRuleEvaluation.mismatch("se solicita contrato laboral o ingresos demostrables");
            }
            if (status == RuleStatus.UNKNOWN) {
                missingQuestions.add("si dispones de contrato laboral, nominas o ingresos demostrables");
            }
        }

        if (!missingQuestions.isEmpty()) {
            return PropertyRuleEvaluation.needsMoreInfo(missingQuestions);
        }

        return PropertyRuleEvaluation.match();
    }

    private boolean hasPetsRule(String rules) {
        return containsAny(rules, "no mascotas", "sin mascotas", "mascotas no", "no se aceptan mascotas");
    }

    private boolean hasStudentsRule(String rules) {
        return containsAny(rules, "no estudiantes", "estudiantes no", "no se aceptan estudiantes");
    }

    private boolean hasLongStayRule(String rules) {
        return containsAny(rules, "solo larga estancia", "larga estancia", "larga duracion", "largo plazo");
    }

    private boolean hasEmploymentOrIncomeRule(String rules) {
        return containsAny(rules, "contrato laboral", "nomina", "nominas", "ingresos", "ingresos demostrables");
    }

    private RuleStatus evaluatePets(String message) {
        if (containsAny(message, "no tengo mascotas", "sin mascotas", "no mascotas", "no tengo perro", "no tengo gato")) {
            return RuleStatus.MATCH;
        }
        if (containsAny(message, "tengo perro", "tengo un perro", "tenemos perro", "tenemos un perro", "tengo gato", "tengo un gato", "tenemos gato", "tenemos un gato", "voy con mascota", "con mascota", "mascota")) {
            return RuleStatus.MISMATCH;
        }
        return RuleStatus.UNKNOWN;
    }

    private RuleStatus evaluateStudents(String message) {
        if (containsAny(message, "no soy estudiante", "no somos estudiantes", "no estudiante")) {
            return RuleStatus.MATCH;
        }
        if (containsAny(message, "soy estudiante", "somos estudiantes", "estudio en la universidad", "estudiante", "universidad")) {
            return RuleStatus.MISMATCH;
        }
        return RuleStatus.UNKNOWN;
    }

    private RuleStatus evaluateLongStay(String message) {
        if (containsAny(message, "larga estancia", "larga duracion", "largo plazo", "larga temporada")) {
            return RuleStatus.MATCH;
        }
        if (containsAny(message, "solo 2 meses", "solo dos meses", "para un mes", "un mes", "2 meses", "dos meses", "3 meses", "tres meses", "alquiler temporal", "temporal", "corta estancia", "temporada")) {
            return RuleStatus.MISMATCH;
        }
        return RuleStatus.UNKNOWN;
    }

    private RuleStatus evaluateEmploymentOrIncome(String message) {
        if (containsAny(message, "contrato laboral", "tengo contrato", "contrato indefinido", "nomina", "nominas", "ingresos demostrables", "ingresos", "trabajo fijo")) {
            return RuleStatus.MATCH;
        }
        if (containsAny(message, "sin contrato", "no tengo contrato", "sin nomina", "sin nominas", "no tengo ingresos", "sin ingresos")) {
            return RuleStatus.MISMATCH;
        }
        return RuleStatus.UNKNOWN;
    }

    private boolean containsAny(String text, String... expressions) {
        for (String expression : expressions) {
            if (text.contains(expression)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
    }

    private enum RuleStatus {
        MATCH,
        MISMATCH,
        UNKNOWN
    }
}
