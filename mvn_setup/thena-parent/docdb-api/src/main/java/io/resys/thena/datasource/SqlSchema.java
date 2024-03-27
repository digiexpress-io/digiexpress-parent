package io.resys.thena.datasource;

import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.spi.DbCollections;

public interface SqlSchema extends DbCollections.WithOptions<SqlSchema>{
  SqlSchema withOptions(DbCollections options);
  

  // create git like db model
  Sql createGitBlobs();

  Sql createGitCommits();
  Sql createGitCommitsConstraints();
  
  Sql createGitTreeItemsConstraints();
  Sql createGitTreeItems();

  Sql createGitTrees();

  Sql createGitRefs();
  Sql createGitRefsConstraints();

  Sql createGitTags();
  Sql createGitTagsConstraints();
  
  Sql dropGitBlobs();
  Sql dropGitCommits();
  Sql dropGitTreeItems();
  Sql dropGitTrees();
  Sql dropGitRefs();
  Sql dropGitTags();
  
  
  
  // single doc db model
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
  Sql createRepo();
  Sql dropRepo();


  
}
