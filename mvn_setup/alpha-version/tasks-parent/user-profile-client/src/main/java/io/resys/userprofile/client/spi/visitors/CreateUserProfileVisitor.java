package io.resys.userprofile.client.spi.visitors;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.resys.thena.api.actions.DocCommitActions.CreateManyDocs;
import io.resys.thena.api.actions.DocCommitActions.ManyDocsEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.spi.DocStoreException;
import io.resys.thena.spi.ThenaDocConfig;
import io.resys.userprofile.client.api.model.UserProfile;
import io.resys.userprofile.client.api.model.UserProfileCommand.CreateUserProfile;
import io.resys.userprofile.client.spi.support.DataConstants;
import io.resys.userprofile.client.spi.visitors.UserProfileCommandVisitor.NoChangesException;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateUserProfileVisitor implements ThenaDocConfig.DocCreateVisitor<UserProfile> {
  private final List<? extends CreateUserProfile> commands;
  private final List<UserProfile> profiles = new ArrayList<UserProfile>();
  
  @Override
  public CreateManyDocs start(ThenaDocConfig config, CreateManyDocs builder) {
    builder
      .commitAuthor(config.getAuthor().get())
      .commitMessage("creating user profile");
    
    for(final var command : commands) {
      try {
        final var entity = new UserProfileCommandVisitor(config).visitTransaction(Arrays.asList(command));
        final var json = JsonObject.mapFrom(entity.getItem1());
        builder.item()
          .docType(DataConstants.DOC_TYPE_USER_PROFILE)
          .branchContent(json)
          .docId(entity.getItem1().getId())
          .externalId(entity.getItem1().getId())
          .commands(entity.getItem2())
          .next();
        profiles.add(entity.getItem1());
      } catch (NoChangesException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }
    return builder;
  }

  @Override
  public List<DocBranch> visitEnvelope(ThenaDocConfig config, ManyDocsEnvelope envelope) {
    if(envelope.getStatus() == CommitResultStatus.OK) {
      return envelope.getBranch();
    }
    throw new DocStoreException("USER_PROFILE_CREATE_FAIL", DocStoreException.convertMessages(envelope));
  }

  @Override
  public List<UserProfile> end(ThenaDocConfig config, List<DocBranch> branches) {
    final Map<String, UserProfile> configsById = new HashMap<>(
        this.profiles.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
    
    branches.forEach(branch -> {
      final var next = FindAllUserProfilesVisitor.mapToUserProfile(branch);
      configsById.put(next.getId(), next);
    });
    
    return Collections.unmodifiableList(new ArrayList<>(configsById.values()));
  }

}
