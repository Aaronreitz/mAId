package dev.maid;

import dev.maid.anthropic.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ClaudeClient {

    @ConfigProperty(name = "anthropic.api.key")
    String apiKey;

    @Inject
    @RestClient
    AnthropicRestClient anthropicClient;

    public String chat(String model, String userMessage, List<Map<String, String>> history) {
        var messages = new ArrayList<AnthropicMessage>();

        if (history != null) {
            for (var entry : history) {
                messages.add(new AnthropicMessage(
                    entry.getOrDefault("role", "user"),
                    entry.getOrDefault("content", "")
                ));
            }
        }
        messages.add(new AnthropicMessage("user", userMessage));

        var request = new AnthropicRequest(model, 1024, messages);
        var response = anthropicClient.messages(apiKey, "2023-06-01", request);

        return response.content().get(0).text();
    }
}
