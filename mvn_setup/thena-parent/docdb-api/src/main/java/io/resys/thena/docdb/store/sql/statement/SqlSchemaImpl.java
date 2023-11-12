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
  public Sql createBlobs() {
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
  public Sql createCommits() {
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
  public Sql createCommitsConstraints() {
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
  public Sql createTreeItems() {
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
  public Sql createTreeItemsConstraints() {
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
  public Sql createTrees() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getTrees()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY").ln()
    .append(");").ln()
    .build()).build();
  }
  
  @Override
  public Sql createRefs() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getRefs()).ln()
    .append("(").ln()
    .append("  name VARCHAR(100) PRIMARY KEY,").ln()
    .append("  commit VARCHAR(40) NOT NULL").ln()
    .append(");").ln()
    .build()).build();
  }
  @Override
  public Sql createRefsConstraints() {
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
  public Sql createTags() {
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
  public Sql createTagsConstraints() {
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


  @Override public Sql dropRepo() { return dropTableIfNotExists(options.getRepos()); }
  @Override public Sql dropBlobs() { return dropTableIfNotExists(options.getBlobs()); }
  @Override public Sql dropCommits() { return dropTableIfNotExists(options.getCommits()); }
  @Override public Sql dropTreeItems() { return dropTableIfNotExists(options.getTreeItems()); }
  @Override public Sql dropTrees() { return dropTableIfNotExists(options.getTrees()); }
  @Override public Sql dropRefs() { return dropTableIfNotExists(options.getRefs()); }
  @Override public Sql dropTags() {return dropTableIfNotExists(options.getTags()); }
  @Override public Sql dropDoc() { return dropTableIfNotExists(options.getDoc()); }
  @Override public Sql dropDocBranch() { return dropTableIfNotExists(options.getDocBranch()); }
  @Override public Sql dropDocCommit() { return dropTableIfNotExists(options.getDocCommits()); }
  @Override public Sql dropDocLog() { return dropTableIfNotExists(options.getDocLog()); }

  private Sql dropTableIfNotExists(String tableName) {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(tableName).append(";").ln()
        .build()).build();
  }

}
