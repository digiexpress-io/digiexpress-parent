package io.resys.thena.api.registry.org;

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

import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface OrgPartyRegistry extends ThenaRegistryService<OrgParty, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.SqlTuple getById(String id); //group name or id or external_id
  
  
  ThenaSqlClient.SqlTuple findAllByRightId(String rightId);
  ThenaSqlClient.SqlTuple findAllByMemberId(String memberId);
  
  
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTuple findAll(Collection<String> id);
  ThenaSqlClient.SqlTupleList insertAll(Collection<OrgParty> OrgGroup);
  ThenaSqlClient.SqlTupleList updateMany(Collection<OrgParty> groups);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, OrgParty> defaultMapper();
  
}
