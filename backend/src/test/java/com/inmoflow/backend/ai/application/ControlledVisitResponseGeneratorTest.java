package com.inmoflow.backend.ai.application;

import com.inmoflow.backend.lead.domain.Lead;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ControlledVisitResponseGeneratorTest {

    private final ControlledVisitResponseGenerator generator = new ControlledVisitResponseGenerator();

    @Test
    void returnsEmptyWhenMessageHasNoVisitIntent() {
        Optional<String> response = generator.generateResponse(context("Cuanto cuesta el piso?", null));

        assertThat(response).isEmpty();
    }

    @Test
    void asksForAvailabilityWhenVisitIntentHasNoAvailability() {
        Optional<String> response = generator.generateResponse(context("Hola, quiero visitar el piso", null));

        assertThat(response).contains("Perfecto, podemos ayudarte a coordinar una visita. Podrias indicarme que dia o franja horaria te vendria mejor?");
    }

    @Test
    void acknowledgesAvailabilityAndDoesNotAskPhoneWhenLeadPhoneExists() {
        Lead lead = Lead.builder()
                .phone("600123123")
                .build();

        Optional<String> response = generator.generateResponse(context("Hola, quiero ver el piso el jueves por la tarde", lead));

        assertThat(response).contains("Perfecto, he anotado que te vendria bien jueves por la tarde. Un agente revisara la disponibilidad y te confirmara una hora concreta.");
    }

    @Test
    void acknowledgesAvailabilityAndAsksPhoneWhenLeadPhoneIsMissing() {
        Optional<String> response = generator.generateResponse(context("Quiero agendar visita esta semana", null));

        assertThat(response).contains("Perfecto, he anotado que te vendria bien esta semana. Un agente revisara la disponibilidad y te confirmara una hora concreta. Podrias indicarme tu telefono para coordinar la visita?");
    }

    @Test
    void detectsAccentedAvailabilityExpressions() {
        Optional<String> response = generator.generateResponse(context("Me gustaria verlo mañana", Lead.builder().phone("600123123").build()));

        assertThat(response).contains("Perfecto, he anotado que te vendria bien manana. Un agente revisara la disponibilidad y te confirmara una hora concreta.");
    }

    private AiResponseContext context(String message, Lead lead) {
        return new AiResponseContext(message, lead, List.of());
    }
}
