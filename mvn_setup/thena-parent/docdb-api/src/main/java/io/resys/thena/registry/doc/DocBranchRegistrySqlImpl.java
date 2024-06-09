package io.resys.thena.registry.doc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.CommitLockStatus;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocLock.DocBranchLock;
import io.resys.thena.api.entities.doc.ImmutableDoc;
import io.resys.thena.api.entities.doc.ImmutableDocBranch;
import io.resys.thena.api.entities.doc.ImmutableDocBranchLock;
import io.resys.thena.api.entities.doc.ImmutableDocCommit;
import io.resys.thena.api.entities.doc.Doc.DocFilter;
import io.resys.thena.api.registry.doc.DocBranchRegistry;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ImmutableSqlTupleList;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.storesql.support.SqlStatement;
import io.resys.thena.structures.doc.DocQueries.DocBranchLockCriteria;
import io.resys.thena.structures.doc.DocQueries.DocLockCriteria;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DocBranchRegistrySqlImpl implements DocBranchRegistry {
  private final TenantTableNames options;

  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT branch.*, ").ln()
        .append(" branch_updated_commit.created_at as updated_at,").ln()
        .append(" branch_created_commit.created_at as created_at").ln()
        .append(" FROM ").append(options.getDocBranch()).append(" as branch").ln()
        
        .append(" LEFT JOIN ").append(options.getDocCommits()).append(" as branch_updated_commit").ln()
        .append(" ON(branch_updated_commit.id = branch.commit_id)").ln()
        .append(" LEFT JOIN ").append(options.getDocCommits()).append(" as branch_created_commit").ln()
        .append(" ON(branch_created_commit.id = branch.created_with_commit_id)").ln()

        .build())
        .build();
  }

  @Override
  public ThenaSqlClient.SqlTuple getById(String branchId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT branch.*, ").ln()
        .append(" branch_updated_commit.created_at as updated_at,").ln()
        .append(" branch_created_commit.created_at as created_at").ln()
        .append(" FROM ").append(options.getDocBranch()).append(" as branch")
        
        .append(" LEFT JOIN ").append(options.getDocCommits()).append(" as branch_updated_commit").ln()
        .append(" ON(branch_updated_commit.id = branch.commit_id)").ln()
        .append(" LEFT JOIN ").append(options.getDocCommits()).append(" as branch_created_commit").ln()
        .append(" ON(branch_created_commit.id = branch.created_with_commit_id)").ln()

        
        .append(" WHERE branch.branch_id = $1 OR $1 IS NULL")
        .append(" FETCH FIRST ROW ONLY")
        .build())
        .props(Tuple.of(branchId))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTupleList insertAll(Collection<DocBranch> docs) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getDocBranch())
        .append(" (branch_id, branch_name, branch_status, commit_id, doc_id, value, created_with_commit_id) VALUES($1, $2, $3, $4, $5, $6, $7)")
        .build())
        .props(docs.stream().map(ref -> Tuple.tuple(Arrays.asList(
              ref.getId(), ref.getBranchName(), ref.getStatus().name(), ref.getCommitId(), ref.getDocId(), ref.getValue(), ref.getCreatedWithCommitId()
          ))
        ) .collect(Collectors.toList()))
        .build();
  }  

  @Override
  public ThenaSqlClient.SqlTupleList updateAll(List<DocBranch> docs) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getDocBranch())
        .append(" SET commit_id = $1, branch_name = $2, value = $3, branch_status = $4")
        .append(" WHERE branch_id = $5")
        .build())        
        .props(docs.stream().map(ref -> Tuple.of(ref.getCommitId(), ref.getBranchName(), ref.getValue(), ref.getStatus(), ref.getId())).collect(Collectors.toList()))
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
    
    if(filter.getParentId() != null) {
      final var index = params.size() + 1;
      filters.add(" ( docs.doc_parent_id = $" + index + " ) ");
      params.add(filter.getParentId());
    }
    
    if(filter.getOwnerId() != null) {
      final var index = params.size() + 1;
      filters.add(" ( docs.owner_id = $" + index + " ) ");
      params.add(filter.getOwnerId());
    }    
    
    if(filter.getDocTypes() != null && !filter.getDocTypes().isEmpty()) {
      final var index = params.size() + 1;
      filters.add(" ( docs.doc_type = ANY($" + index + " ) ) ");
      params.add(filter.getDocTypes().toArray());
    }
    

    if(filter.getBranch() != null) {
      final var index = params.size() + 1;
      filters.add(" ( branch.branch_name = $" + index + " OR branch.branch_id = $" + index + ") ");
      params.add(filter.getBranch());
    }
    
    final var where = (params.isEmpty() ? "" : " WHERE ") + String.join(" AND ", filters);
    
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()

        .append("SELECT branch.*, ").ln()
        .append(" branch_updated_commit.created_at as updated_at,").ln()
        .append(" branch_created_commit.created_at as created_at").ln()
            
        .append(" FROM ").append(options.getDocBranch()).append(" as branch")
        
        .append(" INNER JOIN ").append(options.getDoc()).append(" as docs").ln()
        .append(" ON(branch.doc_id = docs.id)")

        .append(" LEFT JOIN ").append(options.getDocCommits()).append(" as branch_updated_commit").ln()
        .append(" ON(branch_updated_commit.id = branch.commit_id)").ln()
        .append(" LEFT JOIN ").append(options.getDocCommits()).append(" as branch_created_commit").ln()
        .append(" ON(branch_created_commit.id = branch.created_with_commit_id)").ln()

        .append(where).ln()
        .build())
        .props(Tuple.from(params))
        .build();
  }

  @Override
  public ThenaSqlClient.SqlTuple getBranchLock(DocBranchLockCriteria crit) {
    final var branchName = crit.getBranchName();
    final var docId = crit.getDocId();

    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ")
        .append("  doc.external_id as external_id,").ln()
        .append("  doc.doc_type as doc_type,").ln()
        .append("  doc.doc_status as doc_status,").ln()
        .append("  doc.doc_meta as doc_meta,").ln()
        .append("  doc.doc_parent_id as doc_parent_id,").ln()
        .append("  doc.commit_id as doc_commit_id,")
        .append("  doc.created_with_commit_id as doc_created_commit_id,")
        .append("  doc_updated_commit.created_at as doc_updated_at,").ln()
        .append("  doc_created_commit.created_at as doc_created_at,").ln()

        .append("  branch.created_with_commit_id as branch_created_with_commit_id,")
        .append("  branch.doc_id as doc_id,").ln()
        .append("  branch.branch_id as branch_id,").ln()
        .append("  branch.branch_name as branch_name,").ln()
        .append("  branch.commit_id as branch_commit_id,").ln()
        .append("  branch.branch_status as branch_status,").ln()
        .append("  branch.value as branch_value,").ln()
        .append("  commits.created_at as branch_updated_at,").ln()
        .append("  branch_created_commit.created_at as branch_created_at,").ln()
        
        .append("  commits.author as author,").ln()
        .append("  commits.created_at as created_at,").ln()
        .append("  commits.message as message,").ln()
        .append("  commits.parent as commit_parent,").ln()
        .append("  commits.id as commit_id").ln()
        
        .append(" FROM (SELECT * FROM ").append(options.getDocBranch()).append(" WHERE (branch_name = $1 OR branch_id = $1) AND doc_id = $2 FOR UPDATE NOWAIT) as branch").ln()
        .append(" JOIN ").append(options.getDocCommits()).append(" as commits ON(commits.branch_id = branch.branch_id and commits.id = branch.commit_id)").ln()
        .append(" JOIN ").append(options.getDoc()).append(" as doc ON(doc.id = branch.doc_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getDocCommits()).append(" as doc_updated_commit").ln()
        .append(" ON(doc_updated_commit.id = doc.commit_id)").ln()
        .append(" LEFT JOIN ").append(options.getDocCommits()).append(" as doc_created_commit").ln()
        .append(" ON(doc_created_commit.id = doc.created_with_commit_id)").ln()
        
        
        .append(" LEFT JOIN ").append(options.getDocCommits()).append(" as branch_created_commit").ln()
        .append(" ON(branch_created_commit.id = branch.created_with_commit_id)").ln()

        
        .build())
        .props(Tuple.of(branchName, docId))
        .build();
  }
  
  @Override
  public ThenaSqlClient.SqlTuple getDocLock(DocLockCriteria crit) {
    final var docId = crit.getDocId();

    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ")
        .append("  doc.external_id as external_id,").ln()
        .append("  doc.doc_type as doc_type,").ln()
        .append("  doc.doc_status as doc_status,").ln()
        .append("  doc.doc_meta as doc_meta,").ln()
        .append("  doc.doc_parent_id as doc_parent_id,").ln()
        .append("  doc.commit_id as doc_commit_id,")
        .append("  doc.created_with_commit_id as doc_created_commit_id,")
        .append("  doc_updated_commit.created_at as doc_updated_at,").ln()
        .append("  doc_created_commit.created_at as doc_created_at,").ln()

        .append("  branch.created_with_commit_id as branch_created_with_commit_id,")
        .append("  branch.doc_id as doc_id,").ln()
        .append("  branch.branch_id as branch_id,").ln()
        .append("  branch.branch_name as branch_name,").ln()
        .append("  branch.commit_id as branch_commit_id,").ln()
        .append("  branch.branch_status as branch_status,").ln()
        .append("  branch.value as branch_value,").ln()
        .append("  commits.created_at as branch_updated_at,").ln()
        .append("  branch_created_commit.created_at as branch_created_at,").ln()
        
        .append("  commits.author as author,").ln()
        .append("  commits.created_at as created_at,").ln()
        .append("  commits.message as message,").ln()
        .append("  commits.parent as commit_parent,").ln()
        .append("  commits.id as commit_id").ln()
        
        .append(" FROM (SELECT * FROM ").append(options.getDocBranch()).append(" WHERE doc_id = $1 FOR UPDATE NOWAIT) as branch").ln()
        .append(" JOIN ").append(options.getDocCommits()).append(" as commits ON(commits.branch_id = branch.branch_id AND commits.id = branch.commit_id)").ln()
        .append(" JOIN ").append(options.getDoc()).append(" as doc ON(doc.id = branch.doc_id)").ln()
        
        
        .append(" LEFT JOIN ").append(options.getDocCommits()).append(" as doc_updated_commit").ln()
        .append(" ON(doc_updated_commit.id = doc.commit_id)").ln()
        .append(" LEFT JOIN ").append(options.getDocCommits()).append(" as doc_created_commit").ln()
        .append(" ON(doc_created_commit.id = doc.created_with_commit_id)").ln()
        
        
        .append(" LEFT JOIN ").append(options.getDocCommits()).append(" as branch_created_commit").ln()
        .append(" ON(branch_created_commit.id = branch.created_with_commit_id)").ln()

        
        .build())
        .props(Tuple.of(docId))
        .build();  
  }

  @Override
  public ThenaSqlClient.SqlTuple getBranchLocks(List<DocBranchLockCriteria> criteria) {
    final var props = new ArrayList<Object>();
    var index = 1;
    final var where = new StringBuilder();
    for(final var crit : criteria) {
      props.add(crit.getBranchName());
      props.add(crit.getDocId());
      if(index > 1) {
        where.append(" OR ");
      }
      where
        .append(" (")
        .append(" branch_name = $").append(index++)
        .append(" AND doc_id = $").append(index++)
        .append(") "); 
    }
    
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ")
        .append("  doc.external_id as external_id,").ln()
        .append("  doc.doc_type as doc_type,").ln()
        .append("  doc.doc_status as doc_status,").ln()
        .append("  doc.doc_meta as doc_meta,").ln()
        .append("  doc.doc_parent_id as doc_parent_id,").ln()
        .append("  doc.commit_id as doc_commit_id,")
        .append("  doc.created_with_commit_id as doc_created_commit_id,")
        .append("  doc_updated_commit.created_at as doc_updated_at,").ln()
        .append("  doc_created_commit.created_at as doc_created_at,").ln()

        .append("  branch.created_with_commit_id as branch_created_with_commit_id,")
        .append("  branch.doc_id as doc_id,").ln()
        .append("  branch.branch_id as branch_id,").ln()
        .append("  branch.branch_name as branch_name,").ln()
        .append("  branch.commit_id as branch_commit_id,").ln()
        .append("  branch.branch_status as branch_status,").ln()
        .append("  branch.value as branch_value,").ln()
        .append("  commits.created_at as branch_updated_at,").ln()
        .append("  branch_created_commit.created_at as branch_created_at,").ln()
        
        .append("  commits.author as author,").ln()
        .append("  commits.created_at as created_at,").ln()
        .append("  commits.message as message,").ln()
        .append("  commits.parent as commit_parent,").ln()
        .append("  commits.id as commit_id").ln()
        
        .append(" FROM (SELECT * FROM ").append(options.getDocBranch()).append(where.length() > 0 ? " WHERE " : "").append(where.toString()).append(" FOR UPDATE NOWAIT) as branch").ln()
        .append(" JOIN ").append(options.getDocCommits()).append(" as commits ON(commits.branch_id = branch.branch_id and commits.id = branch.commit_id)").ln()
        .append(" JOIN ").append(options.getDoc()).append(" as doc ON(doc.id = branch.doc_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getDocCommits()).append(" as doc_updated_commit").ln()
        .append(" ON(doc_updated_commit.id = doc.commit_id)").ln()
        .append(" LEFT JOIN ").append(options.getDocCommits()).append(" as doc_created_commit").ln()
        .append(" ON(doc_created_commit.id = doc.created_with_commit_id)").ln()
        

        .append(" LEFT JOIN ").append(options.getDocCommits()).append(" as branch_created_commit").ln()
        .append(" ON(branch_created_commit.id = branch.created_with_commit_id)").ln()

        
        .build())
        .props(Tuple.from(props))
        .build();  
  }
  
  
  @Override
  public ThenaSqlClient.SqlTuple getDocLocks(List<DocLockCriteria> criteria) {
    final var props = new ArrayList<Object>();
    var index = 1;
    final var where = new StringBuilder();
    for(final var crit : criteria) {
      props.add(crit.getDocId());
      if(index > 1) {
        where.append(" OR ");
      }
      where
        .append(" (")
        .append(" doc_id = $").append(index++)
        .append(") "); 
    }
    
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ")
        .append("  doc.external_id as external_id,").ln()
        .append("  doc.doc_type as doc_type,").ln()
        .append("  doc.doc_status as doc_status,").ln()
        .append("  doc.doc_meta as doc_meta,").ln()
        .append("  doc.doc_parent_id as doc_parent_id,").ln()
        .append("  doc.commit_id as doc_commit_id,").ln()
        .append("  doc.created_with_commit_id as doc_created_commit_id,")
        .append("  doc_updated_commit.created_at as doc_updated_at,").ln()
        .append("  doc_created_commit.created_at as doc_created_at,").ln()
        
        
        .append("  branch.created_with_commit_id as branch_created_with_commit_id,")
        .append("  branch.doc_id as doc_id,").ln()
        .append("  branch.branch_id as branch_id,").ln()
        .append("  branch.branch_name as branch_name,").ln()
        .append("  branch.commit_id as branch_commit_id,").ln()
        .append("  branch.branch_status as branch_status,").ln()
        .append("  branch.value as branch_value,").ln()
        .append("  commits.created_at as branch_updated_at,").ln()
        .append("  branch_created_commit.created_at as branch_created_at,").ln()
        
        
        .append("  commits.author as author,").ln()
        .append("  commits.created_at as created_at,").ln()
        .append("  commits.message as message,").ln()
        .append("  commits.parent as commit_parent,").ln()
        .append("  commits.id as commit_id").ln()
        
        .append(" FROM (SELECT * FROM ").append(options.getDocBranch()).append(" WHERE ").append(where.toString()).append(" FOR UPDATE NOWAIT) as branch").ln()
        .append(" JOIN ").append(options.getDocCommits()).append(" as commits ON(commits.branch_id = branch.branch_id and commits.id = branch.commit_id)").ln()
        .append(" JOIN ").append(options.getDoc()).append(" as doc ON(doc.id = branch.doc_id)").ln()
        
        
        .append(" LEFT JOIN ").append(options.getDocCommits()).append(" as doc_updated_commit").ln()
        .append(" ON(doc_updated_commit.id = doc.commit_id)").ln()
        .append(" LEFT JOIN ").append(options.getDocCommits()).append(" as doc_created_commit").ln()
        .append(" ON(doc_created_commit.id = doc.created_with_commit_id)").ln()
        
        
        .append(" LEFT JOIN ").append(options.getDocCommits()).append(" as branch_created_commit").ln()
        .append(" ON(branch_created_commit.id = branch.created_with_commit_id)").ln()
        
        .build())
        .props(Tuple.from(props))
        .build();  
  }
  @Override
  public ThenaSqlClient.Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
        .append("CREATE TABLE ").append(options.getDocBranch()).ln()
        .append("(").ln()
        .append("  doc_id                   VARCHAR(100) NOT NULL,").ln()
        .append("  branch_id                VARCHAR(40) NOT NULL,").ln()
        .append("  commit_id                VARCHAR(40) NOT NULL,").ln()
        .append("  created_with_commit_id   VARCHAR(40) NOT NULL,").ln()
        .append("  branch_name              VARCHAR(255) NOT NULL,").ln()
        .append("  branch_status            VARCHAR(40) NOT NULL,").ln()
        .append("  value                    JSONB NOT NULL,").ln()
        
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
  public ThenaSqlClient.Sql createConstraints() {
    return ImmutableSql.builder()
        .value(new SqlStatement().ln()
        .append("ALTER TABLE ").append(options.getDocBranch()).ln()
        .append("  ADD CONSTRAINT ").append(options.getDocBranch()).append("_DOC_ID_FK").ln()
        .append("  FOREIGN KEY (doc_id)").ln()
        .append("  REFERENCES ").append(options.getDoc()).append(" (id) ON DELETE CASCADE;").ln().ln()
        .build())
        .build();
  }

  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE IF EXISTS ").append(options.getDocBranch()).append(";").ln()
        .build()).build();
  }
  

  @Override
  public Function<Row, DocBranch> defaultMapper() {
    return DocBranchRegistrySqlImpl::docBranch;
  }

  @Override
  public Function<Row, DocBranchLock> docBranchLockMapper() {
    return DocBranchRegistrySqlImpl::docBranchLock;
  }

  private static DocBranch docBranch(Row row) {
    return ImmutableDocBranch.builder()
        .id(row.getString("branch_id"))
        .docId(row.getString("doc_id"))
        .commitId(row.getString("commit_id"))
        .branchName(row.getString("branch_name"))
        .createdAt(row.getOffsetDateTime("created_at"))
        .updatedAt(row.getOffsetDateTime("updated_at"))
        .createdWithCommitId(row.getString("created_with_commit_id"))
        .value(jsonObject(row, "value"))
        .status(Doc.DocStatus.valueOf(row.getString("branch_status")))
        .build();
  }

  private static DocBranchLock docBranchLock(Row row) {
    return ImmutableDocBranchLock.builder()
        .status(CommitLockStatus.LOCK_TAKEN)
        .doc(ImmutableDoc.builder()
            .id(row.getString("doc_id"))
            .externalId(row.getString("external_id"))
            .createdWithCommitId(row.getString("doc_created_commit_id"))
            .parentId(row.getString("doc_parent_id"))
            .type(row.getString("doc_type"))
            .status(Doc.DocStatus.valueOf(row.getString("doc_status")))
            .meta(jsonObject(row, "doc_meta"))
            .commitId(row.getString("doc_commit_id"))
            .createdAt(row.getOffsetDateTime("doc_created_at"))
            .updatedAt(row.getOffsetDateTime("doc_updated_at"))
            .build())
        .branch(ImmutableDocBranch.builder()
            .id(row.getString("branch_id"))
            .docId(row.getString("doc_id"))
            .createdAt(row.getOffsetDateTime("branch_created_at"))
            .updatedAt(row.getOffsetDateTime("branch_updated_at"))
            .status(Doc.DocStatus.valueOf(row.getString("branch_status")))
            .commitId(row.getString("branch_commit_id"))
            .branchName(row.getString("branch_name"))
            .createdWithCommitId(row.getString("branch_created_with_commit_id"))
            .value(jsonObject(row, "branch_value"))
            .status(Doc.DocStatus.valueOf(row.getString("branch_status")))
            .build())
        .commit(ImmutableDocCommit.builder()
            .id(row.getString("commit_id"))
            .commitAuthor(row.getString("author"))
            .createdAt(row.getOffsetDateTime("created_at"))
            .commitMessage(row.getString("message"))
            .parent(Optional.ofNullable(row.getString("commit_parent")))
            .branchId(row.getString("branch_id"))
            .docId(row.getString("doc_id"))
            .commitLog("")
            .build())
        .build();
  }
  private static JsonObject jsonObject(Row row, String columnName) {
    // string based - new JsonObject(row.getString(columnName));
    return row.getJsonObject(columnName);
  }

}
