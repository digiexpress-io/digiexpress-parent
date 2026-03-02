package io.resys.limaone.spi.program.assignment;

import java.io.Serializable;
import java.util.Map;

import io.vertx.core.json.JsonObject;


public class Initializer {

  private final Serializable target;
  private final boolean isMap;
  private final boolean isString;
  private final boolean isEncodedMap;
  private final Map<String, Serializable> map;
  
  @SuppressWarnings("unchecked")
  public Initializer(Serializable target) {
    this.target = target;
    
    this.isMap = Map.class.isAssignableFrom(target.getClass());
    this.isString = target instanceof String;
    this.isEncodedMap = this.isString &&  ((String) target).indexOf("{") > -1; 
    
    if(isMap) {
      this.map = (Map<String, Serializable>) target;
    } else if (isEncodedMap) {
      this.map = new JsonObject((String) target).mapTo(Map.class);
    } else {
      this.map = null;
    }
  }
  
  public boolean isMap() {
    return isMap;
  }
  
  public Serializable getRaw() {
    return target;
  }
 
  public Map<String, Serializable> getMap() {
    if(isMap) {
      return map;      
    }
    return null;
  }
  
  public Map<String, Serializable> getExploded() {
    if(isEncodedMap) {
      return (Map<String, Serializable>) map;
    }
    return null;
  }
  
  public Map<String, Serializable> getAnyMap() {
    return map;
  }
}
