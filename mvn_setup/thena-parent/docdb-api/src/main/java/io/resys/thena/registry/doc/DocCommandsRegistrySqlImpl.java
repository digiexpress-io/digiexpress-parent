package io.resys.thena.registry.doc;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.doc.DocCommands;
import io.resys.thena.api.entities.doc.ImmutableDocCommands;
import io.resys.thena.api.registry.doc.DocCommandsRegistry;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ImmutableSqlTupleList;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DocCommandsRegistrySqlImpl implements DocCommandsRegistry {
  private final TenantTableNames options;
  
  
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getDocCommands()).append(";").ln()
        .build()).build();
  }
  @Override
  public SqlTupleList insertAll(Collection<DocCommands> commits) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getDocCommands())
        .append(" (id, commit_id, doc_id, branch_id, commands) VALUES($1, $2, $3, $4, $5)")
        .build())
        .props(commits.stream().map(command -> Tuple.of(
            command.getId(),
            command.getCommitId(),
            command.getDocId(),
            command.getBranchId().orElse(null), 
            command.getCommands().toArray()
        ))
        .collect(Collectors.toList()))
        .build();
  }
  @Override
  public SqlTuple findAllByDocIdsAndBranch(Collection<String> docIds, String branchId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT commands.*, commits.created_at as created_at, commits.author as created_by ")
        .append(" FROM ").append(options.getDocCommands()).append(" as commands")
        
        .append(" INNER JOIN ").append(options.getDocCommits()).append(" as commits").ln()
        .append(" ON(commands.commit_id = commits.commit_id)").ln()

        .append(" INNER JOIN ").append(options.getDoc()).append(" as docs").ln()
        .append(" ON(docs.id = commands.doc_id)")
        
        .append(" INNER JOIN ").append(options.getDocBranch()).append(" as branches").ln()
        .append(" ON(branches.doc_id = docs.id)")

        .append(" WHERE ").ln() 
        .append(" (docs.id = ANY($1) or docs.external_id = ANY($1)) ").ln()
        .append(" AND ").ln()
        .append(" (branch.branch_name = $2 OR branch.id = $2)").ln()
        
        .build())
        .props(Tuple.of(docIds, branchId))
        .build();
  }
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT commands.*, commits.created_at as created_at, commits.author as created_by ")
        .append(" FROM ").append(options.getDocCommands()).append(" as commands")
        .append(" LEFT JOIN ").append(options.getDocCommits()).append(" as commits").ln()
        .append(" ON(commands.commit_id = commits.id)").ln()
        .build())
        .build();
  }

  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT commands.*, commits.created_at as created_at, commits.author as created_by ")
        .append(" FROM ").append(options.getDocCommands()).append(" as commands")
        .append(" LEFT JOIN ").append(options.getDocCommits()).append(" as commits").ln()
        .append(" ON(commands.commit_id = commits.id)").ln()
        .append(" WHERE (commands.id = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getDocCommands()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  doc_id VARCHAR(40) NOT NULL,").ln()
    .append("  branch_id VARCHAR(40),").ln()
    .append("  commands JSONB[] NOT NULL").ln()
    .append(");").ln()
    
    .append("CREATE INDEX ").append(options.getDocCommands()).append("_DOC_INDEX")
    .append(" ON ").append(options.getDocCommands()).append(" (doc_id);").ln()

    .build()).build();
  }


  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
      .ln().append("--- constraints for").append(options.getDocCommands()).ln()
      
      .append("ALTER TABLE ").append(options.getDocCommands()).ln()
      .append("  ADD CONSTRAINT ").append(options.getDocCommands()).append("_DOC_FK").ln()
      .append("  FOREIGN KEY (doc_id)").ln()
      .append("  REFERENCES ").append(options.getDoc()).append(" (id);").ln().ln()
      
      .append("ALTER TABLE ").append(options.getDocCommands()).ln()
      .append("  ADD CONSTRAINT ").append(options.getDocCommands()).append("_BRANCH_FK").ln()
      .append("  FOREIGN KEY (branch_id)").ln()
      .append("  REFERENCES ").append(options.getDocBranch()).append(" (branch_id);").ln().ln()
      
      
    .build()).build();
  }


  @Override
  public Function<Row, DocCommands> defaultMapper() {
    return (row) -> {
      return ImmutableDocCommands.builder()
          .id(row.getString("id"))
          .commitId(row.getString("commit_id"))
          .docId(row.getString("doc_id"))
          .branchId(Optional.ofNullable(row.getString("branch_id")))
          .createdAt(row.getOffsetDateTime("created_at"))
          .createdBy(row.getString("created_by"))
          .commands(Arrays.asList(row.getArrayOfJsonObjects("commands")))
          .build();
    };
  }
}