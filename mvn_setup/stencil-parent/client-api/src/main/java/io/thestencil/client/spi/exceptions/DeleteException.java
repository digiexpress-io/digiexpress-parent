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

import java.util.List;

import io.resys.thena.api.actions.DocCommitActions.OneDocEnvelope;
import io.resys.thena.api.actions.GitCommitActions.CommitResultEnvelope;
import io.resys.thena.api.actions.GitPullActions;
import io.resys.thena.api.envelope.Message;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.thestencil.client.api.StencilClient.Entity;
import io.thestencil.client.api.StencilClient.EntityType;

public class DeleteException extends RuntimeException {
  private static final long serialVersionUID = 7190168525508589141L;
  
  private final String entityId;
  private final EntityType type;
  private final List<Message> commit;
  
  public DeleteException(String entityId, EntityType type, QueryEnvelope<GitPullActions.PullObjects> commit) {
    super(msg(entityId, type, commit.getMessages()));
    this.entityId = entityId;
    this.type = type;
    this.commit = commit.getMessages();
  }
  public DeleteException(Entity<?> entity, CommitResultEnvelope commit) {
    super(msg(entity.getId(), entity.getType(), commit.getMessages()));
    this.entityId = entity.getId();
    this.type = entity.getType();
    this.commit = commit.getMessages();
  }
  
  public DeleteException(Entity<?> entity, OneDocEnvelope commit) {
    super(msg(entity.getId(), entity.getType(), commit.getMessages()));
    this.entityId = entity.getId();
    this.type = entity.getType();
    this.commit = commit.getMessages();
  }

  public String getEntityId() {
    return entityId;
  }
  public EntityType getType() {
    return type;
  }
  public List<Message> getCommit() {
    return commit;
  }
  private static String msg(String entityId, EntityType type, List<Message> commit) {
    StringBuilder messages = new StringBuilder();
    for(var msg : commit) {
      messages
      .append(System.lineSeparator())
      .append("  - ").append(msg.getText());
    }
    
    return new StringBuilder("Can't delete entity: ").append(type)
        .append(", id: ").append(entityId)
        .append(", because of: ").append(messages)
        .toString();
  }
}
