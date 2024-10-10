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

import java.util.ArrayList;
import java.util.List;

import org.immutables.value.Value;

import io.resys.hdes.ast.api.HdesException;
import io.resys.hdes.ast.api.nodes.BodyNode;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.HdesNode;
import io.resys.hdes.ast.api.nodes.HdesNode.ErrorNode;
import io.resys.hdes.ast.api.nodes.HdesTree.DecisionTableTree;
import io.resys.hdes.ast.api.nodes.HdesTree.FlowTree;
import io.resys.hdes.ast.api.nodes.HdesTree.RootTree;
import io.resys.hdes.ast.api.nodes.HdesTree.ServiceTree;
import io.resys.hdes.ast.api.nodes.RootNode;
import io.resys.hdes.ast.api.nodes.ServiceNode.ServiceBody;
import io.resys.hdes.ast.api.visitors.HdesVisitor.RootNodeVisitor;
import io.resys.hdes.ast.spi.ImmutableHdesTree;
import io.resys.hdes.ast.spi.validators.RootNodeValidator.RootNodeErrors;

public class RootNodeValidator implements RootNodeVisitor<RootNodeErrors, RootNodeErrors> {

  private final DecisionTableValidator decisionTableValidator = new DecisionTableValidator();
  private final FlowValidator flowValidator = new FlowValidator();
  private final ServiceValidator serviceValidator = new ServiceValidator();
  
  @Value.Immutable
  public interface RootNodeErrors {
    List<ErrorNode> getErrors();
  }

  @Override
  public RootNodeErrors visitBody(RootNode root) {
    RootTree ctx = (RootTree) ImmutableHdesTree.builder().value(root).build();
    List<ErrorNode> errors = new ArrayList<>();
    root.getBody().values().stream().forEach(s -> errors.addAll(visitNode(s, ctx).getErrors()));
    return ImmutableRootNodeErrors.builder().errors(errors).build();
  }

  @Override
  public RootNodeErrors visitFlow(FlowTree ctx) {
    return flowValidator.visitBody(ctx);
  }

  @Override
  public RootNodeErrors visitDecisionTable(DecisionTableTree ctx) {
    return decisionTableValidator.visitBody(ctx);
  }
  
  @Override
  public RootNodeErrors visitService(ServiceTree ctx) {
    return serviceValidator.visitBody(ctx);
  }

  @Override
  public RootNodeErrors visitNode(BodyNode ast, RootTree ctx) {
    if (ast instanceof DecisionTableBody) {
      return visitDecisionTable((DecisionTableTree) ctx.next((DecisionTableBody) ast));
    } else if (ast instanceof FlowBody) {
      return visitFlow((FlowTree) ctx.next((FlowBody) ast));
    } else if (ast instanceof ServiceBody) {
      return ImmutableRootNodeErrors.builder().build();
    }
    throw new HdesException(unknownAst(ast));
  }
  
  private String unknownAst(HdesNode ast) {
    return new StringBuilder()
        .append("Unknown AST: ").append(ast.getClass())
        .append("  - ").append(ast.getClass()).append(System.lineSeparator())
        .append("  supported types are: ").append(System.lineSeparator())
        .append("    - ").append(DecisionTableBody.class).append(System.lineSeparator())
        .append("    - ").append(ServiceBody.class).append(System.lineSeparator())
        .append("    - ").append(FlowBody.class).append(System.lineSeparator())
        .toString();
  }
}
