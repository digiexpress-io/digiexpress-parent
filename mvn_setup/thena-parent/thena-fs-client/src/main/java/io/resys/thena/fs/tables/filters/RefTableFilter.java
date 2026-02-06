package io.resys.thena.fs.tables.filters;

import java.util.ArrayList;
import java.util.function.Consumer;

import org.apache.commons.lang3.mutable.MutableInt;
import org.immutables.value.Value;

import io.resys.thena.api.annotations.TenantSql.SqlBuilder;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.fs.api.trees.NameExpressionBuilder;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.annotation.Nullable;


@Value.Immutable
public interface RefTableFilter {
  @Nullable Consumer<NameExpressionBuilder> getNameExpr();
  @Nullable String getBranchId();
  
  final static class SQL implements SqlBuilder<RefTableFilter> {
    
    @Override
    public SqlTuple apply(Tenant tenant, String baseline, RefTableFilter filter) {
      final var stmt = new SqlStatement();
      final var params = new ArrayList<Object>();
      final MutableInt index = new MutableInt(0);

      // Handle branch ID filter
      if (filter.getBranchId() != null) {
        stmt.append("ref_name = $").append(index.incrementAndGet());
        params.add(filter.getBranchId());
      }
      
      // Handle name expression filter
      if (filter.getNameExpr() != null) {
        if (!params.isEmpty()) {
          stmt.append(" AND ");
        }
        
        
        final var nameSql = new StringBuilder();
        final var nameBuilder = new NameExpressionBuilderImpl(
          "ref_name",
          param -> {
            params.add(param);
            return index.incrementAndGet();
          },
          nameSql
        );
        
        filter.getNameExpr().accept(nameBuilder);
        nameBuilder.close();
        stmt.append(nameSql.toString());
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
