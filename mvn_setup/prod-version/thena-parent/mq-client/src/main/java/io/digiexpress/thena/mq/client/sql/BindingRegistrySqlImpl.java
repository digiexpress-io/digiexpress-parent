package io.digiexpress.thena.mq.client.sql;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.digiexpress.thena.mq.client.api.entities.Binding;
import io.digiexpress.thena.mq.client.api.entities.Binding.BindingStatus;
import io.digiexpress.thena.mq.client.api.entities.ImmutableBinding;
import io.digiexpress.thena.mq.client.api.persistence.BindingRegistry;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqTableNames;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ImmutableSqlTupleList;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
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
  public ThenaSqlClient.SqlTuple getByIdOrName(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getBindings()).ln()
        .append("  WHERE (id = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }

  @Override
  public SqlTupleList updateMany(List<Binding> docs) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getBindings())
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
  public ThenaSqlClient.SqlTupleList insertMany(List<Binding> docs) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getBindings())
        .append(" (id, queue_id, message_id, status, comment, created_by, created_at)").ln()
        .append(" VALUES($1, $2, $3, $4, $5, $6, $7)").ln()
        .build())
        .props(docs.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getQueueId(),
                doc.getMessageId(),
                doc.getStatus().name(),
                
                doc.getComment(),
                doc.getCreatedBy(), 
                
                doc.getCreatedAt()
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
        .append("  queue_id       VARCHAR(100) NOT NULL,").ln()
        .append("  message_id     VARCHAR(100) NOT NULL,").ln()
        .append("  status         VARCHAR(100) NOT NULL,").ln()
        
        .append("  comment        TEXT NOT NULL,").ln()
        .append("  created_by     TEXT NOT NULL,").ln()
        .append("  updated_at     TIMESTAMP WITH TIME ZONE,").ln()
        .append("  created_at     TIMESTAMP WITH TIME ZONE NOT NULL").ln()


        .append(");").ln()

        .append("CREATE INDEX IF NOT EXISTS ").append(options.getBindings()).append("_QUEUE_INDEX")
        .append(" ON ").append(options.getBindings()).append(" (queue_id);").ln()
        
        .append("CREATE INDEX IF NOT EXISTS ").append(options.getBindings()).append("_MSG_INDEX")
        .append(" ON ").append(options.getBindings()).append(" (message_id);").ln()
        
        .append("CREATE INDEX IF NOT EXISTS ").append(options.getBindings()).append("_STATUS_INDEX")
        .append(" ON ").append(options.getBindings()).append(" (status);").ln()

        .build()).build();
  }
  
  @Override
  public ThenaSqlClient.Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        .ln().append("--- constraints for ").append(options.getBindings()).ln()
        
        .append("ALTER TABLE ").append(options.getBindings()).ln()
        .append("  ADD CONSTRAINT ").append(options.getBindings()).append("_MSG_FK").ln()
        .append("  FOREIGN KEY (message_id)").ln()
        .append("  REFERENCES ").append(options.getMessages()).append(" (id);").ln()
        
        .append("ALTER TABLE ").append(options.getBindings()).ln()
        .append("  ADD CONSTRAINT ").append(options.getBindings()).append("_QUEUE_FK").ln()
        .append("  FOREIGN KEY (queue_id)").ln()
        .append("  REFERENCES ").append(options.getQueues()).append(" (id);").ln()
        
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
          .messageId(row.getString("message_id"))
          .queueId(row.getString("queue_id"))
          .status(BindingStatus.valueOf(row.getString("status")))
          .createdBy(row.getString("created_by"))
          .createdAt(row.getOffsetDateTime("created_at"))
          .updatedAt(row.getOffsetDateTime("updated_at"))
          .comment(row.getString("comment"))
          .build()
    ;
  }
}
