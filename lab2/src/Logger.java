import java.util.Objects;

public class Logger {
    // each logger keeps its own name
    private final String name;

    // One minimum level applies to all loggers.
    private static LogLevel level = LogLevel.INFO;
    private final Formatter formatter = new Formatter();

    // store the supplied name; null is not accepted
    public Logger(String name) {
        this.name = Objects.requireNonNull(name, "Logger name cannot be null");
    }

    // setters affect existing and future logger objects
    public static void setLevel(LogLevel newLevel) {
        level = Objects.requireNonNull(newLevel, "Log level cannot be null");
    }

    public static void setFormat(String newFormat) {
        Formatter.setFormat(newFormat);
    }

    public void log(LogLevel messageLevel, String message) {
        Objects.requireNonNull(messageLevel, "Message level cannot be null");
        Objects.requireNonNull(message, "Message cannot be null");

        // hide messages below the minimum level
        if (!level.allows(messageLevel)) {
            return;
        }

        System.out.println(formatter.format(messageLevel, name, message));
    }

    // these methods supply the message level to log()
    public void trace(String message) {
        log(LogLevel.TRACE, message);
    }

    public void info(String message) {
        log(LogLevel.INFO, message);
    }

    public void warn(String message) {
        log(LogLevel.WARN, message);
    }

    public void error(String message) {
        log(LogLevel.ERROR, message);
    }
}
