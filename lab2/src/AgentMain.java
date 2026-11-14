import java.util.Scanner;

public class AgentMain {
    public static void main(String[] args) {
        Agent agent = new Agent("Alex", "Be friendly and clear. Use one short sentence.",
                "http://127.0.0.1:1234", "google/gemma-4-e4b");
        System.out.println("Chat with Alex. Type quit to exit.");
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
