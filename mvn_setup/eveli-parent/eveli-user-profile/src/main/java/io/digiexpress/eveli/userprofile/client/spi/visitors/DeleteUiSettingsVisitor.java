package io.digiexpress.eveli.userprofile.client.spi.visitors;

import java.util.Arrays;

/*-
 * #%L
 * thena-Projects-client
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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.digiexpress.eveli.userprofile.client.api.model.ImmutableUiSettings;
import io.digiexpress.eveli.userprofile.client.api.model.UiSettings;
import io.digiexpress.eveli.userprofile.client.spi.UserProfileStore;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocCommands;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocCommitTree;
import io.resys.thena.api.entities.doc.DocContainer.DocObject;
import io.resys.thena.api.envelope.ImmutableQueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.doc.api.DocQueryActions.DocObjectsQuery;
import io.resys.thena.doc.api.ThenaDocConfig;
import io.resys.thena.doc.api.ThenaDocConfig.DocObjectVisitor;
import io.resys.thena.doc.spi.support.DocStoreException;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;


public class DeleteUiSettingsVisitor implements DocObjectVisitor<Optional<UiSettings>> {
  private final String userId;
  private final String settingsId;
  
  public DeleteUiSettingsVisitor(String userId, String settingsId, UserProfileStore ctx) {
    super();
    

    RepoAssert.notEmpty(userId, () -> "userId must be defined!");
    RepoAssert.notEmpty(settingsId, () -> "settingsId must be defined!");
    
    this.userId = userId;
    this.settingsId = settingsId;
  }

  @Override
  public Uni<QueryEnvelope<DocObject>> start(ThenaDocConfig config, DocObjectsQuery builder) {
    
    return builder
        .ownerId(settingsId)
        .parentId(userId)
        .deleteOne()
        .onItem().transform(envelope -> {
          
          if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
            throw DocStoreException.builder("GET_UI_SETTINGS_BY_IDS_FOR_DELETE_FAIL")
              .add(config, envelope)
              .add((callback) -> callback.addArgs(Arrays.asList(userId, settingsId).stream().collect(Collectors.joining(",", "{", "}"))))
              .build();
          }
          
          return ImmutableQueryEnvelope.<DocObject>builder()
              .messages(envelope.getMessages())
              .repo(envelope.getRepo())
              .objects(envelope.getObjects())
              .status(envelope.getStatus())
              .build();
        });
  }

  @Override
  public DocObject visitEnvelope(ThenaDocConfig config, QueryEnvelope<DocObject> envelope) {
    final var result = envelope.getObjects();
    return result;
  }

  @Override
  public Optional<UiSettings> end(ThenaDocConfig config, DocObject blob) {
    if(blob == null) {
      return Optional.empty();
    }
    
    final var deleted = blob.accept((
        Doc doc, 
        DocBranch docBranch, 
        Map<String, DocCommit> commit, 
        List<DocCommands> _commands,
        List<DocCommitTree> trees
    ) -> docBranch.getValue().mapTo(ImmutableUiSettings.class));
    
    return deleted.stream().findFirst().map(e -> e);
  }

}
