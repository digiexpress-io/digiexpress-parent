package io.digiexpress.mig.client.spi;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.digiexpress.mig.client.api.MigClient.TargetDialobBuilder;
import io.digiexpress.mig.client.api.SourceForms;
import io.digiexpress.mig.client.api.SourceForms.SourceForm;
import io.digiexpress.mig.client.api.SourceForms.SourceFormDocument;
import io.digiexpress.mig.client.api.SourceForms.SourceFormRev;
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
        createForms(conn, source),
        createFormRevs(conn, source),
        createQuestionnaires(conn, source)
    ).asTuple().onItem().transform(e -> {
      logger.ok(source);
      
      return source;
    });
  }
  
  private Uni<?> createQuestionnaires(io.vertx.mutiny.sqlclient.SqlConnection conn, SourceForms source) {
    final var sql = """
  INSERT INTO questionnaire(
    id,
    rev,
    created,
    updated,
    data,
    status,
    form_document_id,
    tenant_id,
    owner
  ) 
  VALUES(
    $1,
    $2,
    $3,
    $4,
    $5,
    $6,
    $7,
    $8,
    $9
  )
  ON CONFLICT (id) DO NOTHING
""";

    final var props = source.getQuestionnaires().stream()
      .map(doc -> Tuple.from(Arrays.asList(
        doc.getId(),
        doc.getRev(),
        doc.getCreated(),
        doc.getUpdated(),
        doc.getData().orElse(null),
        doc.getStatus().orElse(null),
        doc.getForm_document_id(),
        doc.getTenant_id(),
        doc.getOwner().orElse(null)
      )))
      .collect(Collectors.toList());
   
    return batch(conn, SourceFormRev.class, sql, props);
  }
  
  private Uni<?> createFormRevs(io.vertx.mutiny.sqlclient.SqlConnection conn, SourceForms source) {
    final var sql = """
  INSERT INTO form_rev(
    form_name,
    name,
    created,
    updated,
    form_document_id,
    tenant_id,
    description,
    type,
    ref_name
  ) 
  VALUES(
    $1,
    $2,
    $3,
    $4,
    $5,
    $6,
    $7,
    $8,
    $9
  )
  ON CONFLICT (tenant_id, form_name, name) DO NOTHING
""";

    final var props = source.getFormRev().stream()
      .map(doc -> Tuple.from(Arrays.asList(
        doc.getForm_name(),
        doc.getName(),
        doc.getCreated(),
        doc.getUpdated(),
        doc.getForm_document_id(),
        doc.getTenant_id(),
        doc.getDescription().orElse(null),
        doc.getType().orElse(null),
        doc.getRef_name().orElse(null)
      )))
      .collect(Collectors.toList());
   
    return batch(conn, SourceFormRev.class, sql, props);
  }
  
  
  private Uni<?> createForms(io.vertx.mutiny.sqlclient.SqlConnection conn, SourceForms source) {
    final var sql = """
  INSERT INTO form(
    name,
    created,
    updated,
    latest_form_id,
    label,
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
  ON CONFLICT (tenant_id, name) DO NOTHING
""";

    final var props = source.getForms().stream()
      .map(doc -> Tuple.from(Arrays.asList(
        doc.getName(), 
        doc.getCreated(),
        doc.getUpdated(),
        doc.getLatest_form_id(),
        doc.getLabel().orElse(null),
        doc.getTenant_id()
      )))
      .collect(Collectors.toList());
   
    return batch(conn, SourceForm.class, sql, props);
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
