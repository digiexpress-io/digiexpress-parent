package io.resys.thena.api.registry;

import java.util.function.Function;

import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTuple;

public interface ThenaRegistryService<T extends ThenaTable, R> {
  // marker interface
  interface ThenaTable {}
  
  Sql findAll();
  SqlTuple getById(String id);
  
  /*
  SqlTuple insertOne(T entity);
  SqlTupleList insertAll(Collection<T> logs);
  
  SqlTuple updateOne(T entity);
  SqlTupleList updateMany(Collection<T> users);
  
  SqlTupleList deleteAll(Collection<T> users);
  SqlTuple deleteOne(T repo);
  */
  
  Sql createTable();
  Sql createConstraints();
  Sql dropTable();
  
  Function<R, T> defaultMapper();
}
