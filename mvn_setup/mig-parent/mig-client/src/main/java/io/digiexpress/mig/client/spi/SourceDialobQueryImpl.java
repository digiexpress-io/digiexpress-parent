package io.digiexpress.mig.client.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import io.digiexpress.mig.client.api.ImmutableSourceForm;
import io.digiexpress.mig.client.api.ImmutableSourceFormDocument;
import io.digiexpress.mig.client.api.ImmutableSourceFormRev;
import io.digiexpress.mig.client.api.ImmutableSourceForms;
import io.digiexpress.mig.client.api.ImmutableSourceQuestionnaire;
import io.digiexpress.mig.client.api.MigClient.FormFilter;
import io.digiexpress.mig.client.api.MigClient.SourceDialobQuery;
import io.digiexpress.mig.client.api.SourceForms;
import io.digiexpress.mig.client.api.SourceForms.SourceForm;
import io.digiexpress.mig.client.api.SourceForms.SourceFormDocument;
import io.digiexpress.mig.client.api.SourceForms.SourceFormRev;
import io.digiexpress.mig.client.api.SourceForms.SourceQuestionnaire;
import io.digiexpress.mig.client.spi.loggers.SourceDialobLogger;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SourceDialobQueryImpl implements SourceDialobQuery {
  private final SourceDialobLogger logger = new SourceDialobLogger();
  private final io.vertx.mutiny.pgclient.PgPool pool;
  private final List<String> onlyRelatedToQuestionnaires = new ArrayList<>();
  private final List<FormFilter> includeFrom = new ArrayList<>();
  
  @Override
  public Uni<SourceForms> findAll() {
    return Uni.combine().all().unis(
        getForms(),
        getFormDocument(),
        getFormRev(),
        getQuestionnaires()
    ).asTuple().onItem().transform(sources -> {
      final SourceForms result = filter(ImmutableSourceForms.builder()
          .forms(sources.getItem1())
          .formDocument(sources.getItem2())
          .formRev(sources.getItem3())
          .questionnaires(sources.getItem4())
          .build());
      logger.ok(result);
      return result;
    })
    .onFailure().invoke(e -> logger.fail(e));
  }
  
  @Override
  public SourceDialobQuery includeFromQuestionnaires(List<String> questionnaires) {
    this.onlyRelatedToQuestionnaires.addAll(questionnaires);
    return this;
  }

  @Override
  public SourceDialobQuery includeFrom(List<? extends FormFilter> formMeta) {
    this.includeFrom.addAll(formMeta);
    return this;
  }
  
  private SourceForms filter(SourceForms dialob) {
    if(onlyRelatedToQuestionnaires.isEmpty() && includeFrom.isEmpty()) {
      return dialob;
    }
    
    return new SourceDbDialobQueryFilter(onlyRelatedToQuestionnaires, includeFrom, dialob, logger).apply();
  }

  private Uni<List<SourceForm>> getForms() {
    final var sql = "select * from form";
    return processAnyQuery(SourceForm.class, sql, row -> ImmutableSourceForm.builder()
        .name(row.getString("name"))
        .updated(row.getLocalDateTime("updated"))
        .created(row.getLocalDateTime("created"))
        .latest_form_id(row.getUUID("latest_form_id").toString())
        .label(Optional.ofNullable(row.getString("label")))
        .tenant_id(row.getUUID("tenant_id").toString())
        .build());
  }
  
  private Uni<List<SourceFormDocument>> getFormDocument() {
    final var sql = "select * from form_document";
    return processAnyQuery(SourceFormDocument.class, sql, row -> ImmutableSourceFormDocument.builder()
        .id(row.getUUID("id").toString())
        .rev(row.getInteger("rev"))
        .updated(row.getLocalDateTime("updated"))
        .created(row.getLocalDateTime("created"))
        .data(Optional.ofNullable(row.getJsonObject("data")))
        .tenant_id(row.getString("tenant_id"))
        .build());
  }
  private Uni<List<SourceFormRev>> getFormRev() {
    final var sql = "select * from form_rev";
    return processAnyQuery(SourceFormRev.class, sql, row -> ImmutableSourceFormRev.builder()
        .form_name(row.getString("form_name"))
        .name(row.getString("name"))
        .updated(row.getLocalDateTime("updated"))
        .created(row.getLocalDateTime("created"))
        .form_document_id(row.getUUID("form_document_id").toString())
        .tenant_id(row.getString("tenant_id"))
        .description(Optional.ofNullable(row.getString("description")))
        .type(Optional.ofNullable(row.getString("type")))
        .ref_name(Optional.ofNullable(row.getString("ref_name")))
        .build());
  }
  private Uni<List<SourceQuestionnaire>> getQuestionnaires() {
    final var sql = "select * from questionnaire";
    return processAnyQuery(SourceQuestionnaire.class, sql, row -> ImmutableSourceQuestionnaire.builder()
        .id(row.getUUID("id").toString())
        .rev(row.getInteger("rev"))
        .updated(row.getLocalDateTime("updated"))
        .created(row.getLocalDateTime("created"))
        .data(Optional.ofNullable(row.getJsonObject("data")))
        .status(Optional.ofNullable(row.getString("status")))
        .form_document_id(row.getUUID("form_document_id").toString())
        .tenant_id(row.getString("tenant_id"))
        .owner(Optional.ofNullable(row.getString("owner")))
        .build());
  }
  
  private <T> Uni<List<T>> processAnyQuery(Class<T> type, String sql, Function<io.vertx.mutiny.sqlclient.Row, T> mapper) {
    final var logger = this.logger.entityQuery(type);
    logger.query(sql);
    return pool.preparedQuery(sql)
      .mapping(row -> {
        try {
          final T result = mapper.apply(row);
          logger.mappingOk(result);
          return result;
        } catch(Exception e) {
          logger.mappingFail(row, e);
          return null;
        }
      })
      .execute()
      .onItem()
      .transformToMulti(RowSet::toMulti).collect().asList()
      .onItem().transform(data -> data.stream().filter(e -> e != null).toList())
      .onItem().invoke(e -> logger.queryOk(e))
      .onFailure().invoke(e -> logger.queryFail(e));
  }
}