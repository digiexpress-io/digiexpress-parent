package io.resys.thena.registry.doc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocFlatted;
import io.resys.thena.api.entities.doc.ImmutableDoc;
import io.resys.thena.api.entities.doc.ImmutableDocFlatted;
import io.resys.thena.api.registry.doc.DocMainRegistry;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ImmutableSqlTupleList;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.storesql.support.SqlStatement;
import io.resys.thena.structures.doc.DocQueries.FlattedCriteria;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DocMainRegistrySqlImpl implements DocMainRegistry {
  private final TenantTableNames options;
  
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getDoc())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
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
  public ThenaSqlClient.SqlTuple findById(String id) {
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
  public ThenaSqlClient.SqlTuple insertOne(Doc doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getDoc())
        .append(" (id, external_id, doc_type, doc_status, doc_meta, doc_parent_id, owner_id) VALUES($1, $2, $3, $4, $5, $6, $7)").ln()
        .build())
        .props(Tuple.from(new Object[]{ doc.getId(), doc.getExternalId(), doc.getType(), doc.getStatus(), doc.getMeta(), doc.getParentId(), doc.getOwnerId() }))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple deleteById(String id) {
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
  public ThenaSqlClient.SqlTuple updateOne(Doc doc) {
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
  public ThenaSqlClient.SqlTupleList insertMany(List<Doc> docs) {
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
  public ThenaSqlClient.SqlTupleList updateMany(List<Doc> docs) {
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
  public ThenaSqlClient.Sql findAllFlatted() {
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
  public ThenaSqlClient.SqlTuple findAllFlatted(FlattedCriteria criteria) {
    
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

    final var status = criteria.getOnlyActiveDocs() ? Doc.DocStatus.IN_FORCE.name() : Doc.DocStatus.ARCHIVED.name();
    
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
  @Override
  public Function<Row, Doc> defaultMapper() {
    return DocMainRegistrySqlImpl::doc;
  }
  @Override
  public Function<Row, DocFlatted> docFlattedMapper() {
    return DocMainRegistrySqlImpl::docFlatted;
  }
  private static Doc doc(Row row) {
    return ImmutableDoc.builder()
        .id(row.getString("id"))
        .externalId(row.getString("external_id"))
        .parentId(row.getString("doc_parent_id"))
        .externalIdDeleted(row.getString("external_id_deleted"))
        .type(row.getString("doc_type"))
        .status(Doc.DocStatus.valueOf(row.getString("doc_status")))
        .meta(jsonObject(row, "doc_meta"))
        .build();
  }
  private static DocFlatted docFlatted(Row row) {
    return ImmutableDocFlatted.builder()
        .externalId(row.getString("external_id"))
        .docId(row.getString("doc_id"))
        .docType(row.getString("doc_type"))
        .docStatus(Doc.DocStatus.valueOf(row.getString("doc_status")))
        .docMeta(Optional.ofNullable(jsonObject(row, "doc_meta")))
        .docParentId(Optional.ofNullable(row.getString("doc_parent_id")))
        .externalIdDeleted(Optional.ofNullable(row.getString("external_id_deleted")))
        
        .branchId(row.getString("branch_id"))
        .branchName(row.getString("branch_name"))
        .branchNameDeleted(Optional.ofNullable(row.getString("branch_name_deleted")))
        .branchValue(jsonObject(row, "branch_value"))
        .branchStatus(Doc.DocStatus.valueOf(row.getString("branch_status")))
        
        .commitId(row.getString("commit_id"))
        .commitAuthor(row.getString("commit_author"))
        .commitMessage(row.getString("commit_message"))
        .commitParent(Optional.ofNullable(row.getString("commit_parent")))
        .commitDateTime(LocalDateTime.parse(row.getString("commit_datetime")))
        
        .docLogId(Optional.ofNullable(row.getString("doc_log_id")))
        .docLogValue(Optional.ofNullable(jsonObject(row, "doc_log_value")))
        
        .build();
  }
  
  private static JsonObject jsonObject(Row row, String columnName) {
    // string based - new JsonObject(row.getString(columnName));
    return row.getJsonObject(columnName);
  }
  @Override
  public ThenaSqlClient.Sql createTable() {
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
  public ThenaSqlClient.Sql createConstraints() {
    return ImmutableSql.builder().value("").build();
  }
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getDoc()).append(";").ln()
        .build()).build();
  }


}
