import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.Strictness;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Agent {
    private static final int MAX_TOOL_CALLS = 3;
    private static final Gson JSON = new GsonBuilder().setStrictness(Strictness.STRICT).create();

    private final String name;
    private final String personality;
    private final Memory memory;
    private final LMStudioClient client;
    private final Toolbox toolbox;
    private final Logger logger = new Logger("Agent");

    public Agent(String name, String personality, Memory memory, String baseUrl, String model) {
        this.name = name;
        this.personality = personality;
        this.memory = memory;
        this.client = new LMStudioClient(baseUrl, model);
        this.toolbox = new Toolbox();
    }

    public String chat(String userMessage) throws IOException, InterruptedException {
        Message question = new Message("user", userMessage);
        List<Message> messages = new ArrayList<>();
        messages.add(systemMessage());
        messages.addAll(memory.getMessages());
        messages.add(question);

        int calls = 0;
        while (true) {
            String reply = ask(messages);
            ToolCall toolCall;
            try {
                toolCall = readToolCall(reply);
            } catch (IllegalArgumentException error) {
                throw new IOException("Could not read the tool request: " + error.getMessage(), error);
            }
            if (toolCall == null) {
                remember(question, reply);
                return reply;
            }
            if (calls == MAX_TOOL_CALLS) {
                logger.warn("Tool call limit reached (% per question)", MAX_TOOL_CALLS);
                return "I reached the tool call limit. Try asking again.";
            }

            // Keep tool messages with this question until the model gives a final reply.
            calls++;
            messages.add(new Message("assistant", reply));
            messages.add(new Message("user", runTool(toolCall)));
        }
    }

    private Message systemMessage() {
        return new Message("system", "You are " + name + ". " + personality + "\n" + """
                For a calculation, the current date, or the current time, ask for a tool.
                Reply with just one JSON object. Example: {"tool":"calculator","input":"12 * 7"}
                The tools are calculator, get_date, and get_time. Use an empty input for date or time.
                After you get a tool result, answer the original question in plain text.
                For all other questions, answer in plain text.
                """);
    }

    private String ask(List<Message> messages) throws IOException, InterruptedException {
        logger.trace("Sending % messages to LM Studio", messages.size());
        LMResponse response = client.send(messages);
        double rate = response.elapsedSeconds() > 0
                ? response.completionTokens() / response.elapsedSeconds() : 0;
        logger.info("Prompt tokens: % | Completion tokens: % | Tokens/sec: %",
                response.promptTokens(), response.completionTokens(),
                String.format(Locale.ROOT, "%.2f", rate));
        return response.content();
    }

    private void remember(Message question, String reply) {
        if (memory.addExchange(question, new Message("assistant", reply))) {
            logger.warn("Memory is full. Dropped the oldest exchange.");
        }
        logger.trace("Added the user message to memory");
        logger.trace("Added the assistant reply to memory");
    }

    private ToolCall readToolCall(String reply) {
        int start = reply.indexOf('{');
        if (start < 0 || !reply.substring(start).contains("\"tool\"")) {
            return null;
        }
        int end = reply.lastIndexOf('}');
        if (end < start) {
            throw new IllegalArgumentException("Missing closing brace");
        }

        JsonObject request;
        try {
            request = JSON.fromJson(reply.substring(start, end + 1), JsonObject.class);
        } catch (JsonParseException error) {
            throw new IllegalArgumentException("Invalid JSON", error);
        }
        if (!request.has("tool")) {
            return null;
        }
        String tool = readString(request.get("tool"), "tool");
        JsonElement input = request.get("input");
        if (!reply.substring(0, start).isBlank() || !reply.substring(end + 1).isBlank()) {
            logger.warn("Found extra text around the tool request");
        }
        return new ToolCall(tool, input == null ? "" : readString(input, "input"));
    }

    private String readString(JsonElement value, String field) {
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException(field + " must be a string");
        }
        return value.getAsString();
    }

    private String runTool(ToolCall call) {
        try {
            String result = toolbox.run(call.name(), call.input());
            logger.trace("Tool % returned %", call.name(), result);
            return "Tool " + call.name() + " returned: " + result;
        } catch (IllegalArgumentException | ArithmeticException error) {
            return "Tool " + call.name() + " failed: " + error.getMessage();
        }
    }

    private record ToolCall(String name, String input) { }
}
