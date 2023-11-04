package io.resys.thena.docdb.file;

import io.resys.thena.docdb.file.builders.BlobHistoryFilePool;
import io.resys.thena.docdb.file.builders.BlobQueryFilePool;
import io.resys.thena.docdb.file.builders.CommitQueryFilePool;
import io.resys.thena.docdb.file.builders.RefQueryFilePool;
import io.resys.thena.docdb.file.builders.TagQueryFilePool;
import io.resys.thena.docdb.file.builders.TreeQueryFilePool;
import io.resys.thena.docdb.file.tables.Table.FileClientWrapper;
import io.resys.thena.docdb.file.tables.Table.FileMapper;

/*-
 * #%L
 * thena-docdb-mongo
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import io.resys.thena.docdb.spi.GitDbQueries;
import io.resys.thena.docdb.spi.ErrorHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ClientQueryFilePool implements GitDbQueries {
  
  private final FileClientWrapper wrapper;
  private final FileMapper mapper;
  private final FileBuilder builder;
  private final ErrorHandler errorHandler;
  
  @Override
  public TagQuery tags() {
    return new TagQueryFilePool(wrapper.getClient(), mapper, builder, errorHandler);
  }

  @Override
  public CommitQuery commits() {
    return new CommitQueryFilePool(wrapper.getClient(), mapper, builder, errorHandler);
  }

  @Override
  public RefQuery refs() {
    return new RefQueryFilePool(wrapper.getClient(), mapper, builder, errorHandler);
  }

  @Override
  public TreeQuery trees() {
    return new TreeQueryFilePool(wrapper.getClient(), mapper, builder, errorHandler);
  }

  @Override
  public BlobQuery blobs() {
    return new BlobQueryFilePool(wrapper.getClient(), mapper, builder, errorHandler);
  }

  @Override
  public BlobHistoryQuery blobHistory() {
    return new BlobHistoryFilePool(wrapper.getClient(), mapper, builder);
  }
}
