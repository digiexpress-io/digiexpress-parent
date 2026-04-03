package io.resys.limaone.spi.program.assignment;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
    this.isEncodedMap = this.isString && ((String) target).indexOf("{") > -1; 
    
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
