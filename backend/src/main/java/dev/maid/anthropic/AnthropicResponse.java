package dev.maid.anthropic;

import java.util.List;

public record AnthropicResponse(String id, String role, List<ContentBlock> content) {}
