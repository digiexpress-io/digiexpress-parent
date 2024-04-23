package io.resys.thena.api.entities.doc;

public interface DocEntity {
  interface IsDocObject { String getId(); DocType getDocType(); }
  
  
  enum DocType {
    DOC, DOC_BRANCH, DOC_COMMANDS 
  }
}
