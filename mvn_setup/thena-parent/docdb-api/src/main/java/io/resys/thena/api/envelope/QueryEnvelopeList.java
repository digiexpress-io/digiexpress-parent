package io.resys.thena.api.envelope;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import javax.annotation.Nullable;

import org.immutables.value.Value;
import org.slf4j.Logger;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.ThenaObjects;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.exceptions.RepoException;


@Value.Immutable
public interface QueryEnvelopeList<T extends ThenaObjects> extends ThenaEnvelope {
  @Nullable
  Tenant getRepo();    
  @Nullable
  List<T> getObjects();
  
  QueryEnvelopeStatus getStatus();
  List<Message> getMessages();
  
  public static <T extends ThenaObjects> QueryEnvelopeList<T> repoNotFound(String repoId, Logger log) {
    final var ex = RepoException.builder().notRepoWithName(repoId);
    log.warn(ex.getText());
    return ImmutableQueryEnvelopeList
        .<T>builder()
        .status(QueryEnvelopeStatus.ERROR)
        .addMessages(ex)
        .build();
  }  
  
}
