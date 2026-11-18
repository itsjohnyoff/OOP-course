import java.util.ArrayList;
import java.util.List;

public final class Memory {
    private final int capacity;
    private final List<Message> messages = new ArrayList<>();

    private Memory(int capacity) {
        this.capacity = capacity;
    }

    public static Memory tier1() {
        return new Memory(2);
    }

    public static Memory tier2() {
        return new Memory(5);
    }

    public static Memory tier3() {
        return new Memory(10);
    }

    public boolean addExchange(Message question, Message answer) {
        // Capacity counts full exchanges, so we drop both messages together.
        boolean full = messages.size() == capacity * 2;
        if (full) {
            messages.subList(0, 2).clear();
        }
        messages.add(question);
        messages.add(answer);
        return full;
    }

    public List<Message> getMessages() {
        return List.copyOf(messages);
    }
}
