import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Logger {
    // each logger keeps its own name
    private final String name;

    // shared settings for all logger objects
    private static LogLevel level = LogLevel.INFO;
    private static String format = "[%t] [%l] %n: %m";

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    // store the supplied name; null is not accepted
    public Logger(String name) {
        this.name = Objects.requireNonNull(name, "Logger name cannot be null");
    }

    // setters affect existing and future logger objects
    public static void setLevel(LogLevel newLevel) {
        level = Objects.requireNonNull(newLevel, "Log level cannot be null");
    }

    public static void setFormat(String newFormat) {
        format = Objects.requireNonNull(newFormat, "Log format cannot be null");
    }

    public void log(LogLevel messageLevel, String message) {
        Objects.requireNonNull(messageLevel, "Message level cannot be null");
        Objects.requireNonNull(message, "Message cannot be null");

        // hide messages below the minimum level
        if (!level.allows(messageLevel)) {
            return;
        }

        System.out.println(formatMessage(messageLevel, message));
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

    private String formatMessage(LogLevel messageLevel, String message) {
        String template = format;
        String time = LocalTime.now().format(TIME_FORMAT);
        String randomPhrase = null;

        StringBuilder result = new StringBuilder();

        // replace placeholders in the template only
        for (int i = 0; i < template.length(); i++) {
            char current = template.charAt(i);

            // copy ordinary text, including a trailing percent sign
            if (current != '%' || i + 1 >= template.length()) {
                result.append(current);
                continue;
            }

            char placeholder = template.charAt(i + 1);

            switch (placeholder) {
                case 't':
                    result.append(time);
                    break;
                case 'l':
                    result.append(messageLevel.name());
                    break;
                case 'n':
                    result.append(name);
                    break;
                case 'm':
                    result.append(message);
                    break;
                case 'r':
                    // reuse the same phrase within this entry
                    if (randomPhrase == null) {
                        randomPhrase = messageLevel.randomPhrase();
                    }

                    result.append(randomPhrase);
                    break;
                default:
                    result.append('%').append(placeholder);
                    break;
            }

            // skip the placeholder letter already processed
            i++;
        }

        return result.toString();
    }
}