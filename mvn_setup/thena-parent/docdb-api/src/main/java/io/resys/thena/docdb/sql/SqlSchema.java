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

import io.resys.thena.docdb.spi.ClientCollections;
import io.resys.thena.docdb.sql.SqlBuilder.Sql;

public interface SqlSchema extends ClientCollections.WithOptions<SqlSchema>{
  SqlSchema withOptions(ClientCollections options);
  
  Sql createRepo();
  Sql createBlobs();

  Sql createCommits();
  Sql createCommitsConstraints();
  
  Sql createTreeItemsConstraints();
  Sql createTreeItems();

  Sql createTrees();

  Sql createRefs();
  Sql createRefsConstraints();

  Sql createTags();
  Sql createTagsConstraints();
  
  
  Sql createDoc();
  
  Sql createDocBranch();
  Sql createDocBranchConstraints();
  
  Sql createDocCommits();
  Sql createDocCommitsConstraints();
  
  Sql createDocLog();
  Sql createDocLogConstraints();
  
  Sql dropDoc();
  Sql dropDocBranch();
  Sql dropDocCommit();
  Sql dropDocLog();
  
  Sql dropRepo();
  Sql dropBlobs();
  Sql dropCommits();
  Sql dropTreeItems();
  Sql dropTrees();
  Sql dropRefs();
  Sql dropTags();
}
