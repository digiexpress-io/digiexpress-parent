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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.digiexpress.eveli.userprofile.client.api.model.ImmutableUserProfile;
import io.digiexpress.eveli.userprofile.client.api.model.UserProfile;
import io.digiexpress.eveli.userprofile.client.spi.support.DataConstants;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocCommands;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocCommitTree;
import io.resys.thena.api.entities.doc.DocContainer.DocTenantObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.doc.api.ThenaDocConfig;
import io.resys.thena.doc.api.DocQueryActions.DocObjectsQuery;
import io.resys.thena.doc.api.ThenaDocConfig.DocObjectsVisitor;
import io.resys.thena.doc.spi.support.DocStoreException;
import io.smallrye.mutiny.Uni;

public class FindAllUserProfilesVisitor implements DocObjectsVisitor<List<UserProfile>> {
  @Override
  public Uni<QueryEnvelope<DocTenantObjects>> start(ThenaDocConfig config, DocObjectsQuery builder) {
    return builder.docType(DataConstants.DOC_TYPE_USER_PROFILE).findAll();
  }
  @Override
  public DocTenantObjects visitEnvelope(ThenaDocConfig config, QueryEnvelope<DocTenantObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocStoreException.builder("FIND_ALL_USER_PROFILES_FAIL").add(config, envelope).build();
    }
    return envelope.getObjects();
  }

  @Override
  public List<UserProfile> end(ThenaDocConfig config, DocTenantObjects ref) {
    if(ref == null) {
      return Collections.emptyList();
    }
    return ref.accept((
        Doc doc, 
        DocBranch docBranch, 
        Map<String, DocCommit> commit, 
        List<DocCommands> commands,
        List<DocCommitTree> trees) -> mapToUserProfile(docBranch)
    );
  }
  
  public static ImmutableUserProfile mapToUserProfile(DocBranch docBranch) {
    return docBranch.getValue()
      .mapTo(ImmutableUserProfile.class)
      .withVersion(docBranch.getCommitId())
      .withCreated(docBranch.getCreatedAt().toInstant())
      .withUpdated(docBranch.getUpdatedAt().toInstant());
  }
}
