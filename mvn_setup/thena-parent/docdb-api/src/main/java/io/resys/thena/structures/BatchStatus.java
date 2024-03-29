package io.resys.thena.structures;

import io.resys.thena.api.entities.CommitResultStatus;

public enum BatchStatus { 
  OK, EMPTY, ERROR, CONFLICT;

  public static CommitResultStatus mapStatus(BatchStatus src) {
    if(src == OK) {
      return CommitResultStatus.OK;
    } else if(src == CONFLICT) {
      return CommitResultStatus.CONFLICT;
    }
    return CommitResultStatus.ERROR; 
  } 
}