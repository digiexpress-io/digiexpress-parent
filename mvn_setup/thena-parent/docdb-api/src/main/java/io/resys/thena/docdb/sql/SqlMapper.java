package io.resys.thena.docdb.sql;

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
import io.resys.thena.docdb.api.models.ThenaGitObject.Blob;
import io.resys.thena.docdb.api.models.ThenaGitObject.BlobHistory;
import io.resys.thena.docdb.api.models.ThenaGitObject.Branch;
import io.resys.thena.docdb.api.models.ThenaGitObject.Commit;
import io.resys.thena.docdb.api.models.ThenaGitObject.Tag;
import io.resys.thena.docdb.api.models.ThenaGitObject.Tree;
import io.resys.thena.docdb.api.models.ThenaGitObject.TreeValue;
import io.resys.thena.docdb.spi.support.DataMapper;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;

public interface SqlMapper extends DataMapper<Row>{
  Repo repo(Row row);
  Commit commit(Row row);
  Tree tree(Row row);
  TreeValue treeItem(Row row);
  Tag tag(Row row);
  Branch ref(Row row);
  Blob blob(Row row);
  BlobHistory blobHistory(Row row);
  JsonObject jsonObject(Row row, String columnName);
}
