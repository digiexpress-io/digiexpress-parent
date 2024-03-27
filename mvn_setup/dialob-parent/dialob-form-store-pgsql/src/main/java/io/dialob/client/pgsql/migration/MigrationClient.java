package io.dialob.client.pgsql.migration;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.dialob.client.api.DialobDocument.FormReleaseDocument;
import io.dialob.client.api.DialobDocument.FormRevisionDocument;
import io.dialob.client.spi.DialobTypesMapperImpl;
import io.dialob.client.spi.migration.MigrationSupport;
import io.dialob.client.spi.migration.MigrationSupport.Migration;
import io.resys.thena.storesql.PgErrors;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * connect manually and upload db dump:  database, user, host
 * in terminal: psql -p 5432 -d postgres -U postgres -h localhost -f dbdump.sql
 */
@RequiredArgsConstructor
@Slf4j
public class MigrationClient {
  private final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
  private final io.vertx.mutiny.sqlclient.Pool pool;
  private static final Map<String, String> PROPS = new HashMap<>();
  private static final DialobTypesMapperImpl MAPPER;

  static {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new GuavaModule());
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.registerModule(new Jdk8Module());
    MAPPER = new DialobTypesMapperImpl(objectMapper);
  }


  @Data @Builder
  private static class Sql {
    private String id;
    private String value;
  }

  private void setupTables(String ...versions) {
    try {
      for(final var version : versions) {
        final List<Sql> sqls = new ArrayList<>();
        final var resource = resolver.getResource("classpath:migration_postgresql/" + version);

        int index = 1;

        final var content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        final var splitOn = content.indexOf("@") > 1 ? "@" : ";";
        for(final var statement : content.split(splitOn)) {
          if(statement.trim().isEmpty()) {
            continue;
          }
          final var sql = Sql.builder().id(resource.getFilename().substring(0, 4) +  ": " + index++).value(statement).build();

          sqls.add(sql);
        }


        for(final var sql : sqls) {
          pool.
          preparedQuery(sql.getValue()).execute()
              .onItem().transformToUni(data -> Uni.createFrom().voidItem())
              .onFailure().invoke(e -> PgErrors.deadEnd("Can't migrate: " + sql.getId() + " sql:'" + sql.getValue() + "'!", e))
              .await().atMost(Duration.ofSeconds(5));
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public static Map<String, String> withProfile(Map<String, String> props) {
    PROPS.putAll(props);
    return PROPS;
  }

  public void createDB() {
    setupTables(
        "V1_0__init.sql",
        "V1_1__label.sql",
        "V1_2__tenant.sql",
        "V1_3__delete-label.sql",
        "V1_4__questionnaire_owner.sql",
        "V1_5__raise_form_name_length_to_128.sql",
        "V1_6__add_form_name_view.sql",
        "V1_7__add_tag_comment_field.sql",
        "V1_8__add_tag_type_field.sql",
        "V1_9__extend_owner_column_to_64.sql"
    );
    log.error(
        "DB created." + System.lineSeparator() +
        "  - Use terminal to import dump." + System.lineSeparator() +
        "  - In terminal: psql -p 5432 -d postgres -U postgres -h localhost -f dbdump.sql"
    );
  }


  public DialobTypesMapperImpl getMapper() {
    return MAPPER;
  }

  public Migration getRelease(String tenant) {
    final var mapper = getMapper();
    final var visitor = new MigrationVisitor(mapper);

    pool.preparedQuery(
        "SELECT" +
        " name, label, created, updated, latest_form_id" +
        " FROM form" +
        " where tenant_id = $1 " +
        " ORDER BY name, created ")
    .execute(Tuple.of(tenant))
    .onItem().transform(rowset -> {
      final var  revs = new ArrayList<FormRevisionDocument>();
      final var iterator = rowset.iterator();
      while(iterator.hasNext()) {
        visitor.visitForm(iterator.next());
      }

      return revs;
    }).await().atMost(Duration.ofSeconds(5));

    pool.preparedQuery(
        "SELECT" +
        " form_name, name, created, updated, form_document_id, description" +
        " FROM form_rev" +
        " where tenant_id = $1 " +
        " ORDER BY form_name, created ")
    .execute(Tuple.of(tenant))
    .onItem().transform(rowset -> {
      final var  revs = new ArrayList<FormRevisionDocument>();
      final var iterator = rowset.iterator();
      while(iterator.hasNext()) {
        visitor.visitFormRev(iterator.next());
      }

      return revs;
    }).await().atMost(Duration.ofSeconds(5));


    pool.preparedQuery(
        "SELECT" +
        " id, rev, created, updated, CAST(data AS TEXT)" +
        " FROM form_document" +
        " where tenant_id = $1 " +
        " ORDER BY id, created ")
    .execute(Tuple.of(tenant))
    .onItem().transform(rowset -> {
      final var  revs = new ArrayList<FormRevisionDocument>();
      final var iterator = rowset.iterator();
      while(iterator.hasNext()) {
        visitor.visitFormDocument(iterator.next());
      }

      return revs;
    }).await().atMost(Duration.ofSeconds(30));


    return visitor.build();
  }

  public void write(Migration migration, OutputStream output) {
    new MigrationSupport(MAPPER).write(migration, output);
  }

  public FormReleaseDocument read(InputStream input) {
    return new MigrationSupport(MAPPER).read(input).getRelease();
  }

}
