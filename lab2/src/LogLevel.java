import java.util.concurrent.ThreadLocalRandom;

// each level stores a severity and its own random phrases
public enum LogLevel {
    TRACE(
            10,
            "Following program steps",
            "Inspecting execution",
            "Tracking progress"
    ),
    INFO(
            20,
            "Here is your result",
            "Progress update",
            "Event recorded"
    ),
    WARN(
            30,
            "Check this situation",
            "Something needs attention",
            "Proceed with care"
    ),
    ERROR(
            40,
            "Operation failed",
            "Invalid input detected",
            "A problem occurred"
    );

    private final int severity;
    private final String[] phrases;

    // extra phrase arguments become a string array
    LogLevel(int severity, String... phrases) {
        this.severity = severity;
        this.phrases = phrases;
    }

    public boolean allows(LogLevel messageLevel) {
        // accept this minimum severity and higher
        return messageLevel.severity >= this.severity;
    }

    public String randomPhrase() {
        // choose an index from 0 up to phrases.length - 1
        int index = ThreadLocalRandom.current().nextInt(phrases.length);
        return phrases[index];
    }
}
