package io.resys.thena.api.registry.org;

import java.util.Collection;
import java.util.function.Function;

import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgMemberFlattened;
import io.resys.thena.api.entities.org.OrgMemberHierarchyEntry;
import io.resys.thena.api.entities.org.OrgRightFlattened;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface OrgMemberRegistry extends ThenaRegistryService<OrgMember, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.SqlTuple getById(String id); //username or id or external_id
  ThenaSqlClient.SqlTuple findAllUserPartiesAndRightsByMemberId(String userId);
  ThenaSqlClient.SqlTuple findAllRightsByMemberId(String userId);
  ThenaSqlClient.SqlTuple getStatusByUserId(String userId);
  
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTuple findAll(Collection<String> id);
  ThenaSqlClient.SqlTuple findAllByPartyId(String partyId);
  ThenaSqlClient.SqlTuple insertOne(OrgMember user);
  ThenaSqlClient.SqlTupleList insertAll(Collection<OrgMember> users);
  ThenaSqlClient.SqlTuple updateOne(OrgMember user);
  ThenaSqlClient.SqlTupleList updateMany(Collection<OrgMember> users);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, OrgMember> defaultMapper();
  Function<io.vertx.mutiny.sqlclient.Row, OrgRightFlattened> rightFlattenedMapper();
  Function<io.vertx.mutiny.sqlclient.Row, OrgMemberFlattened> memberFlattenedMapper();
  Function<io.vertx.mutiny.sqlclient.Row, OrgMemberHierarchyEntry> memberHierarchyEntryMapper();
  
}