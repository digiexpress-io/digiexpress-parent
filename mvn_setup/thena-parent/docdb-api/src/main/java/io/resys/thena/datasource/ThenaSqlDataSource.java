package io.resys.thena.datasource;

import java.util.Optional;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.registry.ThenaRegistry;
import io.resys.thena.datasource.ThenaSqlClient.ThenaSqlPool;


/**
 * SQL pool and client wrapper
 */
public interface ThenaSqlDataSource extends ThenaDataSource {
  ThenaSqlDataSource withTenant(Tenant tenant);
  ThenaSqlDataSource withTx(ThenaSqlClient tx);
  
  // SQL pool = DB connection
  ThenaSqlPool getPool();
  
  // Ongoing SQL transactions
  Optional<ThenaSqlClient> getTx();
  
  // get transaction if started or just the connection
  default ThenaSqlClient getClient() {
    return getTx().orElse(getPool());
  }
  
  boolean isTenantLoaded();
  ThenaRegistry getRegistry();
  ThenaSqlDataSourceErrorHandler getErrorHandler();
  

}
