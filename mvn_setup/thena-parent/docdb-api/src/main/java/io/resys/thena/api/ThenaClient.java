package io.resys.thena.api;

import io.resys.thena.api.actions.BranchActions;
import io.resys.thena.api.actions.CommitActions;
import io.resys.thena.api.actions.DiffActions;
import io.resys.thena.api.actions.DocCommitActions;
import io.resys.thena.api.actions.DocQueryActions;
import io.resys.thena.api.actions.HistoryActions;
import io.resys.thena.api.actions.OrgCommitActions;
import io.resys.thena.api.actions.OrgHistoryActions;
import io.resys.thena.api.actions.OrgQueryActions;
import io.resys.thena.api.actions.PullActions;
import io.resys.thena.api.actions.TagActions;
import io.resys.thena.api.actions.TenantActions;
import io.resys.thena.api.actions.TenantActions.RepoResult;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.doc.ThenaDocObjects.DocProjectObjects;
import io.resys.thena.api.entities.git.ThenaGitObjects.GitRepoObjects;
import io.resys.thena.api.entities.org.ThenaOrgObjects.OrgProjectObjects;
import io.resys.thena.api.models.QueryEnvelope;
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

    // build world state
    interface OrgProjectQuery {
      Uni<QueryEnvelope<OrgProjectObjects>> get();
    }
  }


  // single document model
  interface DocStructuredTenant {
    DocCommitActions commit();
    DocQueryActions find();
    

    // build world state
    interface DocProjectQuery {
      DocProjectQuery projectName(String projectName);
      Uni<QueryEnvelope<DocProjectObjects>> get();
    }
  }

  
  // multi doc model, cropped git replica
  interface GitStructuredTenant {
    CommitActions commit();
    TagActions tag();
    DiffActions diff();
    HistoryActions history();
    PullActions pull();
    BranchActions branch();
    GitRepoQuery project();
    
    // build world state
    interface GitRepoQuery {
      Uni<QueryEnvelope<GitRepoObjects>> get();
    }

  }
}