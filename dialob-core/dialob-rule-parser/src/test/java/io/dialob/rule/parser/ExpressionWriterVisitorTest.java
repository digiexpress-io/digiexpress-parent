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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ExpressionWriterVisitorTest {
    @Test
    public void test() {
        assertAstDump("1+2+4 > 9","1 + 2 + 4 > 9");
        assertAstDump("1*(hn+4) > 9","1 * (hn + 4) > 9");
        assertAstDump("1/(hn+4) > 9","1 / (hn + 4) > 9");
        assertAstDump("1/(hn+4) > 9 or 9-0 <= 9*3+1","1 / (hn + 4) > 9 or 9 - 0 <= 9 * 3 + 1");
        assertAstDump("true and true","true and true");
        assertAstDump("true or true","true or true");
        assertAstDump("true or true and false","true or true and false");
        assertAstDump("(true or true) and false","(true or true) and false");
        assertAstDump("(not true or true) and false","(not true or true) and false");
        assertAstDump("not (true or true) and false","not (true or true) and false");
        assertAstDump("1+2 = 3 and false = true","(1 + 2) = 3 and false = true");
        assertAstDump("4 = 1+2 or false = false","4 = (1 + 2) or false = false");
        assertAstDump("func(1,4)","func ( 1, 4 )");
        assertAstDump("func(1,x,6)","func ( 1, x, 6 )");
        assertAstDump("func(1,x-2,6)","func ( 1, x - 2, 6 )");
        assertAstDump("func(1,(x-2)*8,6)","func ( 1, (x - 2) * 8, 6 )");
        assertAstDump("-1+2+3-3+2+1 = -8*9+-2+6","-1 + 2 + 3 - 3 + 2 + 1 = -8 * 9 + -2 + 6");
        assertAstDump("-(9+6)","-(9 + 6)");
        assertAstDump("-(9*6)","-(9*6)");
        assertAstDump("opt in opt1","opt in ('opt1')");
        assertAstDump("opt notIn opt1","opt not in ('opt1')");

    }

    private void assertAstDump(String expected, String original) {
        ExpressionWriterVisitor visitor;
        NodeBase ast;
        visitor = new ExpressionWriterVisitor();
        final Expression expression = Expression.createExpression(original);
        assertFalse(expression.hasErrors());
        ast = expression.getAst();
        System.out.println(ast);
        ast.accept(visitor);
        assertEquals(expected, visitor.toString());
    }


}

