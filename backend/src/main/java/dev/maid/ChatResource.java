package dev.maid;

import dev.maid.dto.ChatRequest;
import dev.maid.dto.ChatResponse;
import dev.maid.service.ClaudeService;
import dev.maid.service.OpenAIService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/api/chat")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChatResource {

    @Inject
    ClaudeService claudeService;

    @Inject
    OpenAIService openAIService;

    @POST
    public Uni<ChatResponse> chat(ChatRequest request) {
        return switch (request.model()) {
            case "claude" -> claudeService.chat(request);
            case "gpt"    -> openAIService.chat(request);
            default -> Uni.createFrom().failure(
                new BadRequestException("Unknown model: " + request.model())
            );
        };
    }

    @POST
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<String> chatStream(ChatRequest request) {
        return switch (request.model()) {
            case "claude" -> claudeService.stream(request);
            case "gpt"    -> openAIService.stream(request);
            default -> Multi.createFrom().failure(
                new BadRequestException("Unknown model: " + request.model())
            );
        };
    }
}
