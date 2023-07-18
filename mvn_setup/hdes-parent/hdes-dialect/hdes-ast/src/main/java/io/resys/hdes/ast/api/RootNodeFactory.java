package io.resys.hdes.ast.api;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.resys.hdes.ast.HdesLexer;
import io.resys.hdes.ast.HdesParser;
import io.resys.hdes.ast.api.nodes.BodyNode;
import io.resys.hdes.ast.api.nodes.HdesNode.ErrorNode;
import io.resys.hdes.ast.api.nodes.RootNode;
import io.resys.hdes.ast.spi.ImmutableRootNode;
import io.resys.hdes.ast.spi.antlr.util.HdesAntlrErrorListener;
import io.resys.hdes.ast.spi.antlr.visitors.HdesParserVisitor;
import io.resys.hdes.ast.spi.antlr.visitors.HdesParserVisitor.ContentNode;
import io.resys.hdes.ast.spi.antlr.visitors.RootNodeDependencyVisitor;
import io.resys.hdes.ast.spi.validators.RootNodeValidator;

public interface RootNodeFactory {
  
  interface Builder {
    Builder ignoreErrors();
    SourceBuilder<Builder> add();
    RootNode build();
  }
  
  interface SourceBuilder<R> {
    SourceBuilder<R> externalId(String externalId);
    R src(String src);
  }
  

  public static Builder builder() {
    return new GenericBuilder();
  }
  
  public static class GenericBuilder implements Builder {
    private static final Logger LOGGER = LoggerFactory.getLogger(RootNodeFactory.class);
    private final Map<String, BodyNode> body = new HashMap<>();
    private final Map<String, List<ErrorNode>> errors = new HashMap<>();
    private final Map<String, String> origin = new HashMap<>();
    private final HdesParserVisitor visitor = new HdesParserVisitor();
    private boolean ignoreErrors;
    
    @Override
    public Builder ignoreErrors() {
      this.ignoreErrors = true;
      return this;
    }
    @Override
    public RootNode build() {
      RootNode result = new RootNodeDependencyVisitor()
          .visitBody(
              new ImmutableRootNode(
                Collections.unmodifiableMap(body), 
                Collections.unmodifiableMap(origin),
                Collections.unmodifiableMap(errors)));
      List<ErrorNode> logicalErrors = new ArrayList<>(new RootNodeValidator().visitBody(result).getErrors());
      if(LOGGER.isDebugEnabled()) {
        log(this.errors, logicalErrors);
      }
      
      if(!ignoreErrors) {
        List<ErrorNode> errors = new ArrayList<>();
        
        // collect all errors
        this.errors.values().forEach(v -> errors.addAll(v));
        errors.addAll(logicalErrors);
        
        if(!errors.isEmpty()) {
          throw new HdesException(errors);
        }
      }      
      return result;
    }

    @Override
    public SourceBuilder<Builder> add() {
      Builder parent = this;
      MutableInt naming = new MutableInt(1);
      return new GenericSourceBuilder() {
        private String externalId;
        
        private Builder addBody(ContentNode node) {
          for(BodyNode tree : node.getValues()) {
            String id = tree.getId().getValue();
            if(body.containsKey(id)) {
              String previousOrigin = origin.get(id);
              errorListener().conflict(tree, externalId, previousOrigin);
            } else {
              body.put(id, tree);
              origin.put(id, externalId);
            }
          }          
          return parent;
        }
        
        private HdesAntlrErrorListener errorListener() {
          final List<ErrorNode> nodes;
          if(errors.containsKey(externalId)) {
            nodes = errors.get(externalId);
          } else {
            nodes = new ArrayList<>();
            errors.put(externalId, nodes);
          }
          return new HdesAntlrErrorListener(externalId, nodes);
        }
        
        @Override
        public SourceBuilder<Builder> externalId(String externalId) {
          String name = externalId == null ? "undefined" : externalId;
          String newName = name;
          while(origin.values().contains(newName)) {
            newName = name + "_" + naming.getAndIncrement();
          }
          
          this.externalId = newName;
          return this;
        }
        
        @Override
        public Builder src(String src) {
          HdesLexer lexer = new HdesLexer(CharStreams.fromString(src));
          CommonTokenStream tokens = new CommonTokenStream(lexer);
          HdesParser parser = new HdesParser(tokens);
          parser.getErrorListeners().clear();
          parser.addErrorListener(errorListener());
          ParseTree tree = parser.hdesContent();
          try {
            ContentNode contents = (ContentNode) tree.accept(visitor);
            return addBody(contents);
          } catch(Exception e) {
            errorListener().add(e);
            return parent;
          }
        }
      };
    } 
  
    private static void log(Map<String, List<ErrorNode>> errors, List<ErrorNode> logicalErrors) {
      StringBuilder debugger = new StringBuilder();
      
      for(Map.Entry<String, List<ErrorNode>> entries : errors.entrySet()) {
        if(entries.getValue().isEmpty()) {
          continue;
        }
        debugger.append("Syntax error in: '").append(entries.getKey()).append("'").append(System.lineSeparator());
        
        for(ErrorNode node : entries.getValue()) {
          debugger.append("  ").append(node.getBodyId())
          .append(" at (")
          .append(node.getTarget().getToken().getStart().getLine())
          .append(":")
          .append(node.getTarget().getToken().getStart().getCol())
          .append(") ");
          
          if(node.getTargetLink().isPresent()) {
            debugger
            .append("and at (")
            .append(node.getTargetLink().get().getToken().getStart().getLine())
            .append(":")
            .append(node.getTargetLink().get().getToken().getStart().getCol())
            .append(") ");
          }
          
          debugger
          .append(node.getMessage())
          .append(System.lineSeparator());
        }
      }
      
      if(!logicalErrors.isEmpty()) {
        debugger.append(System.lineSeparator());
        debugger.append("Logical errors: ").append(System.lineSeparator());
        for(ErrorNode node : logicalErrors) {
          debugger.append("  ").append(node.getBodyId())
          .append(" at (")
          .append(node.getTarget().getToken().getStart().getLine())
          .append(":")
          .append(node.getTarget().getToken().getStart().getCol())
          .append(") ");
          
          if(node.getTargetLink().isPresent()) {
            debugger
            .append("and at (")
            .append(node.getTargetLink().get().getToken().getStart().getLine())
            .append(":")
            .append(node.getTargetLink().get().getToken().getStart().getCol())
            .append(") ");
          }
          
          debugger
          .append(node.getMessage())
          .append(System.lineSeparator());
        }
      }
      String msg = debugger.toString();
      if(!msg.isEmpty()) {
        LOGGER.debug(msg);
      }
    }
  }
  public static abstract class GenericSourceBuilder implements SourceBuilder<Builder> {}
}
