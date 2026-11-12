import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Formatter {
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    private final String template;

    public Formatter() {
        this("[%t] [%l] %n: %m");
    }

    public Formatter(String template) {
        this.template = Objects.requireNonNull(template, "Log format cannot be null");
    }

    public String format(LogLevel messageLevel, String name, String message) {
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

    String expandMessage(String message, Object... values) {
        Objects.requireNonNull(values, "Message values cannot be null");
        if (message.indexOf('%') < 0) {
            return message;
        }

        StringBuilder result = new StringBuilder();
        int valueIndex = 0;
        for (int i = 0; i < message.length(); i++) {
            char current = message.charAt(i);
            if (current != '%') {
                result.append(current);
            } else if (i + 1 < message.length() && message.charAt(i + 1) == '%') {
                // Two percent signs stand for one literal percent.
                result.append('%');
                i++;
            } else if (valueIndex < values.length) {
                result.append(values[valueIndex++]);
            } else {
                // Keep a placeholder when there is no value left.
                result.append('%');
            }
        }
        return result.toString();
    }
}
