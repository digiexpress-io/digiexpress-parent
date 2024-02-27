package io.resys.thena.docdb.store.sql.queries;


import io.resys.thena.docdb.models.org.OrgInserts;
import io.resys.thena.docdb.store.sql.SqlBuilder;
import io.resys.thena.docdb.store.sql.SqlMapper;
import io.resys.thena.docdb.store.sql.support.SqlClientWrapper;
import io.resys.thena.docdb.support.ErrorHandler;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OrgDbInsertsSqlPool implements OrgInserts {
  private final SqlClientWrapper wrapper;
  private final SqlMapper sqlMapper;
  private final SqlBuilder sqlBuilder;
  private final ErrorHandler errorHandler;
  @Override
  public Uni<OrgBatchForOne> batchOne(OrgBatchForOne output) {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public Uni<OrgBatchForMany> batchMany(OrgBatchForMany output) {
    // TODO Auto-generated method stub
    return null;
  }

}
