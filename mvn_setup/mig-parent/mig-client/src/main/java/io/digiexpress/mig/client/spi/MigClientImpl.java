package io.digiexpress.mig.client.spi;

import io.digiexpress.mig.client.api.MigClient;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class MigClientImpl implements MigClient {
  private final io.vertx.mutiny.pgclient.PgPool src_tasks;
  private final io.vertx.mutiny.pgclient.PgPool src_dialob;
  private final io.vertx.mutiny.pgclient.PgPool src_thena;
  private final io.vertx.mutiny.pgclient.PgPool target_dialob;
  private final io.vertx.mutiny.pgclient.PgPool target_tasks;
  
  private final String taskTenant;

  @Override
  public SourceTaskQuery taskQuery() {
    return new SourceTaskQueryImpl(src_tasks);
  }

  @Override
  public SourceDialobQuery dialobQuery() {
    return new SourceDialobQueryImpl(src_dialob);
  }

  @Override
  public TargetDialobBuilder dialobBuilder() {
    return new TargetDialobBuilderImpl(target_dialob);
  }

  @Override
  public TargetTaskBuilder taskBuilder() {
    return new TargetTaskBuilderImpl(target_tasks, taskTenant);
  }

  @Override
  public SourceThenaQuery thenaQuary() {
    return new SourceThenaQueryImpl(src_thena);
  }
}
