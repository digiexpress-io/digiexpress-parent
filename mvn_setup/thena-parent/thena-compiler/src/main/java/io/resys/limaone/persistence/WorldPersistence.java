package io.resys.limaone.persistence;

import java.util.function.Function;

import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.smallrye.mutiny.Uni;

public interface WorldPersistence {

  WorldBuilder worldBuilder();
  
  
  interface WorldBuilder {
    WorldBuilder docs(BodyType ... type);
    WorldBuilder lock();
    WorldBuilder lockWithCommit(String commitId);
    <T> Uni<T> build(Function<NextWorld, T> mergeFunction); 
  }
  
  interface NextWorld {
    // model state at current commit
    ModelWorld getCurrentWorld();
    
    <T extends Model.Body> Model<T> newModel(T body);
    <T extends Model.Body> Model<T> mergeModel(String id, T body);
  }
}
