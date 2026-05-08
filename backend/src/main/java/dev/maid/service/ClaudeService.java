package dev.maid.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.maid.client.ClaudeClient;
import dev.maid.dto.ChatRequest;
import dev.maid.dto.ChatResponse;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.Optional;

@ApplicationScoped
public class ClaudeService {

    private static final String MODEL = "claude-sonnet-4-6";
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

        ObjectNode body = mapper.createObjectNode();
        body.put("model", MODEL);
        body.put("max_tokens", 4096);

        ArrayNode messages = body.putArray("messages");
        for (var msg : request.messages()) {
            messages.addObject()
                .put("role", msg.role())
                .put("content", msg.content());
        }

        return claudeClient.createMessage(apiKey.get(), ANTHROPIC_VERSION, body)
            .map(res -> new ChatResponse(
                res.path("content").path(0).path("text").asText(),
                MODEL
            ));
    }
}
