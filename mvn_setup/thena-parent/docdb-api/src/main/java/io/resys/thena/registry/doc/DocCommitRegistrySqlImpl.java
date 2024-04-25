package io.resys.thena.registry.doc;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.ImmutableDocCommit;
import io.resys.thena.api.entities.doc.Doc.DocFilter;
import io.resys.thena.api.registry.doc.DocCommitRegistry;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ImmutableSqlTupleList;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DocCommitRegistrySqlImpl implements DocCommitRegistry {
  private final TenantTableNames options;
 
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getDocCommits())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getDocCommits())
        .append(" WHERE id = $1")
        .append(" FETCH FIRST ROW ONLY")
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTuple findAll(DocFilter filter) {
    

    final var params = new ArrayList<Object>();
    final var filters = new ArrayList<String>();
    
    if(filter.getDocIds() != null) {
      final var index = params.size() + 1;
      filters.add(" ( docs.id = ANY($" + index +") OR docs.external_id = ANY($" + index + ") ) ");
      params.add(filter.getDocIds().toArray());
    }

    if(filter.getDocType() != null) {
      final var index = params.size() + 1;
      filters.add(" ( docs.doc_type = $" + index + " ) ");
      params.add(filter.getDocType());
    }
    

    if(filter.getBranch() != null) {
      final var index = params.size() + 1;
      filters.add(
          new StringBuilder()
          .append("(SELECT count(branch_id) ")
          .append(" FROM ").append(options.getDocBranch()).append(" as branches ")
          .append(" WHERE branches.doc_id = docs.id ")
          .append(" AND branches.branch_name = $" + index + " OR branches.branch_id = $" + index)
          .append(") > 0")
          .toString());
      params.add(filter.getBranch());
    }
    
    final var where = (params.isEmpty() ? "" : " WHERE ") + String.join(" AND ", filters);
    
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT commits.* FROM ").append(options.getDocCommits()).append(" as commits ").ln()
        
        .append(" LEFT JOIN ").append(options.getDoc()).append(" as docs").ln()
        .append(" ON(docs.id = commits.doc_id) ").ln()
        
        .append(where).ln()
        .build())
        .props(Tuple.from(params))
        .build();
  }
  
  @Override
  public ThenaSqlClient.SqlTupleList insertAll(Collection<DocCommit> commits) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getDocCommits())
        .append(" (id, created_at, author, message, branch_id, doc_id, parent, commit_log) VALUES($1, $2, $3, $4, $5, $6, $7, $8)")
        .build())
        .props(commits.stream().map(commit -> {
          
          return Tuple.from(Arrays.asList(
              commit.getId(), 
              commit.getCreatedAt(), 
              commit.getCommitAuthor(), 
              commit.getCommitMessage(), 
              commit.getBranchId().orElse(null), 
              commit.getDocId(), 
              commit.getParent().orElse(null), 
              commit.getCommitLog()));
          
        }) .collect(Collectors.toList()))
        .build();
  }
  

  @Override
  public Function<Row, DocCommit> defaultMapper() {
    return DocCommitRegistrySqlImpl::docCommit;
  }
  private static DocCommit docCommit(Row row) {
    return ImmutableDocCommit.builder()
        .id(row.getString("id"))
        .commitAuthor(row.getString("author"))
        .createdAt(row.getOffsetDateTime("created_at"))
        .commitMessage(row.getString("message"))
        .parent(Optional.ofNullable(row.getString("parent")))
        .branchId(Optional.ofNullable(row.getString("branch_id")))
        .commitLog(row.getString("commit_log"))
        .docId(row.getString("doc_id"))
        .build();
  }
  @Override
  public ThenaSqlClient.Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getDocCommits()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  branch_id VARCHAR(40),").ln()
    .append("  doc_id VARCHAR(40) NOT NULL,").ln()
    .append("  created_at TIMESTAMP WITH TIME ZONE NOT NULL,").ln()
    .append("  author VARCHAR(255) NOT NULL,").ln()
    .append("  message TEXT NOT NULL,").ln()
    .append("  commit_log TEXT NOT NULL,").ln()
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
  public ThenaSqlClient.Sql createConstraints() {
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
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE IF EXISTS ").append(options.getDocCommits()).append(";").ln()
        .build()).build();
  }

}
