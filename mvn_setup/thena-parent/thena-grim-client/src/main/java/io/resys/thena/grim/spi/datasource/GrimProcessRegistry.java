package io.resys.thena.grim.spi.datasource;

/*-
 * #%L
 * thena-grim-client
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

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.function.Function;

import io.resys.thena.api.entities.grim.GrimProcess;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface GrimProcessRegistry extends ThenaRegistryService<GrimProcess, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.Sql findAll(boolean includeFormBody);
  
  ThenaSqlClient.Sql getNextSequence();
  ThenaSqlClient.SqlTuple getNextSequence(long howMany);
  
  
  
  ThenaSqlClient.SqlTuple deleteOneById(String id);
  ThenaSqlClient.SqlTuple getById(String id, boolean includeFormBody);
  ThenaSqlClient.SqlTuple getOneByIdWithLock(String id, boolean includeFormBody);
  ThenaSqlClient.SqlTuple findAllExpired(boolean includeFormBody);
  ThenaSqlClient.SqlTuple findAllAnsweredFrom(OffsetDateTime pickupFrom, boolean includeFormBody);
  
  ThenaSqlClient.SqlTuple findOneByQuestionnaireId(String questionnaireId, boolean includeFormBody);
  ThenaSqlClient.SqlTuple findOneByMissionId(String missionId, boolean includeFormBody);
  ThenaSqlClient.SqlTuple findNotArchivedByUserId(String userId, boolean includeFormBody);
  ThenaSqlClient.SqlTuple findOnOrAfter(OffsetDateTime createdOnOrAfter, boolean includeFormBody);
  ThenaSqlClient.SqlTuple findOnOrBeforeWithoutMission(OffsetDateTime onOrBefore, boolean includeFormBody);
  
  ThenaSqlClient.SqlTupleList insertAll(Collection<GrimProcess> mission);
  ThenaSqlClient.SqlTupleList updateAll(Collection<GrimProcess> mission);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, GrimProcess> defaultMapper();
}
