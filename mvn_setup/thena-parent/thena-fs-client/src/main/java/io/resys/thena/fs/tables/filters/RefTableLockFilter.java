package io.resys.thena.fs.tables.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import io.resys.thena.api.annotations.TenantSql.SqlBuilder;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Tuple;


@Value.Immutable
public interface RefTableLockFilter {
  Optional<List<String>> getDocIds();
  String getRefName();
  
  final static class SQL implements SqlBuilder<RefTableLockFilter> {
    
    private static final String NULL_QUERY =
"""
 CROSS JOIN (
   SELECT
     NULL::JSONB as props_labels,
     NULL::JSONB as props_comments,
     NULL::JSONB as props_permissions,
     NULL::JSONB as props_flags
 ) as props
 CROSS JOIN (
   SELECT
     NULL::TEXT as blob_type,
     NULL::JSONB as blob_value
 ) as blobs
""";
    
    
    @Override
    public SqlTuple apply(Tenant tenant, String baseline, RefTableLockFilter filter) {
      final var params = new ArrayList<Object>();
      params.add(filter.getRefName());

      return ImmutableSqlTuple.builder()
          .value(baseline + " " + extendedQuery(filter, params))
          .props(Tuple.from(params))
          .build();
    }
    
    public String extendedQuery(RefTableLockFilter filter, List<Object> params) {
      final var stmt = new SqlStatement();
      if(filter.getDocIds().isEmpty()) {
        return stmt.ln().append(NULL_QUERY).ln().build();
      }
      
    
      final var index = params.size() +1;
      
      return stmt
        .append("LEFT JOIN {props} props ON props.id = node.props_id AND nodes.node_id = ANY($").append(index).append(")")
        .append("LEFT JOIN {blobs} blobs ON blobs.id = node.blob_id AND nodes.node_id = ANY($").append(index).append(")")
        .build();
      
    }
  }
}
