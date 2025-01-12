package io.digiexpress.thena.mq.client.spi.persistence;

import java.util.Optional;
import java.util.function.Function;

import io.digiexpress.thena.mq.client.api.ThenaMqClient;
import io.digiexpress.thena.mq.client.api.ThenaMqLogConstants;
import io.digiexpress.thena.mq.client.api.entities.Channel;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope.OperationStatus;
import io.digiexpress.thena.mq.client.api.persistence.ImmutableChannelBatch;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqDataSource;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqTableNames;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqTableRegistry;
import io.digiexpress.thena.mq.client.spi.ChannelException;
import io.digiexpress.thena.mq.client.spi.ThenaMqClientImpl;
import io.digiexpress.thena.mq.client.spi.visitors.ChannelBatchToSqlVisitor;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.datasource.vertx.ThenaSqlPoolVertx;
import io.resys.thena.storesql.PgErrors;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j(topic = ThenaMqLogConstants.SHOW_SQL)
public class ThenaMqChannelStateImpl implements ThenaMqChannelState {
  private final ThenaMqDataSource dataSource;

  @Override
  public ThenaMqDataSource getDataSource() {
    return dataSource;
  }
  
  @Override
  public ThenaMqChannelState withChannel(Channel channel) {
    return new ThenaMqChannelStateImpl(dataSource.withChannel(channel));
  }
  
  @Override
  public Uni<ThenaMqChannelState> withChannel(String channelId) {
    return queryChannels().getByNameOrId(channelId).onItem().transformToUni(channel -> {
      if(channel.isEmpty()) {
        return channelNotFound(channelId);
      }
      return Uni.createFrom().item(withChannel(channel.get()));
    });
  }

  @Override
  public <R> Uni<R> withChannelTransaction(ChannelTxScope scope, ChannelTransaction<R> callback) {
    return withChannel(scope.getChannelId()).onItem().transformToUni(state -> dataSource.getPool().withTransaction(conn -> {
        final var ongoingTx = state.getDataSource().withTx(conn);
        final var nextStateWithTx = new ThenaMqChannelStateImpl(ongoingTx);
        return callback.apply(nextStateWithTx);
      })
    );
  }
  
  @Override
  public InternalChannelQuery queryChannels() {
    return new InternalChannelQueryImpl(dataSource);
  }
  @Override
  public InternalMessageQuery queryMessages() {
    return new InternalMessageQueryImpl(dataSource);
  }
  @Override
  public InternalDeliveryQuery queryDeliveries() {
    return new InternalDeliveryQueryImpl(dataSource);
  }
  
  @Override
  public Uni<Channel> insertOne(final Channel newRepo) {
    final var next = dataSource.withChannel(newRepo);
    final var pool = next.getPool();
    
    return pool.withTransaction(tx -> {
      
      final var batch = ImmutableChannelBatch.builder()
        .batchStatus(OperationStatus.OK)
        .log("")
        .channelId(newRepo.getId())
        .newChannel(newRepo)
        .build();
      
      return batchMany(batch).onItem().transform(junk -> newRepo);
    });
  }
  
  @Override
  public Uni<ChannelBatch> batchMany(ChannelBatch input) {
    return new ChannelBatchToSqlVisitor(dataSource).accept(input);
  }
  @Override
  public InternalThenaMqContainersQuery queryContainers() {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public InternalQueueConsumerQuery queryQueueConsumer() {
    return new InternalQueueConsumerQueryImpl(dataSource);
  }
  @Override
  public InternalQueueQuery queryQueues() {
    return new InternalQueueQueryImpl(dataSource);
  }
  
  private <T> Uni<T> channelNotFound(String tenantId) {
    return queryChannels().findAll().onItem().transform(repos -> {
      final var ex = ChannelException.builder().notChannelWithName(tenantId, repos).getText();
      log.error(ex);
      throw new ChannelException(ex);
    }); 
  }

  public static ThenaMqChannelStateImpl create(ThenaMqTableNames names, io.vertx.mutiny.sqlclient.Pool client) {
    final var pool = new ThenaSqlPoolVertx(client);
    final var errorHandler = new PgErrors();
    final var dataSource = new ThenaMqDataSourceImpl(
        "", names, pool, errorHandler, 
        Optional.empty(),
        Builder.defaultRegistry(names)
    );
    return new ThenaMqChannelStateImpl(dataSource);
  }
  
  public static Builder create() {
    return new Builder();
  }

  public static class Builder {
    private io.vertx.mutiny.sqlclient.Pool client;
    private String db = "docdb";
    private ThenaSqlDataSourceErrorHandler errorHandler;
    private Function<ThenaMqTableNames, ThenaMqTableRegistry> registry;
    
    public Builder registry(Function<ThenaMqTableNames, ThenaMqTableRegistry> registry) {this.registry = registry; return this; }
    
    public Builder errorHandler(ThenaSqlDataSourceErrorHandler errorHandler) {this.errorHandler = errorHandler; return this; }
    public Builder db(String db) { this.db = db; return this; }
    public Builder client(io.vertx.mutiny.sqlclient.Pool client) { this.client = client; return this; }
    public static ThenaMqTableRegistry defaultRegistry(ThenaMqTableNames ctx) { return new ThenaMqTableRegistryImpl(ctx); }
    
    public ThenaMqClient build() {
      RepoAssert.notNull(client, () -> "client must be defined!");
      RepoAssert.notNull(db, () -> "db must be defined!");
      
      final var ctx = ThenaMqTableNames.defaults(db);
      this.errorHandler = new PgErrors();
      
      final Function<ThenaMqTableNames, ThenaMqTableRegistry> registry = this.registry == null ? Builder::defaultRegistry : this.registry;
      final var pool = new ThenaSqlPoolVertx(client);
      
      final var dataSource = new ThenaMqDataSourceImpl(
          db, ctx, pool, errorHandler, 
          Optional.empty(),
          registry.apply(ctx)
      );
      
      final var state = new ThenaMqChannelStateImpl(dataSource);
      return new ThenaMqClientImpl(state);
    }
  }
}
