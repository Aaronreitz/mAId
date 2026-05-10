package dev.maid.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.maid.client.ClaudeClient;
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
public class ClaudeService {

    private static final String ANTHROPIC_VERSION = "2023-06-01";

    @RestClient
    ClaudeClient claudeClient;

    @Inject
    ObjectMapper mapper;

    @ConfigProperty(name = "claude.api.key")
    Optional<String> apiKey;

    public Uni<ChatResponse> chat(ChatRequest request) {
        if (apiKey.isEmpty() || apiKey.get().isBlank()) {
            return Uni.createFrom().failure(
                new BadRequestException("Claude API key not configured — set CLAUDE_API_KEY")
            );
        }

        return claudeClient.createMessage(apiKey.get(), ANTHROPIC_VERSION, buildBody(request, false))
            .map(res -> new ChatResponse(
                res.path("content").path(0).path("text").asText(),
                request.model()
            ));
    }

    public Multi<String> stream(ChatRequest request) {
        if (apiKey.isEmpty() || apiKey.get().isBlank()) {
            return Multi.createFrom().failure(
                new BadRequestException("Claude API key not configured — set CLAUDE_API_KEY")
            );
        }

        return claudeClient.streamMessage(apiKey.get(), ANTHROPIC_VERSION, buildBody(request, true))
            .filter(data -> data != null && !data.isBlank())
            .map(data -> {
                try {
                    JsonNode node = mapper.readTree(data);
                    if ("content_block_delta".equals(node.path("type").asText())
                            && "text_delta".equals(node.path("delta").path("type").asText())) {
                        return node.path("delta").path("text").asText(null);
                    }
                } catch (Exception ignored) {}
                return null;
            })
            .filter(text -> text != null && !text.isEmpty());
    }

    private ObjectNode buildBody(ChatRequest request, boolean stream) {
        ObjectNode body = mapper.createObjectNode();
        body.put("model", request.model());
        body.put("max_tokens", 4096);
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
