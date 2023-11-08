package io.resys.thena.docdb.api.models;

import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import io.resys.thena.docdb.api.models.ThenaDocObject.Doc;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocBranch;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocCommit;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocLog;
import io.resys.thena.docdb.api.models.ThenaDocObject.IsDocObject;
import io.resys.thena.docdb.api.models.ThenaEnvelope.ThenaObjects;

public interface ThenaDocObjects extends ThenaObjects {
  
  @Value.Immutable
  interface DocProjectObjects extends ThenaDocObjects {
    Map<String, DocBranch> getBranches();
    Map<String, IsDocObject> getValues();   
  }
  

  @Value.Immutable
  interface DocObject extends ThenaDocObjects {
    Doc getDoc();
    Map<String, DocBranch> getBranches();
    Map<String, List<DocLog>> getLogs();        // branch id - latest commit logs 
    Map<String, DocCommit> getCommits();        // branch id - latest commit
  }

  @Value.Immutable
  interface DocObjects extends ThenaDocObjects {
    List<Doc> getDocs();
    Map<String, List<DocBranch>> getBranches(); // doc id    - list of document branches 
    Map<String, DocCommit> getCommits();        // branch id - latest commit
    Map<String, List<DocLog>> getLogs();        // branch id - latest commit logs 
  }

  @Value.Immutable
  interface DocBranchObject extends ThenaDocObjects {
    Doc getDoc();
    DocBranch getBranch();
    DocCommit getCommit();
    List<DocLog> getLogs();
  }
}
