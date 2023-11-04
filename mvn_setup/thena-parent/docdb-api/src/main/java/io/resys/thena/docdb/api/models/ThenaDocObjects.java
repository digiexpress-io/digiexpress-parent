package io.resys.thena.docdb.api.models;

import java.util.Map;

import org.immutables.value.Value;

import io.resys.thena.docdb.api.models.ThenaGitObject.Branch;
import io.resys.thena.docdb.api.models.ThenaGitObject.IsObject;
import io.resys.thena.docdb.api.models.ThenaGitObject.Tag;

public interface ThenaDocObjects { 
  @Value.Immutable
  public interface ProjectObjects extends ThenaDocObjects {
    Map<String, Branch> getBranches();
    Map<String, Tag> getTags();
    Map<String, IsObject> getValues();   
  }
}
