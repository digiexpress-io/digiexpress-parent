package io.digiexpress.mig.client.spi;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.digiexpress.mig.client.api.MigClient.TargetDialobBuilder;
import io.digiexpress.mig.client.api.SourceForms;
import io.digiexpress.mig.client.api.SourceForms.SourceFormDocument;
import io.digiexpress.mig.client.spi.loggers.EntityQueryLogger;
import io.digiexpress.mig.client.spi.loggers.TargetDialobLogger;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class TargetDialobBuilderImpl implements TargetDialobBuilder {
  private final io.vertx.mutiny.pgclient.PgPool target_dialob;
  private final TargetDialobLogger logger = new TargetDialobLogger();

  
  @Override
  public Uni<SourceForms> build(SourceForms source) {
    return target_dialob.withTransaction(conn -> execute(conn, source));
  }
  
  private Uni<SourceForms> execute(io.vertx.mutiny.sqlclient.SqlConnection conn, SourceForms source) {
    
    return Uni.combine().all().unis(
        createFormDocs(conn, source),
        Uni.createFrom().voidItem()
    ).asTuple().onItem().transform(e -> {
      logger.ok(source);
      
      return source;
    });
  }
  
  
  
  private Uni<?> createFormDocs(io.vertx.mutiny.sqlclient.SqlConnection conn, SourceForms source) {
    final var sql = """
  INSERT INTO form_document(
    id,
    rev,
    created,
    updated,
    data,
    tenant_id
  ) 
  VALUES(
    $1,
    $2,
    $3,
    $4,
    $5,
    $6
  )
  ON CONFLICT (id) DO NOTHING
""";

    final var props = source.getFormDocument().stream()
      .map(doc -> Tuple.from(Arrays.asList(
        doc.getId(), 
        doc.getRev(),
        doc.getCreated(),
        doc.getUpdated(),
        doc.getData().orElse(null),
        doc.getTenant_id()
      )))
      .collect(Collectors.toList());
   
    return batch(conn, SourceFormDocument.class, sql, props);
  }
  
  
  private <T> Uni<RowSet<Row>> batch(
      io.vertx.mutiny.sqlclient.SqlConnection conn,
      Class<T> type,
      String sql, 
      List<Tuple> props
  ) {
    
    final EntityQueryLogger<T> logger = this.logger.entityQuery(type).query(sql, props);
    return conn.preparedQuery(sql).executeBatch(props)
      .onItem().invoke(data -> logger.queryOk(data))
      .onFailure().invoke(e -> logger.queryFail(e));
  }

}
