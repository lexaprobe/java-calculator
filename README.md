# Simple Calculator

A simple scientific calculator built with Gradle and javafx.
Loosely based on the Casio fx-82.

## Features
- Capable of handling very large calculations thanks to [BigDecimal](https://docs.oracle.com/javase/8/docs/api/java/math/BigDecimal.html)
- Unary operations (factorials, negative numbers, sqaure roots)
- Trigonometric functions (sin, cos, tan)
    - N.B. the precision on these is a little off - I probably wouldn't trust them
- Natural logarithm function
- Parenthesis nesting
- Plus all the basic operators and operands that appear on a normal calculator 

## How to build and run
On macOS/Linux: `./gradlew run`  
On Windows: `gradlew.bat run`

## Resources
Font used: [Casio fx-9860GII](https://www.dafont.com/casio-fx-9860gii.font)