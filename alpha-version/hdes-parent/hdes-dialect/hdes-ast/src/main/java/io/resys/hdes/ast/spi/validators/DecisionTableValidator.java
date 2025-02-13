package io.resys.hdes.ast.spi.validators;

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
import java.util.Optional;
import java.util.stream.Collectors;

import io.resys.hdes.ast.api.HdesException;
import io.resys.hdes.ast.api.nodes.BodyNode.ContextTypeDef;
import io.resys.hdes.ast.api.nodes.BodyNode.Headers;
import io.resys.hdes.ast.api.nodes.BodyNode.Literal;
import io.resys.hdes.ast.api.nodes.BodyNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.BodyNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.BodyNode.ScalarType;
import io.resys.hdes.ast.api.nodes.BodyNode.TypeDef;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicy;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyAll;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyFirst;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyMapping;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.MappingRow;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.RuleRow;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.ThenRuleRow;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.WhenRuleRow;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;
import io.resys.hdes.ast.api.nodes.HdesTree.DecisionTableTree;
import io.resys.hdes.ast.api.nodes.HdesTree.TypeDefAccepts;
import io.resys.hdes.ast.api.nodes.HdesTree.TypeDefReturns;
import io.resys.hdes.ast.api.nodes.HdesTree;
import io.resys.hdes.ast.api.nodes.ImmutableErrorNode;
import io.resys.hdes.ast.api.visitors.DecisionTableVisitor;
import io.resys.hdes.ast.spi.validators.RootNodeValidator.RootNodeErrors;

public class DecisionTableValidator implements DecisionTableVisitor<RootNodeErrors, RootNodeErrors> {

  @Override
  public RootNodeErrors visitBody(DecisionTableTree ctx) {
    ImmutableRootNodeErrors.Builder result = ImmutableRootNodeErrors.builder();
    
    DecisionTableBody body = ctx.getValue();
    result.addAllErrors(visitHeaders(body.getHeaders(), ctx).getErrors());
    result.addAllErrors(visitHitPolicy(body.getHitPolicy(), ctx).getErrors());
    return result.build();
  }

  @Override
  public RootNodeErrors visitHeaders(Headers node, HdesTree ctx) {
    ImmutableRootNodeErrors.Builder result = ImmutableRootNodeErrors.builder();
    HdesTree next = ctx.next(node);
    node.getAcceptDefs().forEach(t -> result.addAllErrors(visitHeader(t, next).getErrors()));
    node.getReturnDefs().forEach(t -> result.addAllErrors(visitHeader(t, next).getErrors()));
    return result.build();
  }

  @Override
  public RootNodeErrors visitHeader(TypeDef node, HdesTree ctx) {
    ImmutableRootNodeErrors.Builder result = ImmutableRootNodeErrors.builder();
    if(node.getArray()) {
      return result.addErrors(ImmutableErrorNode.builder()
        .bodyId(ctx.get().bodyId())
        .target(node)
        .message("Array types are not supported on decision tables!")
        .build()).build();
    }
    
    if(node instanceof ScalarDef) {
      return visitHeader((ScalarDef) node, ctx);
    }
    return visitHeader((ObjectDef) node, ctx);
  }

  @Override
  public RootNodeErrors visitHeader(ScalarDef node, HdesTree ctx) {
    ImmutableRootNodeErrors.Builder result = ImmutableRootNodeErrors.builder();
    if(node.getFormula().isPresent()) {
      result.addAllErrors(visitFormula(node, ctx).getErrors());
    }
    return result.build();
  }

  @Override
  public RootNodeErrors visitHeader(ObjectDef node, HdesTree ctx) {
    return ImmutableRootNodeErrors.builder().addErrors(ImmutableErrorNode.builder()
        .bodyId(ctx.get().bodyId())
        .target(node)
        .message("Object types are not supported on decision tables!")
        .build()).build();
  }

  @Override
  public RootNodeErrors visitFormula(ScalarDef node, HdesTree ctx) {
    ExpressionBody formula = node.getFormula().get();
    TypeDefReturns formulaDef = ctx.next(node).returns().build(formula);
    TypeDef returns = formulaDef.getReturns();
    
    ImmutableRootNodeErrors.Builder result = ImmutableRootNodeErrors.builder();
    if(!(returns instanceof ScalarDef)) {
      result.addErrors(ImmutableErrorNode.builder()
              .bodyId(ctx.get().bodyId())
              .target(formula)
              .message("Formula expression: '" + formula.getSrc() + "' returns: OBJECT but expecting: " + node.getType() + "!")
              .build());
    } else if(node.getType() != ((ScalarDef) returns).getType()) {
      ScalarDef def = (ScalarDef) returns;
      result.addErrors(ImmutableErrorNode.builder()
          .bodyId(ctx.get().bodyId())
          .target(formula)
          .message("Formula expression: '" + formula.getSrc() + "' returns: " + def.getType() + " but expecting: " + node.getType() + "!")
          .build());
    }
    
    // input formulas, can't contain types produced in output
    if(node.getContext() == ContextTypeDef.ACCEPTS) {
      for(TypeDefAccepts def : formulaDef.getAccepts()) {
        
        if( def.getNode().getContext() == ContextTypeDef.MATCHES || 
            def.getNode().getContext() == ContextTypeDef.RETURNS) {
          
          result.addErrors(ImmutableErrorNode.builder()
              .bodyId(ctx.get().bodyId())
              .target(def.getInvocation())
              .message("Accepts formula expression: '" + formula.getSrc() + "' can't use def: '" + def.getNode().getName()  + "' because it's only available in returns formulas!")
              .build());
        }
      }
    }
    return result.build();
  }

  @Override
  public RootNodeErrors visitHitPolicy(HitPolicy node, HdesTree ctx) {
    if(node instanceof HitPolicyFirst) {
      return visitHitPolicyFirst((HitPolicyFirst) node, ctx);
    } else if(node instanceof HitPolicyAll) {
      return visitHitPolicyAll((HitPolicyAll) node, ctx);
    }
    return visitHitPolicyMapping((HitPolicyMapping) node, ctx);
  }
  
  @Override
  public RootNodeErrors visitHitPolicyMapping(HitPolicyMapping node, HdesTree ctx) {
    HdesTree next = ctx.next(node);
    List<TypeDef> accepts = ctx.get().body().getHeaders().getAcceptDefs();
    ScalarType defFrom = node.getDefFrom();
    
    ImmutableRootNodeErrors.Builder result = ImmutableRootNodeErrors.builder();
    for(MappingRow row : node.getMapsTo()) {
      String id = row.getAccepts().getValue();
      Optional<ScalarDef> typeDef = accepts.stream()
          .filter(r -> r.getName().equals(id))
          .filter(r -> r instanceof ScalarDef)
          .map(r -> (ScalarDef) r)
          .findAny();
      
      if(typeDef.isEmpty()) {
        result.addErrors(ImmutableErrorNode.builder()
            .bodyId(ctx.get().bodyId())
            .target(row.getAccepts())
            .message("Mapping row type: " + id + " is not defined in accepts!")
            .build());
        
      } else if(!typeDef.get().getType().equals(defFrom)) {
        result.addErrors(ImmutableErrorNode.builder()
            .bodyId(ctx.get().bodyId())
            .target(row.getAccepts())
            .message("Mapping row type: " + id + " is: " + typeDef.get().getType() + "  but expecting: " + defFrom + "!")
            .build());
      } else {
        result.addAllErrors(visitMappingRow(row, next).getErrors());
      }
    }
    
    
    // When expressions
    for(ExpressionBody expression : node.getWhen().getValues()) {
      
      for(MappingRow row : node.getMapsTo()) {
        String id = row.getAccepts().getValue();
        Optional<ScalarDef> returns = accepts.stream()
            .filter(t -> t.getName().equals(id)).findFirst()
            .filter(r -> r instanceof ScalarDef)
            .map(r -> (ScalarDef) r);

        if(returns.isEmpty()) {
          continue;
        }
        
        TypeDef returnsDef;
        try {
          returnsDef = next.next(returns.get()).returns().build(expression).getReturns();
        } catch(HdesException e) {
          if(e.getErrors().isEmpty()) {
            result.addErrors(ImmutableErrorNode.builder()
                .bodyId(ctx.get().bodyId())
                .target(row)
                .message(e.getMessage())
                .build());
          } else {
            result.addAllErrors(e.getErrors().stream()
                .map(v -> ImmutableErrorNode.builder().from(v).targetLink(row).build())
                .collect(Collectors.toList()));
          }
          continue;
        }
        
        if(!(returnsDef instanceof ScalarDef)) {
          result.addErrors(ImmutableErrorNode.builder()
              .bodyId(ctx.get().bodyId())
              .target(expression)
              .message("When expression: '" + expression.getSrc() + "' returns: OBJECT but expecting: boolean!")
              .build());
        }
        
        ScalarDef scalar = (ScalarDef) returnsDef;
        if(scalar.getType() != ScalarType.BOOLEAN) {
          result.addErrors(ImmutableErrorNode.builder()
              .bodyId(ctx.get().bodyId())
              .target(expression)
              .message("When expression: '" + expression.getSrc() + "' returns: " + scalar.getType() + " but expecting: boolean!")
              .build());
        }
        
      }
    }      
    
    return result.build();
  }

  @Override
  public RootNodeErrors visitHitPolicyAll(HitPolicyAll node, HdesTree ctx) {
    HdesTree next = ctx.next(node);
    ImmutableRootNodeErrors.Builder result = ImmutableRootNodeErrors.builder();
    node.getRows().forEach(row -> result.addAllErrors(visitRuleRow(row, next).getErrors()));
    return result.build();
  }

  @Override
  public RootNodeErrors visitHitPolicyFirst(HitPolicyFirst node, HdesTree ctx) {
    HdesTree next = ctx.next(node);
    ImmutableRootNodeErrors.Builder result = ImmutableRootNodeErrors.builder();
    node.getRows().forEach(row -> result.addAllErrors(visitRuleRow(row, next).getErrors()));
    return result.build();
  }

  @Override
  public RootNodeErrors visitRuleRow(RuleRow node, HdesTree ctx) {
    Headers headers = ctx.get().body().getHeaders();
    List<ScalarDef> accepts = headers.getAcceptDefs().stream()
        .filter(r -> r instanceof ScalarDef)
        .map(r -> (ScalarDef) r)
        .collect(Collectors.toList());
    List<ScalarDef> returns = headers.getReturnDefs().stream()
        .filter(r -> r instanceof ScalarDef)
        .map(r -> (ScalarDef) r)
        .filter(r -> r.getFormula().isEmpty())
        .collect(Collectors.toList());
    
    ImmutableRootNodeErrors.Builder result = ImmutableRootNodeErrors.builder();
    
    boolean whenFail = node.getWhen().getValues().size() != accepts.size();
    if(whenFail) {
      result.addErrors(ImmutableErrorNode.builder()
          .bodyId(ctx.get().bodyId())
          .target(node.getWhen())
          .message("When is expecting: " + accepts.size() + " elements but got: " + node.getWhen().getValues().size() + "!")
          .build());  
    }
    
    boolean thenFail = node.getThen().getValues().size() != returns.size();
    if(thenFail) {
      result.addErrors(ImmutableErrorNode.builder()
          .bodyId(ctx.get().bodyId())
          .target(node.getThen())
          .message("Then is expecting: " + returns.size() + " elements but got: " + node.getThen().getValues().size() + "!")
          .build());  
    }
    
    if(whenFail || thenFail) {
      return result.build();      
    }
    
    int placeholder = 0;
    for(Literal literal : node.getThen().getValues()) {
      ScalarDef defFrom = returns.get(placeholder);
      
      if(literal.getType() != defFrom.getType()) {
        result.addErrors(ImmutableErrorNode.builder()
            .bodyId(ctx.get().bodyId())
            .target(literal)
            .message("Then value: '" + literal.getValue() + "' is: " + literal.getType() + "  but expecting: " + defFrom.getType() + "!")
            .build());  
      }
      
      placeholder++;
    }
    

    placeholder = -1;
    for(ExpressionBody expression : node.getWhen().getValues()) {
      ScalarDef defFrom = accepts.get(++placeholder);
      HdesTree next = ctx.next(defFrom);
      
      TypeDef returnsDef;
      try {
        returnsDef = next.returns().build(expression).getReturns();
      } catch(HdesException e) {
        if(e.getErrors().isEmpty()) {
          result.addErrors(ImmutableErrorNode.builder()
              .bodyId(ctx.get().bodyId())
              .target(expression)
              .message(e.getMessage())
              .build());
        } else {
          result.addAllErrors(e.getErrors());
        }
        continue;
      }
      
      if(!(returnsDef instanceof ScalarDef)) {
        result.addErrors(ImmutableErrorNode.builder()
            .bodyId(ctx.get().bodyId())
            .target(expression)
            .message("When expression: '" + expression.getSrc() + "' returns: OBJECT but expecting: boolean!")
            .build());
      } else if(((ScalarDef) returnsDef).getType() != ScalarType.BOOLEAN) {
        result.addErrors(ImmutableErrorNode.builder()
            .bodyId(ctx.get().bodyId())
            .target(expression)
            .message("When expression: '" + expression.getSrc() + "' returns: '" + ((ScalarDef) returnsDef).getType() + "' but expecting: boolean!")
            .build()); 
      }
    }
    
    return result.build();
  }

  @Override
  public RootNodeErrors visitMappingRow(MappingRow node, HdesTree ctx) {
    HitPolicyMapping hitPolicy = ctx.get().node(HitPolicyMapping.class);
    ScalarType defTo = hitPolicy.getDefTo();
    
    ImmutableRootNodeErrors.Builder result = ImmutableRootNodeErrors.builder();
    for(Literal literal : node.getThen().getValues()) {
      if(literal.getType() != defTo) {
        result.addErrors(ImmutableErrorNode.builder()
            .bodyId(ctx.get().bodyId())
            .target(literal)
            .message("Mapping row value: '" + literal.getValue() + "' is: " + literal.getType() + " but expecting: " + defTo + "!")
            .build());  
      }
    }
    
    boolean thenFail = node.getThen().getValues().size() != hitPolicy.getWhen().getValues().size();
    if(thenFail) {
      result.addErrors(ImmutableErrorNode.builder()
          .bodyId(ctx.get().bodyId())
          .target(node)
          .message("Mapping row: '" + node.getAccepts().getValue() + "' is expecting: " + hitPolicy.getWhen().getValues().size() + " elements but got: " + node.getThen().getValues().size() + "!")
          .build());  
    }
    
    Optional<TypeDef> returns = ctx.get().body().getHeaders().getReturnDefs().stream()
        .filter(t -> t.getName().equals(node.getAccepts().getValue()))
        .findFirst();
    
    if(returns.isPresent()) {
      result.addErrors(ImmutableErrorNode.builder()
          .bodyId(ctx.get().bodyId())
          .target(returns.get())
          .message("Mapping row type name: '" + node.getAccepts().getValue() + "' can't have the same name as returns type !")
          .build());  
    }
    
    return result.build();
  }

  @Override
  public RootNodeErrors visitWhenRuleRow(WhenRuleRow node, HdesTree ctx) {
    return ImmutableRootNodeErrors.builder().build();
  }

  @Override
  public RootNodeErrors visitThenRuleRow(ThenRuleRow node, HdesTree ctx) {
    return ImmutableRootNodeErrors.builder().build();
  }
}
