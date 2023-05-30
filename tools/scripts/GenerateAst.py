import os
import sys

arguments = sys.argv

if len(arguments) != 2:
    print('Usage: python GenerateAst <output_directory>')
    exit(1)

outputDir = arguments[-1]


def defineAst(outputDir, baseName, types):
    path = os.path.join(outputDir, baseName + ".java")
    print(f'Generating file at {path}')

    with open(path, 'w') as file:
        file.write('package com.kebab;\n')
        file.write('\n')
        file.write('import java.util.List;\n')
        file.write('\n')
        file.write('abstract class ' + baseName + ' {\n')

        # Define visitor
        file.write('\tinterface Visitor<R> {\n')

        for t in types:
            typeName = t.split(':')[0].strip()
            file.write(f'\t\tR visit{typeName}{baseName}({typeName} {baseName.lower()});\n')
        file.write('\t}\n\n')

        # Define types
        for t in types:
            className = t.split(':')[0].strip()
            fields = t.split(':')[1].strip()

            file.write(f'\tstatic class {className} extends {baseName} {{\n')

            for field in fields.split(', '):
                if field == "":
                    continue
                file.write(f'\t\tfinal {field};\n')

            file.write(f'\t\t{className} ({fields}) {{\n')

            toStringList = []
            for field in fields.split(', '):
                if len(field.split(" ")) <= 1:
                    continue
                name = field.split(" ")[1]
                file.write(f'\t\t\tthis.{name} = {name};\n')
                toStringList.append(f"{name}.toString()")
            file.write('\t\t}\n')

            file.write('\t\t@Override\n')
            file.write('\t\t<R> R accept(Visitor<R> visitor) {\n')
            file.write(f'\t\t\treturn visitor.visit{className}{baseName}(this);\n')
            file.write('\t\t}\n')

            file.write('\t\t@Override\n')
            file.write('\t\tpublic String toString() {\n')
            file.write('\t\t\treturn "({}: " + {} + ")";\n'.format(className, ' + " | " + '.join(toStringList)))
            file.write('\t\t}\n')
        # @Override
        # public String toString() {
        #     return "(Assign: " + name.toString() + " | " + value.toString();
        # }
            file.write('\t}\n\n')

        file.write('\n\tabstract <R> R accept(Visitor<R> visitor);\n')
        file.write('\n\tpublic String toString() { return ""; }\n');
        file.write('}')


defineAst(
    outputDir,
    "Expr",
    [
        "Assign: Token name, Expr value",
        "Binary: Expr left, Token operator, Expr right",
        "Ternary: Expr condition, Expr left, Expr right",
        "Call: Expr callee, Token paren, List<Expr> arguments",
        "Grouping: Expr expression",
        "Lambda: List<Token> params, List<Stmt> body",
        "Literal : Object value",
        "Logical : Expr left, Token operator, Expr right",
        "Unary :Token operator, Expr right",
        "Variable: Token name"
    ]
)

defineAst(
    outputDir,
    "Stmt",
    [
        "Block: List<Stmt> statements",
        "Break: Token token",
        "Expression: Expr expression",
        "Function: Token name, List<Token> params, List<Stmt> body",
        "If: Expr condition, Stmt thenBranch, Stmt elseBranch",
        "Print : Expr expression",
        "Return: Token keyword, Expr value",
        "Var: Token name, Expr initializer",
        "While: Expr condition, Stmt body",
    ]
)

