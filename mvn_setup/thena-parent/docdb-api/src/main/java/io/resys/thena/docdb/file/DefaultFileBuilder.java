package io.resys.thena.docdb.file;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2022 Copyright 2021 ReSys OÜ
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

import io.resys.thena.docdb.file.spi.BlobFileBuilderImpl;
import io.resys.thena.docdb.file.spi.CommitFileBuilderImpl;
import io.resys.thena.docdb.file.spi.RefFileBuilderImpl;
import io.resys.thena.docdb.file.spi.RepoFileBuilderImpl;
import io.resys.thena.docdb.file.spi.TagFileBuilderImpl;
import io.resys.thena.docdb.file.spi.TreeFileBuilderImpl;
import io.resys.thena.docdb.file.spi.TreeItemFileBuilderImpl;
import io.resys.thena.docdb.spi.DbCollections;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultFileBuilder implements FileBuilder {
  private final DbCollections ctx;

  @Override
  public RepoFileBuilder repo() {
    return new RepoFileBuilderImpl(ctx);
  }

  @Override
  public RefFileBuilder refs() {
    return new RefFileBuilderImpl(ctx);
  }

  @Override
  public TagFileBuilder tags() {
    return new TagFileBuilderImpl(ctx);
  }

  @Override
  public BlobFileBuilder blobs() {
    return new BlobFileBuilderImpl(ctx);
  }

  @Override
  public CommitFileBuilder commits() {
    return new CommitFileBuilderImpl(ctx);
  }

  @Override
  public TreeFileBuilder trees() {
    return new TreeFileBuilderImpl(ctx);
  }

  @Override
  public TreeItemFileBuilder treeItems() {
    return new TreeItemFileBuilderImpl(ctx);
  }

  @Override
  public FileBuilder withOptions(DbCollections options) {
    return new DefaultFileBuilder(options);
  }
}
