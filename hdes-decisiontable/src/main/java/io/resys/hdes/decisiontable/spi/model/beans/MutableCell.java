package io.resys.hdes.decisiontable.spi.model.beans;

/*-
 * #%L
 * hdes-decisiontable
 * %%
 * Copyright (C) 2020 Copyright 2020 ReSys OÜ
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


public class MutableCell {
  private final int id;
  private final int row;
  private String value;

  public MutableCell(int id, int row) {
    super();
    this.id = id;
    this.row = row;
  }
  public String getValue() {
    return value;
  }
  public void setValue(String value) {
    this.value = value;
  }
  public int getRow() {
    return row;
  }
  public int getId() {
    return id;
  }
}
