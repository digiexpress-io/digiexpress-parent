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
import io.resys.hdes.ast.spi.antlr.visitors.HdesParserVisitor;
import io.resys.hdes.ast.spi.antlr.visitors.HdesParserVisitor.ContentNode;

public class DecisionTableTest {
  
  @Test
  public void basic() throws IOException {
    ContentNode node = parse("basic");
    assetNode(node, "basic");
  }

  @Test
  public void headers() throws IOException {
    ContentNode node = parse("headers");
    assetNode(node, "headers");
  }

  @Test
  public void values() throws IOException {
    ContentNode node = parse("values");
    assetNode(node, "values");
  }

  @Test
  public void matchExpressions() throws IOException {
    ContentNode node = parse("matchExpressions");
    assetNode(node, "matchExpressions");
  }
  
  @Test
  public void equalityExpressions() throws IOException {
    ContentNode node = parse("equalityExpressions");
    assetNode(node, "equalityExpressions");
  }
  
  
  
  public ContentNode parse(String file) {
    String value = DataFormatTestUtil.file("ast/DecisionTableTest_" + file + ".hdes");
    HdesLexer lexer = new HdesLexer(CharStreams.fromString(value));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    HdesParser parser = new HdesParser(tokens);
    parser.addErrorListener(new ErrorListener());
    ParseTree tree = parser.hdesContent();
    return (ContentNode) tree.accept(new HdesParserVisitor());
  }
  
  public static void assetNode(ContentNode node, String file) {
    String actual = DataFormatTestUtil.yaml(node);
    String expected = DataFormatTestUtil.file("ast/DecisionTableTest_" + file + ".yaml");
    Assertions.assertLinesMatch(expected.lines(), actual.lines());
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
