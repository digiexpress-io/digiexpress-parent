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
package io.dialob.rule.parser.modifier;

import io.dialob.rule.parser.AstMatcher;
import io.dialob.rule.parser.CloneVisitor;
import io.dialob.rule.parser.api.ValueType;
import io.dialob.rule.parser.node.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;

public class ModifyingMinifierVisitor extends AstMatcher {

    {
        whenMatches(callNode(operator(or(is("+"),is("-"),is("*"),is("neg"))).and(args(allMatches(constNode(valueType(is(ValueType.INTEGER))))))), node -> {
            final CallExprNode callNode = (CallExprNode) node;
            final List<NodeBase> arguments = callNode.getSubnodes();
            if (arguments.isEmpty()) {
                return null;
            }
            if ("neg".equals(callNode.getNodeOperator().getOperator())) {
                assert arguments.size() == 1;
                ConstExprNode constExprNode = (ConstExprNode) arguments.get(0);
                return new ConstExprNode(node.getParent(), Integer.toString(-((Integer)constExprNode.getAsValueType())), null, ValueType.INTEGER, node.getSpan());
            }

            Integer sum = null;
            BinaryOperator<Integer> op = null;
            if (callNode.getNodeOperator().isPlusOp()) {
                op = (integer, integer2) -> integer != null && integer2 != null ? integer + integer2 : null;
            } else if (callNode.getNodeOperator().isMinusOp()) {
                op = (integer, integer2) -> integer != null && integer2 != null ? integer - integer2 : null;
            } else if (callNode.getNodeOperator().isMultOp()) {
                op = (integer, integer2) -> integer != null && integer2 != null ? integer * integer2 : null;
            }
            for (NodeBase arg : arguments) {
                Object value = ((ConstExprNode) arg).getAsValueType();
                if (sum == null) {
                    sum = (Integer)value;
                } else {
                    sum = op.apply(sum,(Integer)value);
                }
            }
            return new ConstExprNode(node.getParent(), Integer.toString(sum), null, ValueType.INTEGER, node.getSpan());
        });


      whenMatches(callNode(operator(or(is("+"),is("-"))).and(args(allMatches(constNode(valueType(is(ValueType.PERIOD))))))), node -> {
        final CallExprNode callNode = (CallExprNode) node;
        final List<NodeBase> arguments = callNode.getSubnodes();
        if (arguments.isEmpty()) {
          return null;
        }
        if ("neg".equals(callNode.getNodeOperator().getOperator())) {
          assert arguments.size() == 1;
          ConstExprNode constExprNode = (ConstExprNode) arguments.get(0);
          return new ConstExprNode(node.getParent(), ((Period)constExprNode.getAsValueType()).negated().toString(), null, ValueType.PERIOD, node.getSpan());
        }

        Period sum = null;
        BinaryOperator<Period> op = null;
        if (callNode.getNodeOperator().isPlusOp()) {
          op = (period, period2) -> period != null && period2 != null ? period.plus(period2).normalized() : null;
        } else if (callNode.getNodeOperator().isMinusOp()) {
          op = (period, period2) -> period != null && period2 != null ? period.minus(period2).normalized() : null;
        }
        for (NodeBase arg : arguments) {
          Object value = ((ConstExprNode) arg).getAsValueType();
          if (sum == null) {
            sum = (Period)value;
          } else {
            sum = op.apply(sum,(Period)value);
          }
        }
        return new ConstExprNode(node.getParent(), sum.toString(), null, ValueType.PERIOD, node.getSpan());
      });


      whenMatches(callNode(operator(or(is("+"),is("-"))).and(args(allMatches(constNode(valueType(is(ValueType.DURATION))))))), node -> {
        final CallExprNode callNode = (CallExprNode) node;
        final List<NodeBase> arguments = callNode.getSubnodes();
        if (arguments.isEmpty()) {
          return null;
        }
        if ("neg".equals(callNode.getNodeOperator().getOperator())) {
          assert arguments.size() == 1;
          ConstExprNode constExprNode = (ConstExprNode) arguments.get(0);
          return new ConstExprNode(node.getParent(), ((Duration)constExprNode.getAsValueType()).negated().toString(), null, ValueType.DURATION, node.getSpan());
        }

        Duration sum = null;
        BinaryOperator<Duration> op = null;
        if (callNode.getNodeOperator().isPlusOp()) {
          op = (duration, duration2) -> duration != null && duration2 != null ? duration.plus(duration2) : null;
        } else if (callNode.getNodeOperator().isMinusOp()) {
          op = (duration, duration2) -> duration != null && duration2 != null ? duration.minus(duration2) : null;
        }
        for (NodeBase arg : arguments) {
          Object value = ((ConstExprNode) arg).getAsValueType();
          if (sum == null) {
            sum = (Duration)value;
          } else {
            sum = op.apply(sum,(Duration)value);
          }
        }
        return new ConstExprNode(node.getParent(), sum.toString(), null, ValueType.DURATION, node.getSpan());
      });



      whenMatches(callNode(operator(or(is("+"),is("-"),is("*"),is("/"),is("neg"))).and(args(allMatches(constNode(valueType(or(is(ValueType.INTEGER), is(ValueType.DECIMAL)))))))), node -> {
            BinaryOperator<BigDecimal> op = null;
            final CallExprNode callNode = (CallExprNode) node;
            final List<NodeBase> arguments = callNode.getSubnodes();
            BigDecimal sum = null;
            if ("neg".equals(callNode.getNodeOperator().getOperator())) {
              assert arguments.size() == 1;
                ConstExprNode constExprNode = (ConstExprNode) arguments.get(0);
                return new ConstExprNode(node.getParent(), ((BigDecimal)constExprNode.getAsValueType()).negate().toString(), null, ValueType.DECIMAL, node.getSpan());
            }

            if (callNode.getNodeOperator().isPlusOp()) {
                op = (decimal, decimal2) -> decimal != null && decimal2 != null ? decimal.add(decimal2) : null;
            } else if (callNode.getNodeOperator().isMinusOp()) {
                op = (decimal, decimal2) -> decimal != null && decimal2 != null ? decimal.subtract(decimal2) : null;
            } else if (callNode.getNodeOperator().isMultOp()) {
                op = (decimal, decimal2) -> decimal != null && decimal2 != null ? decimal.multiply(decimal2) : null;
            } else if (callNode.getNodeOperator().isDivOp()) {
                op = (decimal, decimal2) -> decimal != null && decimal2 != null ? decimal.divide(decimal2) : null;
            }

            for (NodeBase arg : arguments) {
                Object value = ((ConstExprNode) arg).getAsValueType();
                if (sum != null) {
                    if (value instanceof Integer) {
                        sum = op.apply(sum, new BigDecimal((Integer) value));
                    } else if (value instanceof BigDecimal) {
                        sum = op.apply(sum, (BigDecimal) value);
                    }
                } else {
                    if (value instanceof Integer) {
                        sum = new BigDecimal((Integer) value);
                    } else if (value instanceof BigDecimal) {
                        sum = (BigDecimal) value;
                    }
                }
            }
            return new ConstExprNode(callNode.getParent(), sum.toString(), null, ValueType.DECIMAL, callNode.getSpan());
        });


        whenMatches(callNode(operCategory(is(NodeOperator.Category.RELATION)).and(args(allMatches(constNode(valueType(or(is(ValueType.INTEGER), is(ValueType.DECIMAL)))))))), node -> {
            final CallExprNode callNode = (CallExprNode) node;
            final List<NodeBase> arguments = callNode.getSubnodes();
            BigDecimal sum = BigDecimal.ZERO;
            assert arguments.size() == 2;
            ConstExprNode leftHand = (ConstExprNode) arguments.get(0);
            ConstExprNode rightHand = (ConstExprNode) arguments.get(1);
            BigDecimal left = toBigDecimal(leftHand);
            BigDecimal right = toBigDecimal(rightHand);
            // NE|LE|GE|LT|GT|EQ
            final String operator = callNode.getNodeOperator().getOperator();
            Boolean result = compare(operator, left, right);


            return new ConstExprNode(callNode.getParent(), result.toString(), null, ValueType.BOOLEAN, callNode.getSpan());
        });

        whenMatches(callNode(operCategory(is(NodeOperator.Category.RELATION)).and(args(allMatches(constNode(valueType(is(ValueType.STRING))))))), node -> {
            final CallExprNode callNode = (CallExprNode) node;
            final List<NodeBase> arguments = callNode.getSubnodes();
            BigDecimal sum = BigDecimal.ZERO;
            assert arguments.size() == 2;
            ConstExprNode leftHand = (ConstExprNode) arguments.get(0);
            ConstExprNode rightHand = (ConstExprNode) arguments.get(1);
            String left = leftHand.getValue();
            String right = rightHand.getValue();
            // NE|LE|GE|LT|GT|EQ
            Boolean result = compare(callNode.getNodeOperator().getOperator(),left,right);
            return new ConstExprNode(callNode.getParent(), result.toString(), null, ValueType.BOOLEAN, callNode.getSpan());
        });

        whenMatches(callNode(operCategory(is(NodeOperator.Category.LOGICAL))), node -> {
            BinaryOperator<Boolean> op = null;
            final CallExprNode callNode = (CallExprNode) node;
            final List<NodeBase> arguments = callNode.getSubnodes();
            BigDecimal sum = BigDecimal.ZERO;
            // NE|LE|GE|LT|GT|EQ
            Boolean result = null;
            final String operator = callNode.getNodeOperator().getOperator();
            List<NodeBase> newArguments = new ArrayList<>();
            final boolean andOperator = "and".equals(operator);
            final boolean orOperator = "or".equals(operator);
            final boolean notOperator = "not".equals(operator);

            if (notOperator) {
              assert arguments.size() == 1;
                final NodeBase arg0 = arguments.get(0);
                if (arg0 instanceof ConstExprNode) {
                    result = !((Boolean) ((ConstExprNode) arg0).getAsValueType());
                    return new ConstExprNode(callNode.getParent(), result.toString(), null, ValueType.BOOLEAN, callNode.getSpan());
                }
            } else {
                if (andOperator) {
                    op = (identity,value) -> identity == null ? value : value && identity;
                } else if (orOperator) {
                    op = (identity,value) -> identity == null ? value : value || identity;
                }
                for (NodeBase arg : arguments) {
                    if (arg instanceof ConstExprNode) {
                        result = op.apply(result,(Boolean) ((ConstExprNode) arg).getAsValueType());
                    } else {
                        newArguments.add(arg);
                    }
                }
            }

            // Whole infixExpression evaluates into constant
            if (result != null) {
                if ((!result || newArguments.isEmpty()) && andOperator) {
                    return new ConstExprNode(node.getParent(), result.toString(), null, ValueType.BOOLEAN, node.getSpan());
                }
                if ((result || newArguments.isEmpty()) && orOperator) {
                    return new ConstExprNode(node.getParent(), result.toString(), null, ValueType.BOOLEAN, node.getSpan());
                }
            }

            // We do not need current node anymore
            if (newArguments.size() == 1) {
                NodeBase argument = newArguments.get(0);
                CloneVisitor cloneVisitor = new CloneVisitor();
                argument.accept(cloneVisitor);
                return cloneVisitor.getASTBuilder().build();
            }

            // We'll filter constants from call
            if (newArguments.size() > 0) {
                ASTBuilder astBuilder = new ASTBuilder().callExprNode(callNode);
                for (NodeBase argument : newArguments) {
                    argument.accept(new CloneVisitor(astBuilder));
                }
                return astBuilder.getTopNode();
            }
            return node;
        });


        whenMatches(callNode(operator(is("+")).and(args(allMatches(constNode(valueType(or(is(ValueType.STRING), is(ValueType.INTEGER)))))))), callNode -> {
            StringBuilder stringBuilder = new StringBuilder();
            for (NodeBase arg : callNode.getSubnodes()) {
                Object value = ((ConstExprNode) arg).getValue();
                stringBuilder.append(value);
            }
            return new ConstExprNode(callNode.getParent(), stringBuilder.toString(), null, ValueType.STRING, callNode.getSpan());
        });

    }

    private <T> Boolean compare(String operator, Comparable<T> left, T right) {
        Boolean result;
        if (left != null && right != null) {
            final int diff = left.compareTo(right);
            switch (operator) {
                case "!=":
                    result = diff != 0;
                    break;
                case "<=":
                    result = diff <= 0;
                    break;
                case ">=":
                    result = diff >= 0;
                    break;
                case ">":
                    result = diff > 0;
                    break;
                case "<":
                    result = diff < 0;
                    break;
                case "=":
                    result = diff == 0;
                    break;
                default:
                    throw new IllegalStateException("Unknown relation operator " + operator);
            }
        } else  {
            // null <operator> null
            switch (operator) {
                case "!=":
                case ">":
                case "<":
                    result = false;
                    break;
                case "<=":
                case "=":
                case ">=":
                    result = true;
                    break;
                default:
                    throw new IllegalStateException("Unknown relation operator " + operator);
            }
            // null <operator> non-null / non-null <operator> null
            if (left != null || right != null) {
                result = !result;
            }
        }
        return result;
    }

    private BigDecimal toBigDecimal(ConstExprNode constNode) {
        if (constNode.getValueType() == ValueType.DECIMAL) {
            Object value = constNode.getAsValueType();
            if (value != null) {
                return (BigDecimal) constNode.getAsValueType();
            }
            return null;
        }
        if (constNode.getValueType() == ValueType.INTEGER) {
            Object value = constNode.getAsValueType();
            if (value != null) {
                return new BigDecimal((Integer) value);
            }
            return null;
        }
        throw new IllegalStateException();
    }
}
