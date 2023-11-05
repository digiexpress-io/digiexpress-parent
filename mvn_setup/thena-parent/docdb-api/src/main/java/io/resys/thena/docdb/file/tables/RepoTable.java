package io.resys.thena.docdb.file.tables;

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

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.thena.docdb.api.models.Repo.RepoType;
import io.resys.thena.docdb.file.tables.RepoTable.RepoTableRow;
import io.resys.thena.docdb.spi.DbCollections;

public interface RepoTable extends Table<RepoTableRow> {
  
  BlobTable getBlobs();
  CommitTable getCommits();
  RefTable getRefs();
  TagTable getTags();
  TreeItemTable getTreeItems();
  TreeTable getTrees();
  
  DbCollections getContext();
  
  @Value.Immutable @JsonSerialize(as = ImmutableRepoTableRow.class) @JsonDeserialize(as = ImmutableRepoTableRow.class)
  interface RepoTableRow extends Table.Row {
    String getId();
    String getRev();
    String getPrefix();
    String getName();
    RepoType getType();
  }
  
  RepoTable withContext(DbCollections ctx);
  
}