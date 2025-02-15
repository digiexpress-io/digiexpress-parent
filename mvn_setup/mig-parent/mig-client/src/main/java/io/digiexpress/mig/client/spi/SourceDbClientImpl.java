package io.digiexpress.mig.client.spi;

import io.digiexpress.mig.client.api.MigClient;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class SourceDbClientImpl implements MigClient {
  private final io.vertx.mutiny.pgclient.PgPool taskDbPool;
  private final io.vertx.mutiny.pgclient.PgPool dialobDbPool;

  @Override
  public SourceDbTaskQuery taskQuery() {
    return new SourceDbTaskQueryImpl(taskDbPool);
  }

  @Override
  public SourceDbDialobQuery dialobQuery() {
    return new SourceDbDialobQueryImpl(dialobDbPool);
  }
}
