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

import io.dialob.rule.parser.AstMatcher;
import io.dialob.rule.parser.api.ValueType;
import io.dialob.rule.parser.node.NodeBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConvertDateMinusDateVisitor extends AstMatcher {
  {
    whenMatches(callNode(operator(is("-")).and(lhs(valueType(is(ValueType.DATE)))).and(rhs(valueType(is(ValueType.DATE))))), nodeBase -> {
      final List<NodeBase> subnodes = nodeBase.getSubnodes();
      return newASTBuilder()
        .callExprNode("java.time.Period.between",ValueType.PERIOD,nodeBase.getSpan())
         .exprNodes(reverse(subnodes).toArray(new NodeBase[0]))
        .closeExpr().build();
    });


    whenMatches(callNode(operator(is("-")).and(lhs(valueType(is(ValueType.TIME)))).and(rhs(valueType(is(ValueType.TIME))))), nodeBase -> {
      final List<NodeBase> subnodes = nodeBase.getSubnodes();
      return newASTBuilder()
        .callExprNode("java.time.Duration.between",ValueType.DURATION,nodeBase.getSpan())
          .exprNodes(reverse(subnodes).toArray(new NodeBase[0]))
        .closeExpr().build();

    });

    whenMatches(callNode(operator(is("-").or(is("+"))).and(lhs(valueType(is(ValueType.DURATION)))).and(rhs(valueType(is(ValueType.DURATION))))), nodeBase -> {
      final List<NodeBase> subnodes = nodeBase.getSubnodes();
      String function ;
      if ("+".equals(nodeBase.getNodeOperator().getOperator())){
        function = "io.dialob.rule.parser.PeriodUtil.sumDurations";
      } else {
        function = "io.dialob.rule.parser.PeriodUtil.minusDurations";
      }
      return newASTBuilder()
        .callExprNode(function,ValueType.DURATION,nodeBase.getSpan())
          .exprNodes(subnodes.toArray(new NodeBase[0]))
        .closeExpr().build();
    });

    whenMatches(callNode(operator(is("-").or(is("+"))).and(lhs(valueType(is(ValueType.PERIOD)))).and(rhs(valueType(is(ValueType.PERIOD))))), nodeBase -> {
      final List<NodeBase> subnodes = nodeBase.getSubnodes();
      String function ;
      if ("+".equals(nodeBase.getNodeOperator().getOperator())){
        function = "io.dialob.rule.parser.PeriodUtil.sumPeriods";
      } else {
        function = "io.dialob.rule.parser.PeriodUtil.minusPeriods";
      }
      return newASTBuilder()
        .callExprNode(function,ValueType.PERIOD,nodeBase.getSpan())
          .exprNodes(subnodes.toArray(new NodeBase[0]))
        .closeExpr().build();
    });


    whenMatches(callNode(operator(is("-").or(is("+"))).and(lhs(valueType(is(ValueType.DATE)))).and(rhs(valueType(is(ValueType.PERIOD))))), nodeBase -> {
      final List<NodeBase> subnodes = nodeBase.getSubnodes();
      String function ;
      if ("+".equals(nodeBase.getNodeOperator().getOperator())){
        function = "io.dialob.rule.parser.PeriodUtil.datePlusPeriod";
      } else {
        function = "io.dialob.rule.parser.PeriodUtil.dateMinusPeriod";
      }
      return newASTBuilder()
        .callExprNode(function,ValueType.DATE,nodeBase.getSpan())
        .exprNodes(subnodes.toArray(new NodeBase[0]))
        .closeExpr().build();
    });

    whenMatches(callNode(operator(is("-").or(is("+"))).and(lhs(valueType(is(ValueType.TIME)))).and(rhs(valueType(is(ValueType.DURATION))))), nodeBase -> {
      final List<NodeBase> subnodes = nodeBase.getSubnodes();
      String function ;
      if ("+".equals(nodeBase.getNodeOperator().getOperator())){
        function = "io.dialob.rule.parser.PeriodUtil.timePlusDuration";
      } else {
        function = "io.dialob.rule.parser.PeriodUtil.timeMinusDuration";
      }
      return newASTBuilder()
        .callExprNode(function,ValueType.TIME,nodeBase.getSpan())
          .exprNodes(subnodes.toArray(new NodeBase[0]))
        .closeExpr().build();
    });


  }

  private List<?> reverse(List<?> subnodes) {
    ArrayList<?> reserved = new ArrayList<>(subnodes);
    Collections.reverse(reserved);
    return reserved;
  }

}
