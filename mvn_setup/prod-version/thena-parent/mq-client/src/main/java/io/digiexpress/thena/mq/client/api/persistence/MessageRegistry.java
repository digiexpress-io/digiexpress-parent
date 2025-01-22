package io.digiexpress.thena.mq.client.api.persistence;

/*-
 * #%L
 * thena-mq-client
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

import io.digiexpress.thena.mq.client.api.entities.QueueMessage;
import io.digiexpress.thena.mq.client.api.entities.Delivery.DeliveryStatus;
import io.digiexpress.thena.mq.client.api.entities.QueueMessage.QueueMessageStatus;
import io.resys.thena.datasource.ThenaSqlClient;

public interface MessageRegistry extends ThenaMqRegistryTemplate<QueueMessage, io.vertx.mutiny.sqlclient.Row> {

  ThenaSqlClient.SqlTupleList insertMany(List<QueueMessage> docs);
  ThenaSqlClient.SqlTupleList updateMany(List<QueueMessage> docs);
  ThenaSqlClient.SqlTuple findAllByStatus(QueueMessageStatus status, boolean lockForUpdate);
  ThenaSqlClient.SqlTuple findAllByAppIdAndDeliveryStatus(String appId, DeliveryStatus status) ;
  ThenaSqlClient.SqlTuple findLastNEntries(long entries);
  
  @Override ThenaSqlClient.SqlTuple getByIdOrName(String id);  // matches by external_id or id
  @Override ThenaSqlClient.Sql findAll();
  @Override ThenaSqlClient.Sql createTable();
  @Override ThenaSqlClient.Sql createConstraints();
  @Override ThenaSqlClient.Sql dropTable();
  @Override Function<io.vertx.mutiny.sqlclient.Row, QueueMessage> defaultMapper();
}
