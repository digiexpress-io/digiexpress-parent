package io.resys.thena.api.registry.org;

import java.util.Collection;
import java.util.function.Function;

import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgMemberFlattened;
import io.resys.thena.api.entities.org.OrgMemberHierarchyEntry;
import io.resys.thena.api.entities.org.OrgRightFlattened;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTuple;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTupleList;


public interface OrgMemberRegistry extends ThenaRegistryService<OrgMember, io.vertx.mutiny.sqlclient.Row> {
  SqlTuple getById(String id); //username or id or external_id
  SqlTuple findAllUserPartiesAndRightsByMemberId(String userId);
  SqlTuple findAllRightsByMemberId(String userId);
  SqlTuple getStatusByUserId(String userId);
  
  Sql findAll();
  SqlTuple findAll(Collection<String> id);
  SqlTuple insertOne(OrgMember user);
  SqlTupleList insertAll(Collection<OrgMember> users);
  SqlTuple updateOne(OrgMember user);
  SqlTupleList updateMany(Collection<OrgMember> users);
  
  Sql createTable();
  Sql createConstraints();
  Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, OrgMember> defaultMapper();
  Function<io.vertx.mutiny.sqlclient.Row, OrgRightFlattened> rightFlattenedMapper();
  Function<io.vertx.mutiny.sqlclient.Row, OrgMemberFlattened> memberFlattenedMapper();
  Function<io.vertx.mutiny.sqlclient.Row, OrgMemberHierarchyEntry> memberHierarchyEntryMapper();
  
}