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
An emnormous advantage that this data structure offers is that it simplifies the process of designing recursive algorithms. Takes the binary operation `a*b+c` for example: the top level binary operation in this case would be the `+`.

#### The algorithm handles the following simplifiable forms:
BINARY OPERATIONS
```java
(a*b)^#  -> a^#*b^#     a^b*a^c  -> a^(b+c)
a*a^b    -> a^(b+1)     x-x      -> 0
0^0      -> undef       x+x      -> 2*x
0*x      -> 0           x*x      -> x^x
x*x^2    -> x^3         x^0      -> 1
0^x      -> 0
1^x      -> 1
x^1      -> x
a^b^c    -> a^(b*c)
x/1      -> x
0/x      -> 0
0^(-1)   -> undef
x/0      -> undef
```
IRRATIONAL/RATIONAL NUMBERS
```bash
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

