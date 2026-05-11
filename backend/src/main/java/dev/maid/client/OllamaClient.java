package dev.maid.client;

import com.fasterxml.jackson.databind.JsonNode;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "ollama-api")
@Path("/")
public interface OllamaClient {

    @POST
    @Path("v1/chat/completions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<JsonNode> createCompletion(JsonNode body);

    @POST
    @Path("v1/chat/completions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.SERVER_SENT_EVENTS)
    Multi<String> streamCompletion(JsonNode body);

    @GET
    @Path("api/tags")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<JsonNode> listModels();
}
