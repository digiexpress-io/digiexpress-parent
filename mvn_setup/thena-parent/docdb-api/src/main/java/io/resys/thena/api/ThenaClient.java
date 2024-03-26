package io.resys.thena.api;

import java.util.Map;

import org.immutables.value.Value;

import io.resys.thena.api.actions.DocCommitActions;
import io.resys.thena.api.actions.DocQueryActions;
import io.resys.thena.api.actions.GitBranchActions;
import io.resys.thena.api.actions.GitCommitActions;
import io.resys.thena.api.actions.GitDiffActions;
import io.resys.thena.api.actions.GitHistoryActions;
import io.resys.thena.api.actions.GitPullActions;
import io.resys.thena.api.actions.GitTagActions;
import io.resys.thena.api.actions.OrgCommitActions;
import io.resys.thena.api.actions.OrgHistoryActions;
import io.resys.thena.api.actions.OrgQueryActions;
import io.resys.thena.api.actions.TenantActions;
import io.resys.thena.api.actions.TenantActions.RepoResult;
import io.resys.thena.api.entities.DocContainer;
import io.resys.thena.api.entities.GitContainer;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocEntity.IsDocObject;
import io.resys.thena.api.entities.git.Branch;
import io.resys.thena.api.entities.git.GitEntity.IsGitObject;
import io.resys.thena.api.entities.git.Tag;
import io.resys.thena.api.entities.org.ThenaOrgObjects.OrgProjectObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.smallrye.mutiny.Uni;

public interface ThenaClient {  
  TenantActions tenants();
  
  GitStructuredTenant git(String tenantIdOrName);
  GitStructuredTenant git(RepoResult repo);
  GitStructuredTenant git(Tenant repo);
  
  DocStructuredTenant doc(String tenantIdOrName);
  DocStructuredTenant doc(RepoResult repo);
  DocStructuredTenant doc(Tenant repo);
  
  OrgStructuredTenant org(String tenantIdOrName);
  OrgStructuredTenant org(RepoResult repo);
  OrgStructuredTenant org(Tenant repo);
  

  interface OrgStructuredTenant {
    OrgCommitActions commit();
    OrgQueryActions find();
    OrgHistoryActions history();
    OrgProjectQuery project();
  }
  // build world state
  interface OrgProjectQuery {
    Uni<QueryEnvelope<OrgProjectObjects>> get();
  }


  // single document model
  interface DocStructuredTenant {
    DocCommitActions commit();
    DocQueryActions find();
  }
  // build world state
  interface DocProjectQuery {
    DocProjectQuery projectName(String projectName);
    Uni<QueryEnvelope<ThenaClient.DocProjectObjects>> get();
  }
  @Value.Immutable
  interface DocProjectObjects extends DocContainer {
    Map<String, DocBranch> getBranches();
    Map<String, IsDocObject> getValues();   
  }
  
  // multi doc model, cropped git replica
  interface GitStructuredTenant {
    GitCommitActions commit();
    GitTagActions tag();
    GitDiffActions diff();
    GitHistoryActions history();
    GitPullActions pull();
    GitBranchActions branch();
    GitRepoQuery project();
  }
  
  // build world state
  interface GitRepoQuery {
    Uni<QueryEnvelope<ThenaClient.GitRepoObjects>> get();
  }

  @Value.Immutable 
  interface GitRepoObjects extends GitContainer {
    Map<String, Branch> getBranches();
    Map<String, Tag> getTags();
    Map<String, IsGitObject> getValues();   
  }
}