import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Toolbox {
    private static final DateTimeFormatter CLOCK_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String NUMBER = "[+-]?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)";
    private static final Pattern EXPRESSION = Pattern.compile(
            "\\s*(" + NUMBER + ")\\s*([+*/-])\\s*(" + NUMBER + ")\\s*");

    private final Logger logger = new Logger("Toolbox");

    public String run(String toolName, String input) {
        try {
            return switch (toolName) {
                case "get_time" -> LocalTime.now().format(CLOCK_FORMAT);
                case "get_date" -> LocalDate.now().toString();
                case "calculator" -> calculate(input);
                default -> throw new IllegalArgumentException("Unknown tool: " + toolName);
            };
        } catch (IllegalArgumentException | ArithmeticException error) {
            logger.error("Tool % failed: %", toolName, error.getMessage());
            throw error;
        }
    }

    private String calculate(String input) {
        // Read two numbers and one operator.
        Matcher expression = EXPRESSION.matcher(input);
        if (input.length() > 100 || !expression.matches()) {
            throw new IllegalArgumentException("Use two numbers and one of +, -, *, or /.");
        }

        BigDecimal left = new BigDecimal(expression.group(1));
        BigDecimal right = new BigDecimal(expression.group(3));
        BigDecimal result = switch (expression.group(2).charAt(0)) {
            case '+' -> left.add(right);
            case '-' -> left.subtract(right);
            case '*' -> left.multiply(right);
            case '/' -> left.divide(right, MathContext.DECIMAL64);
            default -> throw new IllegalStateException("Invalid operator");
        };
        return result.stripTrailingZeros().toPlainString();
    }
}
