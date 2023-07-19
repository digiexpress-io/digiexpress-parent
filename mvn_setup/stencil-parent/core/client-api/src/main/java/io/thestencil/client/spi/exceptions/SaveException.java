package io.thestencil.client.spi.exceptions;

/*-
 * #%L
 * stencil-persistence
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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
import java.util.Arrays;
import java.util.List;

import io.resys.thena.docdb.api.actions.CommitActions.CommitResult;
import io.thestencil.client.api.StencilClient.Entity;

public class SaveException extends RuntimeException {
  private static final long serialVersionUID = 7190168525508589141L;
  
  private final List<Entity<?>> entity = new ArrayList<>();
  private final CommitResult commit;
  
  public SaveException(Entity<?> entity, CommitResult commit) {
    super(msg(Arrays.asList(entity), commit));
    this.entity.add(entity);
    this.commit = commit;
  }
  public SaveException(List<Entity<?>> entity, CommitResult commit) {
    super(msg(entity, commit));
    this.entity.addAll(entity);
    this.commit = commit;
  }
  
  public List<Entity<?>> getEntity() {
    return entity;
  }
  public CommitResult getCommit() {
    return commit;
  }
  
  private static String msg(List<Entity<?>> entity, CommitResult commit) {
    StringBuilder messages = new StringBuilder();
    for(var msg : commit.getMessages()) {
      messages
      .append(System.lineSeparator())
      .append("  - ").append(msg.getText());
    }
    return new StringBuilder("Can't save entity: ")
        .append(entity.get(0).getType())
        .append(", because of: ").append(messages)
        .toString();
  }
}
