package io.resys.thena.fs.jackson;

/*-
 * #%L
 * thena-fs-client
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

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import io.resys.thena.fs.entities.Blob;
import io.resys.thena.fs.entities.ImmutableBlob;
import io.resys.thena.fs.entities.ImmutableIndex;
import io.resys.thena.fs.entities.ImmutableNode;
import io.resys.thena.fs.entities.ImmutableNodeTransitives;
import io.resys.thena.fs.entities.ImmutableProps;
import io.resys.thena.fs.entities.Index;
import io.resys.thena.fs.entities.Props;



public class NodeParser {
  private final JsonParser root;
  
  private UUID nodeId = null;
  private String objectId = null;
  private String nodePath = null;
  private String nodeName = null;
  @SuppressWarnings("unused")
  private String nodeFullName = null;
  private UUID blobId = null;
  private UUID propsId = null;
  private UUID treeId = null;
  
  // For building transitives
  private Index index = null;
  private Blob blob = null;
  private Props props = null;
  
  // Temporary storage for Index fields
  private String createdAt = null;
  private String updatedAt = null;
  private String createdByAuthor = null;
  private UUID createdBy = null;
  private String updatedByAuthor = null;
  private UUID updatedBy = null;
  
  // Temporary storage for Blob fields
  private String blobType = null;
  private String blobClass = null;
  private io.vertx.core.json.JsonObject blobValue = null;
  
  // Temporary storage for Props fields
  private io.vertx.core.json.JsonObject propsLabels = null;
  private io.vertx.core.json.JsonObject propsComments = null;
  private io.vertx.core.json.JsonObject propsPermissions = null;
  private io.vertx.core.json.JsonObject propsFlags = null;

  
  public NodeParser(JsonParser root) throws IOException {
    super();
    this.root = root;
    if(root.currentToken() != JsonToken.START_OBJECT) {
      throw new IOException("Expected object");
    }
  }  
  public ImmutableNode parse() throws IOException {  
    while (root.currentToken() != JsonToken.END_OBJECT) {
      walk();
    }
    
    // Build Index if we have created_at (indicates index data is present)
    if (createdAt != null) {
      index = ImmutableIndex.builder()
        .treeId(treeId)
        .objectId(objectId)
        .createdAt(OffsetDateTime.parse(createdAt))
        .updatedAt(OffsetDateTime.parse(updatedAt))
        .createdBy(createdBy)
        .updatedBy(updatedBy)
        .createdByAuthor(createdByAuthor)
        .updatedByAuthor(updatedByAuthor)
        .build();
    }
    
    // Build Blob if we have blobId
    if (blobId != null) {
      blob = ImmutableBlob.builder()
        .id(blobId)
        .blobType(blobType)
        .blobClass(Optional.ofNullable(blobClass))
        .blobValue(blobValue)
        .build();
    }
    
    // Build Props if we have propsId
    if (propsId != null) {
      props = ImmutableProps.builder()
        .id(propsId)
        .propsLabels(Optional.ofNullable(propsLabels))
        .propsComments(Optional.ofNullable(propsComments))
        .propsPermissions(Optional.ofNullable(propsPermissions))
        .propsFlags(Optional.ofNullable(propsFlags))
        .build();
    }
    
    return ImmutableNode.builder()
      .id(nodeId)
      .objectId(objectId)
      .nodePath(Optional.ofNullable(nodePath))
      .nodeName(nodeName)
      .blobId(Optional.ofNullable(blobId))
      .propsId(Optional.ofNullable(propsId))
      .transitives(ImmutableNodeTransitives.builder()
        .objectIndex(index)
        .blob(blob)
        .props(props)
        .build())
      .build();
  }
  
  private void walk() throws IOException {
    if(root.currentToken() == JsonToken.START_OBJECT) {
      root.nextToken();  
    }
    final var fieldName = root.currentName();
    root.nextToken();

    
    
    switch (fieldName) {
      case "node_id":
        nodeId = UUID.fromString(root.getText());
        break;
      case "object_id":
        objectId = root.getText();
        break;
      case "node_path":
        nodePath = nullOrText();
        break;
      case "node_name":
        nodeName = root.getText();
        break;
      case "node_full_name":
        nodeFullName = root.getText();
        break;
      case "tree_id":
        treeId = UUID.fromString(root.getText());
        break;
      case "blob_id":
        blobId = nullOrUUID();
        break;
      case "props_id":
        propsId = nullOrUUID();
        break;
      case "created_at":
        createdAt = root.getText();
        break;
      case "updated_at":
        updatedAt = root.getText();
        break;
      case "created_by_author":
        createdByAuthor = root.getText();
        break;
      case "created_by":
        createdBy = UUID.fromString(root.getText());
        break;
      case "updated_by_author":
        updatedByAuthor = root.getText();
        break;
      case "updated_by":
        updatedBy = UUID.fromString(root.getText());
        break;
      case "blob_type":
        blobType = nullOrText();
        break;
      case "blob_class":
        blobClass = nullOrText();
        break;
      case "blob_value":
        if (root.currentToken() == JsonToken.START_OBJECT) {
          String rawJson = root.getCodec().readTree(root).toString();
          blobValue = new io.vertx.core.json.JsonObject(rawJson);
        }
        break;
      case "props_labels":
        if (root.currentToken() == JsonToken.START_OBJECT) {
          String rawJson = root.getCodec().readTree(root).toString();
          propsLabels = new io.vertx.core.json.JsonObject(rawJson);
        }
        break;
      case "props_comments":
        if (root.currentToken() == JsonToken.START_OBJECT) {
          String rawJson = root.getCodec().readTree(root).toString();
          propsComments = new io.vertx.core.json.JsonObject(rawJson);
        }
        break;
      case "props_permissions":
        if (root.currentToken() == JsonToken.START_OBJECT) {
          String rawJson = root.getCodec().readTree(root).toString();
          propsPermissions = new io.vertx.core.json.JsonObject(rawJson);
        }
        break;
      case "props_flags":
        if (root.currentToken() == JsonToken.START_OBJECT) {
          String rawJson = root.getCodec().readTree(root).toString();
          propsFlags = new io.vertx.core.json.JsonObject(rawJson);
        }
        break;
      default:
        root.skipChildren();
    }
    root.nextToken();
  }
  private UUID nullOrUUID() throws IOException {
    if(root.currentToken() == JsonToken.VALUE_NULL) {
      return null;
    }
    return UUID.fromString(root.getText());
  }
  private String nullOrText() throws IOException {
    if(root.currentToken() == JsonToken.VALUE_NULL) {
      return null;
    }
    return root.getText();
  }
}
