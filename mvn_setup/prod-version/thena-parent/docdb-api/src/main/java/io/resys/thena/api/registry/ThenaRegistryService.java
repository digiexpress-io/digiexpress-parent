package io.resys.thena.api.registry;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.function.Function;

import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;
import io.resys.thena.datasource.ThenaSqlClient;

public interface ThenaRegistryService<T extends ThenaTable, R> {
  // marker interface
  interface ThenaTable {}
  
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTuple getById(String id);
  
  /*
  SqlTuple insertOne(T entity);
  SqlTupleList insertAll(Collection<T> logs);
  
  SqlTuple updateOne(T entity);
  SqlTupleList updateMany(Collection<T> users);
  
  SqlTupleList deleteAll(Collection<T> users);
  SqlTuple deleteOne(T repo);
  */
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<R, T> defaultMapper();
}
