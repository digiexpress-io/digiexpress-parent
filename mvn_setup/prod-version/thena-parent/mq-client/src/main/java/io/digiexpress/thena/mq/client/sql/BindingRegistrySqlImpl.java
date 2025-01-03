package io.digiexpress.thena.mq.client.sql;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.digiexpress.thena.mq.client.api.entities.Binding;
import io.digiexpress.thena.mq.client.api.entities.ImmutableBinding;
import io.digiexpress.thena.mq.client.api.persistence.BindingRegistry;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqTableNames;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ImmutableSqlTupleList;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BindingRegistrySqlImpl implements BindingRegistry {
  private final ThenaMqTableNames options;
  
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getBindings())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getBindings()).ln()
        .append("  WHERE id = $1").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTupleList insertMany(List<Binding> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getBindings())
        .append(" (id, message_id, created_at, source_id, queue_name, routing_key, address_name)").ln()
        .append(" VALUES($1, $2, $3, $4, $5, $6, $7)").ln()
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getMessageId(), 
                doc.getCreatedAt(), 
                doc.getSourceId(), 
                doc.getQueueName(), 
                doc.getRoutingKey(), 
                doc.getAddressName()
             }))
            .collect(Collectors.toList()))
        .build();
  }

  @Override
  public ThenaSqlClient.Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("CREATE TABLE IF NOT EXISTS ").append(options.getBindings()).ln()
        .append("(").ln()
        .append("  id             VARCHAR(40) PRIMARY KEY,").ln()
        .append("  message_id     VARCHAR(40) NOT NULL,").ln()
        .append("  created_at     TIMESTAMP WITH TIME ZONE NOT NULL,").ln()
        .append("  source_id      VARCHAR(40) NOT NULL,").ln()
        .append("  queue_name     TEXT NOT NULL,").ln()
        .append("  routing_key    TEXT NOT NULL,").ln()
        .append("  address_name   TEXT NOT NULL").ln()
        
        .append(");").ln()
        
        .append("CREATE INDEX IF NOT EXISTS ").append(options.getBindings()).append("_MSG_INDEX")
        .append(" ON ").append(options.getBindings()).append(" (message_id);").ln()

        .append("CREATE INDEX IF NOT EXISTS ").append(options.getBindings()).append("_SRC_INDEX")
        .append(" ON ").append(options.getBindings()).append(" (source_id);").ln()
        
        .append("CREATE INDEX IF NOT EXISTS ").append(options.getBindings()).append("_QUEUE_INDEX")
        .append(" ON ").append(options.getBindings()).append(" (queue_name);").ln()
        
        .append("CREATE INDEX IF NOT EXISTS ").append(options.getBindings()).append("_ROUTING_INDEX")
        .append(" ON ").append(options.getBindings()).append(" (routing_key);").ln()
        
        .build()).build();
  }
  
  @Override
  public SqlTuple deleteById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("DELETE FROM ").append(options.getBindings())
        .append(" WHERE id = $1")
        .build())
        .props(Tuple.of(id))
        .build();
  }

  @Override
  public ThenaSqlClient.Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        .ln().append("--- constraints for ").append(options.getBindings()).ln()
        
        .append("ALTER TABLE ").append(options.getBindings()).ln()
        .append("  ADD CONSTRAINT ").append(options.getBindings()).append("_MSG_FK").ln()
        .append("  FOREIGN KEY (message_id)").ln()
        .append("  REFERENCES ").append(options.getMessages()).append(" (id);").ln()
        
        
        .build())
        .build();
  }

  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getBindings()).append(";").ln()
        .build()).build();
  }

  @Override
  public Function<Row, Binding> defaultMapper() {
    return row -> ImmutableBinding.builder()
        .id(row.getString("id"))
        .sourceId(row.getString("source_id"))
        .createdAt(row.getOffsetDateTime("created_at"))
        .messageId(row.getString("message_id"))
        .queueName(row.getString("queue_name"))
        .routingKey(row.getString("routing_key"))
        .addressName(row.getString("address_name"))
        .build();
  }
}
