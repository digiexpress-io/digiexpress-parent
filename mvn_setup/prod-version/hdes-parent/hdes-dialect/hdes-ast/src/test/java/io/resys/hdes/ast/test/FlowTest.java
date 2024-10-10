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
import org.junit.jupiter.api.Test;

import io.resys.hdes.ast.HdesLexer;
import io.resys.hdes.ast.HdesParser;
import io.resys.hdes.ast.spi.antlr.visitors.HdesParserVisitor;

public class FlowTest {
  
  @Test
  public void basic() throws IOException {
    parse("basic");
  }

  @Test
  public void taskWhenThen() throws IOException {
    parse("taskWhenThen");
  }

  @Test
  public void taskThen() throws IOException {
    parse("taskThen");
  }

  @Test
  public void taskManualTask() throws IOException {
    parse("taskManualTask");
  }

  @Test
  public void taskServiceTask() throws IOException {
    parse("taskServiceTask");
  }

  @Test
  public void taskFlowTaskOverArray() throws IOException {
    parse("taskFlowTaskOverArray");
  }
  
  @Test
  public void simpleIterationOverArray() throws IOException {
    parse("simpleIterationOverArray");
  }

  @Test
  public void taskOverDTOutputArray() throws IOException {
    parse("taskOverDTOutputArray");
  }

  @Test
  public void taskDTArray() throws IOException {
    parse("taskDTArray");
  }

  @Test
  public void taskMapping() throws IOException {
    parse("taskMapping");
  }
  
  public void parse(String file) {
    String value = DataFormatTestUtil.file("ast/FlowTest_" + file + ".hdes");
    HdesLexer lexer = new HdesLexer(CharStreams.fromString(value));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    HdesParser parser = new HdesParser(tokens);
    parser.addErrorListener(new ErrorListener());
    ParseTree tree = parser.hdesContent();
    //tree.accept(new HdesParserConsoleVisitor());
    tree.accept(new HdesParserVisitor());
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
