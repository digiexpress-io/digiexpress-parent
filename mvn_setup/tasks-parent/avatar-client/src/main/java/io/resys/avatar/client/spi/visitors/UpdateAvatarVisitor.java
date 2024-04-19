package io.resys.avatar.client.spi.visitors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

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
import java.util.stream.Collectors;

import io.resys.thena.api.actions.DocCommitActions.CreateManyDocs;
import io.resys.thena.api.actions.DocCommitActions.ManyDocsEnvelope;
import io.resys.thena.api.actions.DocCommitActions.ModifyManyDocBranches;
import io.resys.avatar.client.api.Avatar;
import io.resys.avatar.client.api.AvatarCommand.AvatarUpdateCommand;
import io.resys.avatar.client.spi.store.AvatarStore;
import io.resys.avatar.client.spi.store.AvatarStoreConfig;
import io.resys.avatar.client.spi.store.AvatarStoreException;
import io.resys.avatar.client.spi.store.AvatarStoreConfig.AvatarDocObjectsVisitor;
import io.resys.avatar.client.spi.visitors.AvatarCommandVisitor.NoChangesException;
import io.resys.thena.api.actions.DocQueryActions;
import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocLog;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.userprofile.client.api.ImmutableAvatar;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;


public class UpdateAvatarVisitor implements AvatarDocObjectsVisitor<Uni<List<Avatar>>> {
  private final AvatarStore ctx;
  private final List<String> profileIds;
  private final ModifyManyDocBranches updateBuilder;
  private final CreateManyDocs createBuilder;
  private final List<Avatar> allExistingProfiles;
  private final Map<String, List<AvatarUpdateCommand>> commandsByAvatarId; 
  
  public UpdateAvatarVisitor(List<AvatarUpdateCommand> commands, AvatarStore ctx, List<Avatar> allExistingProfiles) {
    super();
    this.allExistingProfiles = allExistingProfiles;
    this.ctx = ctx;
    final var config = ctx.getConfig();
    this.commandsByAvatarId = commands.stream()
        .collect(Collectors.groupingBy(AvatarUpdateCommand::getId));
    this.profileIds = new ArrayList<>(commandsByAvatarId.keySet());
    this.updateBuilder = config.getClient().doc(config.getTenantId()).commit().modifyManyBranches()
        .message("Update avatar: " + commandsByAvatarId.size())
        .author(config.getAuthor().get());
    this.createBuilder = config.getClient().doc(config.getTenantId()).commit().createManyDocs()
        .docType(AvatarStoreConfig.DOC_TYPE)
        .message("Upsert avatar: " + commandsByAvatarId.size())
        .author(config.getAuthor().get())
        .branchName(AvatarStoreConfig.HEAD_NAME);
  }

  @Override
  public DocObjectsQuery start(AvatarStoreConfig config, DocObjectsQuery builder) {
    return builder.matchIds(profileIds);
  }

  @Override
  public DocQueryActions.DocObjects visitEnvelope(AvatarStoreConfig config, QueryEnvelope<DocQueryActions.DocObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw AvatarStoreException.builder("GET_AVATAR_BY_IDS_FOR_UPDATE_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(profileIds.stream().collect(Collectors.joining(",", "{", "}"))))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null) {
      throw AvatarStoreException.builder("GET_AVATAR_BY_IDS_FOR_UPDATE_NOT_FOUND")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(profileIds.stream().collect(Collectors.joining(",", "{", "}"))))
        .build();
    }
    return result;
  }

  @Override
  public Uni<List<Avatar>> end(AvatarStoreConfig config, DocQueryActions.DocObjects blob) {
    return applyUpdates(config, blob).onItem()
      .transformToUni(updated -> applyInserts(config, blob).onItem().transform(inserted -> {
        final var result = new ArrayList<Avatar>();
        result.addAll(updated);
        result.addAll(inserted);
        return Collections.unmodifiableList(result);
      }));
  }
  
  private Uni<List<Avatar>> applyInserts(AvatarStoreConfig config, DocQueryActions.DocObjects blob) {
    final var insertedProfiles = new ArrayList<Avatar>(); 
    for(final var entry : commandsByAvatarId.entrySet()) {
      try {
        final var inserted = new AvatarCommandVisitor(ctx.getConfig(), allExistingProfiles).visitTransaction(entry.getValue());
        this.createBuilder.item()
          .docId(inserted.getId())
          .append(JsonObject.mapFrom(inserted))
          .next();
        insertedProfiles.add(inserted);
      } catch(NoChangesException e) {
        // nothing to do
      }
    }
    
    if(insertedProfiles.isEmpty()) {
      return Uni.createFrom().item(insertedProfiles);
    }
    return createBuilder.build().onItem().transform(envelope -> mapInsertedResponse(envelope, insertedProfiles));
  }

  private Uni<List<Avatar>> applyUpdates(AvatarStoreConfig config, DocQueryActions.DocObjects blob) {
    final var updatedProfiles = blob.accept((Doc doc, DocBranch docBranch, DocCommit commit, List<DocLog> log) -> {  
      final var start = docBranch.getValue().mapTo(ImmutableAvatar.class);
      
      final List<AvatarUpdateCommand> commands = new ArrayList<>();
      if(commandsByAvatarId.containsKey(start.getId())) {
        commands.addAll(commandsByAvatarId.get(start.getId()));
        commandsByAvatarId.remove(start.getId());
      }
      if(commandsByAvatarId.containsKey(start.getId())) {
        commands.addAll(commandsByAvatarId.get(start.getId()));
        commandsByAvatarId.remove(start.getId());
      }
      
      if(commands.isEmpty()) {
        throw AvatarStoreException.builder("AVATAR_UPDATE_FAIL_COMMANDS_ARE_EMPTY")   
          .add((callback) -> callback.addArgs(profileIds.stream().collect(Collectors.joining(",", "{", "}"))))
          .build();
      }
      try {
        final var updated = new AvatarCommandVisitor(start, ctx.getConfig(), allExistingProfiles).visitTransaction(commands);
        this.updateBuilder.item()
          .docId(updated.getId())
          .branchName(docBranch.getBranchName())
          .append(JsonObject.mapFrom(updated))
          .next();
        
        return updated;
      } catch(NoChangesException e) {
        return start;
      }
    });
    
    if(updateBuilder.getItemsAdded() == 0) {
      return Uni.createFrom().item(updatedProfiles);
    }
    
    return updateBuilder.build().onItem().transform(response -> mapUpdateResponse(response, updatedProfiles));
  }
  
  
  private List<Avatar> mapInsertedResponse(ManyDocsEnvelope envelope, List<Avatar> insertedProfiles) {
    if(envelope.getStatus() != CommitResultStatus.OK) {
      throw new AvatarStoreException("USER_PROFILE_CREATE_FAIL", AvatarStoreException.convertMessages(envelope));
    }
    
    final var branches = envelope.getBranch();
    final Map<String, Avatar> createdById = new HashMap<>(insertedProfiles.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
    branches.forEach(branch -> {
      final var next = ImmutableAvatar.builder()
          .from(createdById.get(branch.getDocId()))
          .version(branch.getCommitId())
          .build();
      
      createdById.put(next.getId(), next);
    });
    return Collections.unmodifiableList(new ArrayList<>(createdById.values()));
  }
  
  private List<Avatar> mapUpdateResponse(ManyDocsEnvelope response, List<Avatar> updatedProfiles) {
    if(response.getStatus() != CommitResultStatus.OK) {
      final var failedUpdates = profileIds.stream().collect(Collectors.joining(",", "{", "}"));
      throw new AvatarStoreException("AVATAR_UPDATE_FAIL", JsonObject.of("failedUpdates", failedUpdates), AvatarStoreException.convertMessages(response));
    }
    final Map<String, Avatar> profileById = new HashMap<>(updatedProfiles.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
    response.getCommit().forEach(commit -> {
      
      final var next = ImmutableAvatar.builder()
          .from(profileById.get(commit.getDocId()))
          .version(commit.getId())
          .build();
      profileById.put(next.getId(), next);
    });
    
    return Collections.unmodifiableList(new ArrayList<>(profileById.values()));
  }
}
