package io.resys.thena.docdb.store.sql.statement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.models.ThenaDocObject.Doc;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocStatus;
import io.resys.thena.docdb.models.doc.DocQueries.FlattedCriteria;
import io.resys.thena.docdb.spi.DbCollections;
import io.resys.thena.docdb.store.sql.ImmutableSql;
import io.resys.thena.docdb.store.sql.ImmutableSqlTuple;
import io.resys.thena.docdb.store.sql.ImmutableSqlTupleList;
import io.resys.thena.docdb.store.sql.SqlBuilder.DocSqlBuilder;
import io.resys.thena.docdb.store.sql.SqlBuilder.Sql;
import io.resys.thena.docdb.store.sql.SqlBuilder.SqlTuple;
import io.resys.thena.docdb.store.sql.SqlBuilder.SqlTupleList;
import io.resys.thena.docdb.store.sql.support.SqlStatement;
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
        .append("  WHERE (id = $1 OR external_id = $1)").ln()
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTuple findById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getDoc()).ln()
        .append("  WHERE (id = $1 OR external_id = $1)").ln()
        .append("UNION ").ln()        
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getDoc()).ln()
        .append("  WHERE ").ln()
        .append("    doc_parent_id = (select id from ").append(options.getDoc()).append(" external_id = $1))").ln()
        .append("    OR doc_parent_id = $1")
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTuple insertOne(Doc doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getDoc())
        .append(" (id, external_id, doc_type, doc_status, doc_meta, doc_parent_id, owner_id) VALUES($1, $2, $3, $4, $5, $6, $7)").ln()
        .build())
        .props(Tuple.from(new Object[]{ doc.getId(), doc.getExternalId(), doc.getType(), doc.getStatus(), doc.getMeta(), doc.getParentId(), doc.getOwnerId() }))
        .build();
  }
  @Override
  public SqlTuple deleteById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("DELETE FROM ").append(options.getDoc())
        .append(" WHERE ").ln()
        .append(" (id = $1 OR external_id = $1)")
        .append(" OR doc_parent_id = (select id from ").append(options.getDoc()).append(" external_id = $1))").ln()
        .append(" OR doc_parent_id = $1")
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTuple updateOne(Doc doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getDoc())
        .append(" SET external_id = $1, doc_type = $2, doc_status = $3, doc_meta = $4, external_id_deleted = $5, doc_parent_id = $6")
        .append(" WHERE id = $7")
        .build())
        .props(Tuple.from(new Object[]{doc.getExternalId(), doc.getType(), doc.getStatus(), doc.getMeta(), doc.getExternalIdDeleted(), doc.getParentId(), doc.getId()}))
        .build();
  }
  
  @Override
  public SqlTupleList insertMany(List<Doc> docs) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getDoc())
        .append(" (id, external_id, doc_type, doc_status, doc_meta, doc_parent_id) VALUES($1, $2, $3, $4, $5, $6)").ln()
        .build())
        .props(docs.stream()
            .map(doc -> Tuple.of(doc.getId(), doc.getExternalId(), doc.getType(), doc.getStatus(), doc.getMeta(), doc.getParentId()))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public SqlTupleList updateMany(List<Doc> docs) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getDoc())
        .append(" SET external_id = $1, doc_type = $2, doc_status = $3, doc_meta = $4")
        .append(" WHERE id = $5")
        .build())
        .props(docs.stream()
            .map(doc ->Tuple.of(doc.getExternalId(), doc.getType(), doc.getStatus(), doc.getMeta(), doc.getId()))
            .collect(Collectors.toList()))
        .build();
  }

  @Override
  public Sql findAllFlatted() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT ")
        .append("  doc.external_id as external_id,").ln()
        .append("  doc.external_id_deleted as external_id_deleted,").ln()
        .append("  doc.doc_type as doc_type,").ln()
        .append("  doc.doc_status as doc_status,").ln()
        .append("  doc.doc_meta as doc_meta,").ln()
        .append("  doc.doc_parent_id as doc_parent_id,").ln()
        .append("  doc.owner_id as owner_id,").ln()
        
        .append("  branch.doc_id as doc_id,").ln()
        .append("  branch.branch_id as branch_id,").ln()
        .append("  branch.branch_name as branch_name,").ln()
        .append("  branch.branch_name_deleted as branch_name_deleted,").ln()
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
  public SqlTuple findAllFlatted(FlattedCriteria criteria) {
    
    final var props = new ArrayList<Object>();
    final var additional = new StringBuilder();
    var index = 0;
    
    if(criteria.getDocType() != null) {
      index++;
      props.add(criteria.getDocType());
      additional.append(" AND doc.doc_type = $").append(index);
    }
    
    if(Boolean.TRUE.equals(criteria.getMatchOwners())) {
      index++;
      props.add(criteria.getMatchId().toArray(new String[]{}));
      additional.append("doc.owner_id = ANY($" + index +  ")");
    }
    
    
    if(criteria.getBranchName() != null) {
      index++;
      props.add(criteria.getBranchName());
      additional.append(" AND branch.branch_name = $").append(index);
    }
    if(!criteria.getMatchId().isEmpty()) {
      index++;
      props.add(criteria.getMatchId().toArray(new String[]{}));
      
      var value = "doc.id = ANY($" + index +  ") OR doc.external_id = ANY($" + index +  ") OR branch.branch_id = ANY($" + index +  ")";
      if(criteria.getChildren()) {
        final var children = new SqlStatement().ln()
            .append("SELECT inner_branch.doc_id ")
            .append(" FROM ").append(options.getDocBranch()).append(" as inner_branch").ln()
            .append(" JOIN ").append(options.getDoc())      .append(" as inner_doc ON(inner_doc.id = inner_branch.doc_id)").ln()
            .append(" WHERE").ln()
            .append("   inner_doc.id = ANY($" + index +  ") OR inner_doc.external_id = ANY($" + index +  ") OR inner_branch.branch_id = ANY($" + index +  ")").ln()
            .ln().build();
        value += " OR doc.doc_parent_id = (" + children + ")";
      }
      additional.append(" AND (" + value + ")");
    }    

    final var status = criteria.getOnlyActiveDocs() ? DocStatus.IN_FORCE.name() : DocStatus.ARCHIVED.name();
    
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ")
        .append("  doc.external_id as external_id,").ln()
        .append("  doc.external_id_deleted as external_id_deleted,").ln()
        .append("  doc.doc_type as doc_type,").ln()
        .append("  doc.doc_status as doc_status,").ln()
        .append("  doc.doc_meta as doc_meta,").ln()
        .append("  doc.doc_parent_id as doc_parent_id,").ln()
        .append("  doc.owner_id as owner_id,").ln()
        
        .append("  branch.doc_id as doc_id,").ln()
        .append("  branch.branch_id as branch_id,").ln()
        .append("  branch.branch_name as branch_name,").ln()
        .append("  branch.branch_name_deleted as branch_name_deleted,").ln()
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
        .append(" WHERE ").ln()
        .append(" doc.doc_status = '").append(status).append("'").ln()    
        .append(" AND branch.branch_status = '").append(status).append("'").ln()
        .append(additional.toString()).ln()
        .build())
        .props(Tuple.from(props))
        .build();  
  }
}
