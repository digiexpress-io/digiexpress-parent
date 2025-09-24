package io.digiexpress.eveli.userprofile.client.spi.visitors;

/*-
 * #%L
 * eveli-user-profile
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import io.digiexpress.eveli.userprofile.client.api.UserProfileClient.UiSettingsNotFoundException;
import io.digiexpress.eveli.userprofile.client.api.model.ImmutableUiSettings;
import io.digiexpress.eveli.userprofile.client.api.model.UiSettings;
import io.digiexpress.eveli.userprofile.client.spi.support.DataConstants;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocCommands;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocCommitTree;
import io.resys.thena.api.entities.doc.DocContainer.DocObject;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.doc.api.ThenaDocConfig;
import io.resys.thena.doc.api.DocQueryActions.DocObjectsQuery;
import io.resys.thena.doc.api.ThenaDocConfig.DocObjectVisitor;
import io.resys.thena.doc.spi.support.DocStoreException;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GetUserUiSettingsVisitor implements DocObjectVisitor<UiSettings>{
  private final String userId;
  private final String settingsId;
  
  @Override
  public Uni<QueryEnvelope<DocObject>> start(ThenaDocConfig config, DocObjectsQuery query) {
    return query.docType(DataConstants.DOC_TYPE_USER_PROFILE_SETTINGS).parentId(userId).ownerId(settingsId).get();
  }
  @Override
  public DocObject visitEnvelope(ThenaDocConfig config, QueryEnvelope<DocObject> envelope) {
    if(envelope.isNotFound()) {
      throw new UiSettingsNotFoundException("Can't find ui settings for user profile id: " + userId  + ", owner id: " + settingsId + "!");
    }
    
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocStoreException.builder("GET_UI_SETTINGS_BY_USER_PROFILE_ID_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(userId, settingsId))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null) {
      throw DocStoreException.builder("GET_UI_SETTINGS_BY_USER_PROFILE_ID_NOT_FOUND")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(userId, settingsId))
        .build();
    }
    return result;
  }

  @Override
  public UiSettings end(ThenaDocConfig config, DocObject ref) {
    return ref.accept((
        Doc doc, 
        DocBranch docBranch, 
        Map<String, DocCommit> commit, 
        List<DocCommands> commands,
        List<DocCommitTree> trees
    ) -> mapToUiSettings(docBranch)).iterator().next();
  }

  public static UiSettings mapToUiSettings(DocBranch docBranch) {
    return docBranch.getValue().mapTo(ImmutableUiSettings.class);
  }
}
