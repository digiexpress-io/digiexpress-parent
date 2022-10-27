package io.digiexpress.client.api;

import java.io.Serializable;

import org.immutables.value.Value;

public interface ServiceCache {

  ServiceCache withName(String name);
  void flush(String id);
//  
//  Optional<DialobProgram> getProgram(StoreEntity src);
//  Optional<DialobDocument> getAst(StoreEntity src);
//  
//  DialobProgram setProgram(DialobProgram program, StoreEntity src);
//  DialobDocument setAst(DialobDocument ast, StoreEntity src);
//  

//  
  @Value.Immutable
  interface CacheEntry extends Serializable {
    String getId();
    String getRev();
//    StoreEntity getSource();
//    DialobDocument getAst();
//    Optional<DialobProgram> getProgram();
  }

}
