package io.digiexpress.thena.mq.client.sql;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.digiexpress.thena.mq.client.api.ThenaMqConsumer.MessageResponseStatus;
import io.digiexpress.thena.mq.client.api.entities.Delivery.DeliveryAttempt;
import io.digiexpress.thena.mq.client.api.entities.ImmutableDeliveryAttempt;
import io.digiexpress.thena.mq.client.api.persistence.DeliveryAttemptRegistry;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqTableNames;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ImmutableSqlTupleList;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DeliveryAttemptRegistrySqlImpl implements DeliveryAttemptRegistry {
  private final ThenaMqTableNames options;
  
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getChannel())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getByIdOrName(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getDeliveryAttempt()).ln()
        .append("  WHERE id = $1").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTupleList insertMany(List<DeliveryAttempt> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getDeliveryAttempt())
        .append(" (id, delivery_id, created_at, consumer_comment, consumer_error, consumer_status)").ln()
        .append(" VALUES($1, $2, $3, $4, $5, $6, $7)").ln()
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getDeliveryId(), 
                doc.getCreatedAt(), 
                doc.getConsumerComment(), 
                doc.getConsumerError(),
                doc.getConsumerStatus()
             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTupleList updateMany(List<DeliveryAttempt> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getDeliveryAttempt())
        .append(" SET consumer_comment = $1, consumer_error = $2, consumer_status = $3 ")
        .append(" WHERE id = $4")
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getConsumerComment(),
                doc.getConsumerError(),
                doc.getConsumerStatus(),
                doc.getId() 
             }))
            .collect(Collectors.toList()))
        .build();
  }

  @Override
  public ThenaSqlClient.Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("CREATE TABLE IF NOT EXISTS ").append(options.getDeliveryAttempt()).ln()
        .append("(").ln()
        .append("  id               VARCHAR(40) PRIMARY KEY,").ln()
        .append("  delivery_id      VARCHAR(40) NOT NULL,").ln()
        
        .append("  created_at   TIMESTAMP WITH TIME ZONE NOT NULL,").ln()
        
        .append("  consumer_comment  TEXT,").ln()
        .append("  consumer_error    JSONB,").ln()
        .append("  consumer_status   VARCHAR(100)").ln()

        .append(");").ln()

        .append("CREATE INDEX IF NOT EXISTS ").append(options.getDeliveryAttempt()).append("_DEL_INDEX")
        .append(" ON ").append(options.getDeliveryAttempt()).append(" (delivery_id);").ln()
        
        .build()).build();
  }
  
  @Override
  public ThenaSqlClient.Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        .ln().append("--- constraints for ").append(options.getDeliveryAttempt()).ln()
        
        .append("ALTER TABLE ").append(options.getDeliveryAttempt()).ln()
        .append("  ADD CONSTRAINT ").append(options.getDeliveryAttempt()).append("_DELV_FK").ln()
        .append("  FOREIGN KEY (delivery_id)").ln()
        .append("  REFERENCES ").append(options.getDelivery()).append(" (id);").ln()
        
        
        .build())
        .build();
  }

  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getDeliveryAttempt()).append(";").ln()
        .build()).build();
  }

  @Override
  public Function<Row, DeliveryAttempt> defaultMapper() {
    return row -> ImmutableDeliveryAttempt.builder()
        .id(row.getString("id"))
        .deliveryId(row.getString("delivery_id"))
        .createdAt(row.getOffsetDateTime("created_at"))
        .consumerComment(row.getString("consumer_comment"))
        .consumerError(row.getJsonObject("consumer_error"))
        .consumerStatus(MessageResponseStatus.valueOf(row.getString("consumer_status")))
        .build();
  }
}
