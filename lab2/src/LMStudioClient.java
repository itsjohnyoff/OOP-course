import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.Strictness;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

// AI-assisted: LM Studio integration
public final class LMStudioClient {
    private static final Gson JSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setStrictness(Strictness.STRICT)
            .create();

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final URI endpoint;
    private final String model;

    public LMStudioClient(String baseUrl, String model) {
        String server = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.endpoint = URI.create(server + "/v1/chat/completions");
        this.model = model;
    }

    public LMResponse send(List<Message> messages) throws IOException, InterruptedException {
        String body = JSON.toJson(new ChatRequest(model, messages, false, 0, 384));
        HttpRequest request = HttpRequest.newBuilder(endpoint)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(90))
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        // Include the time spent waiting for the whole reply.
        long start = System.nanoTime();
        HttpResponse<String> response;
        try {
            response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException error) {
            throw new IOException("Could not reach LM Studio at " + endpoint + " ("
                    + error.getClass().getSimpleName() + ").", error);
        }
        double elapsed = (System.nanoTime() - start) / 1_000_000_000.0;
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("LM Studio returned HTTP " + response.statusCode() + ".");
        }

        ChatResponse data;
        try {
            data = JSON.fromJson(response.body(), ChatResponse.class);
        } catch (JsonParseException error) {
            throw new IOException("Could not read the JSON reply from LM Studio.", error);
        }
        if (data == null || data.choices() == null || data.choices().isEmpty() || data.usage() == null) {
            throw new IOException("LM Studio reply is missing a choice or token counts.");
        }
        Choice first = data.choices().getFirst();
        Message message = first == null ? null : first.message();
        Usage usage = data.usage();
        if (message == null || usage.promptTokens() == null || usage.promptTokens() < 0
                || usage.completionTokens() == null || usage.completionTokens() < 0) {
            throw new IOException("LM Studio reply is missing text or usable token counts.");
        }
        if (message.content() == null || message.content().isBlank()) {
            throw new IOException("LM Studio returned an empty reply.");
        }
        return new LMResponse(message.content().strip(), usage.promptTokens(), usage.completionTokens(), elapsed);
    }

    private record ChatRequest(String model, List<Message> messages, boolean stream,
                               double temperature, int maxTokens) { }

    private record ChatResponse(List<Choice> choices, Usage usage) { }

    private record Choice(Message message) { }

    private record Usage(Integer promptTokens, Integer completionTokens) { }
}
