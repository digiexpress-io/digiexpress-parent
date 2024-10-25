package io.digiexpress.eveli.assets.spi.exceptions;

/*-
 * #%L
 * eveli-assets
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import io.digiexpress.eveli.assets.api.EveliAssetClient.EntityType;
import io.resys.thena.docdb.api.actions.ObjectsActions.ObjectsResult;
import io.resys.thena.docdb.api.models.Message;



public class QueryException extends RuntimeException {
  private static final long serialVersionUID = 7190168525508589141L;
  
  private final String entityId;
  private final EntityType type;
  private final List<Message> commit;
  
  public QueryException(String entityId, EntityType type, ObjectsResult<?> commit) {
    super(msg(entityId, type, commit.getMessages()));
    this.entityId = entityId;
    this.type = type;
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
    
    return new StringBuilder("Can't find entity: ").append(type)
        .append(", id: ").append(entityId)
        .append(", because of: ").append(messages)
        .toString();
  }
}
