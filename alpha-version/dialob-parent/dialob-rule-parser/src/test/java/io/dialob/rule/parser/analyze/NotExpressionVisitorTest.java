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
package io.dialob.rule.parser.analyze;

import io.dialob.rule.parser.Expression;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NotExpressionVisitorTest {


    @Test
    public void test() {
        String expr = "true";
        String expect = "false";
        assertExpressionNot("(notMatches a \".*\")", "a matches '.*'");

        assertExpressionNot("false", "true");
        assertExpressionNot("true", "false");
        assertExpressionNot("true", "not true");
        assertExpressionNot("(or false false)", "true and true");
        assertExpressionNot("(and (!= a 0) (= b 1))", "a = 0 or b != 1");
        assertExpressionNot("(or (<= a 0) (< b 1))", "a > 0 and  b >= 1");
        assertExpressionNot("(or (<= a 0) (and true false))", "a > 0 and not (true and false)");
        assertExpressionNot("(or (<= a 0) (not (or false true)))", "a > 0 and not not (false or true)");
        assertExpressionNot("(isNotAnswered a)", "a is answered");
        assertExpressionNot("(isAnswered a)", "a is not answered");
        assertExpressionNot("(matches a \".*\")", "a not matches '.*'");
        assertExpressionNot("(notIn a 1 2 3)", "a in (1,2,3)");
        assertExpressionNot("(in a 1 2 3)", "a not in (1,2,3)");
    }

    protected void assertExpressionNot(String expect, String expr) {
        Expression expression = Expression.createExpression(expr);
        expression.accept(new NotExpressionVisitor());
        assertEquals(expect,expression.getAst().toString());
    }

}
