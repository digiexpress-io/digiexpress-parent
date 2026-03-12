package io.resys.limaone.persistence;

import java.time.OffsetDateTime;
import java.util.function.Function;

import io.resys.limaone.authoring.Authoring.WorldQuery;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.smallrye.mutiny.Uni;

public interface WorldPersistence {

  
  WorldQuery worldQuery();
  WorldBuilder worldBuilder();
  
  
  interface WorldBuilder {
    WorldBuilder author(String string);
    WorldBuilder createdAt(OffsetDateTime createdAt);
    WorldBuilder docs(BodyType ... type);
    WorldBuilder lockWithCommit(String commitId);
    <T> Uni<T> build(Function<NextWorld, T> mergeFunction); 
  }
  
  interface NextWorld {
    // model state at current commit
    ModelWorld getCurrentWorld();
    
    <T extends Model.Body> Model<T> newModel(String fileName, T body);
    <T extends Model.Body> Model<T> mergeModel(String id, String fileName, T body);
    <T extends Model.Body> Model<T> deleteModel(String id, T body);
  }


}
