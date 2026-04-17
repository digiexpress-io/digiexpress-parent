package io.digiexpress.eveli.client.spi.dms;

/*-
 * #%L
 * eveli-client
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.digiexpress.eveli.client.api.TaskClient.Task;
import io.digiexpress.eveli.client.api.TaskClient.TaskStatus;
import io.digiexpress.eveli.client.spi.dms.DocContainer.Doc;
import io.digiexpress.eveli.client.spi.task.TaskMapper;
import io.resys.thena.api.entities.grim.ThenaGrimMergeObject.MergeMission;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;

public class DocContainerClientDummy implements DocContainerClient {

  @Override
  public CreateDoc createDoc() {
    return new CreateDoc() {
      private final List<Doc> docsToMerge = new ArrayList<>();
      @SuppressWarnings("unused")
      private Task task;
      
      @Override
      public CreateDoc title(String userGivenId) {
        return this;
      }
      @Override
      public CreateDoc externalId(String externalId) {
        return this;
      }
      @Override
      public CreateDoc draftedBy(String draftedBy) {
        return this;
      }
      @Override
      public CreateDoc decidedBy(String decidedBy) {
        return this;
      }
      @Override
      public CreateDoc createdBy(String createdBy) {
        return this;
      }
      @Override
      public CreateDoc props(Map<String, String> props) {
        return this;
      }
      @Override
      public Uni<DocContainerEnvelope> build() {
        return Uni.createFrom().item(ImmutableDocContainerEnvelope.builder()
            .status(DocContainerEnvelope.DocContainerEnvelopeStatus.OK)
            .objects(ImmutableDocContainer.builder().id("dummy-doc-container-id").build())
            .build());
      }
      @Override
      public CreateDoc addDocument(Doc newDocument) {
        docsToMerge.add(newDocument);
        return this;
      }
      @Override
      public CreateDoc task(Task task) {
        this.task = task;
        return this;
      }

    };
  }

  @Override
  public DocContainerTasUpdater updateTask() {
    return new DocContainerTasUpdater() {
      private MergeMission merge;
      private DocContainer doc;
      private String userId;
      
      @Override
      public DocContainerTasUpdater task(MergeMission task) {
        this.merge = task;
        return this;
      }
      
      @Override
      public DocContainerTasUpdater doc(DocContainer doc) {
        this.doc = doc;
        return this;
      }

      @Override
      public DocContainerTasUpdater userId(String userId) {
        this.userId = userId;
        return this;
      }
      
      @Override
      public void build() {

        final var docContainerId = doc.getId();
        final var props = doc.getProps();
        final var files = doc.getDocs().values();
        
        final var linkBody = JsonObject.mapFrom(props);
        linkBody.put("files", files.stream().map(e -> e.getName()).toList());
        
        
        final var previousTransfer = merge.getCurrentState().getLinks().values().stream()
          .filter(e -> TaskMapper.LINK_TYPE_TRANSFERRED_ID.equals(e.getLinkType()))
          .findFirst();
        
        if(previousTransfer.isEmpty()) {
          merge.addLink(newLink -> newLink
            .linkType(TaskMapper.LINK_TYPE_TRANSFERRED_ID)
            .linkValue(docContainerId)
            .linkBody(linkBody)
            .build());
        } else {
          merge.modifyLink(previousTransfer.get().getId(), modLink -> {
            modLink
              .linkType(TaskMapper.LINK_TYPE_TRANSFERRED_ID)
              .linkValue(docContainerId)
              .linkBody(linkBody)
              .build();
          });
        }
        
        merge.status(TaskStatus.TRANSFERRED.name())
        // change is viewed by worker who created it
        .addViewer(viewer -> viewer.userId(userId).usedFor(TaskMapper.VIEWER_WORKER).currentTxCommit().build())
        .build();
        
        
        if(previousTransfer.isEmpty()) {
          merge.addLink(newLink -> newLink
            .linkType(TaskMapper.LINK_TYPE_TRANSFERRED_ID)
            .linkValue(docContainerId)
            .linkBody(linkBody)
            .build());
        } else {
          merge.modifyLink(previousTransfer.get().getId(), modLink -> {
            modLink
              .linkType(TaskMapper.LINK_TYPE_TRANSFERRED_ID)
              .linkValue(docContainerId)
              .linkBody(linkBody)
              .build();
          });
        }
        
        merge.status(TaskStatus.TRANSFERRED.name())
        // change is viewed by worker who created it
        .addViewer(viewer -> viewer.userId(userId).usedFor(TaskMapper.VIEWER_WORKER).currentTxCommit().build())
        .build();
        
      }

    };
  }

}
