# Crafting interpreters jlox repo

This is learning repo made following the code from http://craftinginterpreters.com/ and the challenges that were given at the end of the chapters.

## Grammar rules
```
# Statement grammar
program -> declaration* EOF
declaration -> funcDecl | varDecl | statement
funcDecl -> "func" function
function -> IDENTIFIER "(" parameters? ")" block
parameters -> IDENTIFIER ("," IDENTIFIER )*
varDecl -> "var" IDENTFIER ("=" expression)? ";"
statement -> exprStmt | printStmt | block | ifStmt | whileStmt | forStmt | breakStmt
breakStmt -> "break" ";"
forStmt -> "for" "(" (varDecl | exprStmt | ";") expression? ";" expression? ")" statement
whileStmt -> "while" | "(" expression ")" statement
block -> "{" declaration* "}"
exprStmt -> expression ";"
printStmt -> "print" expression ";"
ifStmt -> "if" "(" expression ")" statement ("else" statement)?

# Expression grammar
expression -> assignment ("," assignment)*
assignment -> IDENTIFIER "=" assignment | ternary
ternary -> logic_or ("?" logic_or ":" logic_or)?
logic_or -> logic_and ("or" logic_and)*
logic_and -> equality ("and" equality)*
equality -> comparison ( ("==" | "!=") comparison)*
comparison -> term ( ("<" | ">" | "<=" | ">=") term)*
term -> factor ( ("+" | "-") factor)*
factor -> unary ( ("*" | "/") unary)*
unary -> ("!" | "-") unary | call
call -> primary ("(" arguments? ")")*
arguments -> ternary (",", ternary)*
primary -> NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" | IDENTIFIER
```
