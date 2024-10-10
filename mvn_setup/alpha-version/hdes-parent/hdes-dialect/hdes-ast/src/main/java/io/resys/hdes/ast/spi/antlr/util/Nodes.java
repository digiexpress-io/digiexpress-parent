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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.ParseTree;

import io.resys.hdes.ast.api.nodes.HdesNode;
import io.resys.hdes.ast.api.nodes.ImmutablePosition;
import io.resys.hdes.ast.api.nodes.ImmutableToken;

public class Nodes {
  private final HdesNode.Token token;
  private final List<Object> results;
  
  private Nodes(List<Object> results, HdesNode.Token token) {
    super();
    this.results = results;
    this.token = token;
  }

  @SuppressWarnings("unchecked")
  public <T> Optional<T> of(Class<T> clazz) {
    for(Object entry :results) {
      if(clazz.isAssignableFrom(entry.getClass()) ) {
        return Optional.of((T) entry);
      }
    }
    return Optional.empty();
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> list(Class<T> clazz) {
    List<T> result = new ArrayList<>();
    for(Object entry : results) {
      if(clazz.isAssignableFrom(entry.getClass()) ) {
        result.add((T) entry);
      }
    }
    return result;
  }
  
  public static Nodes from(ParserRuleContext node, AbstractParseTreeVisitor<?> visitor) {
    List<Object> results = new ArrayList<>();
    int n = node.getChildCount();
    for (int i = 0; i < n; i++) {
      ParseTree c = node.getChild(i);
      Object childResult = c.accept(visitor);
      if (childResult != null) {
        results.add(childResult);
      }
    }
    HdesNode.Token token = token(node);
    return new Nodes(results, token);
  }
 
  public static HdesNode.Token token(ParserRuleContext context) {
    Token startToken = context.getStart();
    Token stopToken = context.getStop();
    
    //final int a = context.start.getStartIndex();
    //final int b = context.stop.getStopIndex() + 1;
    //final int delta = (b - a); // total length of the token
    final int startCol = startToken.getCharPositionInLine() + 1;
    final int endCol = // token start + token length
        (stopToken.getCharPositionInLine() + 1) + 
        (context.stop.getStopIndex() + 1 - context.stop.getStartIndex());
    
    
    return ImmutableToken.builder()
        .start(ImmutablePosition.builder().line(startToken.getLine()).col(startCol).build())
        .end(ImmutablePosition.builder().line(stopToken.getLine()).col(endCol).build())
        .text(context.getText()).build();
  }
  
  public HdesNode.Token getToken() {
    return token;
  }
}
