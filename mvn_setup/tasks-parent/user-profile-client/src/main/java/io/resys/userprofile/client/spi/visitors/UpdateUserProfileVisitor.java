package io.resys.userprofile.client.spi.visitors;

import java.util.ArrayList;
import java.util.Arrays;
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
import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocCommands;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocCommitTree;
import io.resys.thena.api.entities.doc.ThenaDocConfig;
import io.resys.thena.api.entities.doc.ThenaDocConfig.DocObjectsVisitor;
import io.resys.thena.api.envelope.DocContainer.DocTenantObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.userprofile.client.api.model.ImmutableUserProfile;
import io.resys.userprofile.client.api.model.UserProfile;
import io.resys.userprofile.client.api.model.UserProfileCommand.UserProfileCommandType;
import io.resys.userprofile.client.api.model.UserProfileCommand.UserProfileUpdateCommand;
import io.resys.userprofile.client.spi.store.UserProfileStore;
import io.resys.userprofile.client.spi.store.UserProfileStoreException;
import io.resys.userprofile.client.spi.support.DataConstants;
import io.resys.userprofile.client.spi.visitors.UserProfileCommandVisitor.NoChangesException;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;


public class UpdateUserProfileVisitor implements DocObjectsVisitor<Uni<List<UserProfile>>> {
  private final UserProfileStore ctx;
  private final List<String> profileIds;
  private final ModifyManyDocBranches updateBuilder;
  private final CreateManyDocs createBuilder;
  private final Map<String, List<UserProfileUpdateCommand>> commandsByUserProfileId; 
  private final List<UserProfileCommandType> upserts = Arrays.asList(UserProfileCommandType.UpsertUserProfile);
  
  public UpdateUserProfileVisitor(List<UserProfileUpdateCommand> commands, UserProfileStore ctx) {
    super();
    this.ctx = ctx;
    final var config = ctx.getConfig();
    this.commandsByUserProfileId = commands.stream()
        .collect(Collectors.groupingBy(UserProfileUpdateCommand::getId));
    this.profileIds = new ArrayList<>(commandsByUserProfileId.keySet());
    this.updateBuilder = config.getClient().doc(config.getRepoId()).commit().modifyManyBranches()
        .commitMessage("Update user profiles: " + commandsByUserProfileId.size())
        .commitAuthor(config.getAuthor().get());
    this.createBuilder = config.getClient().doc(config.getRepoId()).commit().createManyDocs()
        .commitMessage("Upsert user profiles: " + commandsByUserProfileId.size())
        .commitAuthor(config.getAuthor().get());
  }

  @Override
  public Uni<QueryEnvelope<DocTenantObjects>>  start(ThenaDocConfig config, DocObjectsQuery builder) {
    return builder.findAll(profileIds);
  }

  @Override
  public DocTenantObjects visitEnvelope(ThenaDocConfig config, QueryEnvelope<DocTenantObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw UserProfileStoreException.builder("GET_USER_PROFILES_BY_IDS_FOR_UPDATE_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(profileIds.stream().collect(Collectors.joining(",", "{", "}"))))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null) {
      throw UserProfileStoreException.builder("GET_USER_PROFILES_BY_IDS_FOR_UPDATE_NOT_FOUND")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(profileIds.stream().collect(Collectors.joining(",", "{", "}"))))
        .build();
    }
    
    final var totalUpserts = this.commandsByUserProfileId.values().stream().flatMap(e -> e.stream()).filter(e -> upserts.contains(e.getCommandType())).count();
    if(profileIds.size() < Math.max((result.getDocs().size() - totalUpserts), 0)) {
      throw new UserProfileStoreException("USER_PROFILES_UPDATE_FAIL_NOT_ALL_USER_PROFILES_FOUND", JsonObject.of("failedUpdates", profileIds));
    }
    return result;
  }

  @Override
  public Uni<List<UserProfile>> end(ThenaDocConfig config, DocTenantObjects blob) {
    return applyUpdates(config, blob).onItem()
      .transformToUni(updated -> applyInserts(config, blob).onItem().transform(inserted -> {
        final var result = new ArrayList<UserProfile>();
        result.addAll(updated);
        result.addAll(inserted);
        return Collections.unmodifiableList(result);
      }));
  }
  
  private Uni<List<UserProfile>> applyInserts(ThenaDocConfig config, DocTenantObjects blob) {
    final var insertedProfiles = new ArrayList<UserProfile>(); 
    for(final var entry : commandsByUserProfileId.entrySet()) {
      try {
        if(!upserts.contains(entry.getValue().get(0).getCommandType())) {
          continue;
        }
        final var inserted = new UserProfileCommandVisitor(ctx.getConfig()).visitTransaction(entry.getValue());
        this.createBuilder.item()
          .docId(inserted.getItem1().getId())
          .docType(DataConstants.DOC_TYPE_USER_PROFILE)
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

  private Uni<List<UserProfile>> applyUpdates(ThenaDocConfig config, DocTenantObjects blob) {
    final var updatedProfiles = blob.accept((
        Doc doc, 
        DocBranch docBranch, 
        Map<String, DocCommit> commit, 
        List<DocCommands> _commands,
        List<DocCommitTree> trees
        ) -> {  
      final var start = docBranch.getValue().mapTo(ImmutableUserProfile.class);
      
      final List<UserProfileUpdateCommand> commands = new ArrayList<>();
      if(commandsByUserProfileId.containsKey(start.getId())) {
        commands.addAll(commandsByUserProfileId.get(start.getId()));
        commandsByUserProfileId.remove(start.getId());
      }
      if(commandsByUserProfileId.containsKey(start.getId())) {
        commands.addAll(commandsByUserProfileId.get(start.getId()));
        commandsByUserProfileId.remove(start.getId());
      }
      
      if(commands.isEmpty()) {
        throw UserProfileStoreException.builder("USER_PROFILES_UPDATE_FAIL_COMMANDS_ARE_EMPTY")   
          .add((callback) -> callback.addArgs(profileIds.stream().collect(Collectors.joining(",", "{", "}"))))
          .build();
      }
      try {
        final var updated = new UserProfileCommandVisitor(start, ctx.getConfig()).visitTransaction(commands);
        this.updateBuilder.item()
          .docId(updated.getItem1().getId())
          .branchName(docBranch.getBranchName())
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
  
  
  private List<UserProfile> mapInsertedResponse(ManyDocsEnvelope envelope, List<UserProfile> insertedProfiles) {
    if(envelope.getStatus() != CommitResultStatus.OK) {
      throw new UserProfileStoreException("USER_PROFILE_CREATE_FAIL", UserProfileStoreException.convertMessages(envelope));
    }
    
    final var branches = envelope.getBranch();
    final Map<String, UserProfile> createdById = new HashMap<>(insertedProfiles.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
    branches.forEach(branch -> {
      final var next = ImmutableUserProfile.builder()
          .from(createdById.get(branch.getDocId()))
          .version(branch.getCommitId())
          .build();
      
      createdById.put(next.getId(), next);
    });
    return Collections.unmodifiableList(new ArrayList<>(createdById.values()));
  }
  
  private List<UserProfile> mapUpdateResponse(ManyDocsEnvelope response, List<UserProfile> updatedProfiles) {
    if(response.getStatus() != CommitResultStatus.OK) {
      final var failedUpdates = profileIds.stream().collect(Collectors.joining(",", "{", "}"));
      throw new UserProfileStoreException("USER_PROFILES_UPDATE_FAIL", JsonObject.of("failedUpdates", failedUpdates), UserProfileStoreException.convertMessages(response));
    }
    final Map<String, UserProfile> profileById = new HashMap<>(updatedProfiles.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
    response.getBranch().forEach(branch -> {
      final var next = FindAllUserProfilesVisitor.mapToUserProfile(branch);
      profileById.put(next.getId(), next);
    });
    
    return Collections.unmodifiableList(new ArrayList<>(profileById.values()));
  }


}
