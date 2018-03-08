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
The algorithm handles the following simplifiable forms:
1) `(a*b)^#  -> a^#*b^#`
2) `a*a^b    -> a^(b+1)`
3) `0^0      -> undef`
4) `0*x      -> 0`
5) `x*x^2    -> x^3`
6) `0^x      -> 0`
7) `1^x      -> 1`
8) `x^1      -> x`
9) `a^b^c    -> a^(b*c)`
