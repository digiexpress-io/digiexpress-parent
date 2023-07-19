package io.thestencil.client.spi.exceptions;

/*-
 * #%L
 * stencil-client
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

import io.resys.thena.docdb.api.actions.CommitActions.CommitResult;
import io.thestencil.client.api.MigrationBuilder.Sites;
import io.thestencil.client.api.StencilComposer.SiteState;

public class ImportException extends RuntimeException {
  private static final long serialVersionUID = 7190168525508589141L;
  
  private final Object entity;
  private final CommitResult commit;
  
  public ImportException(Sites entity, CommitResult commit) {
    super(msg(entity, commit));
    this.entity = entity;
    this.commit = commit;
  }
  public ImportException(SiteState entity, CommitResult commit) {
    super(msg(entity, commit));
    this.entity = entity;
    this.commit = commit;
  }  
  public Object getEntity() {
    return entity;
  }
  public CommitResult getCommit() {
    return commit;
  }
  
  private static String msg(Object entity, CommitResult commit) {
    StringBuilder messages = new StringBuilder();
    for(var msg : commit.getMessages()) {
      messages
      .append(System.lineSeparator())
      .append("  - ").append(msg.getText());
    }
    return new StringBuilder("Can't import sites because: ")
        .append(messages)
        .append(System.lineSeparator())
        .append(entity.toString())
        .toString();
  }
}
