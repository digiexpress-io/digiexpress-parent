package io.digiexpress.thena.mq.client.api.persistence;

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
