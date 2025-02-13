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

import java.util.Optional;

import io.digiexpress.thena.mq.client.api.entities.Channel;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlClient.ThenaSqlPool;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;

public interface ThenaMqDataSource {
  Channel getChannel();
  ThenaMqTableNames getChannelTableNames();
  ThenaMqDataSource withChannel(Channel channel);
  boolean isLocked(Throwable t);
  
  ThenaMqDataSource withTx(ThenaSqlClient tx);
  
  // SQL pool = DB connection
  ThenaSqlPool getPool();
  
  // Ongoing SQL transactions
  Optional<ThenaSqlClient> getTx();
  
  // get transaction if started or just the connection
  default ThenaSqlClient getClient() {
    return getTx().orElse(getPool());
  }
  
  boolean isChannelLoaded();
  ThenaMqTableRegistry getRegistry();
  ThenaSqlDataSourceErrorHandler getErrorHandler();
}
