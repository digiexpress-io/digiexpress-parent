//package io.resys.thena.api.registry.org;
//
//import java.util.Collection;
//import java.util.List;
//import java.util.function.Function;
//
//import javax.annotation.Nullable;
//
//import io.resys.thena.api.actions.GitPullActions.MatchCriteria;
//import io.resys.thena.api.entities.git.Blob;
//import io.resys.thena.api.entities.git.BlobHistory;
//import io.resys.thena.api.registry.ThenaRegistryService;
//import io.resys.thena.datasource.SqlQueryBuilder.Sql;
//import io.resys.thena.datasource.SqlQueryBuilder.SqlTuple;
//import io.resys.thena.datasource.SqlQueryBuilder.SqlTupleList;
//
//
//public interface BlobRegistry extends ThenaRegistryService<X, io.vertx.mutiny.sqlclient.Row> {
//  
//  Sql createTable();
//  Sql createConstraints();
//  Sql dropTable();
//  
//  Function<io.vertx.mutiny.sqlclient.Row, X> defaultMapper();
//  
//  Function<io.vertx.mutiny.sqlclient.Row, BlobHistory> historyMapper();
//}