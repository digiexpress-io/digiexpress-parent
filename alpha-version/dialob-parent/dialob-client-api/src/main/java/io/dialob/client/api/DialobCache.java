package io.dialob.client.api;

import java.io.Serializable;
import java.util.Optional;

import org.immutables.value.Value;

import io.dialob.client.api.DialobStore.StoreEntity;
import io.dialob.program.DialobProgram;

public interface DialobCache {

  DialobCache withName(String name);
  
  Optional<DialobProgram> getProgram(StoreEntity src);
  Optional<DialobDocument> getAst(StoreEntity src);
  
  DialobProgram setProgram(DialobProgram program, StoreEntity src);
  DialobDocument setAst(DialobDocument ast, StoreEntity src);
  
  void flush(String id);
  
  @Value.Immutable
  interface CacheEntry extends Serializable {
    String getId();
    String getRev();
    StoreEntity getSource();
    DialobDocument getAst();
    Optional<DialobProgram> getProgram();
  }

}
