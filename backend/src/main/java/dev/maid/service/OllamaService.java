package dev.maid.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.maid.client.OllamaClient;
import dev.maid.dto.ChatRequest;
import dev.maid.dto.ChatResponse;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class OllamaService {

    private static final String UNAVAILABLE = "Local models unavailable";

    @RestClient
    OllamaClient ollamaClient;

    @Inject
    ObjectMapper mapper;

    public Uni<ChatResponse> chat(ChatRequest request) {
        return ollamaClient.createCompletion(buildBody(request, false))
            .onFailure().transform(t -> new WebApplicationException(
                Response.status(503).entity(UNAVAILABLE).build()))
            .map(res -> new ChatResponse(
                res.path("choices").path(0).path("message").path("content").asText(),
                request.model()
            ));
    }

    public Multi<String> stream(ChatRequest request) {
        return ollamaClient.streamCompletion(buildBody(request, true))
            .onFailure().recoverWithMulti(t -> Multi.createFrom().failure(
                new WebApplicationException(
                    Response.status(503).entity(UNAVAILABLE).build())))
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

    public Uni<JsonNode> listModels() {
        return ollamaClient.listModels();
    }

    private ObjectNode buildBody(ChatRequest request, boolean stream) {
        ObjectNode body = mapper.createObjectNode();
        String modelName = request.model();
        if (modelName != null && modelName.startsWith("ollama/")) {
            modelName = modelName.substring(7);
        }
        body.put("model", modelName);
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
