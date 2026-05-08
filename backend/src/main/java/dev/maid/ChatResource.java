package dev.maid;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/chat")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChatResource {

    @Inject
    ClaudeClient claudeClient;

    @POST
    public Response chat(ChatRequest request) {
        String model = request.model() != null ? request.model() : "claude-haiku-4-5";
        String reply = claudeClient.chat(model, request.message(), request.history());
        return Response.ok(new ChatResponse(reply, model)).build();
    }
}
