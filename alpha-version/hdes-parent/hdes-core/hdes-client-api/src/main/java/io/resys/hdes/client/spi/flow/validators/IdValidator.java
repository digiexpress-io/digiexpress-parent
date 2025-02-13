package io.resys.hdes.client.spi.flow.validators;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
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

import org.apache.commons.lang3.StringUtils;

import io.resys.hdes.client.api.ast.AstBody.CommandMessageType;
import io.resys.hdes.client.api.ast.AstFlow.AstFlowRoot;
import io.resys.hdes.client.api.ast.ImmutableAstCommandMessage;
import io.resys.hdes.client.api.ast.ImmutableAstFlow;
import io.resys.hdes.client.spi.config.HdesClientConfig.AstFlowNodeVisitor;
import io.resys.hdes.client.spi.flow.ast.AstFlowNodesFactory;

public class IdValidator implements AstFlowNodeVisitor {

  @Override
  public void visit(AstFlowRoot node, ImmutableAstFlow.Builder modelBuilder) {
    if(node.getId() == null) {
      modelBuilder.addMessages(
          ImmutableAstCommandMessage.builder()
          .line(0)
          .value("flow must have id")
          .range(AstFlowNodesFactory.range().build(0, 0))
          .type(CommandMessageType.ERROR)
          .build());
      return;
    }
    if(StringUtils.isEmpty(node.getId().getValue())) {
      modelBuilder.addMessages(
          ImmutableAstCommandMessage.builder()
          .line(node.getId().getStart())
          .value("flow id must have a value")
          .range(AstFlowNodesFactory.range().build(0, node.getId().getSource().getValue().length()))
          .type(CommandMessageType.ERROR)
          .build());
    }
  }
}
