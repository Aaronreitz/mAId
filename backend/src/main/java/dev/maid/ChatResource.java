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
        String model = request.model();
        if (model != null && model.startsWith("claude")) return claudeService.chat(request);
        if (model != null && model.startsWith("gpt"))    return openAIService.chat(request);
        return Uni.createFrom().failure(new BadRequestException("Unknown model: " + model));
    }

    @POST
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<String> chatStream(ChatRequest request) {
        String model = request.model();
        if (model != null && model.startsWith("claude")) return claudeService.stream(request);
        if (model != null && model.startsWith("gpt"))    return openAIService.stream(request);
        return Multi.createFrom().failure(new BadRequestException("Unknown model: " + model));
    }
}
