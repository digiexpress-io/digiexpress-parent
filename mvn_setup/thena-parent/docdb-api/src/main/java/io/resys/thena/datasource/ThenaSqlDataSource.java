package io.resys.thena.datasource;

import java.util.Optional;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.datasource.ThenaSqlClient.ThenaSqlPool;

public interface ThenaSqlDataSource extends ThenaDataSource {
  ThenaSqlDataSource withTenant(Tenant tenant);
  //ThenaSqlDataSource withTx(io.vertx.mutiny.sqlclient.SqlClient tx);
  
  // SQL pool = DB connection
  //io.vertx.mutiny.sqlclient.Pool getPool();
  
  // Ongoing SQL transactions
  //Optional<io.vertx.mutiny.sqlclient.SqlClient> getTx();
  
  // get transaction if started or just the connection
  //default io.vertx.mutiny.sqlclient.SqlClient getClient() {
    //return getTx().orElse(getPool());
  //}

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
  SqlSchema getSchema();
  SqlDataMapper getDataMapper();
  SqlQueryBuilder getQueryBuilder();
  ThenaSqlDataSourceErrorHandler getErrorHandler();
  

}
