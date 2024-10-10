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

import io.dialob.rule.parser.modifier.ModifyingMinifierVisitor;
import io.dialob.rule.parser.node.ASTBuilder;
import io.dialob.rule.parser.node.NodeBase;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;

import java.util.function.UnaryOperator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
public class AstMatcherTest {

    @Test
    public void simpleNodeMatchersAreCalled() {
        // given
        final Expression expression = Expression.createExpression("1 + a + 3");
        final UnaryOperator<NodeBase> constNodeFunctionMock = mock(UnaryOperator.class);
        final UnaryOperator<NodeBase> idNodeFunctionMock = mock(UnaryOperator.class);

        when(constNodeFunctionMock.apply(any(NodeBase.class))).thenAnswer(invocation -> (NodeBase) invocation.getArguments()[0]);
        when(idNodeFunctionMock.apply(any(NodeBase.class))).thenAnswer(invocation -> (NodeBase) invocation.getArguments()[0]);

        // when
        final AstMatcher matcher = new AstMatcher() {{
            whenMatches(constNode(), constNodeFunctionMock);
            whenMatches(idNode(), idNodeFunctionMock);
        }};
        expression.getAst().accept(matcher);

        // then
        verify(constNodeFunctionMock, times(2)).apply(any(NodeBase.class));
        verify(idNodeFunctionMock, times(1)).apply(any(NodeBase.class));
        verifyNoMoreInteractions(constNodeFunctionMock, idNodeFunctionMock);
    }

    @Test
    public void simpleNodeMatchersAreCalledWhenPredicateMatches() {
        // given
        final Expression expression = Expression.createExpression("1 + a + 3");
        final UnaryOperator<NodeBase> constNode3FunctionMock = mock(UnaryOperator.class);
        final UnaryOperator<NodeBase> constNode1FunctionMock = mock(UnaryOperator.class);
        final UnaryOperator<NodeBase> idANodeFunctionMock = mock(UnaryOperator.class);

        when(constNode3FunctionMock.apply(any(NodeBase.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());
        when(constNode1FunctionMock.apply(any(NodeBase.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());
        when(idANodeFunctionMock.apply(any(NodeBase.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());

        // when
        final AstMatcher matcher = new AstMatcher() {{
            whenMatches(constNode(stringValue(is("3"))), constNode3FunctionMock);
            whenMatches(constNode(value(is(1))), constNode1FunctionMock);
            whenMatches(idNode(valueType(isNull())), idANodeFunctionMock);
        }};
        expression.getAst().accept(matcher);

        // then
        verify(constNode3FunctionMock, times(1)).apply(any(NodeBase.class));
        verify(idANodeFunctionMock, times(1)).apply(any(NodeBase.class));
        verify(constNode1FunctionMock, times(1)).apply(any(NodeBase.class));
        verifyNoMoreInteractions(constNode3FunctionMock, idANodeFunctionMock, constNode1FunctionMock);
    }

    @Test
    public void callOperatorMathers() {
        // given
        final Expression expression = Expression.createExpression("1 + a + 3");
        final UnaryOperator<NodeBase> callMatcher = mockMatchFunction();

        // when
        final AstMatcher matcher = new AstMatcher() {{
            whenMatches(callNode(), callMatcher);
        }};
        expression.getAst().accept(matcher);

        // then
        verify(callMatcher, times(2)).apply(any(NodeBase.class));
        verifyNoMoreInteractions(callMatcher);
    }


    @Test
    public void callOperatorArgumentsMatchers() {
        // given
        final Expression expression = Expression.createExpression("1 + a + 3");
        final UnaryOperator<NodeBase> callMatcher1 = mockMatchFunction();
        final UnaryOperator<NodeBase> callMatcher2 = mockMatchFunction();
        final UnaryOperator<NodeBase> callMatcher3 = mockMatchFunction();

        log.debug(expression.getAst().toString());

        // when
        final AstMatcher matcher = new AstMatcher() {{
            whenMatches(callNode(args(length(is(1)))), callMatcher1);
            whenMatches(callNode(args(length(is(2)))), callMatcher2);
            whenMatches(callNode(args(length(is(3)))), callMatcher3);
        }};
        expression.getAst().accept(matcher);

        // then
        verify(callMatcher1, times(0)).apply(any(NodeBase.class));
        verify(callMatcher2, times(2)).apply(any(NodeBase.class));
        verify(callMatcher3, times(0)).apply(any(NodeBase.class));
        verifyNoMoreInteractions(callMatcher1,callMatcher2,callMatcher3);
    }

    @Test
    public void matchersWithHigherPredenceOverridesLowerMatchers() {
        // given
        final Expression expression = Expression.createExpression("1 + 3 + 3 + a");
        final UnaryOperator<NodeBase> callMatcher1 = mockMatchFunction();
        final UnaryOperator<NodeBase> callMatcher2 = mockMatchFunction();

        log.debug(expression.getAst().toString());

        // when
        // Note! predence of matchers. If one of matchers matches, rest of matchers are not evaluated
        final AstMatcher matcher = new AstMatcher() {{
            whenMatches(callNode(args(allMatches(constNode()))), callMatcher1);
            whenMatches(callNode(args(anyMatches(constNode()))), callMatcher2);
        }};
        expression.getAst().accept(matcher);

        // then
        verify(callMatcher1, times(1)).apply(any(NodeBase.class));
        verify(callMatcher2, times(1)).apply(any(NodeBase.class));
        verifyNoMoreInteractions(callMatcher1,callMatcher2);
    }

    @Test
    public void matchersWithHigherPredenceOverridesLowerMatchers2() {
        // given
        final Expression expression = Expression.createExpression("1 + 3 + 3 + a");
        final UnaryOperator<NodeBase> callMatcher1 = mockMatchFunction();
        final UnaryOperator<NodeBase> callMatcher2 = mockMatchFunction();
        final UnaryOperator<NodeBase> callMatcher3 = mockMatchFunction();

        log.debug(expression.getAst().toString());

        // when
        AstMatcher matcher = new AstMatcher() {{
            whenMatches(callNode(args(allMatches(constNode()))), callMatcher1);
        }};
        expression.getAst().accept(matcher);
        matcher = new AstMatcher() {{
            whenMatches(callNode(args(anyMatches(constNode()))), callMatcher2);
        }};
        expression.getAst().accept(matcher);
        matcher = new AstMatcher() {{
            whenMatches(callNode(operator(is("+"))), callMatcher3);
        }};
        expression.getAst().accept(matcher);


        // then
        verify(callMatcher1, times(1)).apply(any(NodeBase.class));
        verify(callMatcher2, times(2)).apply(any(NodeBase.class));
        verify(callMatcher3, times(3)).apply(any(NodeBase.class));
        verifyNoMoreInteractions(callMatcher1,callMatcher2,callMatcher3);
    }


    @Test
    public void relativeMatchers() {
        // given
        final Expression expression = Expression.createExpression("1 + a + 3");
        final UnaryOperator<NodeBase> callMatcher1 = mockMatchFunction();
        final UnaryOperator<NodeBase> callMatcher2 = mockMatchFunction();
        final UnaryOperator<NodeBase> callMatcher3 = mockMatchFunction();

        log.debug(expression.getAst().toString());

        // when
        final AstMatcher matcher = new AstMatcher() {{
            whenMatches(callNode(args(length(is(1)))), callMatcher1);
            whenMatches(callNode(args(length(is(2)))), callMatcher2);
            whenMatches(callNode(args(length(is(3)))), callMatcher3);
        }};
        expression.getAst().accept(matcher);

        // then
        verify(callMatcher1, times(0)).apply(any(NodeBase.class));
        verify(callMatcher2, times(2)).apply(any(NodeBase.class));
        verify(callMatcher3, times(0)).apply(any(NodeBase.class));
        verifyNoMoreInteractions(callMatcher1,callMatcher2,callMatcher3);
    }



    @Test
    public void shouldMatchOnParentNode() {
        // given
        final Expression expression = Expression.createExpression("a + b + c");
        final UnaryOperator<NodeBase> callMatcher1 = mockMatchFunction();
        final UnaryOperator<NodeBase> callMatcher2 = mockMatchFunction();

        log.debug(expression.getAst().toString());

        // when
        final AstMatcher matcher = new AstMatcher() {{
            whenMatches(callNode(operator(is("+")).and(parent(callNode(operator(is("+")))))), callMatcher1);
            whenMatches(callNode(operator(is("+")).and(parent(callNode(operator(is("-")))))), callMatcher2);
        }};
        expression.getAst().accept(matcher);

        // then
        verify(callMatcher1, times(1)).apply(any(NodeBase.class));
        verify(callMatcher2, times(0)).apply(any(NodeBase.class));
        verifyNoMoreInteractions(callMatcher1,callMatcher2);
    }


    @Test
    public void constEvalTest() {
        // given
        assertMinify("6", "1 + 2 + 3");
    }
    @Test
    public void constEvalTest2() {
        // given
        assertMinify("-2", "1 + 2 + 3 - 3 - 6 - 8 + 9");
    }

    @Test
    public void stringPlusIsStringCat() {
        // given
        assertMinify("\"6a689\"", "1 + 2 + 3 + 'a' + 6 + 8 + 9");
    }
    @Test
    public void integerMultiply() {
        // given
        assertMinify("24", "1 * 2 * 3 * 4");
    }

    @Test
    public void decimalMultiply() {
        // given
        assertMinify("26.4", "1 * 2.2 * 3 * 4");
    }

    @Test
    public void divide() {
        // given
        assertMinify("0.5", "1 / 2");
        assertMinify("(/ (* 0.5 a) 6)", "1 / 2 * a / 6");
    }

    @Test
    public void negateInteger() {
        // given
        assertMinify("-1", "-1");
    }
    @Test
    public void negateDecimal() {
        // given
        assertMinify("-5.0", "-1.0 * 5");
    }
    @Test
    public void relOpe() {
        assertMinify("false", "-1.0 > 5");
        assertMinify("false", "-1.0 >= 5");
        assertMinify("true", "-1.0 <= 5");
        assertMinify("true", "-1.0 < 5");
        assertMinify("false", "-1.0 = 5");
        assertMinify("true", "-1.0 != 5");

        assertMinify("false", "-1 > 5");
        assertMinify("false", "-1 >= 5");
        assertMinify("true", "-1 <= 5");
        assertMinify("true", "-1 < 5");
        assertMinify("false", "-1 = 5");
        assertMinify("true", "-1 != 5");
        assertMinify("-1", "5 - 6");

        assertMinify("true", "-1 = 5 - 6");


    }

    @Test
    public void logicalOper() {
        assertMinify("false", "false or false");
        assertMinify("(= a 0)", "true and a = 0");
        assertMinify("false", "false and a = 0");
        assertMinify("(= a (+ b 6))", "false or a = (b + 6)");
        assertMinify("(= a (+ b 6))", "a = (b + 6) or false");
        assertMinify("true", "true or a = (b + 6)");
        assertMinify("true", "not false or not false");
        assertMinify("false", "not true and not true");
        assertMinify("false", "not (true and true)");
        assertMinify("true", "not (true and false)");
        assertMinify("false", "true and false");
        assertMinify("true", "true and true and true");
        assertMinify("true", "6 + 9 = 15 and 8 * 8 < 65");
        assertMinify("(or (and (< question1 (/ question2 -4)) (> (+ question2 18) 0)) (!= (- question3 10) 10))", "question1 < question2 / (2 - 6) and question2 + 2 * 9 > 0 or question3 - 10 != 10");


    }



    private void assertMinify(String expected, String expressionString) {
        // given
        final Expression expression = Expression.createExpression(expressionString);
        final ASTBuilder astBuilder = new ASTBuilder();
        log.debug(expression.getAst().toString());
        // when
        final AstMatcher matcher = new ModifyingMinifierVisitor();
        expression.accept(matcher);

        // then
        Assertions.assertEquals(expected,expression.getAst().toString());
    }

    private UnaryOperator<NodeBase> mockMatchFunction() {
        final UnaryOperator<NodeBase> matchFunction = mock(UnaryOperator.class);
        when(matchFunction.apply(any(NodeBase.class))).thenAnswer(invocation -> (NodeBase) invocation.getArguments()[0]);
        return matchFunction;
    }

}

