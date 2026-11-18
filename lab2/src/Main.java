public class Main {
    public static void main(String[] args) {
        Logger.setLevel(LogLevel.TRACE);

        Formatter sharedFormat = new Formatter("[%t] [%l] %n: %m | %r");
        ConsoleSink console = new ConsoleSink();

        // The input and validator logs share a formatter and sink.
        Logger inputLog = new Logger("Input", sharedFormat, console);
        Logger validatorLog = new Logger("Parentheses", sharedFormat, console);
        ParenthesesValidator validator = new ParenthesesValidator(validatorLog);

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
