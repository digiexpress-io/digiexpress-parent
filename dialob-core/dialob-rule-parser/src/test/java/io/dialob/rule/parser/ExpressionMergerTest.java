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

import io.dialob.rule.parser.node.NodeBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExpressionMergerTest {

    @Test
    public void test() throws Exception {
        assertExpressionMerge("1 + 2 + 3 + 4", "(+ 1 2 3 4)");
        assertExpressionMerge("1 + 2", "(+ 1 2)");
        assertExpressionMerge("1 - 1", "(- 1 1)");
        assertExpressionMerge("1 - 1 - 2", "(- 1 1 2)");
        assertExpressionMerge("1 - 1 - 2 + 1", "(+ (- 1 1 2) 1)");
        assertExpressionMerge("1 + 2 + 3 - 3 + 2 + 1", "(+ (- (+ 1 2 3) 3) 2 1)");
        assertExpressionMerge("-1 + 2 + 3 - 3 + 2 + 1 = -8 * 9 + -2 + 6", "(= (+ (- (+ (neg 1) 2 3) 3) 2 1) (+ (* (neg 8) 9) (neg 2) 6))");

         // "(+ (- (+ 1 2 3) 3) 2 1)"

        // (- 1 2) == (+ 1 -2)
        // 1 - 2 == 1 + -2
        // - 0 1 == 0 + -1
    }

    private void assertExpressionMerge(String expression, String expected) {
        NodeBase ast = Expression.createExpression(expression).getAst();
        final ExpressionMerger expressionMerger = new ExpressionMerger();
        ast.accept(expressionMerger);
        Assertions.assertEquals(expected, expressionMerger.getAstBuilder().getTopNode().toString());

    }

}
