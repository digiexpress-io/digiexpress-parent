package io.digiexpress.thena.mq.client.sql;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.digiexpress.thena.mq.client.api.entities.Delivery.DeliveryAckValue;
import io.digiexpress.thena.mq.client.api.entities.Delivery.DeliveryAttempt;
import io.digiexpress.thena.mq.client.api.entities.Delivery.DeliveryStatus;
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
  public ThenaSqlClient.SqlTuple getById(String id) {
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
        .append(" (id, delivery_id, delivery_status, created_at, updated_at, ack_comment, ack_error, ack_value)").ln()
        .append(" VALUES($1, $2, $3, $4, $5, $6, $7)").ln()
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getDeliveryId(), 
                doc.getDeliveryStatus().name(), 
                doc.getCreatedAt(), 
                doc.getUpdatedAt(), 
                doc.getAckComment(), 
                doc.getAckError(),
                doc.getAckValue()
             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTupleList updateMany(List<DeliveryAttempt> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getDeliveryAttempt())
        .append(" SET delivery_status = $1, updated_at = $2, ack_comment = $3, ack_error = $4, ack_value = $5 ")
        .append(" WHERE id = $6")
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getDeliveryStatus(), 
                doc.getUpdatedAt(), 
                doc.getAckComment(),
                doc.getAckError(),
                doc.getAckValue(),
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
        .append("  delivery_status  VARCHAR(100) NOT NULL,").ln()
        
        .append("  created_at   TIMESTAMP WITH TIME ZONE NOT NULL,").ln()
        .append("  updated_at   TIMESTAMP WITH TIME ZONE,").ln()
        
        .append("  ack_comment  TEXT,").ln()
        .append("  ack_error    JSONB,").ln()
        .append("  ack_value    VARCHAR(100)").ln()

        .append(");").ln()

        .append("CREATE INDEX IF NOT EXISTS ").append(options.getDeliveryAttempt()).append("_STATUS_INDEX")
        .append(" ON ").append(options.getDeliveryAttempt()).append(" (delivery_status);").ln()
        
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
        .deliveryStatus(DeliveryStatus.valueOf(row.getString("delivery_status")))
        .createdAt(row.getOffsetDateTime("created_at"))
        .updatedAt(row.getOffsetDateTime("updated_at"))
        
        .ackComment(row.getString("ack_comment"))
        .ackError(row.getJsonObject("ack_error"))
        .ackValue(DeliveryAckValue.valueOf(row.getString("ack_value")))
        .build();
  }
}
