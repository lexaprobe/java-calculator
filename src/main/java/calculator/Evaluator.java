package calculator;

import calculator.errors.MathError;
import calculator.errors.SyntaxError;
import calculator.tokens.Function;
import calculator.tokens.Operand;
import calculator.tokens.Operator;
import calculator.tokens.Token;
import calculator.tokens.TokenType;
import calculator.tokens.UnaryOperator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

/**
 * The <code>Evaluator</code> class is the core of this calculator. 
 * It is responsible for tokenizing, parsing, and evaluating a given mathematical expression. 
 * To evaluate an expression, simply pass a <code>String</code> to <code>Evaluator.evaluate</code>
 * which will then start the evaluator call chain.
 */
public class Evaluator {

  /**
   * Entry point for evaluating an expression.
   *
   * @param expression a space-seperated mathematical expression
   */
  public static BigDecimal evaluate(String expression) {
    List<Token> infix = tokenize(expression);
    Queue<Token> postfix = toPostfix(infix);
    try {
      return parseExpr(postfix).stripTrailingZeros();
    } catch (NullPointerException e) {
      throw new SyntaxError("empty expression");
    }
  }

  /**
   * Performs the initial tokenization of a given mathematical expression.
   * Each resultant token will either be a <code>Operand</code>, <code>Operator</code>,
   * or a <code>Function</code>.
   *
   * @param expression a space-seperated mathematical expression (in infix notation)
   * @return a tokenized list of the given expression
   * @throws SyntaxError if a character or symbol is unrecognised
   */
  private static List<Token> tokenize(String expression) throws SyntaxError {
    List<Token> tokens = new ArrayList<>();
    String[] expr = expression.split("\\s+");
    for (int i = 0; i < expr.length; i++) {
      String x = expr[i];
      try {
        tokens.add(new Operand(Double.parseDouble(x)));
      } catch (NumberFormatException e) {
        if (x.equals("e")) {
          tokens.add(new Operand(Math.E));
        } else if (x.equals("π")) {
          tokens.add(new Operand(Math.PI));
        } else if (Util.isOperator(x) || Util.isParen(x) || x.equals("!")) {
          // check if unary first
          if (i == 0 || expr[i - 1].equals("(")
          || Util.isOperator(expr[i - 1]) || x.equals("!")) {
            x = x.equals("-") ? "minus" : x;
            tokens.add(new UnaryOperator(x));
          } else {
            tokens.add(new Operator(x));
          }
        } else if (Util.isFunction(x)) {
          tokens.add(new Function(x));
        } else {
          throw new SyntaxError("unrecognised token: '" + x + "'");
        }
      }
    }
    return tokens;
  }

  /**
   * Converts a tokenized infix expression into a postfix expression using an implementation of
   * the <a href="https://en.wikipedia.org/wiki/Shunting_yard_algorithm">Shunting Yard algorithm</a>.
   *
   * @param tokens an infix expression. Must have been tokenized using 
   *      <code>calculator.Evaluator.tokenize</code>
   * @return the associated postfix expression
   * @throws SyntaxError when encountering a circumstantially non-valid token
   */
  private static Queue<Token> toPostfix(List<Token> tokens) throws SyntaxError {
    Queue<Token> postfix = new LinkedList<>();
    Stack<Token> operators = new Stack<>();
    for (Token t : tokens) {
      if (t instanceof Operand) {
        postfix.add(t);
      } else if (t instanceof Function) {
        operators.push(t);
      } else if (Util.isLeftParen(t)) {
        operators.push(t);
      } else if (Util.isRightParen(t)) {
        Token curr;
        while (true) { 
          if (operators.isEmpty()) {
            throw new SyntaxError("unopened parenthesis");
          }
          curr = operators.pop();
          if (Util.isLeftParen(curr)) {
            break;
          } else {
            postfix.add(curr);
          }
        }
      } else if (t instanceof Operator || t instanceof UnaryOperator) {
        if (operators.isEmpty() || Util.isLeftParen(operators.peek())) {
          operators.push(t);
        } else if (t.precedence() > operators.peek().precedence() 
            || (t.precedence() == operators.peek().precedence() && Util.isRightAssociative(t)) 
            || operators.isEmpty() || Util.isLeftParen(t)) {
          operators.push(t);
        } else {
          while (t.precedence() < operators.peek().precedence()
            || (t.precedence() == operators.peek().precedence() && !Util.isRightAssociative(t))) {
            postfix.add(operators.pop());
            if (operators.isEmpty()) {
              break;
            }
          }
          operators.push(t);
        }
      } else {
        throw new SyntaxError("postfix error at token: '" + t.getValue() + "'");
      }
    }
    int stackSize = operators.size();
    for (int i = 0; i < stackSize; i++) {
      postfix.add(operators.pop());
    }
    return postfix;
  }

  /**
   * Parses and evaluates a given postfix expression.
   *
   * @param postfix a postfix expression given as a queue of tokens
   * @return the value of the given expression
   * @throws SyntaxError if parsing results in an unexpected outcome
   * @throws MathError if evaluating results in illegal math operations
   */
  private static BigDecimal parseExpr(Queue<Token> postfix) throws SyntaxError, MathError {
    Stack<BigDecimal> stack = new Stack<>();
    for (Token t : postfix) {
      // add number tokens to the stack
      switch (t.getType()) {
        case TokenType.NUMBER -> {
          BigDecimal value = t.getValue() instanceof BigDecimal n ? n : BigDecimal.ZERO;
          stack.add(value);
        }
        case TokenType.OPERATOR -> {
          if (stack.size() < 2) {
            // each binary operator must have at least two operands
            throw new SyntaxError("bad operand type for operator: '" + t.getValue() + "'");
          }
          BigDecimal right = stack.pop();
          BigDecimal left = stack.pop();
          String operator = t.getValue() instanceof String val ? val : "";
          switch (operator) {
            case "+" -> { stack.add(left.add(right)); }
            case "-" -> { stack.add(left.subtract(right)); }
            case "×" -> { stack.add(left.multiply(right)); }
            case "÷" -> {
              if (isZero(right)) {
                throw new MathError("division by zero: '" + left + "÷" + right + "'");
              }
              stack.add(left.divide(right, 30, RoundingMode.HALF_UP));
            }
            case "(" -> { throw new SyntaxError("unclosed parenthesis"); }
            case "^" -> { 
              if (isOne(left)) {
                stack.add(BigDecimal.ONE);
              } else if (isZero(left)) {
                stack.add(BigDecimal.ZERO);
              } else if (right.compareTo(BigDecimal.valueOf(2147483637)) == 1) {
                throw new MathError("exceeded maximum exponent size");
              } else {
                stack.add(exponentiate(left, right));
              }
            }
            default -> { throw new SyntaxError("unsupported operator: '" + operator + "'"); }
          }
        }
        case TokenType.UNARY -> {
          if (stack.isEmpty()) {
            throw new SyntaxError("no operand found for unary operator");
          }
          BigDecimal value = stack.pop();
          String operator = t.getValue() instanceof String val ? val : "";
          switch (operator) {
            case "minus" -> { stack.add(value.negate()); }
            case "!" -> { stack.add(factorial(value)); }
            case "√" -> { 
              stack.add(value.sqrt(new MathContext(30, RoundingMode.HALF_UP))); 
            }
          }
        }
        case TokenType.FUNCTION -> {
          if (stack.size() < 1) {
            throw new SyntaxError("bad operand type for function: '" + t.getValue() + "'");
          }
          // assuming each input is in degrees for now
          BigDecimal value = stack.pop();
          String function = t.getValue() instanceof String val ? val : "";
          switch (function) {
            case "sin" -> { 
              double angle = Math.toRadians(value.doubleValue());
              stack.add(BigDecimal.valueOf(Math.sin(angle)));
            }
            case "cos" -> {
              double angle = Math.toRadians(value.doubleValue());
              stack.add(BigDecimal.valueOf(Math.cos(angle)));
            }
            case "tan" -> {
              double angle = Math.toRadians(value.doubleValue());
              if (isZero(BigDecimal.valueOf(Math.cos(angle)))) {
                throw new SyntaxError("division by zero: 'tan(" + angle + ")'");
              }
              stack.add(BigDecimal.valueOf(Math.tan(angle)));
            }
            case "ln" -> {
              if (isNegative(value)) {
                throw new MathError("Logarithm of a negative number");
              } else if (isZero(value)) {
                throw new MathError("Logarithm of zero");
              } else if (isOne(value)) {
                stack.add(BigDecimal.ZERO);
              } else if (value.doubleValue() == Math.E) {
                stack.add(BigDecimal.ONE);
              } else {
                stack.add(ln(value));
              }
            }
            case "√" -> {
              stack.add(value.sqrt(new MathContext(30, RoundingMode.HALF_UP)));
            }
            default -> { throw new SyntaxError("unsupported function: '" + function + "'"); }
          }
        }
      }
    }
    if (!stack.isEmpty()) {
      return stack.pop();
    }
    return null;
  }

  /**
   * Simple factorial calculator using <code>java.math.BigDecimal</code>.
   *
   * @param n some integer
   * @return the resultant factorial (n!)
   */
  public static BigDecimal factorial(BigDecimal n) {
    BigDecimal result = BigDecimal.ONE;
    for (int i = 1; i <= n.intValue(); i++) {
      result = result.multiply(BigDecimal.valueOf(i));
    }
    return result;
  }

  /**
   * Perform exponentiation between two BigDeciaml values.
   * @param base
   * @param exp
   * @return result as a BigDecimal
   */
  public static BigDecimal exponentiate(BigDecimal base, BigDecimal exp) {
    double dBase = base.doubleValue();
    double dExp = exp.doubleValue();
    return BigDecimal.valueOf(Math.pow(dBase, dExp));
  }

  /**
   * Retrieve the natural logarithm of a BigDecimal.
   * @param value
   * @return result as a BigDecimal
   */
  public static BigDecimal ln(BigDecimal value) {
    double dValue = value.doubleValue();
    return BigDecimal.valueOf(Math.log(dValue));
  }

  private static boolean isZero(BigDecimal value) {
    return value.compareTo(BigDecimal.ZERO) == 0;
  }

  private static boolean isOne(BigDecimal value) {
    return value.compareTo(BigDecimal.ONE) == 0;
  }

  private static boolean isNegative(BigDecimal value) {
    return value.compareTo(BigDecimal.ZERO) == -1;
  }
}
