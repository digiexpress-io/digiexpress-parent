package io.resys.thena.docdb.api.models;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.immutables.value.Value;

import io.resys.thena.docdb.api.models.ThenaDocObject.Doc;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocBranch;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocCommit;
import io.resys.thena.docdb.api.models.ThenaDocObject.IsDocObject;
import io.resys.thena.docdb.api.models.ThenaEnvelope.ThenaObjects;
import io.vertx.core.json.JsonObject;

public interface ThenaDocObjects extends ThenaObjects { 
  @Value.Immutable
  public interface DocProjectObjects extends ThenaDocObjects {
    Map<String, DocBranch> getBranches();
    Map<String, IsDocObject> getValues();   
  }
  

  @Value.Immutable
  interface DocObject extends ThenaGitObjects, DocObjectContainer {
    Repo getRepo();
    DocCommit getCommit();
    Doc getDoc();
    
    default <T> List<T> accept(DocObjectVisitor<T> visitor) {
      return Arrays.asList(visitor.visit(getDoc().getValue()));
    }
  }

  @Value.Immutable
  interface DocObjects extends ThenaDocObjects, DocObjectContainer {
    Repo getRepo();
    DocCommit getCommit();
    List<Doc> getDocs();
    
    default <T> List<T> accept(DocObjectVisitor<T> visitor) {
      return getDocs().stream()
          .map(blob -> visitor.visit(blob.getValue()))
          .collect(Collectors.toUnmodifiableList());
    }
  }
  
  interface DocObjectContainer {
    <T> List<T> accept(DocObjectVisitor<T> visitor);
  }

  @FunctionalInterface
  interface DocObjectVisitor<T> {
    T visit(JsonObject blobValue);
  }

}
