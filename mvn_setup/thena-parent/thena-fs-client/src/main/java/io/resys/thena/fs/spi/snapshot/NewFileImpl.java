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
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.Value;


@RequiredArgsConstructor
public class NewFileImpl implements NewFile {
  
  
  private String filePath;
  private String fileName;
  private String fileId;
  private String fileType;
  private String fileClass;
  private JsonObject fileValue;
  private Consumer<PropsBuilder> fileProps;
  private boolean validated = false;

  @Override
  public NewFile fileProps(Consumer<PropsBuilder> props) {
    this.fileProps = props;
    return this;
  }

  @Override
  public NewFile fileValue(JsonObject blob) {
    this.fileValue = blob;
    return this;
  }

  @Override
  public NewFile fileType(String type) {
    this.fileType = type;
    return this;
  }

  @Override
  public NewFile fileName(String name) {
    this.fileName = name;
    return this;
  }

  @Override
  public NewFile fileId(String fileId) {
    this.fileId = fileId;
    return this;
  }

  @Override
  public NewFile filePath(String path) {
    this.filePath = path;
    return this;
  }
  @Override
  public NewFile fileClass(String fileClass) {
    this.fileClass = fileClass;
    return this;
  }
  @Override
  public void build() {
    this.validated = true;
  }
  
  public NewFileResult close() {
    RepoAssert.isTrue(validated, () -> "build() method must be called before close()");
    
    final var nodeId = Optional
        .ofNullable(this.fileId)
        .orElseGet(() -> OidUtils.genUUID());
    
    final var blob = Blob.newInstance(this.fileValue, this.fileType, this.fileClass).build();
    
    final var props = Optional.ofNullable(this.fileProps).map(p -> {
      final var builder = new PropsBuilderImpl(Optional.empty());
      p.accept(builder);
      return builder.close().getProps();
    });
    
    final var blobId = Optional.of(blob.getId());
    final var propsId = props.map(Props::getId);
    final var node = Node.newInstance(
        Optional.ofNullable(this.filePath), 
        nodeId, this.fileName, 
        blobId, propsId
    ).build();
    
    return new NewFileResult(node, blob, props);
  }
  
 
  @Value
  public static class NewFileResult {
    Node node;
    Blob blob;
    Optional<Props> props;    
  }
}
