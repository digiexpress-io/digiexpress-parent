package io.resys.limaone.fs;

import java.util.ArrayList;
import java.util.Collections;

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

import java.util.List;
import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.Model.BodyType;
import io.resys.thena.fs.entities.Index;
import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableWorldFs.class) @JsonDeserialize(as = ImmutableWorldFs.class)
public interface WorldFs {
  List<DirentBase> getDirents();
  

  @JsonIgnore
  default List<DirentBase> flatAll() {
    
    final var result = new ArrayList<DirentBase>();
    final var consumer = new Consumer<DirentBase>() {
      @Override
      public void accept(DirentBase t) {
        result.addAll(t.getChildren());
        t.getChildren().forEach(this);
      }
    };
    
    for(final var base : getDirents()) {
      result.add(base);
      consumer.accept(base);
    }
    
    return Collections.unmodifiableList(result);
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableDirentBase.class) @JsonDeserialize(as = ImmutableDirentBase.class)
  interface DirentBase {
    String getId();
    String getName();
    String getFullPath();
    @Nullable Index getCommitIndex(); // null when the folder is pseudo created for grouping only... ie. it's not persisted
    BodyType getType();
    List<DirentBase> getChildren();
    
    @Nullable WorldFsProps getProps();
    
    default boolean isDirectory() {
      return this.getType() == BodyType.FOLDER;
    }
    
  }
}
