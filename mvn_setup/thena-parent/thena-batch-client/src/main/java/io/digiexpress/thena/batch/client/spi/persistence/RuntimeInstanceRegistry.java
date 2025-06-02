package io.digiexpress.thena.batch.client.spi.persistence;

/*-
 * #%L
 * thena-batch-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import java.util.List;
import java.util.function.Function;

import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeStatus;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface RuntimeInstanceRegistry extends ThenaRegistryService<RuntimeInstance, io.vertx.mutiny.sqlclient.Row> {
  
  ThenaSqlClient.Sql getNextRefSequence();
  
  ThenaSqlClient.SqlTuple findAllByStatus(List<RuntimeStatus> status);
  ThenaSqlClient.SqlTuple getById(String id, boolean lockForUpdate);
  ThenaSqlClient.SqlTuple getNextRefSequence(long howMany);
  ThenaSqlClient.SqlTupleList insertMany(List<RuntimeInstance> instances);
  ThenaSqlClient.SqlTupleList updateMany(List<RuntimeInstance> instances);
  
  @Override ThenaSqlClient.Sql findAll();
  @Override ThenaSqlClient.SqlTuple getById(String id);
  
  @Override ThenaSqlClient.Sql createTable();
  @Override ThenaSqlClient.Sql createConstraints();
  @Override ThenaSqlClient.Sql dropTable();
  
  @Override Function<io.vertx.mutiny.sqlclient.Row, RuntimeInstance> defaultMapper();
}
