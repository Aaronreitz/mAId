package dev.maid.client;

import com.fasterxml.jackson.databind.JsonNode;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "claude-api")
@Path("/v1")
public interface ClaudeClient {

    @POST
    @Path("/messages")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ClientHeaderParam(name = "Content-Type", value = MediaType.APPLICATION_JSON)
    Uni<JsonNode> createMessage(
        @HeaderParam("x-api-key") String apiKey,
        @HeaderParam("anthropic-version") String version,
        JsonNode body
    );
}
