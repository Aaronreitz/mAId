package dev.maid.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.maid.client.OpenAIClient;
import dev.maid.dto.ChatRequest;
import dev.maid.dto.ChatResponse;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.Optional;

@ApplicationScoped
public class OpenAIService {

    @RestClient
    OpenAIClient openAIClient;

    @Inject
    ObjectMapper mapper;

    @ConfigProperty(name = "openai.api.key")
    Optional<String> apiKey;

    public Uni<ChatResponse> chat(ChatRequest request) {
        if (apiKey.isEmpty() || apiKey.get().isBlank()) {
            return Uni.createFrom().failure(
                new BadRequestException("OpenAI API key not configured — set OPENAI_API_KEY")
            );
        }

        return openAIClient.createCompletion("Bearer " + apiKey.get(), buildBody(request, false))
            .map(res -> new ChatResponse(
                res.path("choices").path(0).path("message").path("content").asText(),
                request.model()
            ));
    }

    public Multi<String> stream(ChatRequest request) {
        if (apiKey.isEmpty() || apiKey.get().isBlank()) {
            return Multi.createFrom().failure(
                new BadRequestException("OpenAI API key not configured — set OPENAI_API_KEY")
            );
        }

        return openAIClient.streamCompletion("Bearer " + apiKey.get(), buildBody(request, true))
            .filter(data -> data != null && !data.isBlank() && !"[DONE]".equals(data.trim()))
            .map(data -> {
                try {
                    JsonNode node = mapper.readTree(data);
                    return node.path("choices").path(0).path("delta").path("content").asText(null);
                } catch (Exception ignored) {}
                return null;
            })
            .filter(text -> text != null && !text.isEmpty());
    }

    private ObjectNode buildBody(ChatRequest request, boolean stream) {
        ObjectNode body = mapper.createObjectNode();
        body.put("model", request.model());
        if (stream) body.put("stream", true);

        ArrayNode messages = body.putArray("messages");
        for (var msg : request.messages()) {
            messages.addObject()
                .put("role", msg.role())
                .put("content", msg.content());
        }
        return body;
    }
}
