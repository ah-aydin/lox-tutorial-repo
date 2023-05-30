package com.kebab;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    public Environment() {
        this.enclosing = null;
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    private void test(){
        for (int i = 0; i < 10; i = i + 1) {
            for (int j = 0; j < 10; j = j + 1) {
                System.out.println(i * 10 + j);
            }
        }

        { // 0
            int i = 0;
            while (i < 10)
            { // 1
                { // 2
                    { // 3
                        int j = 0;
                        while (j < 10)
                        { // 4
                            { // 5
                                System.out.println(i * 10 + j);
                            }
                            j = j + 1;
                        }
                    }
                }
                i = i + 1;
            }
        }
    }
    public void define(String name, Object value) {
        values.put(name, value);
    }

    public Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        } else if (enclosing != null) {
            return enclosing.get(name);
        }
        
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    public void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        } else if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

	public Object getAt(int distance, String name) {
        return ancestor(distance).values.get(name);
	}

	public void assignAt(Integer distance, Token name, Object value) {
        ancestor(distance).values.put(name.lexeme, value);
	}

    private Environment ancestor(int distance) {
        Environment environment = this;
        for (int i = 0; i < distance; ++i) {
            environment = environment.enclosing;
        }
        return environment;
    }
}
