package io.resys.hdes.ast.test;

/*-
 * #%L
 * hdes-datatype
 * %%
 * Copyright (C) 2020 Copyright 2020 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.resys.hdes.ast.HdesLexer;
import io.resys.hdes.ast.HdesParser;
import io.resys.hdes.ast.api.nodes.BodyNode.ScalarType;
import io.resys.hdes.ast.spi.antlr.visitors.HdesParserVisitor;
import io.resys.hdes.ast.spi.antlr.visitors.HdesParserVisitor.ContentNode;

public class ExpressionTest {

  @Test
  public void invocation() throws IOException {
    ContentNode node = parse()
      .add("_ > 40", ScalarType.BOOLEAN)
      .add("_field1 > 40", ScalarType.BOOLEAN)
      .add("GetX.values._field1 > 40", ScalarType.INTEGER).build();
    assertNode(node, "invocation");
  }

  @Test
  public void placeholderExpression() throws IOException {
    ContentNode node = parse()
        .add("_ >= 20 ? 30 : 40", ScalarType.INTEGER)
        .add("stepx._value1 >= 20 ? 30 : 40", ScalarType.INTEGER)
        .add("_value1 >= 20 ? 30 : 40", ScalarType.INTEGER).build();
    
    assertNode(node, "placeholderExpression");
  }

  @Test
  public void complex() throws IOException {
    ContentNode node = parse()
        .add("x in('aaa', 'bbb', 'x')", ScalarType.BOOLEAN)
        .add("x in(y, x, z)", ScalarType.BOOLEAN)
        
        .add("sum(y, x, z)", ScalarType.INTEGER)
        .add("sum(10, 20, 30)", ScalarType.INTEGER)
        .add("sum(10_0000, 0.20, 30)", ScalarType.DECIMAL)
        .add("sum(sum(), sum(sum(10, 20)), x.value, 30, z.value, v.x = 10 ? sum(sum(10, 20), t) : 100)", ScalarType.DECIMAL)
        
        .add("x.value.children", ScalarType.INTEGER)
        .add("x.value.children.value", ScalarType.INTEGER)
        
        .add("5 = 10 and 20 = 30", ScalarType.BOOLEAN)
        .add("x = 10 or y = 30", ScalarType.BOOLEAN)
        .add("x = 10 or sum(y, x, z) = -30", ScalarType.BOOLEAN)
        .add("true = false ? 10 : sum(20)", ScalarType.INTEGER)
        .add("-x", ScalarType.INTEGER)
        .add("+x + (1-20)", ScalarType.INTEGER)
        .add("(x*y/20) + 1", ScalarType.DECIMAL).build();
    
    assertNode(node, "complex");
  }

  @Test
  public void staticMethods() throws IOException {
    ContentNode node = parse()
        .add("sum(10, 20, 40)", ScalarType.INTEGER)
        .add("min(10, 20, 40)", ScalarType.INTEGER)
        .add("max(10, 20, 40)", ScalarType.INTEGER)
        .add("avg(10, 20, 40)", ScalarType.DECIMAL)
        .build();    
    assertNode(node, "staticMethods");
  }
  
  @Test
  public void globalMethods() throws IOException {
    ContentNode node = parse()
        .add("createEntity(10, 20, 40)")
        .add("createPerson('sam', 'vimes').firstName")
        .build();
    assertNode(node, "globalMethods");
  }
  
  @Test
  public void arrayMethod() throws IOException {
    ContentNode node = parse()
        .add("users.map(e -> e.firstName)")
        .build();
    assertNode(node, "arrayMethods");
  }
  
  @Test
  public void arrayMethodFindFirst() throws IOException {
    ContentNode node = parse()
        .add("users.map(e -> e.firstName).findFirst()")
        .add("users.map(e -> e.firstName).filter(e -> e = 'SAM').sort(e -> e, ASC).findFirst()")
        .build();
    assertNode(node, "arrayMethodsFindFirst");
  }

  @Test
  public void literals() throws IOException {
    ContentNode node = parse()
        .add("-10", ScalarType.INTEGER)
        .add("-10_000", ScalarType.INTEGER)
        .add("-10.5", ScalarType.DECIMAL)
        .add("true", ScalarType.BOOLEAN)
        .add("false", ScalarType.BOOLEAN)
        .add("'words'", ScalarType.STRING).build();
    assertNode(node, "literals");
  }
  
  @Test
  public void conditionalExpression() throws IOException {
    ContentNode node = parse()
        .add("x >= 20 ? 30 : 40 \r\n", ScalarType.INTEGER)
        .add("x > 20 ? 30 : v", ScalarType.INTEGER)
        .add("x <= k ? 30 : 40", ScalarType.INTEGER)
        .add("x < 20 ? 30 : x", ScalarType.INTEGER).build();
    
    assertNode(node, "conditionalExpression");
  }

  @Test
  public void equalityExpression() throws IOException {
    ContentNode node = parse()
        .add("x = 20", ScalarType.BOOLEAN)
        .add("z != 10", ScalarType.BOOLEAN)
        .add("z != w", ScalarType.BOOLEAN)
        .build();
    
    assertNode(node, "equalityExpression");
  }

  @Test
  public void conditionalAndOrExpression() throws IOException {
    ContentNode node = parse()
        .add("x > 6 and z < t", ScalarType.BOOLEAN)
        .add("x = 20 and y = 10 and z = 100", ScalarType.BOOLEAN)
        .add("x = 20 and y = 10 or c > 10", ScalarType.BOOLEAN)
    .build();
    
    assertNode(node, "conditionalAndOrExpression");
  }

  @Test
  public void arithmeticalExpression() throws IOException {
    ContentNode node = parse().add("x+y/z*89*(x+5)", ScalarType.DECIMAL).build();
    
    assertNode(node, "arithmeticalExpression");
  }
  
  
  
  public static void assertNode(ContentNode node, String file) {
    String actual = DataFormatTestUtil.yaml(node);
    String expected = DataFormatTestUtil.file("ast/ExpressionTest_" + file + ".yaml");
    Assertions.assertLinesMatch(expected.lines(), actual.lines());
  }
  
  private static class TestExpressionBuilder {
    
    private final StringBuilder result = new StringBuilder();
    @SuppressWarnings("unused")
    private List<ScalarType> types = new ArrayList<>();
    
    public TestExpressionBuilder add(String value, ScalarType type) {
      result.append("expression {").append(value).append("}").append(System.lineSeparator());
      return this;
    }
    
    public TestExpressionBuilder add(String value) {
      result.append("expression {").append(value).append("}").append(System.lineSeparator());
      return this;
    }
    
    public ContentNode build() {
      HdesLexer lexer = new HdesLexer(CharStreams.fromString(result.toString()));
      CommonTokenStream tokens = new CommonTokenStream(lexer);
      HdesParser parser = new HdesParser(tokens);
      parser.addErrorListener(new ErrorListener());
      ParseTree tree = parser.hdesContent();
      return (ContentNode) tree.accept(new HdesParserVisitor());
    }
  }
  
  public TestExpressionBuilder parse() {
    return new TestExpressionBuilder();
  }

  public static class ErrorListener extends BaseErrorListener {
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
        Object offendingSymbol,
        int line,
        int charPositionInLine,
        String msg,
        RecognitionException e) {
      String error = "line " + line + ":" + charPositionInLine + " " + msg;
      throw new IllegalArgumentException(error);
    }
  }
}
