package dev.maid.anthropic;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AnthropicRequest(
    String model,
    @JsonProperty("max_tokens") int maxTokens,
    List<AnthropicMessage> messages
) {}
