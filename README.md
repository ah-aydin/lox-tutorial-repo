# Crafting interpreters jlox repo

This is learning repo made following the chapters of http://craftinginterpreters.com/ and the challenges that were given at the end of the chapters.

## Running the thigy

```bash
# Compile
mvn clean pacakge

# Run shell
java -jar target/lox-1.0-SNAPSHOT.jar
# Run file
java -jar target/lox-1.0-SNAPSHOT.jar ./test.lox
```

## Grammar rules
```python
# Statement grammar
program -> declaration* EOF
declaration -> classDecl | funcDecl | varDecl | statement
classDecl -> "class" IDENTIFIER "{" (function | staticFunc)* "}"
funcDecl -> "func" function
staticFunc -> "static" function
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
assignment -> (call ".")? IDENTIFIER "=" assignment | ternay | lambda
lambda -> "[" parameters? "]" block
ternary -> logic_or ("?" logic_or ":" logic_or)?
logic_or -> logic_and ("or" logic_and)*
logic_and -> equality ("and" equality)*
equality -> comparison ( ("==" | "!=") comparison)*
comparison -> term ( ("<" | ">" | "<=" | ">=") term)*
term -> factor ( ("+" | "-") factor)*
factor -> unary ( ("*" | "/") unary)*
unary -> ("!" | "-") unary | call
call -> primary ("(" arguments? ")" | "." IDENTIFIER )*
arguments -> ternary (",", ternary)*
primary -> NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" | IDENTIFIER
```
