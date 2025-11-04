package calculator;

import calculator.errors.MathError;
import calculator.errors.SyntaxError;
import java.util.Map;
import java.util.HashSet;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class CalculatorApp extends Application {
  private final double scale = 1.5;
  private final TextArea inputDisplay = new TextArea();
  private final TextArea outputDisplay = new TextArea();
  private final Map<String, Action> actions = Util.loadActions();
  private HashSet<Key> buttons = new HashSet<Key>();
  private String[] cache = {"", ""};

  @Override
  public void start(Stage stage) {
    GridPane root = new GridPane();
    root.setPadding(new Insets(10 * scale, 10 * scale, 10 * scale, 10 * scale));
    root.setHgap(5 * scale);
    root.setVgap(5 * scale);
    Font.loadFont(getClass().getResourceAsStream("/fonts/casio_fx_9860gii/casio-fx-9860gii.ttf"), 10);
    Label inputLabel = new Label("INPUT:");
    inputLabel.setLabelFor(inputDisplay);
    inputLabel.setFont(Font.font("Casio fx-9860GII", 11 * scale));
    inputDisplay.setEditable(false);
    inputDisplay.setMinHeight(35 * scale);
    inputDisplay.setFont(Font.font("Casio fx-9860GII", 15 * scale));
    Label outputLabel = new Label("OUTPUT:");
    outputLabel.setFont(Font.font("Casio fx-9860GII", 11 * scale));
    outputLabel.setLabelFor(outputDisplay);
    outputDisplay.setEditable(false);
    outputDisplay.setMinHeight(35 * scale);
    outputDisplay.setFont(Font.font("Casio fx-9860GII", 15 * scale));
    VBox ver = new VBox(5 * scale);
    ver.getChildren().addAll(inputLabel, inputDisplay, outputLabel, outputDisplay);
    root.add(ver, 0, 0, 5, 2);
    String[] extraButtons = {
      "ln", "√x", "x!", "π", "e",
      "sin", "cos", "tan", "(", ")"
    };
    String[] coreButtons = {
      "7", "8", "9", "DEL", "AC",
      "4", "5", "6", "×", "÷",
      "1", "2", "3", "+", "-",
      "0", ".", "^", "ANS", "="
    };
    int row = 2;
    int col = 0;
    for (String text : extraButtons) {
      Key btn = new Key(text);
      btn.setMinSize(50 * scale, 30 * scale);
      btn.setFont(Font.font("Casio fx-9860GII", 11 * scale));
      btn.setOnAction(_ -> handleInput(text));
      btn.setPadding(new Insets(5 * scale, 0 * scale, 5 * scale, 0 * scale));
      root.add(btn, col, row);
      buttons.add(btn);
      col++;
      if (col > 4) {
        col = 0;
        row++;
      }
    }
    for (String text : coreButtons) {
      Key btn = new Key(text);
      btn.setMinSize(50 * scale, 35 * scale);
      btn.setFont(Font.font("Casio fx-9860GII", 15 * scale));
      btn.setOnAction(_ -> handleInput(text));
      btn.setPadding(new Insets(5 * scale, 0 * scale, 5 * scale, 0 * scale));
      root.add(btn, col, row);
      buttons.add(btn);
      col++;
      if (col > 4) {
        col = 0;
        row++;
      }
    }
    Scene scene = new Scene(root, 290 * scale, 395 * scale);
    scene.setOnKeyPressed(e -> {
      for (Key kb : buttons) {
        if (e.getCode().equals(kb.getKeyCode(kb.getText()))) {
          kb.fire();
        }
      }
    });
    stage.setTitle("Scientific Calculator");
    stage.setScene(scene);
    stage.show();
  }

  private void handleInput(String value) {
    value = value.equals("=") ? "EQUALS" : value;
    Action action;
    String output = "";
    if ((action = actions.get(value)) != null) {
      try {
        cache = action.execute(cache);
      } catch (SyntaxError e) {
        outputDisplay.setText("Syntax ERROR");
        System.out.println("Syntax ERROR: " + e.getReason());
        return;
      } catch (MathError e) {
        outputDisplay.setText("Math ERROR");
        System.out.println("Math ERROR: " + e.getReason());
        return;
      }
      if (action == Action.EQUALS) {
        output = cache[1];
      }
    } else if (Util.isOperator(value) || Util.isParen(value)) {
      cache[0] = cache[0] + " " + value + " ";
    } else if (Util.isUnary(value) || Util.isFunction(value)) {
      String[] rep = Util.getActualFunction(value);
      cache[0] = cache[0] + " " + rep[0] + " ";
    } else {
      cache[0] = cache[0] + value;
    }
    // open bracket, other operator/function
    inputDisplay.setText(Util.removeSpaces(cache[0]));
    outputDisplay.setText(Util.removeSpaces(output));
  }
  
  public static void main(String[] args) {
    launch(args);
  }
}