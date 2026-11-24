import java.io.IOException;
import java.util.Scanner;

public class AgentMain {
    private static final String BASE_URL = System.getProperty("lmstudio.url", "http://127.0.0.1:1234");
    private static final String MODEL = System.getProperty("lmstudio.model", "google/gemma-4-e4b");

    public static void main(String[] args) {
        String tier = args.length > 0 ? args[0] : "2";
        String style = args.length > 1 ? args[1] : "plain";
        if (args.length > 2 || !tier.matches("[123]") || !style.matches("plain|pirate|caveman")) {
            System.out.println("Usage: AgentMain [1|2|3] [plain|pirate|caveman]");
            return;
        }

        Memory memory = switch (tier) {
            case "1" -> Memory.tier1();
            case "3" -> Memory.tier3();
            default -> Memory.tier2();
        };
        String personality = switch (style) {
            case "pirate" -> "Talk like a pirate. Use one short sentence.";
            case "caveman" -> "Talk like a caveman. Use short, simple words, like 'Me see fire. Fire warm.'";
            default -> "Be friendly and clear. Use one short sentence.";
        };

        Logger.setLevel(LogLevel.TRACE);
        showToolChecks();
        Logger chatLog = new Logger("Chat");
        Agent agent = new Agent("Alex", personality, memory, BASE_URL, MODEL);
        System.out.println("Chat with Alex. Memory tier " + tier + ". Style: " + style + ". Type quit to exit.");
        try (Scanner input = new Scanner(System.in)) {
            while (!Thread.currentThread().isInterrupted()) {
                System.out.print("You> ");
                if (!input.hasNextLine()) {
                    break;
                }
                String line = input.nextLine().strip();
                if (line.equalsIgnoreCase("quit")) {
                    break;
                }
                if (line.isEmpty()) {
                    continue;
                }
                try {
                    System.out.println("Alex> " + agent.chat(line));
                } catch (IOException error) {
                    // A failed question should leave the chat open.
                    chatLog.error("%", error.getMessage());
                    System.out.println("Alex> I couldn't get a reply. See the error above.");
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    chatLog.error("The LM Studio request was interrupted.");
                    System.out.println("Alex> The request was interrupted.");
                }
            }
        }
    }

    private static void showToolChecks() {
        Toolbox toolbox = new Toolbox();
        System.out.println("Tool checks before chat (the last three should fail):");
        showToolCheck(toolbox, "calculator", "12 * 7");
        showToolCheck(toolbox, "get_date", "");
        showToolCheck(toolbox, "get_time", "");
        showToolCheck(toolbox, "calculator", "1 / 0");
        showToolCheck(toolbox, "calculator", "abc + 2");
        showToolCheck(toolbox, "unknown", "");
        System.out.println();
    }

    private static void showToolCheck(Toolbox toolbox, String tool, String input) {
        String label = input.isEmpty() ? tool : tool + " (" + input + ")";
        System.out.print(label + ": ");
        try {
            System.out.println(toolbox.run(tool, input));
        } catch (IllegalArgumentException | ArithmeticException error) {
            // Toolbox already printed the reason. Continue to the next check.
        }
    }
}
