package dev.maid;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Path("/api/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {

    @GET
    public Response health() {
        return Response.ok(Map.of("status", "ok")).build();
    }
}
