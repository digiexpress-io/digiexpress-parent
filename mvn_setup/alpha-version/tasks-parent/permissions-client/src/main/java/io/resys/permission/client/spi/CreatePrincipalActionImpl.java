package io.resys.permission.client.spi;

import io.resys.permission.client.api.PermissionClient.CreatePrincipalAction;
import io.resys.permission.client.api.PermissionClient.PrincipalAccessEvaluator;
import io.resys.permission.client.api.model.ImmutablePrincipal;
import io.resys.permission.client.api.model.Principal;
import io.resys.permission.client.api.model.PrincipalCommand.CreatePrincipal;
import io.resys.permission.client.api.model.PrincipalCommand.PrincipalCommandType;
import io.resys.thena.api.actions.OrgCommitActions.CreateOneMember;
import io.resys.thena.api.actions.OrgCommitActions.OneMemberEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.org.OrgActorStatusType;
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
   final CreateOneMember createOneMember = ctx.getOrg(ctx.getConfig().getRepoId()).commit().createOneMember();
    
   if(command.getCommandType() == PrincipalCommandType.CREATE_PRINCIPAL) {
     final var principal = (CreatePrincipal) command;
  
    
    return createOneMember
      .author(ctx.getConfig().getAuthor().get())
      .message("created principal")
      
      .userName(principal.getName())
      .email(principal.getEmail())
      .externalId(principal.getExternalId())
      .addMemberToParties(principal.getRoles())
      .addMemberRight(principal.getPermissions())
      .build();
     }
   
   throw new CreatePrincipalException("failed to create principal");

  }
  
  private Principal createResponse(OneMemberEnvelope response) {
    
    if(response.getStatus() != CommitResultStatus.OK) {
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
        
        //TODO implement inheritance
        .addAllRoles(response.getDirectParties().stream().map(e -> e.getPartyName()).toList())
        .addAllPermissions(response.getDirectRights().stream().map(e -> e.getRightName()).toList())
        
        .directPermissions(response.getDirectRights().stream().map(e -> e.getRightName()).toList())
        .directRoles(response.getDirectParties().stream().map(e -> e.getPartyName()).toList())
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
            response.getMessages().stream().filter(e -> e.getException() != null).forEach(e -> {
              addSuppressed(e.getException());
        }); 
     }
   }

  @Override
  public CreatePrincipalAction evalAccess(PrincipalAccessEvaluator eval) {
    // TODO Auto-generated method stub
    return null;
  }  
 }


  



