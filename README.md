# Crafting interpreters jlox repo

This is learning repo made following the code from http://craftinginterpreters.com/ and the challenges that were given at the end of the chapters.

## Grammar rules
```
# Statement grammar
program -> declaration* EOF
declaration -> varDecl | statement
varDecl -> "var" IDENTFIER ("=" expression)? ";"
statement -> exprStmt | printStmt | block
block -> "{" declaration* "}"
exprStmt -> expression ";"
printStmt -> "print" expression ";"

# Expression grammar
expression -> assignment ("," assignment)*
assignment -> IDENTIFIER "=" assignment | ternary
ternary -> equality ("?" equality ":" equality)?
equality -> comparison ( ("==" | "!=") comparison)*
comparison -> term ( ("<" | ">" | "<=" | ">=") term)*
term -> factor ( ("+" | "-") factor)*
factor -> unary ( ("*" | "/") unary)*
unary -> ("!" | "-") unary | primary
primary -> NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" | IDENTIFIER
```
