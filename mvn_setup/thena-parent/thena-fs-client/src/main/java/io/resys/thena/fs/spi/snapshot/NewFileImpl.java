package io.resys.thena.fs.spi.snapshot;

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

import java.util.Optional;
import java.util.function.Consumer;

import io.resys.thena.fs.api.commits.CommitBuilder.NewFile;
import io.resys.thena.fs.api.commits.CommitBuilder.PropsBuilder;
import io.resys.thena.fs.entities.Blob;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Props;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.Value;


@RequiredArgsConstructor
public class NewFileImpl implements NewFile {
  private final Optional<Ref> lock;
  
  private MutableField<String> filePath = new MutableField<>();
  private MutableField<String> fileName = new MutableField<>();
  private MutableField<String> fileId = new MutableField<>();
  private MutableField<String> fileType = new MutableField<>();
  private MutableField<String> fileClass = new MutableField<>();
  private MutableField<JsonObject> fileValue = new MutableField<>();
  private Consumer<PropsBuilder> fileProps;
  private boolean validated = false;

  @Override
  public NewFile fileProps(Consumer<PropsBuilder> props) {
    this.fileProps = props;
    return this;
  }

  @Override
  public NewFile fileValue(JsonObject blob) {
    this.fileValue.withNewValue(blob);
    return this;
  }

  @Override
  public NewFile fileType(String type) {
    this.fileType.withNewValue(type);
    return this;
  }

  @Override
  public NewFile fileName(String name) {
    this.fileName.withNewValue(name);
    return this;
  }

  @Override
  public NewFile fileId(String fileId) {
    this.fileId.withNewValue(fileId);
    return this;
  }

  @Override
  public NewFile filePath(String path) {
    this.filePath.withNewValue(path);
    return this;
  }
  @Override
  public NewFile fileClass(String fileClass) {
    this.fileClass.withNewValue(fileClass);
    return this;
  }
  @Override
  public void build() {
    this.validated = true;
  }
  
  public NewFileResult close() {
    RepoAssert.isTrue(validated, () -> "build() method must be called before close()");
    
    final var prevNode = lock.flatMap(ref -> {
      if(this.fileId.isNewValueSet()) {
        return ref.getTransitives().getTree().findOneNode(this.fileId.getNewValue());  
      }
      return Optional.empty();
    });
    
    final var objectId = getObjectId(prevNode);
    final var fileValue = getFileValue(prevNode);
    final var fileType = getFileType(prevNode);
    final var fileClass = getFileClass(prevNode);
    
    final var blob = Blob.newInstance(fileValue, fileType, fileClass).build();
    final var props = Optional.ofNullable(this.fileProps).map(p -> {
      final var builder = new PropsBuilderImpl(Optional.empty());
      p.accept(builder);
      return builder.close().getProps();
    });
    
    final var blobId = Optional.of(blob.getId());
    final var propsId = getPropsId(prevNode, props);
    
    final var filePath = getFilePath(prevNode);
    final var fileName = getFileName(prevNode);
    
    final var node = Node.newInstance(
      filePath, 
      objectId, 
      fileName,
      blobId, 
      propsId
    ).build();
    
    return new NewFileResult(node, prevNode, blob, props);
  }
  
  private String getFileName(Optional<Node> prevNode) {
    final var fileName = prevNode.map(Node::getNodeName).orElse(this.fileName.getNewValue());
    RepoAssert.notNull(fileName, () -> "fileName can't be null!");
    return fileName;
  }
  
  private Optional<String> getFilePath(Optional<Node> prevNode) {
    final var filePath = prevNode.flatMap(Node::getNodePath).orElse(this.filePath.getNewValue());
    return Optional.ofNullable(filePath);
  }
  
  private Optional<String> getPropsId(Optional<Node> prevNode, Optional<Props> props) {
    if(props.isPresent()) {
      return props.map(Props::getId);
    }
    return prevNode.flatMap(Node::getPropsId);
  }
  
  private JsonObject getFileValue(Optional<Node> prevNode) {
    final var value = this.fileValue.getNewValue();
    RepoAssert.notNull(
        fileValue, () -> "File value can't be null!");
    return value;
  }
  
  private String getFileType(Optional<Node> prevNode) {
    if(prevNode.isPresent()) {
      final var prevFileType = prevNode.get().getTransitives().getBlob().getBlobType();
      
      if(this.fileType.isNewValueSet()) {
        final var newFileType = this.fileType.getNewValue();
        RepoAssert.isTrue(
          newFileType.equals(prevFileType), 
          () -> "File type can not be changed, expected: '" + prevFileType + "' actual: '" + newFileType + "'!");
      }
      return prevFileType;
    }
    return this.fileType.getNewValue();
  }
  private String getFileClass(Optional<Node> prevNode) {
    
    if(prevNode.isPresent()) {
      final var prevFileClass = prevNode.get().getTransitives().getBlob().getBlobType();
      
      if(this.fileClass.isNewValueSet()) {
        final var newFileClass = this.fileClass.getNewValue();
        RepoAssert.isTrue(
            newFileClass.equals(prevFileClass), 
          () -> "File class can not be changed, expected: '" + newFileClass + "' actual: '" + prevFileClass + "'!");
      }
      return prevFileClass;
    }
    return this.fileClass.getNewValue();
  }
  
  private String getObjectId(Optional<Node> prevNode) {
    final var objectId = prevNode.map(e -> e.getObjectId())
        .or(() -> Optional.ofNullable(this.fileId.getNewValue()))
        .orElseGet(() -> OidUtils.genUUID());
    
    return objectId;
  }
 
  @Value
  public static class NewFileResult {
    Node node;
    Optional<Node> prevNode;
    Blob blob;
    Optional<Props> props;    
  }
}
