package io.resys.hdes.client.spi.composer;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2023 Copyright 2020 ReSys OÜ
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


import io.resys.hdes.client.api.HdesComposer.ComposerState;
import io.resys.hdes.client.api.HdesStore;
import io.resys.hdes.client.api.ImmutableCreateStoreEntity;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.ast.AstCommand;
import io.resys.hdes.client.api.ast.AstCommand.AstCommandValue;
import io.resys.hdes.client.api.ast.ImmutableAstCommand;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CreateBranchVisitor {

  private final ComposerState state;
  private final List<HdesStore.CreateStoreEntity> result = new ArrayList<>();

  public List<HdesStore.CreateStoreEntity> visitCommands(List<AstCommand> commands) {
    for (final var command : commands) {
      visitCommand(command);
    }
    return result;
  }

  private void visitCommand(AstCommand command) {
    switch (command.getType()) {
      case CREATE_BRANCH:
        visitCreateBranch(command);
        break;
      default:
        throw new IllegalArgumentException("Not supported command type: " + command.getType());
    }
  }

  private void visitCreateBranch(AstCommand command) {
    final var tagId = command.getId();
    final var branchName = command.getValue();
    final var tagComposerEntity= Objects.requireNonNull(state.getTags().get(tagId), () -> "Tag '" + tagId + "' not found!");
    final var tag = Objects.requireNonNull(tagComposerEntity.getAst(), () -> "AstTag '" + tagId + "' not found!");

    result.add(ImmutableCreateStoreEntity.builder()
        .bodyType(AstBodyType.BRANCH)
        .addBody(
          ImmutableAstCommand.builder().type(AstCommandValue.SET_BRANCH_NAME).value(branchName).build(),
          ImmutableAstCommand.builder().type(AstCommandValue.SET_BRANCH_CREATED).value(String.valueOf(LocalDateTime.now())).build(),
          ImmutableAstCommand.builder().type(AstCommandValue.SET_BRANCH_TAG).value(tagId).build()
        )
        .build());

    final var decisions = tag.getValues().stream().filter(astTagValue -> astTagValue.getBodyType().equals(AstBodyType.DT)).collect(Collectors.toList());
    final var flows = tag.getValues().stream().filter(astTagValue -> astTagValue.getBodyType().equals(AstBodyType.FLOW)).collect(Collectors.toList());
    final var flowTasks = tag.getValues().stream().filter(astTagValue -> astTagValue.getBodyType().equals(AstBodyType.FLOW_TASK)).collect(Collectors.toList());

    for (final var decision : decisions) {
      result.add(ImmutableCreateStoreEntity.builder()
          .bodyType(AstBodyType.DT)
          .body(decision.getCommands())
          .build());
    }
    for (final var flow : flows) {
      result.add(ImmutableCreateStoreEntity.builder()
          .bodyType(AstBodyType.FLOW)
          .body(flow.getCommands())
          .build());
    }
    for (final var flowTask : flowTasks) {
      result.add(ImmutableCreateStoreEntity.builder()
          .bodyType(AstBodyType.FLOW_TASK)
          .body(flowTask.getCommands())
          .build());
    }
  }
}
