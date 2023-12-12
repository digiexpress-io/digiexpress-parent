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

import io.resys.thena.docdb.api.actions.CommitActions.CommitResultStatus;
import io.resys.thena.docdb.api.actions.DocCommitActions.CreateManyDocs;
import io.resys.thena.docdb.api.actions.DocCommitActions.ManyDocsEnvelope;
import io.resys.thena.docdb.api.actions.DocCommitActions.ModifyManyDocBranches;
import io.resys.thena.docdb.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.docdb.api.models.ThenaDocObject.Doc;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocBranch;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocCommit;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocLog;
import io.resys.thena.docdb.api.models.ThenaDocObjects.DocObjects;
import io.resys.userprofile.client.api.model.Document;
import io.resys.userprofile.client.api.model.ImmutableUserProfile;
import io.resys.userprofile.client.api.model.UserProfile;
import io.resys.userprofile.client.api.model.UserProfileCommand.UserProfileCommandType;
import io.resys.userprofile.client.api.model.UserProfileCommand.UserProfileUpdateCommand;
import io.resys.userprofile.client.spi.store.DocumentConfig;
import io.resys.userprofile.client.spi.store.DocumentConfig.DocObjectsVisitor;
import io.resys.userprofile.client.spi.store.DocumentStore;
import io.resys.userprofile.client.spi.store.DocumentStoreException;
import io.resys.userprofile.client.spi.store.MainBranch;
import io.resys.userprofile.client.spi.visitors.UserProfileCommandVisitor.NoChangesException;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;


public class UpdateUserProfileVisitor implements DocObjectsVisitor<Uni<List<UserProfile>>> {
  private final DocumentStore ctx;
  private final List<String> profileIds;
  private final ModifyManyDocBranches updateBuilder;
  private final CreateManyDocs createBuilder;
  private final Map<String, List<UserProfileUpdateCommand>> commandsByUserProfileId; 
  private final List<UserProfileCommandType> upserts = Arrays.asList(UserProfileCommandType.UpsertUserProfile);
  
  public UpdateUserProfileVisitor(List<UserProfileUpdateCommand> commands, DocumentStore ctx) {
    super();
    this.ctx = ctx;
    final var config = ctx.getConfig();
    this.commandsByUserProfileId = commands.stream()
        .collect(Collectors.groupingBy(UserProfileUpdateCommand::getId));
    this.profileIds = new ArrayList<>(commandsByUserProfileId.keySet());
    this.updateBuilder = config.getClient().doc().commit().modifyManyBranches()
        .repoId(config.getRepoId())
        .message("Update user profiles: " + commandsByUserProfileId.size())
        .author(config.getAuthor().get());
    this.createBuilder = config.getClient().doc().commit().createManyDocs()
        .repoId(config.getRepoId())
        .docType(Document.DocumentType.USER_PROFILE.name())
        .message("Upsert user profiles: " + commandsByUserProfileId.size())
        .author(config.getAuthor().get())
        .branchName(config.getBranchName());
  }

  @Override
  public DocObjectsQuery start(DocumentConfig config, DocObjectsQuery builder) {
    return builder.matchIds(profileIds).branchName(MainBranch.HEAD_NAME);
  }

  @Override
  public DocObjects visitEnvelope(DocumentConfig config, QueryEnvelope<DocObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocumentStoreException.builder("GET_USER_PROFILES_BY_IDS_FOR_UPDATE_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(profileIds.stream().collect(Collectors.joining(",", "{", "}"))))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null) {
      throw DocumentStoreException.builder("GET_USER_PROFILES_BY_IDS_FOR_UPDATE_NOT_FOUND")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(profileIds.stream().collect(Collectors.joining(",", "{", "}"))))
        .build();
    }
    
    final var totalUpserts = this.commandsByUserProfileId.values().stream().flatMap(e -> e.stream()).filter(e -> upserts.contains(e.getCommandType())).count();
    if(profileIds.size() < Math.max((result.getDocs().size() - totalUpserts), 0)) {
      throw new DocumentStoreException("USER_PROFILES_UPDATE_FAIL_NOT_ALL_USER_PROFILES_FOUND", JsonObject.of("failedUpdates", profileIds));
    }
    return result;
  }

  @Override
  public Uni<List<UserProfile>> end(DocumentConfig config, DocObjects blob) {
    return applyUpdates(config, blob).onItem()
      .transformToUni(updated -> applyInserts(config, blob).onItem().transform(inserted -> {
        final var result = new ArrayList<UserProfile>();
        result.addAll(updated);
        result.addAll(inserted);
        return Collections.unmodifiableList(result);
      }));
  }
  
  private Uni<List<UserProfile>> applyInserts(DocumentConfig config, DocObjects blob) {
    final var insertedProfiles = new ArrayList<UserProfile>(); 
    for(final var entry : commandsByUserProfileId.entrySet()) {
      try {
        if(!upserts.contains(entry.getValue().get(0).getCommandType())) {
          continue;
        }
        final var inserted = new UserProfileCommandVisitor(ctx.getConfig()).visitTransaction(entry.getValue());
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

  private Uni<List<UserProfile>> applyUpdates(DocumentConfig config, DocObjects blob) {
    final var updatedProfiles = blob.accept((Doc doc, DocBranch docBranch, DocCommit commit, List<DocLog> log) -> {  
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
        throw DocumentStoreException.builder("USER_PROFILES_UPDATE_FAIL_COMMANDS_ARE_EMPTY")   
          .add((callback) -> callback.addArgs(profileIds.stream().collect(Collectors.joining(",", "{", "}"))))
          .build();
      }
      try {
        final var updated = new UserProfileCommandVisitor(start, ctx.getConfig()).visitTransaction(commands);
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
    
    if(updatedProfiles.isEmpty()) {
      return Uni.createFrom().item(updatedProfiles);
    }
    
    return updateBuilder.build().onItem().transform(response -> mapUpdateResponse(response, updatedProfiles));
  }
  
  
  private List<UserProfile> mapInsertedResponse(ManyDocsEnvelope envelope, List<UserProfile> insertedProfiles) {
    if(envelope.getStatus() != CommitResultStatus.OK) {
      throw new DocumentStoreException("USER_PROFILE_CREATE_FAIL", DocumentStoreException.convertMessages(envelope));
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
      throw new DocumentStoreException("USER_PROFILES_UPDATE_FAIL", JsonObject.of("failedUpdates", failedUpdates), DocumentStoreException.convertMessages(response));
    }
    final Map<String, UserProfile> profileById = new HashMap<>(updatedProfiles.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
    response.getCommit().forEach(commit -> {
      
      final var next = ImmutableUserProfile.builder()
          .from(profileById.get(commit.getDocId()))
          .version(commit.getId())
          .build();
      profileById.put(next.getId(), next);
    });
    
    return Collections.unmodifiableList(new ArrayList<>(profileById.values()));
  }
}
