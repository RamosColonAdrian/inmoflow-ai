package com.inmoflow.backend.ai.application;

import com.inmoflow.backend.lead.domain.Lead;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class ControlledVisitResponseGenerator {

    private static final List<String> VISIT_INTENT_EXPRESSIONS = List.of(
            "concertar visita",
            "agendar visita",
            "ver la vivienda",
            "ver el piso",
            "visitar",
            "visita",
            "verlo"
    );

    private static final List<String> AVAILABILITY_EXPRESSIONS = List.of(
            "por la manana",
            "por la tarde",
            "esta semana",
            "fin de semana",
            "lunes",
            "martes",
            "miercoles",
            "jueves",
            "viernes",
            "sabado",
            "domingo",
            "manana",
            "tarde"
    );

    public Optional<String> generateResponse(AiResponseContext context) {
        String leadMessage = context.leadMessage();
        if (!hasVisitIntent(leadMessage)) {
            return Optional.empty();
        }

        Optional<String> availability = extractAvailability(leadMessage);
        if (availability.isEmpty()) {
            return Optional.of("Perfecto, podemos ayudarte a coordinar una visita. Podrias indicarme que dia o franja horaria te vendria mejor?");
        }

        String response = "Perfecto, he anotado que te vendria bien " + availability.get()
                + ". Un agente revisara la disponibilidad y te confirmara una hora concreta.";
        if (!hasLeadPhone(context.lead())) {
            response += " Podrias indicarme tu telefono para coordinar la visita?";
        }

        return Optional.of(response);
    }

    private boolean hasVisitIntent(String message) {
        String normalizedMessage = normalize(message);
        return VISIT_INTENT_EXPRESSIONS.stream()
                .anyMatch(expression -> containsExpression(normalizedMessage, expression));
    }

    private Optional<String> extractAvailability(String message) {
        String normalizedMessage = normalize(message);
        List<AvailabilityMatch> matches = new ArrayList<>();

        for (String expression : AVAILABILITY_EXPRESSIONS) {
            int index = normalizedMessage.indexOf(expression);
            if (index >= 0 && containsExpression(normalizedMessage, expression)) {
                matches.add(new AvailabilityMatch(expression, index, index + expression.length()));
            }
        }

        if (matches.isEmpty()) {
            return Optional.empty();
        }

        matches.sort(Comparator
                .comparingInt(AvailabilityMatch::start)
                .thenComparing(Comparator.comparingInt(AvailabilityMatch::length).reversed()));

        List<AvailabilityMatch> selected = new ArrayList<>();
        for (AvailabilityMatch match : matches) {
            if (selected.stream().noneMatch(existing -> existing.overlaps(match))) {
                selected.add(match);
            }
        }

        selected.sort(Comparator.comparingInt(AvailabilityMatch::start));
        return Optional.of(String.join(" ", selected.stream()
                .map(AvailabilityMatch::expression)
                .toList()));
    }

    private boolean containsExpression(String normalizedMessage, String expression) {
        return normalizedMessage.matches(".*\\b" + expression + "\\b.*");
    }

    private boolean hasLeadPhone(Lead lead) {
        return lead != null && lead.getPhone() != null && !lead.getPhone().isBlank();
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }

        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
    }

    private record AvailabilityMatch(String expression, int start, int end) {

        private int length() {
            return end - start;
        }

        private boolean overlaps(AvailabilityMatch other) {
            return start < other.end && other.start < end;
        }
    }
}
