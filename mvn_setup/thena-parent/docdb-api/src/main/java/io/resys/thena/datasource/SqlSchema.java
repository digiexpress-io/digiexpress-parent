package io.resys.thena.datasource;

import io.resys.thena.datasource.SqlQueryBuilder.Sql;

public interface SqlSchema extends TenantTableNames.WithTenant<SqlSchema> {
  SqlSchema withTenant(TenantTableNames options);
  
  /* single doc db model
  Sql createDoc();
  
  Sql createDocBranch();
  Sql createDocBranchConstraints();
  
  Sql createDocCommits();
  Sql createDocCommitsConstraints();
  
  Sql createDocLog();
  Sql createDocLogConstraints();
  
  Sql dropDoc();
  Sql dropDocBranch();
  Sql dropDocCommit();
  Sql dropDocLog();
  */
  
  
  // organization model
  Sql createOrgRights();
  Sql createOrgParties();
  Sql createOrgPartyRights();
  
  Sql createOrgMembers();
  Sql createOrgMemberRights();
  Sql createOrgMemberships();

  Sql createOrgActorStatus();
  Sql createOrgCommits();
  Sql createOrgActorData();
  
  Sql createOrgRightsConstraints();
  Sql createOrgMemberConstraints();  
  Sql createOrgPartyConstraints();  
  Sql createOrgCommitConstraints();
  

  Sql dropOrgRights();
  Sql dropOrgParties();
  Sql dropOrgPartyRights();
  
  Sql dropOrgMembers();
  Sql dropOrgMemberRights();
  Sql dropOrgMemberships();

  Sql dropOrgActorStatus();
  Sql dropOrgActorLogs();
  Sql dropOrgActorData();

  
  
  
  // central tracker for tables
  Sql createTenant();
  Sql dropRepo();


  
}
