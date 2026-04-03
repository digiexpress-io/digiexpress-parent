package io.resys.thena.spi;

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

import io.resys.thena.api.entities.Alias;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface AliasRegistry extends ThenaRegistryService<Alias, io.vertx.mutiny.sqlclient.Row> {
  
  ThenaSqlClient.SqlTuple exists();
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTuple getByName(String name);
  ThenaSqlClient.SqlTuple getByNameOrId(String name);
  ThenaSqlClient.SqlTuple insertOne(Alias repo);
  ThenaSqlClient.SqlTuple deleteOne(Alias repo);
  ThenaSqlClient.SqlTuple updateOne(Alias repo);
    
  Function<io.vertx.mutiny.sqlclient.Row, Alias> defaultMapper();
  
}
