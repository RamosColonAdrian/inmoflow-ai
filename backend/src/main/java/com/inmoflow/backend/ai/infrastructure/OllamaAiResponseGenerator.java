package com.inmoflow.backend.ai.infrastructure;

import com.inmoflow.backend.ai.application.AiResponseContext;
import com.inmoflow.backend.ai.application.AiResponseGenerator;
import com.inmoflow.backend.conversation.domain.Message;
import com.inmoflow.backend.lead.domain.Lead;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.List;
import java.util.Map;

@Component
public class OllamaAiResponseGenerator implements AiResponseGenerator {

    private static final String SYSTEM_PROMPT = """
            Actua como un asistente inmobiliario para una agencia.
            Responde siempre en espanol.
            Usa el historico reciente de conversacion y los datos del lead.
            No pidas ningun dato que ya aparezca como disponible.
            Usa un tono breve, natural y profesional de agencia inmobiliaria, sin entusiasmo exagerado.
            Responde en 1 o 2 frases como maximo.
            No inventes nunca datos de inmuebles, precios, disponibilidad, direcciones, superficies, caracteristicas ni condiciones.
            No confirmes nunca visitas, fechas, horarios concretos ni citas cerradas.
            Si el lead pide una visita, di que un agente confirmara la hora exacta.
            Si falta informacion, haz solo una pregunta corta de seguimiento.
            No pidas el telefono si el telefono del lead ya esta disponible.
            No pidas el email si el email del lead ya esta disponible.
            Si Telefono esta disponible, esta prohibido pedir telefono.
            Si Email esta disponible, esta prohibido pedir email.
            No digas ni uses expresiones parecidas a "Excelente eleccion", "Estoy dispuesto", "me encargare" o "voy a revisar".
            No hables como si tu personalmente pudieras revisar calendarios, disponibilidad o fichas internas.
            No uses signos de exclamacion salvo que sea realmente necesario.
            No digas "lo siento" salvo que exista un problema real o haga falta pedir disculpas.
            Si el lead pregunta por datos que no aparecen en su mensaje, indica que un agente puede revisarlo o pide el dato necesario.
            Si el lead quiere visitar un inmueble y no da disponibilidad, pide una franja general como manana, tarde o dia preferido.
            Si el lead ya propone una disponibilidad general, como "jueves por la tarde", reconocela de forma natural y no vuelvas a preguntar por disponibilidad.
            Cuando el lead proponga disponibilidad, di que un agente revisara la disponibilidad y confirmara una hora concreta, sin decir que la visita queda reservada ni que tu la confirmaras.
            Pide el telefono solo si hace falta para coordinar la visita o para que el agente contacte al lead.
            No asegures que un inmueble esta disponible salvo que el lead lo haya indicado explicitamente.
            Prefiere frases neutras como "Perfecto, he anotado...", "Un agente revisara..." y "Para coordinar la visita...".

            Ejemplos:
            Lead context: telefono disponible.
            Lead message: "Hola, quiero visitar este piso el jueves por la tarde."
            Respuesta correcta: "Perfecto, he anotado que te vendria bien el jueves por la tarde. Un agente revisara la disponibilidad y te confirmara una hora concreta."

            Lead: "Quiero verlo manana."
            Respuesta correcta: "Perfecto, podemos ayudarte a coordinar una visita. Podrias indicarme si te vendria mejor por la manana o por la tarde?"

            Lead: "Cuanto cuesta?"
            Respuesta correcta: "No tengo el precio confirmado en este momento. Un agente puede revisarlo y darte la informacion exacta. Quieres que te contacte?"

            Lead: "Sigue disponible?"
            Respuesta correcta: "No puedo confirmar la disponibilidad sin revisar la ficha actualizada. Un agente puede comprobarlo y responderte con la informacion exacta."
            """;

    private static final Map<String, Object> OLLAMA_OPTIONS = Map.of("temperature", 0.2);

    private final OllamaClient ollamaClient;
    private final String model;

    public OllamaAiResponseGenerator(
            OllamaClient ollamaClient,
            @Value("${ollama.model:llama3.2}") String model
    ) {
        this.ollamaClient = ollamaClient;
        this.model = model;
    }

    @Override
    public String generateResponse(String leadMessage) {
        return generateResponse(new AiResponseContext(leadMessage, null, List.of()));
    }

    @Override
    public String generateResponse(AiResponseContext context) {
        OllamaChatRequest request = new OllamaChatRequest(
                model,
                false,
                OLLAMA_OPTIONS,
                List.of(
                        new OllamaChatMessage("system", SYSTEM_PROMPT),
                        new OllamaChatMessage("user", buildContextPrompt(context))
                )
        );

        OllamaChatResponse response = ollamaClient.chat(request);
        if (response == null || response.message() == null || response.message().content() == null) {
            throw new IllegalStateException("Ollama returned an invalid response");
        }

        String content = cleanResponse(response.message().content(), context);
        if (content.isBlank()) {
            throw new IllegalStateException("Ollama returned an empty response");
        }

        return content;
    }

    private String buildContextPrompt(AiResponseContext context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Genera la siguiente respuesta del asistente para este lead.\n\n");
        appendLeadData(prompt, context.lead());
        appendConversationHistory(prompt, context.recentMessages());
        prompt.append("\nUltimo mensaje del lead:\n");
        prompt.append(context.leadMessage());

        return prompt.toString();
    }

    private void appendLeadData(StringBuilder prompt, Lead lead) {
        prompt.append("Datos ya disponibles del lead:\n");
        if (lead == null) {
            prompt.append("- No disponibles\n\n");
            return;
        }

        appendValue(prompt, "Nombre", lead.getName());
        appendValue(prompt, "Email", lead.getEmail());
        appendValue(prompt, "Telefono", lead.getPhone());
        appendValue(prompt, "Origen", lead.getSource());
        appendValue(prompt, "Estado", lead.getStatus());
        appendValue(prompt, "Zona deseada", lead.getDesiredZone());
        appendValue(prompt, "Presupuesto", lead.getBudget());
        appendValue(prompt, "Score", lead.getScore());
        prompt.append("No pidas ningun dato que ya aparezca como disponible.\n");
        prompt.append('\n');
    }

    private void appendConversationHistory(StringBuilder prompt, List<Message> messages) {
        prompt.append("Historico reciente de conversacion, de mas antiguo a mas reciente:\n");
        if (messages == null || messages.isEmpty()) {
            prompt.append("- Sin mensajes previos\n\n");
            return;
        }

        for (Message message : messages) {
            prompt.append("- ");
            prompt.append(message.getSenderType());
            prompt.append(": ");
            prompt.append(message.getContent());
            prompt.append('\n');
        }
        prompt.append('\n');
    }

    private void appendValue(StringBuilder prompt, String label, Object value) {
        if (value == null) {
            return;
        }

        if (value instanceof String text && text.isBlank()) {
            return;
        }

        if (value instanceof BigDecimal decimal) {
            prompt.append("- ").append(label).append(": ").append(decimal.toPlainString()).append('\n');
            return;
        }

        prompt.append("- ").append(label).append(": ").append(value).append('\n');
    }

    private String cleanResponse(String response, AiResponseContext context) {
        String content = removeSurroundingQuotes(response.trim());
        Lead lead = context.lead();
        if (lead == null) {
            return content;
        }

        if (hasText(lead.getPhone())) {
            content = removeSentencesContaining(content, "telefono", "numero de telefono");
        }

        if (hasText(lead.getEmail())) {
            content = removeSentencesContaining(content, "email", "e-mail", "correo");
        }

        if (content.isBlank()) {
            return "Perfecto, he anotado tu solicitud. Un agente revisara la disponibilidad y te confirmara una hora concreta.";
        }

        return content.trim();
    }

    private String removeSurroundingQuotes(String content) {
        if (content.length() < 2) {
            return content;
        }

        if (content.startsWith("\"") && content.endsWith("\"")) {
            return content.substring(1, content.length() - 1).trim();
        }

        return content;
    }

    private String removeSentencesContaining(String content, String... forbiddenTerms) {
        String[] sentences = content.split("(?<=[.!?])\\s+");
        StringBuilder cleaned = new StringBuilder();

        for (String sentence : sentences) {
            if (containsAny(sentence, forbiddenTerms)) {
                continue;
            }

            if (cleaned.length() > 0) {
                cleaned.append(' ');
            }
            cleaned.append(sentence.trim());
        }

        return cleaned.toString();
    }

    private boolean containsAny(String text, String... terms) {
        String normalizedText = normalize(text);
        for (String term : terms) {
            if (normalizedText.contains(normalize(term))) {
                return true;
            }
        }

        return false;
    }

    private String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
