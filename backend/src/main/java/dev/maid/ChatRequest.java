package dev.maid;

import java.util.List;
import java.util.Map;

public record ChatRequest(
    String model,
    String message,
    List<Map<String, String>> history
) {}
