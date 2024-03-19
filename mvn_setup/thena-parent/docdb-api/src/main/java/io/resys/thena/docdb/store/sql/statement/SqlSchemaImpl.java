package io.resys.thena.docdb.store.sql.statement;

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
import io.resys.thena.docdb.store.sql.ImmutableSql;
import io.resys.thena.docdb.store.sql.SqlBuilder.Sql;
import io.resys.thena.docdb.store.sql.SqlSchema;
import io.resys.thena.docdb.store.sql.support.SqlStatement;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SqlSchemaImpl implements SqlSchema {

  protected final DbCollections options;
  
  @Override
  public Sql createRepo() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("CREATE TABLE IF NOT EXISTS ").append(options.getRepos()).ln()
        .append("(").ln()
        .append("  id VARCHAR(40) PRIMARY KEY,").ln()
        .append("  rev VARCHAR(40) NOT NULL,").ln()
        .append("  prefix VARCHAR(40) NOT NULL,").ln()
        .append("  type VARCHAR(3) NOT NULL,").ln()
        .append("  name VARCHAR(255) NOT NULL,").ln()
        .append("  UNIQUE(name), UNIQUE(rev), UNIQUE(prefix)").ln()
        .append(")").ln()
        .build()).build();
  }
  
  @Override
  public Sql createGitBlobs() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getBlobs()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  value jsonb NOT NULL").ln()
    
    // string based -- .append("  value TEXT NOT NULL").ln()
    
    .append(");").ln()
    .build()).build();
  }
  
  
  @Override
  public Sql createGitCommits() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getCommits()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  datetime VARCHAR(29) NOT NULL,").ln()
    .append("  author VARCHAR(40) NOT NULL,").ln()
    .append("  message VARCHAR(255) NOT NULL,").ln()
    .append("  tree VARCHAR(40) NOT NULL,").ln()
    .append("  parent VARCHAR(40),").ln()
    .append("  merge VARCHAR(40)").ln()
    .append(");").ln()
    .build()).build();
  }
  
  @Override
  public Sql createGitCommitsConstraints() {
    return ImmutableSql.builder()
        .value(new SqlStatement().ln()
        .append("ALTER TABLE ").append(options.getCommits()).ln()
        .append("  ADD CONSTRAINT ").append(options.getCommits()).append("_COMMIT_PARENT_FK").ln()
        .append("  FOREIGN KEY (parent)").ln()
        .append("  REFERENCES ").append(options.getCommits()).append(" (id);").ln()
        
        .append("ALTER TABLE ").append(options.getCommits()).ln()
        .append("  ADD CONSTRAINT ").append(options.getCommits()).append("_COMMIT_TREE_FK").ln()
        .append("  FOREIGN KEY (tree)").ln()
        .append("  REFERENCES ").append(options.getTrees()).append(" (id);").ln()
        
        .append("CREATE INDEX ").append(options.getCommits()).append("_TREE_INDEX")
        .append(" ON ").append(options.getTreeItems()).append(" (tree);").ln()
        
        .append("CREATE INDEX ").append(options.getCommits()).append("_PARENT_INDEX")
        .append(" ON ").append(options.getTreeItems()).append(" (tree);").ln()
        .build())
        .build();
  }
  

  @Override
  public Sql createGitTreeItems() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getTreeItems())
    .append("(")
    .append("  id SERIAL PRIMARY KEY,")
    .append("  name VARCHAR(255) NOT NULL,")
    .append("  blob VARCHAR(40) NOT NULL,")
    .append("  tree VARCHAR(40) NOT NULL")
    .append(");")
    .build()).build();
  }
  @Override
  public Sql createGitTreeItemsConstraints() {
    return ImmutableSql.builder()
        .value(new SqlStatement().ln()
        .append("ALTER TABLE ").append(options.getTreeItems()).ln()
        .append("  ADD CONSTRAINT ").append(options.getTreeItems()).append("_TREE_ITEM_BLOB_FK").ln()
        .append("  FOREIGN KEY (blob)").ln()
        .append("  REFERENCES ").append(options.getBlobs()).append(" (id);").ln()
        .append("ALTER TABLE ").append(options.getTreeItems()).ln()
        .append("  ADD CONSTRAINT ").append(options.getTreeItems()).append("_TREE_ITEM_PARENT_FK").ln()
        .append("  FOREIGN KEY (tree)").ln()
        .append("  REFERENCES ").append(options.getTrees()).append(" (id);").ln()
        .append("ALTER TABLE ").append(options.getTreeItems()).ln()
        .append("  ADD CONSTRAINT ").append(options.getTreeItems()).append("_TREE_NAME_BLOB_UNIQUE").ln()
        .append("  UNIQUE (tree, name, blob);").ln()
        
        .append("CREATE INDEX ").append(options.getTreeItems()).append("_TREE_INDEX")
        .append(" ON ").append(options.getTreeItems()).append(" (tree);").ln()
//        .append("CREATE INDEX ").append(options.getTreeItems()).append("_TREE_BLOB_INDEX")
//        .append(" ON ").append(options.getTreeItems()).append(" (tree, blob);").ln()
//        .append("CREATE INDEX ").append(options.getTreeItems()).append("_TREE_NAME_INDEX")
//        .append(" ON ").append(options.getTreeItems()).append(" (tree, name);").ln()
        .build())
        .build();
  }
  

  @Override
  public Sql createGitTrees() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getTrees()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY").ln()
    .append(");").ln()
    .build()).build();
  }
  
  @Override
  public Sql createGitRefs() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getRefs()).ln()
    .append("(").ln()
    .append("  name VARCHAR(100) PRIMARY KEY,").ln()
    .append("  commit VARCHAR(40) NOT NULL").ln()
    .append(");").ln()
    .build()).build();
  }
  @Override
  public Sql createGitRefsConstraints() {
    return ImmutableSql.builder()
        .value(new SqlStatement().ln()
        .append("ALTER TABLE ").append(options.getRefs()).ln()
        .append("  ADD CONSTRAINT ").append(options.getRefs()).append("_REF_COMMIT_FK").ln()
        .append("  FOREIGN KEY (commit)").ln()
        .append("  REFERENCES ").append(options.getCommits()).append(" (id);").ln()
        .build())
        .build();
  }
  
  @Override
  public Sql createGitTags() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getTags()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit VARCHAR(40) NOT NULL,").ln()
    .append("  datetime VARCHAR(29) NOT NULL,").ln()
    .append("  author VARCHAR(40) NOT NULL,").ln()
    .append("  message VARCHAR(100) NOT NULL").ln()
    .append(");").ln()
    .build()).build();
  }
  @Override
  public Sql createGitTagsConstraints() {
    return ImmutableSql.builder()
        .value(new SqlStatement().ln()
        .append("ALTER TABLE ").append(options.getTags()).ln()
        .append("  ADD CONSTRAINT ").append(options.getTags()).append("_TAG_COMMIT_FK").ln()
        .append("  FOREIGN KEY (commit)").ln()
        .append("  REFERENCES ").append(options.getCommits()).append(" (id);").ln()
        .build())
        .build();
  }
  
  @Override
  public SqlSchemaImpl withOptions(DbCollections options) {
    return new SqlSchemaImpl(options);
  }

  @Override
  public Sql createDoc() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getDoc()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  external_id VARCHAR(40) UNIQUE,").ln()
    .append("  external_id_deleted VARCHAR(40),").ln()
    .append("  owner_id VARCHAR(40),").ln()
    .append("  doc_parent_id VARCHAR(40),").ln()
    .append("  doc_type VARCHAR(40) NOT NULL,").ln()
    .append("  doc_status VARCHAR(8) NOT NULL,").ln()
    .append("  doc_meta jsonb").ln()
    .append(");").ln()
    
    .append("CREATE INDEX ").append(options.getDoc()).append("_DOC_EXT_ID_INDEX")
    .append(" ON ").append(options.getDoc()).append(" (external_id);").ln()

    .append("CREATE INDEX ").append(options.getDoc()).append("_DOC_PARENT_ID_INDEX")
    .append(" ON ").append(options.getDoc()).append(" (doc_parent_id);").ln()

    .append("CREATE INDEX ").append(options.getDoc()).append("_DOC_TYPE_INDEX")
    .append(" ON ").append(options.getDoc()).append(" (doc_type);").ln()

    .append("CREATE INDEX ").append(options.getDoc()).append("_DOC_OWNER_INDEX")
    .append(" ON ").append(options.getDoc()).append(" (owner_id);").ln()

    // internal foreign key
    .append("ALTER TABLE ").append(options.getDoc()).ln()
    .append("  ADD CONSTRAINT ").append(options.getDoc()).append("_DOC_PARENT_FK").ln()
    .append("  FOREIGN KEY (doc_parent_id)").ln()
    .append("  REFERENCES ").append(options.getDoc()).append(" (id);").ln().ln()
      
    .build()).build();
  }

  @Override
  public Sql createDocBranch() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getDocBranch()).ln()
    .append("(").ln()
    .append("  branch_name VARCHAR(255) NOT NULL,").ln()
    .append("  branch_name_deleted VARCHAR(255),").ln()
    .append("  branch_id VARCHAR(40) NOT NULL,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  branch_status VARCHAR(8) NOT NULL,").ln()
    .append("  doc_id VARCHAR(40),").ln()
    .append("  value jsonb NOT NULL,").ln()
    .append("  PRIMARY KEY (branch_id),").ln()
    .append("  UNIQUE (doc_id, branch_name)").ln()
    .append(");").ln()
    
    .append("CREATE INDEX ").append(options.getDocBranch()).append("_DOC_DOC_ID_INDEX")
    .append(" ON ").append(options.getDocBranch()).append(" (doc_id);").ln()

    .append("CREATE INDEX ").append(options.getDocBranch()).append("_DOC_BRANCH_NAME_INDEX")
    .append(" ON ").append(options.getDocBranch()).append(" (branch_name);").ln()
    
    .append("CREATE INDEX ").append(options.getDocBranch()).append("_DOC_COMMIT_ID_INDEX")
    .append(" ON ").append(options.getDocBranch()).append(" (commit_id);").ln()
    
    .build()).build();
  }
  
  @Override
  public Sql createDocCommits() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getDocCommits()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  branch_id VARCHAR(40) NOT NULL,").ln()
    .append("  doc_id VARCHAR(40) NOT NULL,").ln()
    .append("  datetime VARCHAR(29) NOT NULL,").ln()
    .append("  author VARCHAR(40) NOT NULL,").ln()
    .append("  message VARCHAR(255) NOT NULL,").ln()
    .append("  parent VARCHAR(40)").ln()
    .append(");").ln()
    
    .append("CREATE INDEX ").append(options.getDocCommits()).append("_DOC_COMMIT_DOC_ID_INDEX")
    .append(" ON ").append(options.getDocCommits()).append(" (doc_id);").ln()
    
    .append("CREATE INDEX ").append(options.getDocCommits()).append("_DOC_COMMIT_PARENT_INDEX")
    .append(" ON ").append(options.getDocCommits()).append(" (parent);").ln()
    
    .append("CREATE INDEX ").append(options.getDocCommits()).append("_DOC_COMMIT_BRANCH_ID_INDEX")
    .append(" ON ").append(options.getDocCommits()).append(" (branch_id);").ln()

     // internal foreign key
    .append("ALTER TABLE ").append(options.getDocCommits()).ln()
    .append("  ADD CONSTRAINT ").append(options.getDocCommits()).append("_DOC_COMMIT_PARENT_FK").ln()
    .append("  FOREIGN KEY (parent)").ln()
    .append("  REFERENCES ").append(options.getDocCommits()).append(" (id);").ln().ln()
    
    .build()).build();
  }
  
  @Override
  public Sql createDocLog() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getDocLog()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  value jsonb NOT NULL").ln()
    .append(");").ln()
    

    .append("CREATE INDEX ").append(options.getDocLog()).append("_DOC_LOG_COMMIT_ID_INDEX")
    .append(" ON ").append(options.getDocLog()).append(" (commit_id);").ln()
    
    .build()).build();
  }

  @Override
  public Sql createDocCommitsConstraints() {
    return ImmutableSql.builder()
        .value(new SqlStatement().ln()
        .append("ALTER TABLE ").append(options.getDocCommits()).ln()
        .append("  ADD CONSTRAINT ").append(options.getDocCommits()).append("_DOC_COMMIT_FK").ln()
        .append("  FOREIGN KEY (doc_id)").ln()
        .append("  REFERENCES ").append(options.getDoc()).append(" (id);").ln().ln()
        
//        .append("ALTER TABLE ").append(options.getDocCommits()).ln()
//        .append("  ADD CONSTRAINT ").append(options.getDocCommits()).append("_BRANCH_ID_FK").ln()
//        .append("  FOREIGN KEY (branch_id)").ln()
//        .append("  REFERENCES ").append(options.getDocBranch()).append(" (branch_id);").ln().ln()
            
        .build())
        .build();
  }

  @Override
  public Sql createDocLogConstraints() {
    return ImmutableSql.builder()
        .value(new SqlStatement().ln()
        .append("ALTER TABLE ").append(options.getDocLog()).ln()
        .append("  ADD CONSTRAINT ").append(options.getDocLog()).append("_DOC_LOG_COMMIT_FK").ln()
        .append("  FOREIGN KEY (commit_id)").ln()
        .append("  REFERENCES ").append(options.getDocCommits()).append(" (id);").ln().ln()
        
        .build())
        .build();
  }
  

  @Override
  public Sql createDocBranchConstraints() {
    return ImmutableSql.builder()
        .value(new SqlStatement().ln()
        .append("ALTER TABLE ").append(options.getDocBranch()).ln()
        .append("  ADD CONSTRAINT ").append(options.getDocBranch()).append("_DOC_ID_FK").ln()
        .append("  FOREIGN KEY (doc_id)").ln()
        .append("  REFERENCES ").append(options.getDoc()).append(" (id);").ln().ln()
        .build())
        .build();
  }

  @Override
  public Sql createOrgRoles() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getOrgRights()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  external_id VARCHAR(40) UNIQUE,").ln()
    .append("  role_name VARCHAR(255) UNIQUE NOT NULL,").ln()
    .append("  role_description VARCHAR(255) NOT NULL").ln()
    .append(");").ln()
    
    
    .append("CREATE INDEX ").append(options.getOrgRights()).append("_NAME_INDEX")
    .append(" ON ").append(options.getOrgRights()).append(" (role_name);").ln()
    
    .append("CREATE INDEX ").append(options.getOrgRights()).append("_COMMIT_INDEX")
    .append(" ON ").append(options.getOrgRights()).append(" (commit_id);").ln()
    
    .append("CREATE INDEX ").append(options.getOrgRights()).append("_EXTERNAL_INDEX")
    .append(" ON ").append(options.getOrgRights()).append(" (external_id);").ln()
    

    .build()).build();
  }

  @Override
  public Sql createOrgGroups() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getOrgParties()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  external_id VARCHAR(40) UNIQUE,").ln()
    .append("  parent_id VARCHAR(40),").ln()
    .append("  group_name VARCHAR(255) UNIQUE NOT NULL,").ln()
    .append("  group_description VARCHAR(255) NOT NULL").ln()
    .append(");").ln().ln()
    
    // parent id, references self
    .append("ALTER TABLE ").append(options.getOrgParties()).ln()
    .append("  ADD CONSTRAINT ").append(options.getOrgParties()).append("_PARENT_FK").ln()
    .append("  FOREIGN KEY (parent_id)").ln()
    .append("  REFERENCES ").append(options.getOrgParties()).append(" (id);").ln()

    
    .append("CREATE INDEX ").append(options.getOrgParties()).append("_NAME_INDEX")
    .append(" ON ").append(options.getOrgParties()).append(" (group_name);").ln()
    
    .append("CREATE INDEX ").append(options.getOrgParties()).append("_COMMIT_INDEX")
    .append(" ON ").append(options.getOrgParties()).append(" (commit_id);").ln()

    .append("CREATE INDEX ").append(options.getOrgParties()).append("_EXTERNAL_INDEX")
    .append(" ON ").append(options.getOrgParties()).append(" (external_id);").ln()
    
    
    .build()).build();
  }

  @Override
  public Sql createOrgGroupRoles() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getOrgPartyRights()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  party_id VARCHAR(40) NOT NULL,").ln()
    .append("  right_id VARCHAR(40) NOT NULL,").ln()
    .append("  UNIQUE (right_id, party_id)").ln()
    .append(");").ln()
    
    
    .append("CREATE INDEX ").append(options.getOrgPartyRights()).append("_COMMIT_INDEX")
    .append(" ON ").append(options.getOrgPartyRights()).append(" (commit_id);").ln()

    .append("CREATE INDEX ").append(options.getOrgPartyRights()).append("_GROUP_INDEX")
    .append(" ON ").append(options.getOrgPartyRights()).append(" (party_id);").ln()

    .append("CREATE INDEX ").append(options.getOrgPartyRights()).append("_ROLE_INDEX")
    .append(" ON ").append(options.getOrgPartyRights()).append(" (right_id);").ln()    

    .build()).build();
  }

  @Override
  public Sql createOrgUsers() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getOrgMembers()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  external_id VARCHAR(40) UNIQUE,").ln()
    .append("  username VARCHAR(255) UNIQUE NOT NULL,").ln()
    .append("  email VARCHAR(255) NOT NULL").ln()
    .append(");").ln()
    
    
    .append("CREATE INDEX ").append(options.getOrgMembers()).append("_COMMIT_INDEX")
    .append(" ON ").append(options.getOrgMembers()).append(" (commit_id);").ln()

    .append("CREATE INDEX ").append(options.getOrgMembers()).append("_EXTERNAL_INDEX")
    .append(" ON ").append(options.getOrgMembers()).append(" (external_id);").ln()

    .append("CREATE INDEX ").append(options.getOrgMembers()).append("_USER_NAME_INDEX")
    .append(" ON ").append(options.getOrgMembers()).append(" (username);").ln()

    .build()).build();
  }

  @Override
  public Sql createOrgUserRoles() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getOrgMemberRights()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  user_id VARCHAR(40) NOT NULL,").ln()
    .append("  right_id VARCHAR(40) NOT NULL,").ln()
    .append("  party_id VARCHAR(40),").ln()
    .append("  UNIQUE (user_id, right_id, party_id)").ln()
    .append(");").ln()
    
    .append("CREATE INDEX ").append(options.getOrgMemberRights()).append("_COMMIT_INDEX")
    .append(" ON ").append(options.getOrgMemberRights()).append(" (commit_id);").ln()
    
    .append("CREATE INDEX ").append(options.getOrgMemberRights()).append("_ROLE_INDEX")
    .append(" ON ").append(options.getOrgMemberRights()).append(" (right_id);").ln()

    .append("CREATE INDEX ").append(options.getOrgMemberRights()).append("_USER_INDEX")
    .append(" ON ").append(options.getOrgMemberRights()).append(" (user_id);").ln()
    

    .append("CREATE INDEX ").append(options.getOrgMemberRights()).append("_GROUP_INDEX")
    .append(" ON ").append(options.getOrgMemberRights()).append(" (party_id);").ln()
    
    .append("CREATE INDEX ").append(options.getOrgMemberRights()).append("_REF_INDEX")
    .append(" ON ").append(options.getOrgMemberRights()).append(" (right_id, user_id);").ln()    

    .append("CREATE INDEX ").append(options.getOrgMemberRights()).append("_REF_2_INDEX")
    .append(" ON ").append(options.getOrgMemberRights()).append(" (right_id, user_id, party_id);").ln()    


    .build()).build();
  }

  @Override
  public Sql createOrgUserMemberships() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getOrgMemberships()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  user_id VARCHAR(40) NOT NULL,").ln()
    .append("  party_id VARCHAR(40) NOT NULL,").ln()
    .append("  UNIQUE (user_id, party_id)").ln()
    .append(");").ln()
    
    .append("CREATE INDEX ").append(options.getOrgMemberships()).append("_COMMIT_INDEX")
    .append(" ON ").append(options.getOrgMemberships()).append(" (commit_id);").ln()
    
    .append("CREATE INDEX ").append(options.getOrgMemberships()).append("_USER_INDEX")
    .append(" ON ").append(options.getOrgMemberships()).append(" (user_id);").ln()
    
    .append("CREATE INDEX ").append(options.getOrgMemberships()).append("_GROUP_INDEX")
    .append(" ON ").append(options.getOrgMemberships()).append(" (party_id);").ln()
    
    .append("CREATE INDEX ").append(options.getOrgMemberships()).append("_REF_INDEX")
    .append(" ON ").append(options.getOrgMemberships()).append(" (party_id, user_id);").ln()    


    .build()).build();
  }

  @Override
  public Sql createOrgActorStatus() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getOrgActorStatus()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  user_id VARCHAR(40),").ln()
    .append("  right_id VARCHAR(40),").ln()
    .append("  party_id VARCHAR(40),").ln()
    .append("  actor_status VARCHAR(100) NOT NULL,").ln() // visibility: in_force | archived 
    .append("  UNIQUE (user_id, right_id, party_id)").ln()
    .append(");").ln()
    
    .append("CREATE INDEX ").append(options.getOrgActorStatus()).append("_COMMIT_INDEX")
    .append(" ON ").append(options.getOrgActorStatus()).append(" (commit_id);").ln()

    .append("CREATE INDEX ").append(options.getOrgActorStatus()).append("_ROLE_INDEX")
    .append(" ON ").append(options.getOrgActorStatus()).append(" (right_id);").ln()
    
    .append("CREATE INDEX ").append(options.getOrgActorStatus()).append("_USER_INDEX")
    .append(" ON ").append(options.getOrgActorStatus()).append(" (user_id);").ln()
    
    .append("CREATE INDEX ").append(options.getOrgActorStatus()).append("_GROUP_INDEX")
    .append(" ON ").append(options.getOrgActorStatus()).append(" (party_id);").ln()
    

    .build()).build();
  }

  @Override
  public Sql createOrgCommits() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getOrgCommits()).ln()
    .append("(").ln()
    .append("  commit_id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  parent_id VARCHAR(40),").ln()
    .append("  created_at TIMESTAMP WITH TIME ZONE NOT NULL,").ln()
    .append("  commit_log TEXT NOT NULL,").ln()
    
    .append("  commit_author VARCHAR(255) NOT NULL,").ln()
    .append("  commit_message VARCHAR(255) NOT NULL").ln()
    .append(");").ln().ln()
    
    
    .append("CREATE TABLE ").append(options.getOrgCommitTrees()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  parent_commit_id VARCHAR(40),").ln()
    .append("  actor_id VARCHAR(40) NOT NULL,").ln()
    .append("  actor_type VARCHAR(40) NOT NULL,").ln()
    .append("  value JSONB NOT NULL").ln()
    .append(");").ln().ln()
    

    // parent id, references self
    .append("ALTER TABLE ").append(options.getOrgCommits()).ln()
    .append("  ADD CONSTRAINT ").append(options.getOrgCommits()).append("_PARENT_FK").ln()
    .append("  FOREIGN KEY (parent_id)").ln()
    .append("  REFERENCES ").append(options.getOrgCommits()).append(" (commit_id);").ln()

    .append("CREATE INDEX ").append(options.getOrgCommits()).append("_PARENT_INDEX")
    .append(" ON ").append(options.getOrgCommits()).append(" (parent_id);").ln()


    .append("ALTER TABLE ").append(options.getOrgCommitTrees()).ln()
    .append("  ADD CONSTRAINT ").append(options.getOrgCommitTrees()).append("_COMMIT_FK").ln()
    .append("  FOREIGN KEY (commit_id)").ln()
    .append("  REFERENCES ").append(options.getOrgCommits()).append(" (commit_id);").ln()

    .append("ALTER TABLE ").append(options.getOrgCommitTrees()).ln()
    .append("  ADD CONSTRAINT ").append(options.getOrgCommitTrees()).append("_PARENT_FK").ln()
    .append("  FOREIGN KEY (parent_commit_id)").ln()
    .append("  REFERENCES ").append(options.getOrgCommits()).append(" (commit_id);").ln()

    
    .append("CREATE INDEX ").append(options.getOrgCommitTrees()).append("_ACTOR_INDEX")
    .append(" ON ").append(options.getOrgCommitTrees()).append(" (actor_type, actor_id);").ln()
    
    .append("CREATE INDEX ").append(options.getOrgCommitTrees()).append("_COMMIT_INDEX")
    .append(" ON ").append(options.getOrgCommitTrees()).append(" (commit_id);").ln()
    
    .append("CREATE INDEX ").append(options.getOrgCommitTrees()).append("_PARENT_INDEX")
    .append(" ON ").append(options.getOrgCommitTrees()).append(" (parent_commit_id);").ln()
    

    .build()).build();
  }

  @Override
  public Sql createOrgActorData() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getOrgActorData()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  parent_id VARCHAR(40),").ln()
    .append("  external_id VARCHAR(40) UNIQUE,").ln()
    .append("  user_id VARCHAR(40),").ln()
    .append("  right_id VARCHAR(40),").ln()
    .append("  party_id VARCHAR(40),").ln()

    .append("  data_type VARCHAR(255) NOT NULL,").ln()
    .append("  value JSONB NOT NULL,").ln()

    .append("  commit_author VARCHAR(255) NOT NULL,").ln()
    .append("  commit_message VARCHAR(255) NOT NULL").ln()
    .append(");").ln().ln()


    // parent id, references self
    .append("ALTER TABLE ").append(options.getOrgActorData()).ln()
    .append("  ADD CONSTRAINT ").append(options.getOrgActorData()).append("_PARENT_FK").ln()
    .append("  FOREIGN KEY (parent_id)").ln()
    .append("  REFERENCES ").append(options.getOrgActorData()).append(" (id);").ln()


    .build()).build();
  }

  @Override
  public Sql createOrgCommitConstraints() {
    return ImmutableSql.builder()
        .value(createOrgCommitFk(options.getOrgMemberRights()))
        .value(createOrgCommitFk(options.getOrgPartyRights()))
        .value(createOrgCommitFk(options.getOrgPartyRights()))
        
        .value(createOrgCommitFk(options.getOrgMembers()))
        .value(createOrgCommitFk(options.getOrgMemberRights()))
        
        .value(createOrgCommitFk(options.getOrgRights()))
        .value(createOrgCommitFk(options.getOrgMemberships()))
        .build();
  }

  @Override
  public Sql createOrgRolesConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append(createOrgRoleFk(options.getOrgActorData())).ln()
        .append(createOrgRoleFk(options.getOrgActorStatus())).ln()
        
        .append(createOrgRoleFk(options.getOrgMemberRights())).ln()
        .append(createOrgRoleFk(options.getOrgPartyRights())).ln()
        .build())
        .build();
  }
  
  @Override
  public Sql createOrgGroupConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append(createOrgGroupFk(options.getOrgActorData())).ln()
        .append(createOrgGroupFk(options.getOrgActorStatus())).ln()
        .append(createOrgGroupFk(options.getOrgPartyRights())).ln()
        .append(createOrgGroupFk(options.getOrgMemberRights())).ln()
        .append(createOrgGroupFk(options.getOrgMemberships())).ln()
        

        .append("ALTER TABLE ").append(options.getOrgMemberRights()).ln()
        .append("  ADD CONSTRAINT ").append(options.getOrgMemberRights()).append("_GROUP_MEMBER_FK").ln()
        .append("  FOREIGN KEY (party_id, user_id)").ln()
        .append("  REFERENCES ").append(options.getOrgMemberships()).append(" (party_id, user_id);").ln().ln()
        
        
        .build())
        .build();
  }

  
  @Override
  public Sql createOrgUserConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append(createOrgUserFk(options.getOrgMemberships())).ln()
        .append(createOrgUserFk(options.getOrgMemberRights())).ln()
        
        .append(createOrgUserFk(options.getOrgActorData())).ln()
        .append(createOrgUserFk(options.getOrgActorStatus())).ln()
        .build())
        .build();
  }
  
  
  @Override public Sql dropRepo() { return dropTableIfNotExists(options.getRepos()); }
  @Override public Sql dropGitBlobs() { return dropTableIfNotExists(options.getBlobs()); }
  @Override public Sql dropGitCommits() { return dropTableIfNotExists(options.getCommits()); }
  @Override public Sql dropGitTreeItems() { return dropTableIfNotExists(options.getTreeItems()); }
  @Override public Sql dropGitTrees() { return dropTableIfNotExists(options.getTrees()); }
  @Override public Sql dropGitRefs() { return dropTableIfNotExists(options.getRefs()); }
  @Override public Sql dropGitTags() {return dropTableIfNotExists(options.getTags()); }
  @Override public Sql dropDoc() { return dropTableIfNotExists(options.getDoc()); }
  @Override public Sql dropDocBranch() { return dropTableIfNotExists(options.getDocBranch()); }
  @Override public Sql dropDocCommit() { return dropTableIfNotExists(options.getDocCommits()); }
  @Override public Sql dropDocLog() { return dropTableIfNotExists(options.getDocLog()); }
  

  @Override public Sql dropOrgRoles() { return dropTableIfNotExists(options.getOrgRights()); }
  @Override public Sql dropOrgGroups() { return dropTableIfNotExists(options.getOrgParties()); }
  @Override public Sql dropOrgGroupRoles() { return dropTableIfNotExists(options.getOrgPartyRights()); }
  @Override public Sql dropOrgUsers() { return dropTableIfNotExists(options.getOrgMembers()); }
  @Override public Sql dropOrgUserRoles() { return dropTableIfNotExists(options.getOrgMemberRights()); }
  @Override public Sql dropOrgUserMemberships() { return dropTableIfNotExists(options.getOrgMemberships()); }
  @Override public Sql dropOrgActorStatus() { return dropTableIfNotExists(options.getOrgActorStatus()); }
  @Override public Sql dropOrgActorLogs() { return dropTableIfNotExists(options.getOrgCommits()); }
  @Override public Sql dropOrgActorData() { return dropTableIfNotExists(options.getOrgActorData()); }

  
  
  private Sql dropTableIfNotExists(String tableName) {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(tableName).append(";").ln()
        .build()).build();
  }
  
  private String createOrgCommitFk(String tableNameThatPointToCommits) {
    return  new SqlStatement().ln()
        .append("ALTER TABLE ").append(tableNameThatPointToCommits).ln()
        .append("  ADD CONSTRAINT ").append(tableNameThatPointToCommits).append("_COMMIT_FK").ln()
        .append("  FOREIGN KEY (commit_id)").ln()
        .append("  REFERENCES ").append(options.getOrgCommits()).append(" (commit_id);").ln().ln()
        .build();
  }

  private String createOrgUserFk(String tableNameThatPointToCommits) {
    return  new SqlStatement().ln()
        .append("ALTER TABLE ").append(tableNameThatPointToCommits).ln()
        .append("  ADD CONSTRAINT ").append(tableNameThatPointToCommits).append("_USER_FK").ln()
        .append("  FOREIGN KEY (user_id)").ln()
        .append("  REFERENCES ").append(options.getOrgMembers()).append(" (id);").ln().ln()
        .build();
  }

  private String createOrgGroupFk(String tableNameThatPointToCommits) {
    return new SqlStatement().ln()
        .append("ALTER TABLE ").append(tableNameThatPointToCommits).ln()
        .append("  ADD CONSTRAINT ").append(tableNameThatPointToCommits).append("_GROUP_FK").ln()
        .append("  FOREIGN KEY (party_id)").ln()
        .append("  REFERENCES ").append(options.getOrgParties()).append(" (id);").ln().ln()
        .build();
  }
  

  private String createOrgRoleFk(String tableNameThatPointToCommits) {
    return  new SqlStatement().ln()
        .append("ALTER TABLE ").append(tableNameThatPointToCommits).ln()
        .append("  ADD CONSTRAINT ").append(tableNameThatPointToCommits).append("_ROLE_FK").ln()
        .append("  FOREIGN KEY (right_id)").ln()
        .append("  REFERENCES ").append(options.getOrgRights()).append(" (id);").ln().ln()
        .build();
  }
}
