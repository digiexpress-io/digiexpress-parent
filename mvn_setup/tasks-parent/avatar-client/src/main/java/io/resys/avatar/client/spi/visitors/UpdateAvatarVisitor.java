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

import io.resys.avatar.client.api.Avatar;
import io.resys.avatar.client.api.AvatarCommand.AvatarUpdateCommand;
import io.resys.avatar.client.api.ImmutableAvatar;
import io.resys.avatar.client.spi.AvatarStore;
import io.resys.avatar.client.spi.visitors.AvatarCommandVisitor.NoChangesException;
import io.resys.thena.api.actions.DocCommitActions.CreateManyDocs;
import io.resys.thena.api.actions.DocCommitActions.ManyDocsEnvelope;
import io.resys.thena.api.actions.DocCommitActions.ModifyManyDocBranches;
import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocCommands;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocCommitTree;
import io.resys.thena.api.envelope.DocContainer.DocTenantObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.spi.DocStoreException;
import io.resys.thena.spi.ThenaDocConfig;
import io.resys.thena.spi.ThenaDocConfig.DocObjectsVisitor;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;


public class UpdateAvatarVisitor implements DocObjectsVisitor<Uni<List<Avatar>>> {
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
    this.updateBuilder = config.getClient().doc(config.getRepoId()).commit().modifyManyBranches()
        .commitMessage("Update avatar: " + commandsByAvatarId.size())
        .commitAuthor(config.getAuthor().get());
    this.createBuilder = config.getClient().doc(config.getRepoId()).commit().createManyDocs()
        .commitMessage("Upsert avatar: " + commandsByAvatarId.size())
        .commitAuthor(config.getAuthor().get());
  }

  @Override
  public Uni<QueryEnvelope<DocTenantObjects>> start(ThenaDocConfig config, DocObjectsQuery builder) {
    return builder.findAll(profileIds);
  }

  @Override
  public DocTenantObjects visitEnvelope(ThenaDocConfig config, QueryEnvelope<DocTenantObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocStoreException.builder("GET_AVATAR_BY_IDS_FOR_UPDATE_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(profileIds.stream().collect(Collectors.joining(",", "{", "}"))))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null) {
      throw DocStoreException.builder("GET_AVATAR_BY_IDS_FOR_UPDATE_NOT_FOUND")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(profileIds.stream().collect(Collectors.joining(",", "{", "}"))))
        .build();
    }
    return result;
  }

  @Override
  public Uni<List<Avatar>> end(ThenaDocConfig config, DocTenantObjects blob) {
    return applyUpdates(config, blob).onItem()
      .transformToUni(updated -> applyInserts(config, blob).onItem().transform(inserted -> {
        final var result = new ArrayList<Avatar>();
        result.addAll(updated);
        result.addAll(inserted);
        return Collections.unmodifiableList(result);
      }));
  }
  
  private Uni<List<Avatar>> applyInserts(ThenaDocConfig config, DocTenantObjects blob) {
    final var insertedProfiles = new ArrayList<Avatar>(); 
    for(final var entry : commandsByAvatarId.entrySet()) {
      try {
        final var inserted = new AvatarCommandVisitor(ctx.getConfig(), allExistingProfiles).visitTransaction(entry.getValue());
        this.createBuilder.item()
          .docType(AvatarStore.DOC_TYPE_AVATAR)
          .docId(inserted.getItem1().getId())
          .branchContent(JsonObject.mapFrom(inserted.getItem1()))
          .commands(inserted.getItem2())
          .next();
        insertedProfiles.add(inserted.getItem1());
      } catch(NoChangesException e) {
        // nothing to do
      }
    }
    
    if(insertedProfiles.isEmpty()) {
      return Uni.createFrom().item(insertedProfiles);
    }
    return createBuilder.build().onItem().transform(envelope -> mapInsertedResponse(envelope, insertedProfiles));
  }

  private Uni<List<Avatar>> applyUpdates(ThenaDocConfig config, DocTenantObjects blob) {
    final var updatedProfiles = blob
      .accept((Doc doc, 
          DocBranch docBranch, 
          Map<String, DocCommit> commit, 
          List<DocCommands> _commands,
          List<DocCommitTree> trees) -> {
      
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
        throw DocStoreException.builder("AVATAR_UPDATE_FAIL_COMMANDS_ARE_EMPTY")   
          .add((callback) -> callback.addArgs(profileIds.stream().collect(Collectors.joining(",", "{", "}"))))
          .build();
      }
      try {
        final var updated = new AvatarCommandVisitor(start, ctx.getConfig(), allExistingProfiles).visitTransaction(commands);
        this.updateBuilder.item()
          .docId(updated.getItem1().getId())
          .replace(JsonObject.mapFrom(updated.getItem1()))
          .commands(updated.getItem2())
          .next();
        
        return updated.getItem1();
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
      throw new DocStoreException("USER_PROFILE_CREATE_FAIL", DocStoreException.convertMessages(envelope));
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
      throw new DocStoreException("AVATAR_UPDATE_FAIL", JsonObject.of("failedUpdates", failedUpdates), DocStoreException.convertMessages(response));
    }
    final Map<String, Avatar> profileById = new HashMap<>(updatedProfiles.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
    response.getBranch().forEach(branch -> {
      final var next = branch.getValue().mapTo(ImmutableAvatar.class);
      profileById.put(next.getId(), next);
    });
    
    return Collections.unmodifiableList(new ArrayList<>(profileById.values()));
  }
}
