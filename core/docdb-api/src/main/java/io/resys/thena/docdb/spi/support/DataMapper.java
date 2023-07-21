package io.resys.thena.docdb.spi.support;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.ThenaObject.Blob;
import io.resys.thena.docdb.api.models.ThenaObject.BlobHistory;
import io.resys.thena.docdb.api.models.ThenaObject.Branch;
import io.resys.thena.docdb.api.models.ThenaObject.Commit;
import io.resys.thena.docdb.api.models.ThenaObject.CommitTree;
import io.resys.thena.docdb.api.models.ThenaObject.Tag;
import io.resys.thena.docdb.api.models.ThenaObject.Tree;
import io.resys.thena.docdb.api.models.ThenaObject.TreeValue;
import io.vertx.mutiny.sqlclient.Row;

public interface DataMapper<T> {
  Repo repo(T row);
  Commit commit(T row);
  Tree tree(T row);
  TreeValue treeItem(T row);
  Tag tag(T row);
  Branch ref(T row);
  Blob blob(T row);
  BlobHistory blobHistory(T row);
  CommitTree commitTree(T row);
  CommitTree commitTreeWithBlobs(Row row);
}
