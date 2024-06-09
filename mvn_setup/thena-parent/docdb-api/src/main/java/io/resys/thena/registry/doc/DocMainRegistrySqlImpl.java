package io.resys.thena.registry.doc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.Doc.DocFilter;
import io.resys.thena.api.entities.doc.ImmutableDoc;
import io.resys.thena.api.registry.doc.DocMainRegistry;
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
public class DocMainRegistrySqlImpl implements DocMainRegistry {
  private final TenantTableNames options;
  
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT ")
        .append(" docs.*, ")
        .append(" updated_commit.created_at as updated_at,").ln()
        .append(" created_commit.created_at as created_at").ln()
        
        .append(" FROM ").append(options.getDoc()).append(" as docs ")
        
        .append(" LEFT JOIN ").append(options.getDocCommits()).append(" as updated_commit").ln()
        .append(" ON(updated_commit.id = docs.commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getDocCommits()).append(" as created_commit").ln()
        .append(" ON(created_commit.id = docs.created_with_commit_id)").ln()
        
        
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ")
        .append(" docs.*, ")
        .append(" updated_commit.created_at as updated_at,").ln()
        .append(" created_commit.created_at as created_at").ln()
        
        .append(" FROM ").append(options.getDoc()).append(" as docs ")
        
        .append(" LEFT JOIN ").append(options.getDocCommits()).append(" as updated_commit").ln()
        .append(" ON(updated_commit.id = docs.commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getDocCommits()).append(" as created_commit").ln()
        .append(" ON(created_commit.id = docs.created_with_commit_id)").ln()
        
        .append("  WHERE (docs.id = $1 OR docs.external_id = $1)").ln()
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
      filters.add(" ( docs.doc_type = ANY($" + index + " ) )");
      params.add(filter.getDocTypes().toArray());
    }
    final var where = (params.isEmpty() ? "" : " WHERE ") + String.join(" AND ", filters);
    
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ")
        .append(" docs.*, ")
        .append(" updated_commit.created_at as updated_at,").ln()
        .append(" created_commit.created_at as created_at").ln()
        
        .append(" FROM ").append(options.getDoc()).append(" as docs ")
        
        .append(" LEFT JOIN ").append(options.getDocCommits()).append(" as updated_commit").ln()
        .append(" ON(updated_commit.id = docs.commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getDocCommits()).append(" as created_commit").ln()
        .append(" ON(created_commit.id = docs.created_with_commit_id)").ln()
        
        .append(where).ln()
        .build())
        .props(Tuple.from(params))
        .build();
  }

  @Override
  public ThenaSqlClient.SqlTuple deleteMany(List<String> id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("DELETE FROM ").append(options.getDoc())
        .append(" WHERE ").ln()
        .append(" (id = ANY($1) OR external_id = ANY($1))") // document itself
        .append(" OR doc_parent_id = ANY($1)")              // child documents
        .append(" OR doc_parent_id IN(select id from ").append(options.getDoc()).append(" WHERE external_id = ANY($1) )").ln()  // child documents
        
        .build())
        .props(Tuple.of(id.toArray()))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTupleList insertMany(List<Doc> docs) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getDoc())
        .append(" (id, external_id, doc_type, doc_status, doc_meta, doc_parent_id, commit_id, created_with_commit_id, owner_id) VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9)").ln()
        .build())
        .props(docs.stream()
            .map(doc -> Tuple.from(Arrays.asList(
                doc.getId(), 
                doc.getExternalId(), 
                doc.getType(), 
                doc.getStatus(), 
                doc.getMeta(), 
                doc.getParentId(),
                doc.getCommitId(),
                doc.getCreatedWithCommitId(),
                doc.getOwnerId()
            )))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTupleList updateMany(List<Doc> docs) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getDoc())
        .append(" SET external_id = $1, doc_type = $2, doc_status = $3, doc_meta = $4, commit_id = $5")
        .append(" WHERE id = $6")
        .build())
        .props(docs.stream()
            .map(doc ->Tuple.of(doc.getExternalId(), doc.getType(), doc.getStatus(), doc.getMeta(), doc.getCommitId(), doc.getId()))
            .collect(Collectors.toList()))
        .build();
  }

  @Override
  public Function<Row, Doc> defaultMapper() {
    return row -> ImmutableDoc.builder()
        .id(row.getString("id"))
        .externalId(row.getString("external_id"))
        .ownerId(row.getString("owner_id"))
        .parentId(row.getString("doc_parent_id"))
        .commitId(row.getString("commit_id"))
        .createdWithCommitId(row.getString("created_with_commit_id"))
        .createdAt(row.getOffsetDateTime("created_at"))
        .updatedAt(row.getOffsetDateTime("updated_at"))
        .type(row.getString("doc_type"))
        .status(Doc.DocStatus.valueOf(row.getString("doc_status")))
        .meta(row.getJsonObject("doc_meta"))
        .build();
  }
  @Override
  public ThenaSqlClient.Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
        .append("CREATE TABLE ").append(options.getDoc()).ln()
        .append("(").ln()
        .append("  id VARCHAR(100) PRIMARY KEY,").ln()
        .append("  commit_id VARCHAR(40) NOT NULL,").ln()
        .append("  created_with_commit_id VARCHAR(40) NOT NULL,").ln()
        
        .append("  external_id VARCHAR(100) UNIQUE,").ln()
        .append("  owner_id VARCHAR(100),").ln()
        .append("  doc_parent_id VARCHAR(100),").ln()
        .append("  doc_type VARCHAR(40) NOT NULL,").ln()
        .append("  doc_status VARCHAR(8) NOT NULL,").ln()
        .append("  doc_meta jsonb").ln()
        .append(");").ln()
        
        .append("CREATE INDEX ").append(options.getDoc()).append("_DOC_EXT_INDEX")
        .append(" ON ").append(options.getDoc()).append(" (external_id);").ln()

        .append("CREATE INDEX ").append(options.getDoc()).append("_DOC_PARENT_INDEX")
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
        .append("DROP TABLE IF EXISTS ").append(options.getDoc()).append(";").ln()
        .build()).build();
  }
}
