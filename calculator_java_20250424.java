import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.math.MathContext;

public class AdvancedCalculator {
    private static final MathContext PRECISION = new MathContext(10);
    private final List<String> history = new ArrayList<>();
    private double memory = 0;

    public static void main(String[] args) {
        AdvancedCalculator calculator = new AdvancedCalculator();
        calculator.run();
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("=== ADVANCED JAVA CALCULATOR ===");
        printHelp();

        while (running) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            try {
                switch (input.toLowerCase()) {
                    case "exit":
                        running = false;
                        break;
                    case "help":
                        printHelp();
                        break;
                    case "history":
                        printHistory();
                        break;
                    case "clear":
                        clearHistory();
                        System.out.println("History cleared.");
                        break;
                    case "memory":
                        System.out.println("Memory value: " + memory);
                        break;
                    case "clear-memory":
                        memory = 0;
                        System.out.println("Memory cleared.");
                        break;
                    default:
                        if (input.startsWith("mem+")) {
                            handleMemoryOperation(input, true);
                        } else if (input.startsWith("mem-")) {
                            handleMemoryOperation(input, false);
                        } else {
                            double result = evaluateExpression(input);
                            System.out.println("Result: " + result);
                            history.add(input + " = " + result);
                        }
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                history.add(input + " = Error: " + e.getMessage());
            }
        }

        scanner.close();
        System.out.println("Calculator closed.");
    }

    private void handleMemoryOperation(String input, boolean add) {
        String expr = input.substring(4).trim();
        if (expr.isEmpty()) {
            throw new IllegalArgumentException("No expression after mem+ or mem-");
        }
        double value = evaluateExpression(expr);
        memory = add ? memory + value : memory - value;
        System.out.println("Memory updated. New value: " + memory);
        history.add(input + " → Memory = " + memory);
    }

    private double evaluateExpression(String expression) {
        // Handle special constants
        expression = expression.replace("pi", Double.toString(Math.PI))
                             .replace("e", Double.toString(Math.E))
                             .replace("mem", Double.toString(memory));

        // Tokenize the expression
        List<String> tokens = tokenize(expression);

        // Convert to RPN (Reverse Polish Notation)
        List<String> rpn = shuntingYard(tokens);

        // Evaluate RPN
        return evaluateRPN(rpn);
    }

    private List<String> tokenize(String expression) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isWhitespace(c)) {
                continue;
            }

            if (isOperator(c) || c == '(' || c == ')') {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
                tokens.add(String.valueOf(c));
            } else {
                currentToken.append(c);
            }
        }

        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
        }

        return tokens;
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '^' || c == '%';
    }

    private int getPrecedence(String op) {
        switch (op) {
            case "+":
            case "-":
                return 1;
            case "*":
            case "/":
            case "%":
                return 2;
            case "^":
                return 3;
            case "sin":
            case "cos":
            case "tan":
            case "log":
            case "ln":
            case "sqrt":
                return 4;
            default:
                return 0;
        }
    }

    private List<String> shuntingYard(List<String> tokens) {
        List<String> output = new ArrayList<>();
        Stack<String> stack = new Stack<>();

        for (String token : tokens) {
            if (isNumeric(token)) {
                output.add(token);
            } else if (isFunction(token)) {
                stack.push(token);
            } else if (token.equals("(")) {
                stack.push(token);
            } else if (token.equals(")")) {
                while (!stack.isEmpty() && !stack.peek().equals("(")) {
                    output.add(stack.pop());
                }
                stack.pop(); // Remove the '(' from stack
                if (!stack.isEmpty() && isFunction(stack.peek())) {
                    output.add(stack.pop());
                }
            } else if (isOperator(token.charAt(0))) {
                while (!stack.isEmpty() && isOperator(stack.peek().charAt(0)) {
                    if (getPrecedence(token) <= getPrecedence(stack.peek())) {
                        output.add(stack.pop());
                    } else {
                        break;
                    }
                }
                stack.push(token);
            }
        }

        while (!stack.isEmpty()) {
            output.add(stack.pop());
        }

        return output;
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isFunction(String str) {
        return str.equals("sin") || str.equals("cos") || str.equals("tan") || 
               str.equals("log") || str.equals("ln") || str.equals("sqrt");
    }

    private double evaluateRPN(List<String> rpn) {
        Stack<Double> stack = new Stack<>();

        for (String token : rpn) {
            if (isNumeric(token)) {
                stack.push(Double.parseDouble(token));
            } else if (isFunction(token)) {
                if (stack.isEmpty()) {
                    throw new IllegalArgumentException("Not enough operands for function " + token);
                }
                double operand = stack.pop();
                stack.push(applyFunction(token, operand));
            } else if (isOperator(token.charAt(0))) {
                if (stack.size() < 2) {
                    throw new IllegalArgumentException("Not enough operands for operator " + token);
                }
                double right = stack.pop();
                double left = stack.pop();
                stack.push(applyOperator(token.charAt(0), left, right));
            }
        }

        if (stack.size() != 1) {
            throw new IllegalArgumentException("Invalid expression");
        }

        return stack.pop();
    }

    private double applyFunction(String function, double operand) {
        switch (function) {
            case "sin":
                return Math.sin(operand);
            case "cos":
                return Math.cos(operand);
            case "tan":
                return Math.tan(operand);
            case "log":
                if (operand <= 0) {
                    throw new IllegalArgumentException("Logarithm of non-positive number");
                }
                return Math.log10(operand);
            case "ln":
                if (operand <= 0) {
                    throw new IllegalArgumentException("Natural log of non-positive number");
                }
                return Math.log(operand);
            case "sqrt":
                if (operand < 0) {
                    throw new IllegalArgumentException("Square root of negative number");
                }
                return Math.sqrt(operand);
            default:
                throw new IllegalArgumentException("Unknown function: " + function);
        }
    }

    private double applyOperator(char op, double left, double right) {
        switch (op) {
            case '+':
                return left + right;
            case '-':
                return left - right;
            case '*':
                return left * right;
            case '/':
                if (right == 0) {
                    throw new IllegalArgumentException("Division by zero");
                }
                return left / right;
            case '^':
                return Math.pow(left, right);
            case '%':
                return left % right;
            default:
                throw new IllegalArgumentException("Unknown operator: " + op);
        }
    }

    private void printHelp() {
        System.out.println("Available operations:");
        System.out.println("- Basic: +, -, *, /, %, ^ (power)");
        System.out.println("- Functions: sin, cos, tan, log (base 10), ln (natural), sqrt");
        System.out.println("- Constants: pi, e");
        System.out.println("- Memory: mem (current value), mem+<expr>, mem-<expr>, clear-memory");
        System.out.println("- History: history, clear");
        System.out.println("- Other: help, exit");
        System.out.println("Example: (3 + 4) * 2 / sin(pi/2)");
    }

    private void printHistory() {
        if (history.isEmpty()) {
            System.out.println("No history yet.");
        } else {
            System.out.println("Calculation History:");
            for (int i = 0; i < history.size(); i++) {
                System.out.println((i + 1) + ". " + history.get(i));
            }
        }
    }

    private void clearHistory() {
        history.clear();
    }
}