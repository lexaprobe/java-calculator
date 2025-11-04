package calculator;

import calculator.tokens.Operator;
import calculator.tokens.Token;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * This class is a collection of utility methods that support the calculator package and its classes.
 */
public class Util {
  private static int maxBdScale = 12;
  private static int maxStrLength = 13;
  private static final Set<String> operators = Set.of(
      "+", "-", "÷", "×", "^"
  );
  private static final Set<String> functions = Set.of(
      "sin", "cos", "tan", "√x", "√", "ln"
  );
  private static final Map<String, String> unarys = Map.of(
    "x!", "!"
  );

  public static int getMaxScale() {
    return maxBdScale;
  }

  public static boolean isOperator(String x) {
    return operators.contains(x.trim());
  }

  public static boolean isUnary(String x) {
    return unarys.containsKey(x.trim());
  }

  public static boolean isFunction(String x) {
    return functions.contains(x.trim());
  }

  public static String[] getActualFunction(String x) {
    if (functions.contains(x.trim()) || unarys.containsKey(x.trim())) {
      switch (x) {
        case "sin" -> { return new String[] {"sin (", ")"}; }
        case "cos" -> { return new String[] {"cos (", ")"}; }
        case "tan" -> { return new String[] {"tan (", ")"}; }
        case "ln" -> { return new String[] {"ln (", ")"}; }
        case "√x" -> { return new String[] {"√"}; }
        case "x!" -> { return new String[] {"!"}; }
      }
    }
    return new String[] {};
  }

  public static boolean isLeftParen(Token token) {
    if (token instanceof Operator o) {
      return o.getValue().equals("(");
    }
    return false;
  }

  public static boolean isRightParen(Token token) {
    if (token instanceof Operator o) {
      return o.getValue().equals(")");
    }
    return false;
  }

  public static boolean isParen(String x) {
    return x.equals("(") || x.equals(")");
  }

  public static boolean isRightAssociative(Token token) {
    if (token instanceof Operator o) {
      return o.getValue().equals("^") || o.getValue().equals("minus");
    }
    return false;
  }

  public static String removeSpaces(String str) {
    String removed = str.replaceAll(" ", "");
    return removed;
  }

  static Map<String, Action> loadActions() {
    Map<String, Action> actions = new HashMap<>();
    for (Action a : Action.values()) {
      actions.put(a.name(), a);
    }
    return actions;
  }

  static boolean previousIsCompatible(String prev) {
    Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
    if (prev == null) {
      return false;
    } else if (prev.equals("(")) {
      return true;
    } else {
      return pattern.matcher(prev).matches();
    }
  }

  static String postfixTracer(Queue<Token> postfix, Stack<Token> operators) {
    StringBuilder sb = new StringBuilder();
    sb.append("Postfix: ");
    sb.append(postfix.toString());
    sb.append("\nStack: ");
    sb.append(operators.toString());
    return sb.toString();
  }

  static String formatBigDecimal(BigDecimal bd) {
    String str = bd.toPlainString();
    if (str.length() > maxStrLength) {
      if (bd.scale() > maxBdScale) {
        // remove unnecessary decimal places
        BigDecimal trunc = bd.setScale(maxBdScale, RoundingMode.HALF_UP);
        str = trunc.toPlainString();
      } else {
        // format large numbers
        str = new DecimalFormat("0.#######E0").format(bd).replace("E", "e");
      }
    }
    return str;
  }

  static void setMaxScale(int scale) {
    Util.maxBdScale = scale;
  }

  static void setMaxLength(int length) {
    Util.maxStrLength = length;
  }
}
