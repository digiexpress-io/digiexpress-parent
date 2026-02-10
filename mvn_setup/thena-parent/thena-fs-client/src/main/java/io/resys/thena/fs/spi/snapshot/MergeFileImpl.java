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
import java.util.function.BiConsumer;

import io.resys.thena.fs.api.commits.CommitBuilder.MergeFile;
import io.resys.thena.fs.api.commits.CommitBuilder.PropsBuilder;
import io.resys.thena.fs.entities.Blob;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Props;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.Value;


@RequiredArgsConstructor
public class MergeFileImpl implements MergeFile {
  private final Node prevNode;    
  
  private final MutableField<String> filePath = new MutableField<String>();
  private final MutableField<String> fileName = new MutableField<String>();
  private final MutableField<JsonObject> fileValue = new MutableField<JsonObject>();
  
  private BiConsumer<Optional<Props>, PropsBuilder> fileProps;
  private boolean validated = false;

  @Override
  public MergeFile fileValue(JsonObject fileValue) {
    this.fileValue.withNewValue(fileValue);
    return this;
  }
  @Override
  public MergeFile fileName(String fileName) {
    this.fileName.withNewValue(fileName);
    return this;
  }
  @Override
  public MergeFile filePath(String filePath) {
    this.filePath.withNewValue(filePath);
    return this;
  }
  @Override
  public MergeFile fileProps(BiConsumer<Optional<Props>, PropsBuilder> props) {
    this.fileProps = props;
    return this;
  }
  @Override
  public void build() {
    RepoAssert.isTrue(
        filePath.isNewValueSet() ||
        fileName.isNewValueSet() || 
        fileValue.isNewValueSet() || 
        fileProps != null,
        () -> "cannot have empty file merge(there are no changes)!");
    
    if(fileValue.isNewValueSet()) {
      RepoAssert.notNull(fileValue.getNewValue(), () -> "fileValue cannot be null");
      RepoAssert.isTrue(!fileValue.getNewValue().isEmpty(), () -> "fileValue cannot be empty");
    }
    this.validated = true;
  }

  public MergeFileResult close() {
    RepoAssert.isTrue(validated, () -> "build() method must be called before close()");
    
    
    // Merge props if provided
    final var nextProps = Optional.ofNullable(this.fileProps).map(p -> {
      final var prevProps = Optional.ofNullable(prevNode.getTransitives().getProps());
      final var builder = new PropsBuilderImpl(prevProps);
      p.accept(prevProps, builder);
      return builder.close().getProps();
    });
    
    
    // update only if defined
    final var prevBlob = prevNode.getTransitives().getBlob();
    
    // static data, once in its in... can't change content type
    final var blobType = prevBlob.getBlobType();

    final var nextBlobValue = fileValue.orElse(prevBlob.getBlobValue());
    final var nextBlob = Blob.newInstance(nextBlobValue, blobType).build();

    // static data, once in its in... can't change PK
    final var nodeId = prevNode.getObjectId();
    
    final var filePath = this.filePath.orElse(prevNode.getNodePath().orElse(null));
    final var fileName = this.fileName.orElse(prevNode.getNodeName());
    
    final var node = Node.newInstance(
        Optional.ofNullable(filePath), 
        nodeId, 
        fileName, 
        Optional.of(nextBlob.getId()), 
        nextProps.map(Props::getId)
      ).build();
    
    return new MergeFileResult(node, nextBlob, nextProps);
  }
  
  @Value
  public static class MergeFileResult {
    Node node;
    Blob blob;
    Optional<Props> props;    
  }
}
