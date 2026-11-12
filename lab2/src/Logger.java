import java.util.Objects;

public class Logger {
    // One minimum level applies to all loggers.
    private static LogLevel level = LogLevel.INFO;

    private final String name;
    private final Formatter formatter;
    private final ConsoleSink sink;

    // Create a default formatter and sink for this logger.
    public Logger(String name) {
        this(name, new Formatter(), new ConsoleSink());
    }

    // Use the formatter and sink supplied by the caller.
    public Logger(String name, Formatter formatter, ConsoleSink sink) {
        this.name = Objects.requireNonNull(name, "Logger name cannot be null");
        this.formatter = Objects.requireNonNull(formatter, "Formatter cannot be null");
        this.sink = Objects.requireNonNull(sink, "Sink cannot be null");
    }

    public static void setLevel(LogLevel newLevel) {
        level = Objects.requireNonNull(newLevel, "Log level cannot be null");
    }

    public void log(LogLevel messageLevel, String message, Object... values) {
        Objects.requireNonNull(messageLevel, "Message level cannot be null");
        Objects.requireNonNull(message, "Message cannot be null");

        // Check the level before formatting.
        if (!level.allows(messageLevel)) {
            return;
        }

        String expanded = formatter.expandMessage(message, values);
        sink.write(formatter.format(messageLevel, name, expanded));
    }

    public void trace(String message, Object... values) {
        log(LogLevel.TRACE, message, values);
    }

    public void info(String message, Object... values) {
        log(LogLevel.INFO, message, values);
    }

    public void warn(String message, Object... values) {
        log(LogLevel.WARN, message, values);
    }

    public void error(String message, Object... values) {
        log(LogLevel.ERROR, message, values);
    }
}
