package io.resys.thena.docdb.store.sql;

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

import io.resys.thena.docdb.spi.DbCollections;
import io.resys.thena.docdb.store.sql.SqlBuilder.Sql;

public interface SqlSchema extends DbCollections.WithOptions<SqlSchema>{
  SqlSchema withOptions(DbCollections options);
  

  // create git like db mdoel
  Sql createGitBlobs();

  Sql createGitCommits();
  Sql createGitCommitsConstraints();
  
  Sql createGitTreeItemsConstraints();
  Sql createGitTreeItems();

  Sql createGitTrees();

  Sql createGitRefs();
  Sql createGitRefsConstraints();

  Sql createGitTags();
  Sql createGitTagsConstraints();
  
  Sql dropGitBlobs();
  Sql dropGitCommits();
  Sql dropGitTreeItems();
  Sql dropGitTrees();
  Sql dropGitRefs();
  Sql dropGitTags();
  
  
  
  // single doc db model
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
  
  
  
  // organization model
  Sql createOrgRoles();
  Sql createOrgGroups();
  Sql createOrgGroupRoles();
  
  Sql createOrgUsers();
  Sql createOrgUserRoles();
  Sql createOrgUserMemberships();

  Sql createOrgActorStatus();
  Sql createOrgActorLogs();
  Sql createOrgActorData();
  
  Sql createOrgRolesConstraints();
  Sql createOrgUserConstraints();  
  Sql createOrgGroupConstraints();  
  Sql createOrgCommitConstraints();
  

  Sql dropOrgRoles();
  Sql dropOrgGroups();
  Sql dropOrgGroupRoles();
  
  Sql dropOrgUsers();
  Sql dropOrgUserRoles();
  Sql dropOrgUserMemberships();

  Sql dropOrgActorStatus();
  Sql dropOrgActorLogs();
  Sql dropOrgActorData();

  
  
  
  // central tracker for tables
  Sql createRepo();
  Sql dropRepo();


  
}
