import java.util.Scanner;

public class AgentMain {
    private static final String BASE_URL = System.getProperty("lmstudio.url", "http://127.0.0.1:1234");
    private static final String MODEL = System.getProperty("lmstudio.model", "google/gemma-4-e4b");

    public static void main(String[] args) {
        String tier = args.length > 0 ? args[0] : "2";
        String style = args.length > 1 ? args[1] : "plain";
        if (args.length > 2 || !tier.matches("[123]") || !style.matches("plain|pirate")) {
            System.out.println("Usage: AgentMain [1|2|3] [plain|pirate]");
            return;
        }

        Memory memory = switch (tier) {
            case "1" -> Memory.tier1();
            case "3" -> Memory.tier3();
            default -> Memory.tier2();
        };
        String personality = style.equals("pirate")
                ? "Talk like a pirate. Use one short sentence."
                : "Be friendly and clear. Use one short sentence.";

        Logger.setLevel(LogLevel.TRACE);
        Agent agent = new Agent("Alex", personality, memory, BASE_URL, MODEL);
        System.out.println("Chat with Alex. Memory tier " + tier + ". Type quit to exit.");
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
                if (!line.isEmpty()) {
                    System.out.println("Alex> " + agent.chat(line));
                }
            }
        }
    }
}
