package io.resys.thena.contract.client.tables;

import java.util.List;
import java.util.Optional;

import io.resys.thena.api.annotations.TenantSql.SqlBuilder;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;

public interface ContractTableFilter {
  Optional<List<String>> getContractIds();
  
  
  
  
  
  
  final static class SQL implements SqlBuilder<ContractTableFilter> {
    @Override
    public SqlTuple apply(Tenant tenant, ContractTableFilter filter) {
      throw new UnsupportedOperationException("Default SQL builder should not be called");
    }
  }
}
