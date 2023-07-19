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

import io.dialob.rule.parser.node.ParseTestBase;
import org.junit.jupiter.api.Test;

public class FFRuleParserTest extends ParseTestBase {
  @Test
  public void test() throws Exception {
    assertExpressionEquals("", "");
    assertExpressionEquals("// comment", "");
    assertExpressionEquals("1", "1");
    assertExpressionEquals("a+1*u/(5+9) = 2 and 0 = 0 ", "(and (= (+ a (/ (* 1 u) (+ 5 9))) 2) (= 0 0))");
    assertExpressionEquals("a-1*u/(5-9) = 2 and 0 = 0 ", "(and (= (- a (/ (* 1 u) (- 5 9))) 2) (= 0 0))");
    assertExpressionEquals("not a-1*u/(5-9) = 2 // This is comment \n and 0 = 0 ", "(and (not (= (- a (/ (* 1 u) (- 5 9))) 2)) (= 0 0))");
    assertExpressionEquals("answer < today // This is comment", "(< answer today)");
    assertExpressionEquals("answer > today", "(> answer today)");
    assertExpressionEquals("answer <= today", "(<= answer today)");
    assertExpressionEquals("answer >= today", "(>= answer today)");
    assertExpressionEquals("answer >= today - 1 day", "(>= answer (- today \"1 day\"))");
    assertExpressionEquals("answer >= today + 12 years", "(>= answer (+ today \"12 years\"))");
    assertExpressionEquals("age(answer, today) < 18", "(< (age answer today) 18)");
    assertExpressionEquals("age(answer) < 18", "(< (age answer) 18)");
    assertExpressionEquals("today - answer < 18 years", "(< (- today answer) \"18 years\")");
    assertExpressionEquals("answer > startDate", "(> answer startDate)");
    assertExpressionEquals("1 - 2 - 3 = -4", "(= (- (- 1 2) 3) (neg 4))");
    assertExpressionEquals("-1 - 2 - 3 = -6", "(= (- (- (neg 1) 2) 3) (neg 6))");
    assertExpressionEquals("q1 is answered", "(isAnswered q1)");
    assertExpressionEquals("q1 is not answered", "(isNotAnswered q1)");
    assertExpressionEquals("not q1 is not answered", "(not (isNotAnswered q1))");
    assertExpressionEquals("not q1 is not answered and q2 is answered", "(and (not (isNotAnswered q1)) (isAnswered q2))");
    assertExpressionEquals("not q1 is not answered and q2 is answered or q3 is answered", "(or (and (not (isNotAnswered q1)) (isAnswered q2)) (isAnswered q3))");
    assertExpressionEquals("not q1 is not answered", "(not (isNotAnswered q1))");
    assertExpressionEquals("answer in (1,2,3)", "(in answer 1 2 3)");
    assertExpressionEquals("answer not in (1,2,3)", "(notIn answer 1 2 3)");
    assertExpressionEquals("answer in listQuestion", "(in answer listQuestion)");
    assertExpressionEquals("answer not in listQuestion", "(notIn answer listQuestion)");
    assertExpressionEquals("question is valid", "(isValid question)");
    assertExpressionEquals("question is not valid", "(isNotValid question)");
    assertExpressionEquals("personWeight/(personHeight*personHeight)", "(/ personWeight (* personHeight personHeight))");
    assertExpressionEquals("sum of wage", "(sumOf wage)");
    assertExpressionEquals("sum of (wage + 1)", "(sumOf (+ wage 1))");
    assertExpressionEquals("all of question is answered", "(allOf (isAnswered question))");
    assertExpressionEquals("1 + sum of wage", "(+ 1 (sumOf wage))");
    assertExpressionEquals("sum of wage + 1", "(+ (sumOf wage) 1)");
    assertExpressionEquals("all of (wage > 0)", "(allOf (> wage 0))");
    assertExpressionEquals("sum of wage > 0", "(> (sumOf wage) 0)");
    assertExpressionEquals("sumOf(wage) + 1", "(+ (sumOf wage) 1)");
    assertExpressionEquals("sumOf(wage + 1)", "(sumOf (+ wage 1))");
    assertExpressionEquals("(all of question is answered) and q2 is not answered", "(and (allOf (isAnswered question)) (isNotAnswered q2))");
    assertExpressionEquals("all of question is answered and q2 is not answered", "(and (allOf (isAnswered question)) (isNotAnswered q2))");
    assertExpressionEquals("q2 is not answered and all of question is answered", "(and (isNotAnswered q2) (allOf (isAnswered question)))");
    assertExpressionEquals("q2 is not answered or all of question is answered ", "(or (isNotAnswered q2) (allOf (isAnswered question)))");
    assertExpressionEquals("all of question is answered or q2 is not answered", "(or (allOf (isAnswered question)) (isNotAnswered q2))");
    assertExpressionEquals("(sum of wage) + 1", "(+ (sumOf wage) 1)");
  }
}

