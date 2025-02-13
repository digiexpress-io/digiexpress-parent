package io.resys.hdes.client.spi.staticresources;

/*-
 * #%L
 * wrench-component-assets-persistence
 * %%
 * Copyright (C) 2016 - 2017 Copyright 2016 ReSys OÜ
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

import io.resys.hdes.client.api.ast.AstBody.AstBodyType;

public class StoreEntityLocation implements Serializable {
  private static final long serialVersionUID = -5312216893265396576L;

  private final String value;

  public StoreEntityLocation(String value) {
    super();
    this.value = value;
  }
  
  //getResourceFullName
  public String getAbsolutePath(AstBodyType type, String pointer) {
    return value + getFileName(type, pointer);
  }
  //getResourceName
  public String getFileName(AstBodyType type, String pointer) {
    return getPath(type) + "/" + pointer + ".json";
  }
  //getResourceId
  public String getBaseName(AstBodyType type, String filename) {
    return filename.substring(0, filename.lastIndexOf("."));
  }
  public String getFlowRegex() {
    return withRegex("**/flow/**/*.json");
  }
  public String getFlowTaskRegex() {
    return withRegex("**/flowtask/**/*.json");
  }
  public String getDtRegex() {
    return withRegex("**/dt/**/*.json");
  }
  public String getTagRegex() {
    return withRegex("**/tag/**/*.json");
  }
  public String getMigrationRegex() {
    return withRegex("**/migration/**/*.json");
  }
  public String getDumpRegex() {
    return withRegex("**/dump/**/*.json");
  }
  public String getValue() {
    return value;
  }
  private String getPath(AstBodyType type) {
    switch (type) {
    case FLOW:
      return "flow";
    case FLOW_TASK:
      return "flowtask";
    case DT:
      return "dt";
    case TAG:
      return "tag";
    default: throw new IllegalArgumentException("Unknown asset type:" + type + "!");
    }
  }
  private String withRegex(String exp) {
    return value + exp;
  }
}
