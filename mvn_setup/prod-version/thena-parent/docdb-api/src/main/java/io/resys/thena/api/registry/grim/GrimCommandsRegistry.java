package io.resys.thena.api.registry.grim;

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

import java.util.Collection;
import java.util.function.Function;

import io.resys.thena.api.entities.grim.GrimCommands;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface GrimCommandsRegistry extends ThenaRegistryService<GrimCommands, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTuple findAllByIds(Collection<String> id);
  ThenaSqlClient.SqlTuple findAllByMissionIds(GrimMissionFilter filter);
  ThenaSqlClient.SqlTupleList insertAll(Collection<GrimCommands> commits);  
  ThenaSqlClient.SqlTuple getById(String id);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, GrimCommands> defaultMapper();
}
