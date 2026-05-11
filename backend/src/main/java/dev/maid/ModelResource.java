package dev.maid;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.maid.service.OllamaService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/models")
@Produces(MediaType.APPLICATION_JSON)
public class ModelResource {

    @Inject
    OllamaService ollamaService;

    @Inject
    ObjectMapper mapper;

    @GET
    public Uni<ArrayNode> list() {
        ArrayNode cloudModels = buildCloudModels();
        return ollamaService.listModels()
            .map(json -> {
                ArrayNode all = cloudModels.deepCopy();
                JsonNode models = json.path("models");
                if (models.isArray()) {
                    for (JsonNode m : models) {
                        String name = m.path("name").asText();
                        String display = name.contains(":") ? name.substring(0, name.indexOf(':')) : name;
                        ObjectNode entry = mapper.createObjectNode();
                        entry.put("id", "ollama/" + name);
                        entry.put("name", display + " (local)");
                        entry.put("provider", "ollama");
                        all.add(entry);
                    }
                }
                return all;
            })
            .onFailure().recoverWithItem(cloudModels);
    }

    private ArrayNode buildCloudModels() {
        ArrayNode arr = mapper.createArrayNode();
        addEntry(arr, "claude-sonnet-4-6", "Claude Sonnet", "anthropic");
        addEntry(arr, "claude-haiku-4-5-20251001", "Claude Haiku", "anthropic");
        addEntry(arr, "gpt-4o", "GPT-4o", "openai");
        addEntry(arr, "gpt-4o-mini", "GPT-4o mini", "openai");
        return arr;
    }

    private void addEntry(ArrayNode arr, String id, String name, String provider) {
        ObjectNode m = mapper.createObjectNode();
        m.put("id", id);
        m.put("name", name);
        m.put("provider", provider);
        arr.add(m);
    }
}
