package dev.maid.anthropic;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "anthropic")
@Path("/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface AnthropicRestClient {

    @POST
    @Path("/messages")
    AnthropicResponse messages(
        @HeaderParam("x-api-key") String apiKey,
        @HeaderParam("anthropic-version") String anthropicVersion,
        AnthropicRequest request
    );
}
