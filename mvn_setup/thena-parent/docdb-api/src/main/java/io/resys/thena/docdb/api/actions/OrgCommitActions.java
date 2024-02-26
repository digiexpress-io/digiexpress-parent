package io.resys.thena.docdb.api.actions;

import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.resys.thena.docdb.api.actions.CommitActions.CommitResultStatus;
import io.resys.thena.docdb.api.models.Message;
import io.resys.thena.docdb.api.models.ThenaEnvelope;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroup;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRole;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUser;
import io.smallrye.mutiny.Uni;

public interface OrgCommitActions {
  
  CreateOneUser createOneUser();
  CreateOneGroup createOneGroup();
  
  ModifyOneGroup createModifyOneGroup();

  
  
  interface CreateOneUser {
    CreateOneUser repoId(String repoId);
    CreateOneUser author(String author);
    CreateOneUser message(String message);
    
    // TODO
    
    Uni<OneUserEnvelope> build();
  }
  
  
  interface CreateOneRole {
    CreateOneRole repoId(String repoId);
    CreateOneRole author(String author);
    CreateOneRole message(String message);
    
    // TODO
    
    Uni<OneRoleEnvelope> build();
  }
  
  

  /*
   * Create doc and doc by: One/Many
   */
  interface CreateOneGroup {
    CreateOneGroup repoId(String repoId);
    CreateOneGroup parentId(@Nullable String parentId);

    CreateOneGroup author(String author);
    CreateOneGroup message(String message);
    
    // TODO
    

    Uni<OneGroupEnvelope> build();
  }
  
  interface ModifyOneGroup {
    ModifyOneGroup repoId(String repoId);
    ModifyOneGroup groupId(String groupId);
    ModifyOneGroup parentId(@Nullable String parentId);
    
    ModifyOneGroup author(String author);
    ModifyOneGroup message(String message);
    
    // TODO
    
    
    Uni<OneGroupEnvelope> build();
  }

  
  
  
  
  
  
  @Value.Immutable
  interface OneRoleEnvelope extends ThenaEnvelope {
    String getRepoId();
    
    @Nullable OrgRole getRole();
    
    
    CommitResultStatus getStatus();
    List<Message> getMessages();
  }
  
  
  @Value.Immutable
  interface OneUserEnvelope extends ThenaEnvelope {
    String getRepoId();
    
    @Nullable OrgUser getUser();
    
    
    CommitResultStatus getStatus();
    List<Message> getMessages();
  }
  
  @Value.Immutable
  interface OneGroupEnvelope extends ThenaEnvelope {
    String getRepoId();
    
    @Nullable OrgGroup getGroup();
    
    
    CommitResultStatus getStatus();
    List<Message> getMessages();
  }
}
