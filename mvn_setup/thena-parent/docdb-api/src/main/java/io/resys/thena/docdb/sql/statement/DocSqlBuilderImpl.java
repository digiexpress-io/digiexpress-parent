package io.resys.thena.docdb.sql.statement;

import io.resys.thena.docdb.api.models.ThenaDocObject.Doc;
import io.resys.thena.docdb.spi.DbCollections;
import io.resys.thena.docdb.sql.ImmutableSql;
import io.resys.thena.docdb.sql.ImmutableSqlTuple;
import io.resys.thena.docdb.sql.SqlBuilder.DocSqlBuilder;
import io.resys.thena.docdb.sql.SqlBuilder.Sql;
import io.resys.thena.docdb.sql.SqlBuilder.SqlTuple;
import io.resys.thena.docdb.sql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DocSqlBuilderImpl implements DocSqlBuilder {
  private final DbCollections options;
  
  @Override
  public Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getDoc())
        .build())
        .build();
  }
  @Override
  public SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getDoc()).ln()
        .append("  WHERE (id = $1 OR external_id = $2)").ln()
        .build())
        .props(Tuple.of(id, id))
        .build();
  }
  @Override
  public SqlTuple insertOne(Doc doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getDoc())
        .append(" (id, external_id, doc_type, doc_status, doc_meta) VALUES($1, $2, $3, $4, $5)").ln()
        .build())
        .props(Tuple.of(doc.getId(), doc.getExternalId(), doc.getType(), doc.getStatus(), doc.getMeta()))
        .build();
  }
  @Override
  public SqlTuple deleteById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("DELETE FROM ").append(options.getDoc())
        .append(" WHERE (id = $1 OR external_id = $2)")
        .build())
        .props(Tuple.of(id, id))
        .build();
  }
  @Override
  public SqlTuple updateOne(Doc doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getDoc())
        .append(" SET external_id = $1, doc_type = $2, doc_status = $3, doc_meta = $4")
        .append(" WHERE id = $5")
        .build())
        .props(Tuple.of(doc.getExternalId(), doc.getType(), doc.getStatus(), doc.getMeta(), doc.getId()))
        .build();
  }
  
  @Override
  public Sql findAllFlatted() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT ")
        .append("  doc.external_id as external_id,").ln()
        .append("  doc.doc_type as doc_type,").ln()
        .append("  doc.doc_status as doc_status,").ln()
        .append("  doc.doc_meta as doc_meta,").ln()
    
        .append("  branch.doc_id as doc_id,").ln()
        .append("  branch.branch_id as branch_id,").ln()
        .append("  branch.branch_name as branch_name,").ln()
        .append("  branch.branch_status as branch_status,").ln()
        .append("  branch.value as branch_value,").ln()
        
        .append("  commits.author as commit_author,").ln()
        .append("  commits.datetime as commit_datetime,").ln()
        .append("  commits.message as commit_message,").ln()
        .append("  commits.parent as commit_parent,").ln()
        .append("  commits.id as commit_id,").ln()
        
        .append("  doc_log.id as doc_log_id,").ln()
        .append("  doc_log.value as doc_log_value").ln()
        
        
        .append(" FROM ").append(options.getDocBranch()).append(" as branch").ln()
        .append(" JOIN ").append(options.getDocCommits()) .append(" as commits ON(commits.branch_id = branch.branch_id AND commits.id = branch.commit_id)").ln()
        .append(" JOIN ").append(options.getDoc())        .append(" as doc ON(doc.id = branch.doc_id)").ln()
        .append(" LEFT JOIN ").append(options.getDocLog()).append(" as doc_log ON(doc_log.commit_id = commits.id)").ln()
        .build())
        .build();  
  }
  @Override
  public SqlTuple findAllFlattedByAnyId(String docId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ")
        .append("  doc.external_id as external_id,").ln()
        .append("  doc.doc_type as doc_type,").ln()
        .append("  doc.doc_status as doc_status,").ln()
        .append("  doc.doc_meta as doc_meta,").ln()
    
        .append("  branch.doc_id as doc_id,").ln()
        .append("  branch.branch_id as branch_id,").ln()
        .append("  branch.branch_name as branch_name,").ln()
        .append("  branch.branch_status as branch_status,").ln()
        .append("  branch.value as branch_value,").ln()
        
        .append("  commits.author as commit_author,").ln()
        .append("  commits.datetime as commit_datetime,").ln()
        .append("  commits.message as commit_message,").ln()
        .append("  commits.parent as commit_parent,").ln()
        .append("  commits.id as commit_id,").ln()
        
        .append("  doc_log.id as doc_log_id,").ln()
        .append("  doc_log.value as doc_log_value").ln()
        
        
        .append(" FROM ").append(options.getDocBranch()).append(" as branch").ln()
        .append(" JOIN ").append(options.getDocCommits()) .append(" as commits ON(commits.branch_id = branch.branch_id AND commits.id = branch.commit_id)").ln()
        .append(" JOIN ").append(options.getDoc())        .append(" as doc ON(doc.id = branch.doc_id)").ln()
        .append(" LEFT JOIN ").append(options.getDocLog()).append(" as doc_log ON(doc_log.commit_id = commits.id)").ln()
        
        .append(" WHERE doc.id = $1 OR doc.external_id = $1 OR branch.branch_id = $1").ln()
        .build())
        .props(Tuple.of(docId))
        .build();  
  }

}
