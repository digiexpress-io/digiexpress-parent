package io.resys.avatar.client.spi.visitors;

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

import io.resys.avatar.client.api.Avatar;
import io.resys.avatar.client.api.AvatarCommand.CreateAvatar;
import io.resys.avatar.client.spi.store.AvatarStoreConfig;
import io.resys.avatar.client.spi.store.AvatarStoreException;
import io.resys.avatar.client.spi.store.AvatarStoreConfig.AvatarDocCreateVisitor;
import io.resys.avatar.client.spi.visitors.AvatarCommandVisitor.NoChangesException;
import io.resys.thena.api.actions.DocCommitActions.CreateManyDocs;
import io.resys.thena.api.actions.DocCommitActions.ManyDocsEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.userprofile.client.api.ImmutableAvatar;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;  

@RequiredArgsConstructor
public class CreateAvatarVisitor implements AvatarDocCreateVisitor<Avatar> {
  private final List<? extends CreateAvatar> commands;
  private final List<Avatar> allExistingProfiles;
  private final List<Avatar> profiles = new ArrayList<Avatar>();
  
  @Override
  public CreateManyDocs start(AvatarStoreConfig config, CreateManyDocs builder) {
    builder.message("creating avatar");
    
    for(final var command : commands) {
      try {
        final var entity = new AvatarCommandVisitor(config, allExistingProfiles).visitTransaction(Arrays.asList(command));
        final var json = JsonObject.mapFrom(entity);
        builder.item()
          .append(json)
          .docId(entity.getId())
          .externalId(entity.getExternalId())
          .next();
        profiles.add(entity);
      } catch (NoChangesException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }
    return builder;
  }

  @Override
  public List<DocBranch> visitEnvelope(AvatarStoreConfig config, ManyDocsEnvelope envelope) {
    if(envelope.getStatus() == CommitResultStatus.OK) {
      return envelope.getBranch();
    }
    throw new AvatarStoreException("AVATAR_CREATE_FAIL", AvatarStoreException.convertMessages(envelope));
  }

  @Override
  public List<Avatar> end(AvatarStoreConfig config, List<DocBranch> branches) {
    final Map<String, Avatar> configsById = new HashMap<>(
        this.profiles.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
    
    branches.forEach(branch -> {
      
      final var next = ImmutableAvatar.builder()
          .from(configsById.get(branch.getDocId()))
          .version(branch.getCommitId())
          .build();
      configsById.put(next.getId(), next);
    });
    
    return Collections.unmodifiableList(new ArrayList<>(configsById.values()));
  }

}
