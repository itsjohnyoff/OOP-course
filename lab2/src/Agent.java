import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Agent {
    private final String name;
    private final String personality;
    private final Memory memory;
    private final LMStudioClient client;
    private final Logger logger = new Logger("Agent");

    public Agent(String name, String personality, Memory memory, String baseUrl, String model) {
        this.name = name;
        this.personality = personality;
        this.memory = memory;
        this.client = new LMStudioClient(baseUrl, model);
    }

    public String chat(String userMessage) {
        Message question = new Message("user", userMessage);
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("system", "You are " + name + ". " + personality));
        messages.addAll(memory.getMessages());
        messages.add(question);

        try {
            logger.trace("Sending % messages to LM Studio", messages.size());
            LMResponse response = client.send(messages);
            double rate = response.elapsedSeconds() > 0
                    ? response.completionTokens() / response.elapsedSeconds() : 0;
            logger.info("Prompt tokens: % | Completion tokens: % | Tokens/sec: %",
                    response.promptTokens(), response.completionTokens(),
                    String.format(Locale.ROOT, "%.2f", rate));

            if (memory.addExchange(question, new Message("assistant", response.content()))) {
                logger.warn("Memory is full. Dropped the oldest exchange.");
            }
            logger.trace("Added the user message to memory");
            logger.trace("Added the assistant reply to memory");
            return response.content();
        } catch (IOException error) {
            logger.error("%", error.getMessage());
            return "I couldn't get a reply. See the error above.";
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            logger.error("The LM Studio request was interrupted.");
            return "The request was interrupted.";
        }
    }
}
