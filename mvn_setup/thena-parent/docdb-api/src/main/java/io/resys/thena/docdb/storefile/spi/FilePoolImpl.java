package io.resys.thena.docdb.storefile.spi;

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

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.thena.docdb.storefile.tables.Table.FilePool;
import io.resys.thena.docdb.storefile.tables.Table.FilePreparedQuery;
import io.resys.thena.docdb.storefile.tables.Table.FileStatement;
import io.resys.thena.docdb.storefile.tables.Table.FileTuple;
import io.resys.thena.docdb.storefile.tables.Table.FileTupleList;
import io.resys.thena.docdb.support.RepoAssert;


public class FilePoolImpl implements FilePool {

  private final File rootDir;
  private final ObjectMapper objectMapper;
  private final FileConnection conn;
  
  public FilePoolImpl(File rootDir, ObjectMapper objectMapper) {
    super();
    this.rootDir = rootDir;
    RepoAssert.isTrue(rootDir.exists(), ()-> "root directory: '" + rootDir.getAbsolutePath() + "' must exist!");
    RepoAssert.isTrue(rootDir.canWrite(), ()-> "root directory: '" + rootDir.getAbsolutePath() + "' must be writtable!");
    RepoAssert.isTrue(rootDir.canRead(), ()-> "root directory: '" + rootDir.getAbsolutePath() + "' must be readable!");
    
    this.objectMapper = objectMapper;
    this.conn = new FileConnection(rootDir, objectMapper);
  }

  @Override
  public FilePreparedQuery<Object> preparedQuery(FileStatement query) {
    return new FileTuplePreparedQuery<Object>(rootDir, query, null, objectMapper, conn);
  }
  @Override
  public FilePreparedQuery<Object> preparedQuery(FileTuple query) {
    return new FileTuplePreparedQuery<Object>(rootDir, query, null, objectMapper, conn);
  }

  @Override
  public FilePreparedQuery<Object> preparedQuery(FileTupleList query) {
    return new FileTuplePreparedQuery<Object>(rootDir, query, null, objectMapper, conn);
  }

}
