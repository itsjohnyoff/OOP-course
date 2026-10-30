public class Main {
    public static void main(String[] args) {
        // configure all logger objects
        Logger.setLevel(LogLevel.TRACE);
        Logger.setFormat("[%t] [%l] %n: %m | %r");

        // create the object that checks the inputs
        ParenthesesValidator validator = new ParenthesesValidator();
        String[] inputs = {
                "()",
                ")((()))",
                "(",
                "(())((()())())"
        };

        // pass one whole string to the validator each time
        for (String input : inputs) {
            validator.isValid(input);
        }
    }
}
