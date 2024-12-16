package io.thestencil.client.spi.exceptions;

import io.resys.thena.api.actions.GitCommitActions.CommitResultEnvelope;
import io.thestencil.client.api.MigrationBuilder.Sites;
import io.thestencil.client.api.StencilComposer.SiteState;

public class ImportException extends RuntimeException {
  private static final long serialVersionUID = 7190168525508589141L;
  
  private final Object entity;
  private final CommitResultEnvelope commit;
  
  public ImportException(Sites entity, CommitResultEnvelope commit) {
    super(msg(entity, commit));
    this.entity = entity;
    this.commit = commit;
  }
  public ImportException(SiteState entity, CommitResultEnvelope commit) {
    super(msg(entity, commit));
    this.entity = entity;
    this.commit = commit;
  }  
  public Object getEntity() {
    return entity;
  }
  public CommitResultEnvelope getCommit() {
    return commit;
  }
  
  private static String msg(Object entity, CommitResultEnvelope commit) {
    StringBuilder messages = new StringBuilder();
    for(var msg : commit.getMessages()) {
      messages
      .append(System.lineSeparator())
      .append("  - ").append(msg.getText());
    }
    return new StringBuilder("Can't import sites because: ")
        .append(messages)
        .append(System.lineSeparator())
        .append(entity.toString())
        .toString();
  }
}
