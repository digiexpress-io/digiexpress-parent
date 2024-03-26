package io.resys.permission.client.spi;

import io.resys.permission.client.api.PermissionClient.CreatePrincipalAction;
import io.resys.permission.client.api.model.ImmutablePrincipal;
import io.resys.permission.client.api.model.Principal;
import io.resys.permission.client.api.model.PrincipalCommand.CreatePrincipal;
import io.resys.permission.client.api.model.PrincipalCommand.PrincipalCommandType;
import io.resys.thena.docdb.api.actions.OrgCommitActions.CreateOneMember;
import io.resys.thena.docdb.api.actions.OrgCommitActions.OneMemberEnvelope;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatusType;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreatePrincipalActionImpl implements CreatePrincipalAction {
  private final PermissionStore ctx;
  
  
  @Override
  public Uni<Principal> createOne(CreatePrincipal command) {
    return createRequest(command).onItem().transform(response -> createResponse(response));
  }

  
  private Uni<OneMemberEnvelope> createRequest(CreatePrincipal command){
   final CreateOneMember createOneMember = ctx.getOrg().commit().createOneMember();
    
   if(command.getCommandType() == PrincipalCommandType.CREATE_PRINCIPAL) {
     CreatePrincipal principal = (CreatePrincipal) command;
  
    
    return createOneMember
      .repoId(ctx.getConfig().getRepoId())
      .author(ctx.getConfig().getAuthor().get())
      .message("created principal")
      
      .userName(principal.getName())
      .email(principal.getEmail())
      .build();
     }
   
   throw new CreatePrincipalException("failed to create principal");

  }
  
  private Principal createResponse(OneMemberEnvelope response) {
    
    if(response.getStatus() != Repo.CommitResultStatus.OK) {
      final var msg = "failed to create principal";
      throw new CreatePrincipalException(msg, response);
    }
    
    final var principal = response.getMember();
    
    return ImmutablePrincipal.builder()
        .id(principal.getId())
        .version(principal.getCommitId())
        
        .name(principal.getUserName())
        .email(principal.getEmail())
        .status(OrgActorStatusType.IN_FORCE)
       // .permissions(null) TODO
       // .roles(null) TODO
        .build();
    
  }
  
  final static class CreatePrincipalException extends RuntimeException {
    private static final long serialVersionUID = -7951697973462698785L;

    public CreatePrincipalException(String message) {
      super(message);
    }
    
    public CreatePrincipalException(String message, OneMemberEnvelope response) {
      super(message + System.lineSeparator() + " " + 
          String.join(System.lineSeparator() + "", response.getMessages().stream().map(e -> e.getText()).toList()));
            response.getMessages().forEach(e -> {
              addSuppressed(e.getException());
        }); 
     }
   }  
 }


  



