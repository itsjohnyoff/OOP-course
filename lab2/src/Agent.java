import java.io.IOException;
import java.util.List;

public final class Agent {
    private final String name;
    private final String personality;
    private final LMStudioClient client;

    public Agent(String name, String personality, String baseUrl, String model) {
        this.name = name;
        this.personality = personality;
        this.client = new LMStudioClient(baseUrl, model);
    }

    public String chat(String userMessage) {
        Message instructions = new Message("system", "You are " + name + ". " + personality);
        Message question = new Message("user", userMessage);
        try {
            return client.send(List.of(instructions, question)).content();
        } catch (IOException error) {
            return "Could not get a reply: " + error.getMessage();
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            return "The request was interrupted.";
        }
    }
}
