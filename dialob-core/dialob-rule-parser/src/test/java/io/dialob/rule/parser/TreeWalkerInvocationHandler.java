/*
 * Copyright © 2015 - 2021 ReSys (info@dialob.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dialob.rule.parser;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public  class TreeWalkerInvocationHandler implements InvocationHandler {

    int level = 0;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        if ("enterEveryRule".equals(name) || "exitEveryRule".equals(name)) {
            return null;
        }
        boolean enter = false;
        if (name.startsWith("enter")) {
            name = name.substring(5);
            enter = true;
            level++;
        } else if (name.startsWith("exit")) {
            name = name.substring(4);
        } else if (name.equals("visitTerminal")) {
            return null;
        }
        ParseTree node = (ParseTree) args[0];
        printNode(enter, name, node);

        if (!enter) {
            level--;
        }
        return null;
    }

    protected void printNode(boolean enter, String ruleName, ParseTree node) throws IllegalAccessException {
        if (!enter) {
            return;
        }
        String op = findOp(node);
        String expr = findExpr(node);
        String left = findLeft(node);
        String right = findRight(node);
        String var = findVar(node);
        String value = findValue(node);

        String argstring = "";

        if (left != null || right != null) {
            argstring = "(" + op + " " + left + " " + right + ")";
        } else if (op != null) {
            argstring = "(" + op + ")";
        } else if (expr != null) {
            argstring = "(" + expr + ")";
        } else if (value != null) {
            argstring = "(" + value + ")";
        } else if (var != null) {
            argstring = "(" + var + ")";
        }

        System.out.println("                         ".substring(0, level) + ruleName + argstring);
    }

    protected String findOp(ParseTree node) throws IllegalAccessException {
        return findField(node,"op");
    }

    protected String findLeft(ParseTree node) throws IllegalAccessException {
        return findField(node,"left");
    }

    protected String findRight(ParseTree node) throws IllegalAccessException {
        return findField(node,"right");
    }

    protected String findExpr(ParseTree node) throws IllegalAccessException {
        return findField(node,"expr");
    }
    protected String findUnit(ParseTree node) throws IllegalAccessException {
        return findField(node,"unit");
    }

    protected String findVar(ParseTree node) throws IllegalAccessException {
        return findField(node,"var");
    }
    protected String findFunc(ParseTree node) throws IllegalAccessException {
        return findField(node,"func");
    }

    protected String findValue(ParseTree node) throws IllegalAccessException {
        return findField(node,"stringValue");
    }

    protected String findField(ParseTree node, String fieldName) throws IllegalAccessException {
        Object op = null;
        try {
            Field opField = node.getClass().getField(fieldName);
            op = opField.get(node);
            if (op instanceof Token) {
                return ((Token)op).getText();
            }
            if (op instanceof ParseTree) {
                return ((ParseTree)op).getText();
            }
            if (op == null) {
                return null;
            }
            return "<ERROR>";
        } catch (NoSuchFieldException e) {

        }
        return null;
    }
}
