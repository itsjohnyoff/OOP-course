import java.util.Objects;

public class ParenthesesValidator {
    private final Logger logger;

    public ParenthesesValidator() {
        this(new Logger("Parentheses"));
    }

    public ParenthesesValidator(Logger logger) {
        this.logger = Objects.requireNonNull(logger, "Logger cannot be null");
    }

    public boolean isValid(String input) {
        // reject a missing string before accessing its length
        if (input == null) {
            return reportInvalid(input, "Input cannot be null");
        }

        // openings still waiting for a closing match
        int balance = 0;

        // read one character at a time
        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);

            if (current == '(') {
                balance++;

                logger.trace(
                        "Opening parenthesis found at index " + i
                                + "; unmatched openings: " + balance
                );
            } else if (current == ')') {
                // a later opening cannot fix an unmatched closing
                if (balance == 0) {
                    logger.warn(
                            "Closing parenthesis at index " + i
                                    + " has no opening parenthesis"
                    );

                    return reportInvalid(
                            input,
                            "Unmatched closing parenthesis at index " + i
                    );
                }

                // this closing matches one waiting opening
                balance--;

                logger.info("Matching pair completed at index " + i);
            } else {
                return reportInvalid(
                        input,
                        "Unexpected character '" + current
                                + "' at index " + i
                );
            }
        }

        // any remaining openings make the input invalid
        if (balance > 0) {
            return reportInvalid(
                    input,
                    "Unmatched opening parentheses remaining: " + balance
            );
        }

        logger.info("Validation result for \"" + input + "\": true");
        return true;
    }

    // all failure paths log an explanation and return false
    private boolean reportInvalid(String input, String reason) {
        logger.error("Invalid input \"" + input + "\": " + reason);
        logger.info("Validation result for \"" + input + "\": false");

        return false;
    }
}
