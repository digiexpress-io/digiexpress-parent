package io.resys.hdes.ast.spi.antlr.util;

/*-
 * #%L
 * hdes-ast
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

import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.apache.commons.lang3.exception.ExceptionUtils;

import io.resys.hdes.ast.api.nodes.BodyNode;
import io.resys.hdes.ast.api.nodes.HdesNode.ErrorNode;
import io.resys.hdes.ast.spi.util.Assertions;
import io.resys.hdes.ast.api.nodes.ImmutableEmptyNode;
import io.resys.hdes.ast.api.nodes.ImmutableErrorNode;
import io.resys.hdes.ast.api.nodes.ImmutablePosition;
import io.resys.hdes.ast.api.nodes.ImmutableToken;

public class HdesAntlrErrorListener extends BaseErrorListener {
  private final List<ErrorNode> errors;
  private final String externalId;
  
  public HdesAntlrErrorListener(String externalId, List<ErrorNode> errors) {
    Assertions.notNull(externalId, () -> "externalId can't be null");
    this.externalId = externalId;
    this.errors = errors;
  }
  
  public List<ErrorNode> getErrors() {
    return errors;
  }

  @Override
  public void syntaxError(
      Recognizer<?, ?> recognizer,
      Object offendingSymbol,
      int line,
      int charPositionInLine,
      String msg,
      RecognitionException e) {
    
    
    // TODO:: System.err.println("line " + line + ":" + charPositionInLine + " " + msg);
    this.errors.add(ImmutableErrorNode.builder()
        .bodyId(externalId)
        .target(ImmutableEmptyNode.builder()
            .token(ImmutableToken.builder()
                .text(e == null ? "" : e.getCtx().getText())
                .start(ImmutablePosition.builder().line(line).col(charPositionInLine).build())
                .build())
            .build())
        .message(msg).build());
  }
  
  public void conflict(BodyNode latest, String latestOrigin, String oldestOrigin) {
    String msg = new StringBuilder()
        .append("Body with")
        .append(" name: '").append(latest.getId().getValue()).append("'")
        .append(" in: '").append(latestOrigin).append("'")
        .append(" is already defined in: '").append(oldestOrigin).append("'").toString();
  
    this.errors.add(ImmutableErrorNode.builder()
        .bodyId(externalId)
        .target(ImmutableEmptyNode.builder()
            .token(ImmutableToken.builder()
                .text(msg)
                .start(ImmutablePosition.builder().line(0).col(0).build())
                .build())
            .build())
        .message(msg).build());
  }
  
  public void add(Exception e) {
    String msg = new StringBuilder()
        .append("Parsing error in body with id: ").append(externalId).append(System.lineSeparator())
        .append("message: ").append(ExceptionUtils.getMessage(e)).append(System.lineSeparator())
        .append("stack trace: ").append(ExceptionUtils.getStackTrace(e)).toString();
  
    this.errors.add(ImmutableErrorNode.builder()
        .bodyId(externalId)
        .target(ImmutableEmptyNode.builder()
            .token(ImmutableToken.builder()
                .text(msg)
                .start(ImmutablePosition.builder().line(0).col(0).build())
                .build())
            .build())
        .message(msg).build());
  }
}
