public class Main {
    public static void main(String[] args) {
        Logger.setLevel(LogLevel.TRACE);

        Formatter sharedFormat = new Formatter("[%t] [%l] %n: %m | %r");
        ConsoleSink console = new ConsoleSink();

        // The input and validator logs share a formatter and sink.
        Logger inputLog = new Logger("Input", sharedFormat, console);
        Logger validatorLog = new Logger("Parentheses", sharedFormat, console);
        ParenthesesValidator validator = new ParenthesesValidator(validatorLog);

        inputLog.info("Bonus examples");
        inputLog.info("One value for two spots: % and %", 3);
        inputLog.info("One spot for two values: %", 3, 7);
        inputLog.info("Progress: 100%%");
        // The percent sign is part of the message here.
        inputLog.info("Ratio is 100%n");

        String[] inputs = {"()", ")((()))", "(", "(())((()())())"};
        inputLog.info("Checking % strings", inputs.length);

        int validCount = 0;
        for (String input : inputs) {
            if (validator.isValid(input)) {
                validCount++;
            }
        }

        Logger summaryLog = new Logger("Summary");
        summaryLog.info("% of % strings are valid", validCount, inputs.length);
    }
}
