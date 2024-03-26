package io.resys.thena.models.git.commits;

import io.resys.thena.api.LogConstants;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = LogConstants.SHOW_COMMIT)
public class CommitLogger {
  private final StringBuilder data = new StringBuilder();
  
  public CommitLogger append(String data) {
    if(log.isDebugEnabled()) {
      this.data.append(data);
    }
    return this;
  }
  @Override
  public String toString() {
    if(log.isDebugEnabled()) {
      log.debug(data.toString());
    } else {
      data.append("Log DEBUG disabled for: " + LogConstants.SHOW_COMMIT + "!");
    }
    return data.toString();
  }
} 
