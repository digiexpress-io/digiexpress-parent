package io.resys.userprofile.client.spi.visitors;

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
import java.util.stream.Collectors;

import io.resys.thena.api.actions.DocCommitActions.CreateOneDoc;
import io.resys.thena.api.actions.DocCommitActions.ModifyOneDocBranch;
import io.resys.thena.api.actions.DocCommitActions.OneDocEnvelope;
import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocCommands;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocCommitTree;
import io.resys.thena.api.envelope.DocContainer.DocObject;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.spi.DocStoreException;
import io.resys.thena.spi.ThenaDocConfig;
import io.resys.thena.spi.ThenaDocConfig.DocObjectVisitor;
import io.resys.userprofile.client.api.model.ImmutableUiSettings;
import io.resys.userprofile.client.api.model.UiSettings;
import io.resys.userprofile.client.api.model.UiSettingsCommand.UiSettingsUpdateCommand;
import io.resys.userprofile.client.spi.UserProfileStore;
import io.resys.userprofile.client.spi.support.DataConstants;
import io.resys.userprofile.client.spi.visitors.UiSettingsCommandVisitor.NoChangesException;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;


public class UpdateUiSettingsVisitor implements DocObjectVisitor<Uni<UiSettings>> {
  private final UserProfileStore ctx;
  private final ModifyOneDocBranch updateBuilder;
  private final CreateOneDoc createBuilder;
  private final UiSettingsUpdateCommand command;
  
  public UpdateUiSettingsVisitor(UiSettingsUpdateCommand command, UserProfileStore ctx) {
    super();
    this.ctx = ctx;
    final var config = ctx.getConfig();
    this.command = command;
    this.updateBuilder = config.getClient().doc(config.getRepoId()).commit().modifyOneBranch()
        .commitMessage("Update user profile ui settings: " + command.getUserId() + "/" + command.getSettingsId())
        .commitAuthor(config.getAuthor().get());
    this.createBuilder = config.getClient().doc(config.getRepoId()).commit().createOneDoc()
        .commitMessage("Insert user profile ui settings: " + command.getUserId() + "/" + command.getSettingsId())
        .commitAuthor(config.getAuthor().get());
  }

  @Override
  public Uni<QueryEnvelope<DocObject>> start(ThenaDocConfig config, DocObjectsQuery builder) {
    return builder.ownerId(command.getSettingsId()).parentId(command.getUserId()).findOne();
  }

  @Override
  public DocObject visitEnvelope(ThenaDocConfig config, QueryEnvelope<DocObject> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocStoreException.builder("GET_UI_SETTINGS_BY_IDS_FOR_UPSERT_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(Arrays.asList(command.getUserId(), command.getSettingsId()).stream().collect(Collectors.joining(",", "{", "}"))))
        .build();
    }
    final var result = envelope.getObjects();
    
    return result;
  }

  @Override
  public Uni<UiSettings> end(ThenaDocConfig config, DocObject blob) {
    if(blob == null) {
      return applyInserts(config, blob);
    } 
    return applyUpdates(config, blob);
  }
  
  private Uni<UiSettings> applyInserts(ThenaDocConfig config, DocObject blob) {
    try {

      final var inserted = new UiSettingsCommandVisitor(ctx.getConfig()).visitTransaction(Arrays.asList(command));
      this.createBuilder
        .docId(inserted.getItem1().getId())
        .docType(DataConstants.DOC_TYPE_USER_PROFILE_SETTINGS)
        .parentDocId(inserted.getItem1().getUserId())
        .ownerId(inserted.getItem1().getSettingsId())
        .branchContent(JsonObject.mapFrom(inserted.getItem1()))
        .commands(inserted.getItem2())
        .build();
    } catch(NoChangesException e) {
      // nothing to do
    }
    return createBuilder.build().onItem().transform(envelope -> mapInsertedResponse(envelope));
  }

  private Uni<UiSettings> applyUpdates(ThenaDocConfig config, DocObject blob) {
    blob.accept((
        Doc doc, 
        DocBranch docBranch, 
        Map<String, DocCommit> commit, 
        List<DocCommands> _commands,
        List<DocCommitTree> trees
    ) -> {
          
      final var start = docBranch.getValue().mapTo(ImmutableUiSettings.class);
      try {
        final var updated = new UiSettingsCommandVisitor(start, ctx.getConfig()).visitTransaction(Arrays.asList(command));
        this.updateBuilder
          .docId(updated.getItem1().getId())
          .branchName(docBranch.getBranchName())
          .replace(JsonObject.mapFrom(updated.getItem1()))
          .commands(updated.getItem2());
        
        return updated.getItem1();
      } catch(NoChangesException e) {
        return start;
      }
    });
    
    
    return updateBuilder.build().onItem().transform(response -> mapUpdateResponse(response));
  }
  
  
  private UiSettings mapInsertedResponse(OneDocEnvelope envelope) {
    if(envelope.getStatus() != CommitResultStatus.OK) {
      throw new DocStoreException("UI_SETTINGS_CREATE_FAIL", DocStoreException.convertMessages(envelope));
    }
    
    return envelope.getBranch().getValue().mapTo(ImmutableUiSettings.class);
  }
  
  private UiSettings mapUpdateResponse(OneDocEnvelope response) {
    if(response.getStatus() != CommitResultStatus.OK) {
      final var failedUpdates = Arrays.asList(command.getUserId(), command.getSettingsId()).stream().collect(Collectors.joining(",", "{", "}"));
      throw new DocStoreException("UI_SETTINGS_UPDATE_FAIL", JsonObject.of("failedUpdates", failedUpdates), DocStoreException.convertMessages(response));
    }
    return response.getBranch().getValue().mapTo(ImmutableUiSettings.class);
  }


}
