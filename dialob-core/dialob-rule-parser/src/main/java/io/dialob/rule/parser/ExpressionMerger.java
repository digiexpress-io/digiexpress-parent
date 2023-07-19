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

import io.dialob.rule.parser.node.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ExpressionMerger implements ASTVisitor {

    private CallExprNode parentNode;

    private List<NodeBase> newArguments = new ArrayList<>();

    private int nodeStack = 0;

    private ASTBuilder astBuilder;

    private Predicate<NodeBase> shouldMergePredicate;

    private static <T> Predicate<T> truePredicate() {
        return t -> true;
    }

    ExpressionMerger() {
        this(new ASTBuilder(), truePredicate());
    }

    public ExpressionMerger(ASTBuilder astBuilder) {
        this(astBuilder, truePredicate());
    }

    public ExpressionMerger(Predicate<NodeBase> shouldMergePredicate) {
        this(new ASTBuilder(), shouldMergePredicate);
    }

    private ExpressionMerger(ASTBuilder astBuilder, Predicate<NodeBase> shouldMergePredicate) {
        this.astBuilder = astBuilder;
        this.shouldMergePredicate = shouldMergePredicate;
    }

    private boolean canMerge(CallExprNode parent, CallExprNode sub) {
        if (!shouldMergePredicate.test(sub)) {
            return false;
        }
        NodeOperator parentOperator = parent.getNodeOperator();
        NodeOperator subOperator = sub.getNodeOperator();
        return parentOperator.getCategory() == subOperator.getCategory()
                && parentOperator.getOperator().equals(subOperator.getOperator());
    }

    @Override
    public ASTVisitor visitCallExpr(@NotNull CallExprNode subNode) {
        push(subNode);
        if (parentNode == null) {
            parentNode = subNode;
            astBuilder = astBuilder.callExprNode(subNode.getNodeOperator(), subNode.getValueType(), subNode.getSpan());
            return this;
        } else if (canMerge(parentNode, subNode)) {
            return this;
        }
        subNode.accept(new ExpressionMerger(astBuilder, shouldMergePredicate));
        return null;
    }

    private void push(CallExprNode node) {
        nodeStack++;
    }

    private void pop() {
        nodeStack--;
   }

    @Override
    @NotNull
    public CallExprNode endCallExpr(@NotNull CallExprNode subNode) {
        pop();
        if (nodeStack == 0) {
            for (NodeBase arg : newArguments) {
                if (arg instanceof ConstExprNode) {
                    astBuilder = astBuilder.constExprNode((ConstExprNode) arg).closeExpr();
                } else if (arg instanceof IdExprNode) {
                    astBuilder = astBuilder.idExprNode((IdExprNode) arg).closeExpr();
                }
            }
            newArguments.clear();
        } else if (!canMerge(parentNode, subNode)) {
            astBuilder = astBuilder.closeExpr();
        }
        return subNode;
    }

    public ASTBuilder getAstBuilder() {
        return astBuilder;
    }

    @Override
    @NotNull
    public NodeBase visitConstExpr(@NotNull ConstExprNode subNode) {
        newArguments.add(subNode);
        return subNode;
    }

    @Override
    @NotNull
    public NodeBase visitIdExpr(@NotNull IdExprNode subNode) {
        newArguments.add(subNode);
        return subNode;
    }
}
