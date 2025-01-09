package io.digiexpress.thena.mq.client.sql;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.digiexpress.thena.mq.client.api.entities.Channel;
import io.digiexpress.thena.mq.client.api.entities.ImmutableChannel;
import io.digiexpress.thena.mq.client.api.persistence.ChannelRegistry;
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
public class ChannelRegistrySqlImpl implements ChannelRegistry {
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
        .append("  FROM ").append(options.getChannel()).ln()
        .append("  WHERE (id = $1 OR external_id = $1 OR channel_name = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple insertOne(Channel doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getChannel())
        .append(" (id, prefix, created_by, created_at, comment, channel_name, external_id)").ln()
        .append(" VALUES($1, $2, $3, $4, $5, $6, $7)").ln()
        .build())
        .props(Tuple.from(new Object[]{ 
            doc.getId(), 
            doc.getPrefix(), 
            doc.getCreatedBy(), 
            doc.getCreatedAt(), 
            doc.getComment(), 
            doc.getChannelName(), 
            doc.getExternalId() 
         }))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTupleList updateMany(List<Channel> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getChannel())
        .append(" SET comment = $1, channel_name = $2, external_id = $3 ")
        .append(" WHERE id = $4")
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getComment(), 
                doc.getChannelName(), 
                doc.getExternalId(), 
                doc.getId() 
             }))
            .collect(Collectors.toList()))
        .build();
  }

  @Override
  public ThenaSqlClient.Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("CREATE TABLE IF NOT EXISTS ").append(options.getChannel()).ln()
        .append("(").ln()
        .append("  id             VARCHAR(40) PRIMARY KEY,").ln()
        .append("  prefix         VARCHAR(40) NOT NULL,").ln()
        .append("  created_at     TIMESTAMP WITH TIME ZONE NOT NULL,").ln()
        
        .append("  created_by     TEXT NOT NULL,").ln()
        .append("  comment        TEXT NOT NULL,").ln()
        .append("  channel_name   TEXT NOT NULL,").ln()
        .append("  external_id    TEXT,").ln()

        .append("  UNIQUE(channel_name), UNIQUE(prefix), UNIQUE(external_id)").ln()
        .append(");").ln()

        .append("CREATE INDEX IF NOT EXISTS ").append(options.getChannel()).append("_NAME_INDEX")
        .append(" ON ").append(options.getChannel()).append(" (channel_name);").ln()
        .append("CREATE INDEX IF NOT EXISTS ").append(options.getChannel()).append("_EXT_INDEX")
        .append(" ON ").append(options.getChannel()).append(" (external_id);").ln()
        
        .build()).build();
  }
  
  @Override
  public SqlTuple deleteById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("DELETE FROM ").append(options.getChannel())
        .append(" WHERE id = $1")
        .build())
        .props(Tuple.of(id))
        .build();
  }

  @Override
  public ThenaSqlClient.Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        .ln().append("--- constraints for ").append(options.getChannel()).ln()
        .build())
        .build();
  }

  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getChannel()).append(";").ln()
        .build()).build();
  }

  @Override
  public Function<Row, Channel> defaultMapper() {
    return row -> ImmutableChannel.builder()
        .id(row.getString("id"))
        .prefix(row.getString("prefix"))
        .createdAt(row.getOffsetDateTime("created_at"))
        .createdBy(row.getString("created_by"))
        .comment(row.getString("comment"))
        .channelName(row.getString("channel_name"))
        .externalId(row.getString("external_id"))
        .build();
  }
}
