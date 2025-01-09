package io.digiexpress.thena.mq.client.api.persistence;

import java.util.function.Function;

import io.digiexpress.thena.mq.client.api.entities.ThenaMqEntity;
import io.resys.thena.datasource.ThenaSqlClient;

public interface ThenaMqRegistryTemplate<T extends ThenaMqEntity, R> {
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTuple getById(String id);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<R, T> defaultMapper();
}