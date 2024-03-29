package io.resys.thena.registry.org;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.org.OrgCommit;
import io.resys.thena.api.registry.org.OrgCommitRegistry;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ImmutableSqlTupleList;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTuple;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTupleList;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrgCommitRegistrySqlImpl implements OrgCommitRegistry {
  private final TenantTableNames options;
  @Override
  public Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getOrgMembers())
        .build())
        .build();
  }
  @Override
  public SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getOrgCommits()).ln()
        .append("  WHERE commit_id = $1").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTuple insertOne(OrgCommit doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgCommits())
        .append(" (commit_id, parent_id, created_at, commit_log, commit_author, commit_message) VALUES($1, $2, $3, $4, $5, $6)").ln()
        .build())
        .props(Tuple.from(new Object[]{ doc.getId(), doc.getParentId(), doc.getCreatedAt(), doc.getLog(), doc.getAuthor(), doc.getMessage() }))
        .build();
  }
  @Override
  public SqlTupleList insertAll(Collection<OrgCommit> docs) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgCommits())
        .append(" (commit_id, parent_id, created_at, commit_log, commit_author, commit_message) VALUES($1, $2, $3, $4, $5, $6)").ln()
        .build())
        .props(docs.stream()
            .map(doc -> Tuple.from(new Object[]{ doc.getId(), doc.getParentId(), doc.getCreatedAt(), doc.getLog(), doc.getAuthor(), doc.getMessage() }))
            .collect(Collectors.toList()))
        .build();
  }

  @Override
  public Function<Row, OrgCommit> defaultMapper() {
    throw new RuntimeException("Not implemented");
  }
  @Override
  public Sql createTable() {
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
  public Sql createConstraints() {
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
  public Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getOrgCommitTrees()).append(";").ln()
        .append("DROP TABLE ").append(options.getOrgCommits()).append(";").ln()
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

}
