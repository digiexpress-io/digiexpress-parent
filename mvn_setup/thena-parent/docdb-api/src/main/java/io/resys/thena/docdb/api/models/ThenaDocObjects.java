package io.resys.thena.docdb.api.models;

import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import io.resys.thena.docdb.api.models.ThenaDocObject.Doc;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocBranch;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocCommit;
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
    Repo getRepo();
    DocCommit getCommit();
    Doc getDoc();
  }

  @Value.Immutable
  interface DocObjects extends ThenaDocObjects {
    Repo getRepo();
    DocCommit getCommit();
    List<Doc> getDocs();
  }

}
