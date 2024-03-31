package io.resys.thena.api.registry.grim;

import java.util.Collection;
import java.util.function.Function;

import io.resys.thena.api.entities.grim.GrimRemark;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface GrimRemarkRegistry extends ThenaRegistryService<GrimRemark, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTuple getById(String id);
  
  ThenaSqlClient.SqlTuple findAllByMissionIds(Collection<String> id);
  ThenaSqlClient.SqlTupleList insertAll(Collection<GrimRemark> remarks);
  ThenaSqlClient.SqlTupleList deleteAll(Collection<GrimRemark> remarks);
  ThenaSqlClient.SqlTupleList updateAll(Collection<GrimRemark> remarks);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, GrimRemark> defaultMapper();
}