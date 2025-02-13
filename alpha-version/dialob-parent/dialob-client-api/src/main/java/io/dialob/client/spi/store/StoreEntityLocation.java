package io.dialob.client.spi.store;

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

import io.dialob.client.api.DialobDocument.DocumentType;

public class StoreEntityLocation implements Serializable {
  private static final long serialVersionUID = -5312216893265396576L;

  private final String value;

  public StoreEntityLocation(String value) {
    super();
    this.value = value;
  }
  
  //getResourceFullName
  public String getAbsolutePath(DocumentType type, String pointer) {
    return value + getFileName(type, pointer);
  }
  //getResourceName
  public String getFileName(DocumentType type, String pointer) {
    return getPath(type) + "/" + pointer + ".json";
  }
  //getResourceId
  public String getBaseName(DocumentType type, String filename) {
    return filename.substring(0, filename.lastIndexOf("."));
  }
  public String getFormRegex() {
    return withRegex("**/form/**/*.json");
  }
  public String getFormTagRegex() {
    return withRegex("**/formrev/**/*.json");
  }
  public String getMigrationRegex() {
    return withRegex("**/dialob_migration/**/*.txt");
  }
  public String getValue() {
    return value;
  }
  private String getPath(DocumentType type) {
    switch (type) {
    case FORM:
      return "form";
    case FORM_REV:
      return "formrev";
    default: throw new IllegalArgumentException("Unknown asset type:" + type + "!");
    }
  }
  private String withRegex(String exp) {
    return value + exp;
  }
}
