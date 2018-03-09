# J Math Context (JGrapher)
An original but powerful math context library with experimental CAS capabilities.
## What's new
Epic update to JGrapher - introducing multi-variable graphing capability!
Type in `x^a*cos(b*x)` and see what happens along the way! Play around with the sliders.
Press `[TAB]` to hide/unhide function input
Immense improvements to the CAS, though not as powerful, it is no longer considered experimental with bugs now gone and inheritance optimized.
Completely original & intuitive way of performing algebra manipulations by an ORIGINAL composite tree structure that proved to be immensely powerful and ingenious.
## Computer Algebra System (CAS)
Powerful and bug-free multi-variable computer algebra system. Right now it handles simplification of many forms and convertion of output expression to a more human readable form. 
### Simplification
The simplification algorithm is based on a **composite binary tree**, an original data structure.
```
 e.g.           BinOp           would represent the expression          (ln(3^x)*(3/4) + (5/e+2.5))
              /       \                                                   /                  \
          BinOp        BinOp                                        ln(3^x) * (3/4)        5/e + 2.5
          /   \        /    \                                         /          \          /     \
        UOp   Frac   BinOp   Raw                                  ln(3^x)       (3/4)     5/e     2.5
         |           /    \                                          |                    / \
        BinOp      Const  Var                                       3^x                  5   e
        /   \                                                       / \
      Raw   Var                                                    3   x
        
```
#### How does it work?
An emnormous advantage that this data structure offers is that it simplifies the process of designing recursive algorithms. Takes the binary operation `a*b+c` for example: the top level binary operation in this case would be the `+`. The following code segment demonstrates how the recursive simplification works.
```java
BinaryOperation mult = new BinaryOperation(new Variable("a"), "*", new Variable("a"));
BinaryOperation exp = new BinaryOperation(new Variable("a"), "^", new RawValue(2));
BinaryOperation add = new BinaryOperation(mult, "+", exp);
System.out.println(add); // prints "a*a+a^2"
```
Call to `add.simplify()` will subsequently invoke `mult.simplify()`, which takes `a*a` and returns `a^2` which then produces the expression `a^2+a^2`, which is then simplified to `2*a^2` by invocation of `this.simplify()`.
```java
System.out.println(add.clone().simplify()); // produces "2*a^2"
```
Fortunately, you don't have to create mathematical expressions using JMC by creating algebraic operations one at a time. That would be extremely painful, slow, and buggy. The JMC library does all the hard parts for you! The expression `a*a+a^2` from the example above could also be created by using the `interpret(String exp)` of the `Expression` class.
```java
Operable op = Expression.interpret("a*a+a^2"); // constructs the binary operation tree.
System.out.println(op.clone().simplify(); // prints "2*a^2"
```
You can also set `Mode.DEBUG = true` to see how it actually performs the interpretation. `Expression.interpret("5+7-log<(11)>+e^2")` with debug on produces the following:
```
formatted input: 5+7-log(11)+e^2
exp:	11
func:	5+7-log<&0>+e^2
exp:	5+7-log<&0>+e^2
exp:	&0
unary:	5+7-log<&0>+e^2
->	5+7-#0+#1
->	#2-#0+#1
->	#3+#1
->	#4
output:	5+7-log(11)+e^2
```

To reduce the complexity of the simplification process, the algorithm will first attempt fundamental operations (arithmetic calculations of rational/irrational numbers, special cases like `x^0` and `0/x`). Then, it converts the mathematic expression to additional only exponential form. For example:
```java
BinaryOperation binOp = (BinaryOperation) Expression.interpret("x*(a-b)/(c+d)^3");
System.out.println(binOp); // prints "x*(a-b)/(c+d)^3"
binOp.toAdditionOnly(); // converts the expression to additional only form
System.out.println(binOp); // prints "x*(a+(-1)*b)/(c+d)^3"
binOp.toExponentialForm(); // converts the expression to exponential form
System.out.println(binOp); // prints "x*(a+(-1)*b)*(c+d)^(-3)"
```
The expression `x*(a+(-1)*b)*(c+d)^(-3)` doesn't look much better than `x*(a-b)/(c+d)^3`, however, although it is considered "much simpler" by the computer as now it only needs to worry about handling `+,^,*` instead of `+,-,*,/,^`. However, to make it more readable to human eyes, it needs to be "beautified." You can do this by invoking `Operable::beautify`, which works by reversing the doings of negative exponentiation and addition. Consider the expression `a/3*2.5/n*b^2.5*a/4`:
```java
Operable op = Expression.interpret("a/3*2.5/n*b^2.5*a/4");
System.out.println(op.clone().simplify()); // prints "a^2*n^(-1)*b^2.5*(5/24)"
System.out.println(op.clone().simplify().beautify()); // prints "a^2*b^2.5*5/(n*24)"
```
#### The algorithm handles the following simplifiable forms:

> Rational/Irrational Numbers
```css
3^(-3)        -> (1/27)
(2/3)^(-1/3)  -> (1/2)*3^(1/3)*2^(2/3)
3^(-1/3)      -> (1/3)*3^(2/3)
(3/2)^(-2/3)  -> (1/9)*4^(1/3)*9^(2/3)
7/8*2         -> (7/4)
2/5+3/7       -> (29/35)
2*5/7         -> (10/7)
3/4*(5/7)     -> (15/28)
3.5/4.7^2     -> (350/2209)
```
> Binary Operations

Original Exp |   -> | Simplified |   |  Original Exp   |   -> | Simplified |   |  Original Exp   |   -> | Simplified 
:-----------:| ---- |:----------:| - |:---------------:| ---- |:----------:| - |:---------------:| ---- |:----------:
(a*b)^#      |   -> | a^#*b^#    |   |     a^b*a^c     |   -> | a^(b+c)    |   |      x*x        |   -> | x^x
a*a^b        |   -> | a^(b+1)    |   |      x-x        |   -> | 0          |   |      x/0        |   -> | undef
0^0          |   -> | undef      |   |      x+x        |   -> | 2*x        |   |      0/x        |   -> | 0
0*x          |   -> | 0          |   |      x^0        |   -> | 1          |   |      0^(-1)     |   -> | undef 
x*x^2        |   -> | x^3        |   |      a^b^c      |   -> | a^(b*c)    |   |      x/1        |   -> | x          
0^x          |   -> | 0          |   |      1^x        |   -> | 1          |   |      x^1        |   -> | x          



### Extensibility
The JMC framework is by no means limited to standard mathematical operations. It is built to be extensible. I made it fairly easy to implement customized binary/unary operations. Just be aware that introducing custom operations would compromise CAS capabilities. (However it is possible to subclass `BinaryOperation` and implement your own simplfication mechanism.) The following section demonstrates how to incorporate user-defined operations into the powerful JMC computer algebra system.
#### Binary Operation
BinaryOperation has a private nested class `RegisteredBinaryOperation` that is invisible outside of the `jmc.cas` package. It conforms to interface `BinEvaluable`, which specifies only one method `double eval(double a, double b)`. This enables it to take advantage of the **lambda expression**. If you are not already familiar with lambda, take a look [here](https://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html "Official Java Documentation"). To define a custom binary operation:
```java
BinaryOperation.define("%", 2, (a, b) -> a % b); // defines the modular binary operation (which is nonstandard)
```
The first argument is the symbolic representation of binary operation. It can be any `String` that contains a single symbol. The second argument is the **priority** of the operation. The priority defines the order of binary operations - it can either be either `1`,`2`, or `3` with 3 being the highest. Addition and subtraction are of **priority 1** (lowest), while multiplification and division are of **priority 2** and exponentiation having **priority 3** (highest). In the code segment above, the `%` is defined to be having the same priority as `*` and `/`. The third argument is of type `BinEvaluable`. You can do any operation with the left/right operand as long as a double is returned.
```java
System.out.println(Expression.interpret("x % 3").eval(5)); // 5 % 3 = 2, prints "2.0"
```
#### Unary Operation
Similar to `BinaryOperation`, `UnaryOperation` has a private nested class `RegisteredUnaryOperation`. Refer to BinaryOperation for how it works. Here's how to use it:
```java
UnaryOperation.define("digits", x -> Integer.toString((int) x).length()); // an unary operation that calculates the numer of digits in the integer part of a number.
```
The first argument is the name of the unary operation, like `log`, `cos`, etc. The name could only contain letters `[a-Z]` and **must has more than 2 characters**. Single letters are reserved for variable names. The second argument is of type `Evaluable`, which must takes in a double and return a double.
```java
Expression.interpret("digits(x)^2").eval(1234) // returns 16 since there are 4 digits in 1234 and 4^2 = 16.
```
#### Constants
Aside from declaring custom binary/unary operations, you can also define constants. Constants in JMC behave differently from what you would expect, however, and here's how it works. All of the constants are managed under the `Constants` class, which contains 2 subclasses, `Constant` and `ComputedConst` with `Constant` being a nested class that is a subclass of `Variable` and `ComputedConst` being a nested interface. The `ComputedConst` interface declares a single method `double compute()` and is utilized by `Constant` to compute a value. Here is how it works in practice:
```java
Constants.define("π", () -> Math.PI); // a constant having the value π. ("pi" is the default name for π in JMC)
Constants.define("seed", Math::random); // a "dynamic constant" that returns a random value between 0 and 1 when evaluated
System.out.println(Constants.valueOf("π")) // prints 3.14159265357659...
System.out.println(Expression.interpret("seed*2-1").val()) // prints a random number between -1 and 1.
```

### What can it do?
As of now, the CAS will attempt to convert any decimal numbers to fractions when needed during the simplification phase. It handles the following senarios:

* Commutative arithmatic & algebra
* Nested unary operation simplification
* Algebraic domain validation
* Trignometric simplfication
* Logarithmic exponential simplification
* Basic irrational/rational number arithmetic

For detailed documentation of the simplifiable expressions, please refer to **simplifiable forms** under the **Simplification** section. 

