package io.digiexpress.thena.mq.client.sql;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.digiexpress.thena.mq.client.api.entities.ImmutableQueueMessage;
import io.digiexpress.thena.mq.client.api.entities.QueueMessage;
import io.digiexpress.thena.mq.client.api.entities.QueueMessage.QueueMessageStatus;
import io.digiexpress.thena.mq.client.api.persistence.MessageRegistry;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqTableNames;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ImmutableSqlTupleList;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class MessageRegistrySqlImpl implements MessageRegistry {
  private final ThenaMqTableNames options;
  
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getMessages())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getByIdOrName(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getMessages()).ln()
        .append("  WHERE (id = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }

  @Override
  public SqlTuple findAllByStatus(QueueMessageStatus status, boolean lockForUpdate) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT messages.* ").ln()
        .append("FROM ").append(options.getMessages()).append(" AS messages ").ln()
        .append("WHERE messages.status = $1 ").ln()
        .append(lockForUpdate ? "FOR UPDATE" : "").ln()  //FOR UPDATE NOWAIT
        
        .build())
        .props(Tuple.of(status))
        .build();
  }
  @Override
  public SqlTupleList updateMany(List<QueueMessage> docs) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getMessages())
        .append(" SET status = $1, updated_at = $2")
        .append(" WHERE id = $3")
        .build())
        .props(docs.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getStatus().name(), 
                doc.getUpdatedAt(),
                doc.getId() 
             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTupleList insertMany(List<QueueMessage> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getMessages())
        .append(" (id, routing_key, status, comment, created_by, created_at, expires_at, starts_at, body_id, body_type, body_value)").ln()
        .append(" VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)").ln()
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getRoutingKey(), 
                doc.getStatus().name(),
                
                doc.getComment(),
                doc.getCreatedBy(), 
                
                doc.getCreatedAt(),
                doc.getExpiresAt(),
                doc.getStartsAt(),
              
                doc.getBodyId(),
                doc.getBodyType(),
                doc.getBodyValue(),
                
             }))
            .collect(Collectors.toList()))
        .build();
  }


  @Override
  public ThenaSqlClient.Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("CREATE TABLE IF NOT EXISTS ").append(options.getMessages()).ln()
        .append("(").ln()
        .append("  id             VARCHAR(40) PRIMARY KEY,").ln()
        .append("  routing_key    TEXT NOT NULL,").ln()
        .append("  routing_log    JSONB,").ln()
        .append("  status         VARCHAR(100) NOT NULL,").ln()
        
        .append("  comment        TEXT NOT NULL,").ln()
        .append("  created_by     TEXT NOT NULL,").ln()
        
        .append("  created_at     TIMESTAMP WITH TIME ZONE NOT NULL,").ln()
        .append("  updated_at     TIMESTAMP WITH TIME ZONE,").ln()
        .append("  expires_at     TIMESTAMP WITH TIME ZONE NOT NULL,").ln()
        .append("  starts_at      TIMESTAMP WITH TIME ZONE NOT NULL,").ln()

        .append("  body_id        TEXT NOT NULL,").ln()
        .append("  body_type      TEXT NOT NULL,").ln()
        .append("  body_value     JSONB NOT NULL").ln()

        .append(");").ln()

        .append("CREATE INDEX IF NOT EXISTS ").append(options.getMessages()).append("_ROUTING_INDEX")
        .append(" ON ").append(options.getMessages()).append(" (routing_key);").ln()
        
        .append("CREATE INDEX IF NOT EXISTS ").append(options.getMessages()).append("_BTYPE_INDEX")
        .append(" ON ").append(options.getMessages()).append(" (body_type);").ln()
        
        .append("CREATE INDEX IF NOT EXISTS ").append(options.getMessages()).append("_BID_INDEX")
        .append(" ON ").append(options.getMessages()).append(" (body_id);").ln()

        .build()).build();
  }
  
  @Override
  public ThenaSqlClient.Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        .ln().append("--- constraints for ").append(options.getMessages()).ln()
        
        .build())
        .build();
  }

  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getMessages()).append(";").ln()
        .build()).build();
  }

  @Override
  public Function<Row, QueueMessage> defaultMapper() {
    return row -> ImmutableQueueMessage.builder()
          .id(row.getString("id"))
          
          .routingKey(row.getString("routing_key"))
          .status(QueueMessageStatus.valueOf(row.getString("status")))

          .comment(row.getString("comment"))
          .createdBy(row.getString("created_by"))
          .createdAt(row.getOffsetDateTime("created_at"))
          .updatedAt(row.getOffsetDateTime("updated_at"))
          
          .expiresAt(row.getOffsetDateTime("expires_at"))
          .startsAt(row.getOffsetDateTime("starts_at"))
          
          .bodyId(row.getString("body_id"))
          .bodyType(row.getString("body_type"))
          .bodyValue(row.getJsonObject("body_value"))
          
          .build()
    ;
  }
}
