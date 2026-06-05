package io.resys.limaone.persistence.fs;


/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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


import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import io.resys.limaone.model.Description;
import io.resys.limaone.model.DescriptionLabels;
import io.resys.limaone.model.ImmutableDescription;
import io.resys.limaone.model.ImmutableDialobFormMeta;
import io.resys.limaone.model.Model.Body;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.spi.dialob.FormDb.FormMetadata;
import io.resys.thena.fs.entities.Entity;
import io.resys.thena.fs.entities.ImmutableBlob;
import io.resys.thena.fs.entities.ImmutableIndex;
import io.resys.thena.fs.entities.ImmutableNode;
import io.resys.thena.fs.entities.ImmutableNodeTransitives;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Ref;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@RequiredArgsConstructor @Getter
public class NodeAndBody {
  private final Node value;
  private final BodyType bodyType;
  private final Optional<Body> body;
  
  private Optional<Description> description;
  private Optional<DescriptionLabels> labels;
  
  public Optional<Description> getDescription() {
    
    if(description == null) {
      if(getValue().getTransitives() == null || getValue().getTransitives().getProps() == null) {
        description =  Optional.empty();
        return description;
      }
      
      final var nodeProps = getValue().getTransitives().getProps();
      if(nodeProps.getPropsDescription().isEmpty()) {
        description =  Optional.empty();
        return description;
      }
      
      description = nodeProps.getPropsDescription()
          .map(text -> ImmutableDescription.builder().text(text).build());
      return description;
    }
    
    
    return description;
  }
  
  public Optional<DescriptionLabels> getLabels() {
    
    if(labels == null) {
      if(getValue().getTransitives() == null || getValue().getTransitives().getProps() == null) {
        labels =  Optional.empty();
        return labels;
      }
      
      final var nodeProps = getValue().getTransitives().getProps();
      if(nodeProps.getPropsLabels().isEmpty()) {
        labels =  Optional.empty();
        return labels;
      }
      
      labels = nodeProps.getPropsLabels().map(e -> e.mapTo(DescriptionLabels.class));
      return labels;
    }
    
    
    return labels;
  }
  
  @SuppressWarnings("unchecked")
  public <T extends Body> T getBodyOfType() {
    return (T) body.orElse(null);
  }
  
  @SuppressWarnings("unchecked")
  public <T extends Body> T getBodyOfType(Class<T> clazz) {
    return (T) body.orElse(null);
  }
  
  public String getObjectId() {
    return value.getObjectId();
  }

  /**
   * resolve dirent type based on blob or other props
   */
  public static Optional<NodeAndBody> of(Node node) {

    if(node.getBlobId().isEmpty()) {
      return Optional.of(new NodeAndBody(node, BodyType.FOLDER, Optional.empty()));
    }
    final var blob = node.getTransitives().getBlob();
    
    
    try {
      final var type = BodyType.valueOf(blob.getBlobType());
      
      final Body body;
      if(blob.getBlobValue() == null) {
        body = null;
      } else {
        body = blob.getBlobValue().mapTo(type.getBodyClass());
      }
      
      return Optional.of(new NodeAndBody(node, type, Optional.of(body)));
    } catch(Exception e) {
      log.warn("Failed to get node type from blob: {}, message: {}", node.getNodeName(), e.getMessage());
      return Optional.empty();
    }
  }
  
  
  /**
   * resolve dirent type based on blob or other props
   */
  public static Optional<NodeAndBody> of(FormMetadata form, Ref ref) {
    
    final var index = ImmutableIndex.builder()
      .treeId(ref.getTransitives().getTree().getId())
      .objectId(form.getId())
      .createdAt(OffsetDateTime.ofInstant(form.getMetadata().getCreated(), ZoneId.systemDefault()))
      .updatedAt(OffsetDateTime.ofInstant(form.getMetadata().getLastSaved(), ZoneId.systemDefault()))
      .createdBy(ref.getTransitives().getCommit().getId())
      .updatedBy(ref.getTransitives().getCommit().getId())
      .createdByAuthor("_unknown")
      .updatedByAuthor("_unknown")
      .build();
    
    final var nodeId = Entity.uuid().append(form.getId()).build();
    final var node = ImmutableNode.builder()
      .id(nodeId)
      .objectId(form.getId())
      .nodePath(Optional.empty())
      .nodeName(form.getId())
      .blobId(Optional.ofNullable(nodeId))
      .propsId(Optional.empty())
      .transitives(ImmutableNodeTransitives.builder()
        .objectIndex(index)
        .blob(ImmutableBlob.builder()
            .id(nodeId)
            .blobClass(Optional.empty())
            .blobType(BodyType.DIALOB_FORM_META.name())
            .build())
        .props(null)
        .build())
      .build();
    
    final var body = ImmutableDialobFormMeta.builder()
        .metadata(form.getMetadata())
        .build();
    
    return Optional.of(new NodeAndBody(node, BodyType.DIALOB_FORM_META, Optional.of(body)));
  }
}
