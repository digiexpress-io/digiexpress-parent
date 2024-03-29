package io.thestencil.client.spi.exceptions;

import io.resys.thena.api.actions.GitBranchActions;
import io.resys.thena.api.actions.GitBranchActions.BranchObjects;
import io.resys.thena.api.envelope.QueryEnvelope;

public class RefException extends RuntimeException {
  private static final long serialVersionUID = 7190168525508589141L;
  
  private final String entity;
  private final QueryEnvelope<GitBranchActions.BranchObjects> commit;
  
  public RefException(String entity, QueryEnvelope<GitBranchActions.BranchObjects> commit) {
    super(msg(entity, commit));
    this.entity = entity;
    this.commit = commit;
  }
  
  public String getEntity() {
    return entity;
  }
  public QueryEnvelope<GitBranchActions.BranchObjects> getCommit() {
    return commit;
  }
  
  private static String msg(String entity, QueryEnvelope<GitBranchActions.BranchObjects> commit) {
    StringBuilder messages = new StringBuilder();
    for(var msg : commit.getMessages()) {
      messages
      .append(System.lineSeparator())
      .append("  - ").append(msg.getText());
    }
    
    return new StringBuilder("Error in repository: ").append(entity)
        .append(", because of: ").append(messages)
        .toString();
  }
}
