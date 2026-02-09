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

import io.resys.thena.fs.api.commits.CommitBuilder.MergeFolder;
import io.resys.thena.fs.api.commits.CommitBuilder.PropsBuilder;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Props;
import io.resys.thena.support.RepoAssert;
import lombok.RequiredArgsConstructor;
import lombok.Value;


@RequiredArgsConstructor
public class MergeFolderImpl implements MergeFolder {
  private final Node prevNode;
  
  private final MutableField<String> folderPath = new MutableField<String>();
  private final MutableField<String> folderName = new MutableField<String>();

  private BiConsumer<Optional<Props>, PropsBuilder> folderProps;
  private boolean validated = false;
  
  @Override
  public MergeFolder folderName(String folderName) {
    this.folderName.withNewValue(folderName);
    return this;
  }
  @Override
  public MergeFolder folderPath(String folderPath) {
    this.folderPath.withNewValue(folderPath);
    return this;
  }
  @Override
  public MergeFolder folderProps(BiConsumer<Optional<Props>, PropsBuilder> folderProps) {
    this.folderProps = folderProps;
    return this;
  }
  @Override
  public void build() {
    RepoAssert.isTrue(
      folderName.isNewValueSet() ||
      folderPath.isNewValueSet() ||  
      folderProps != null,
      () -> "cannot have empty folder('" + prevNode.getNodePath() + "') merge(there are no changes)!");
    this.validated = true;
  }
  
  public MergeFolderResult close() {
    RepoAssert.isTrue(validated, () -> "build() method must be called before close()");
    
    // Merge props if provided
    final var nextProps = Optional.ofNullable(folderProps).map(p -> {
      final var prevProps = Optional.ofNullable(prevNode.getTransitives().getProps());
      final var builder = new PropsBuilderImpl(prevProps);
      p.accept(prevProps, builder);
      return builder.close().getProps();
    });
    
    // static data, once in its in... can't change PK
    final var nodeId = prevNode.getNodeId();
    
    final String folderPath = this.folderPath.orElse(prevNode.getNodePath().orElse(null));

    final String folderName = this.folderName.orElse(prevNode.getNodeName());
    
    final Optional<String> blobId = Optional.empty();
    final var nextNode = Node.newInstance(
        Optional.ofNullable(folderPath), 
        nodeId, 
        folderName, 
        blobId, 
        nextProps.map(Props::getId)
    ).build();
    
    return new MergeFolderResult(nextNode, nextProps);
  }
  
  @Value
  public static class MergeFolderResult {
    Node node;
    Optional<Props> props;    
  }
}
