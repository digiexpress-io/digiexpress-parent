package io.digiexpress.mig.client.spi;

import io.digiexpress.mig.client.api.MigClient;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class MigClientImpl implements MigClient {
  private final io.vertx.mutiny.pgclient.PgPool src_tasks;
  private final io.vertx.mutiny.pgclient.PgPool src_dialob;
  private final io.vertx.mutiny.pgclient.PgPool target_dialob;

  @Override
  public SourceTaskQuery taskQuery() {
    return new SourceDbTaskQueryImpl(src_tasks);
  }

  @Override
  public SourceDialobQuery dialobQuery() {
    return new SourceDbDialobQueryImpl(src_dialob);
  }

  @Override
  public TargetDialobBuilder dialobBuilder() {
    return new TargetDialobBuilderImpl(target_dialob);
  }
}
