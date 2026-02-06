package io.resys.thena.fs.tables.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.mutable.MutableInt;
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
    
    @Override
    public SqlTuple apply(Tenant tenant, String baseline, RefTableLockFilter filter) {
      final var stmt = new SqlStatement();
      final var params = new ArrayList<Object>();
      params.add(filter.getRefName());
      final MutableInt index = new MutableInt(1);

      if(filter.getDocIds().isPresent()) {
        final var nextIndex = index.incrementAndGet();
        stmt.append("(node.node_id = ANY($").append(nextIndex).append(")").append(")").ln();
        params.add(filter.getDocIds().get().toArray(new String[]{}));
        
      }
      
      final var result = stmt.toString();
      final var clause = (result.isBlank() ? "" : " WHERE ") + result;
      return ImmutableSqlTuple.builder()
          .value(baseline + clause)
          .props(Tuple.from(params))
          .build();
    }
  }
}
