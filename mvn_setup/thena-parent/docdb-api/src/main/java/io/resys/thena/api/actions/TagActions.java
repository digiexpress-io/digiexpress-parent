package io.resys.thena.api.actions;

/*-
 * #%L
 * thena-docdb-api
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
import java.util.Optional;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.resys.thena.api.entities.git.Tag;
import io.resys.thena.api.envelope.Message;
import io.resys.thena.api.envelope.ThenaEnvelope;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface TagActions {

  TagBuilder tagBuilder();
  TagQuery tagQuery();
  
  interface TagQuery {
    TagQuery tagName(String tagName);
    
    Multi<Tag> findAll();
    Uni<Optional<Tag>> get();
    Uni<Optional<Tag>> delete();
  }
  
  interface TagBuilder {
    TagBuilder tagName(String name);
    TagBuilder head(String branchNameOrCommitOrTag);
    TagBuilder author(String author);
    TagBuilder message(String message);    
    Uni<TagResult> build();
  }

  enum TagResultStatus {
    OK, ERROR
  }

  @Value.Immutable
  interface TagResult extends ThenaEnvelope {
    @Nullable
    Tag getTag();
    TagResultStatus getStatus();
    List<Message> getMessages();
  }
}
