package io.digiexpress.mig.client.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import io.digiexpress.mig.client.api.ImmutableSourceDbDialob;
import io.digiexpress.mig.client.api.ImmutableSourceDbForm;
import io.digiexpress.mig.client.api.ImmutableSourceDbFormDocument;
import io.digiexpress.mig.client.api.ImmutableSourceDbFormRev;
import io.digiexpress.mig.client.api.ImmutableSourceDbQuestionnaire;
import io.digiexpress.mig.client.api.SourceDbClient.FormFilter;
import io.digiexpress.mig.client.api.SourceDbClient.SourceDbDialob;
import io.digiexpress.mig.client.api.SourceDbClient.SourceDbDialobQuery;
import io.digiexpress.mig.client.api.SourceDbClient.SourceDbForm;
import io.digiexpress.mig.client.api.SourceDbClient.SourceDbFormDocument;
import io.digiexpress.mig.client.api.SourceDbClient.SourceDbFormRev;
import io.digiexpress.mig.client.api.SourceDbClient.SourceDbQuestionnaire;
import io.digiexpress.mig.client.spi.loggers.SourceDbDialobQueryLogger;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SourceDbDialobQueryImpl implements SourceDbDialobQuery {
  private final SourceDbDialobQueryLogger logger = new SourceDbDialobQueryLogger();
  private final io.vertx.mutiny.pgclient.PgPool pool;
  private final List<String> onlyRelatedToQuestionnaires = new ArrayList<>();
  private final List<FormFilter> includeFrom = new ArrayList<>();
  
  @Override
  public Uni<SourceDbDialob> findAll() {
    return Uni.combine().all().unis(
        getForms(),
        getFormDocument(),
        getFormRev(),
        getQuestionnaires()
    ).asTuple().onItem().transform(sources -> {
      final SourceDbDialob result = filter(ImmutableSourceDbDialob.builder()
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
  public SourceDbDialobQuery includeFromQuestionnaires(List<String> questionnaires) {
    this.onlyRelatedToQuestionnaires.addAll(questionnaires);
    return this;
  }

  @Override
  public SourceDbDialobQuery includeFrom(List<? extends FormFilter> formMeta) {
    this.includeFrom.addAll(formMeta);
    return this;
  }
  
  private SourceDbDialob filter(SourceDbDialob dialob) {
    if(onlyRelatedToQuestionnaires.isEmpty() && includeFrom.isEmpty()) {
      return dialob;
    }
    
    return new SourceDbDialobQueryFilter(onlyRelatedToQuestionnaires, includeFrom, dialob, logger).apply();
  }

  private Uni<List<SourceDbForm>> getForms() {
    final var sql = "select * from form";
    return processAnyQuery(SourceDbForm.class, sql, row -> ImmutableSourceDbForm.builder()
        .name(row.getString("name"))
        .updated(row.getLocalDateTime("updated"))
        .created(row.getLocalDateTime("created"))
        .latest_form_id(row.getUUID("latest_form_id").toString())
        .label(Optional.ofNullable(row.getString("label")))
        .tenant_id(row.getUUID("tenant_id").toString())
        .build());
  }
  
  private Uni<List<SourceDbFormDocument>> getFormDocument() {
    final var sql = "select * from form_document";
    return processAnyQuery(SourceDbFormDocument.class, sql, row -> ImmutableSourceDbFormDocument.builder()
        .id(row.getUUID("id").toString())
        .rev(row.getInteger("rev"))
        .updated(row.getLocalDateTime("updated"))
        .created(row.getLocalDateTime("created"))
        .data(Optional.ofNullable(row.getJsonObject("data")))
        .tenant_id(row.getString("tenant_id"))
        .build());
  }
  private Uni<List<SourceDbFormRev>> getFormRev() {
    final var sql = "select * from form_rev";
    return processAnyQuery(SourceDbFormRev.class, sql, row -> ImmutableSourceDbFormRev.builder()
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
  private Uni<List<SourceDbQuestionnaire>> getQuestionnaires() {
    final var sql = "select * from questionnaire";
    return processAnyQuery(SourceDbQuestionnaire.class, sql, row -> ImmutableSourceDbQuestionnaire.builder()
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