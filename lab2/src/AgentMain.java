import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AgentMain {
    public static void main(String[] args) throws IOException, InterruptedException {
        LMStudioClient client = new LMStudioClient("http://127.0.0.1:1234", "google/gemma-4-e4b");
        LMResponse reply = client.send(List.of(new Message("user", "Say hi in five words.")));
        System.out.println(reply.content());
        System.out.printf(Locale.ROOT, "Prompt: %d | Completion: %d | Time: %.2f s%n",
                reply.promptTokens(), reply.completionTokens(), reply.elapsedSeconds());
    }
}
