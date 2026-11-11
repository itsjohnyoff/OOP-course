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
}
