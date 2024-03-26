package io.resys.thena.structures.git.tags;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import java.util.Optional;

import io.resys.thena.api.actions.TagActions.TagQuery;
import io.resys.thena.api.entities.git.Tag;
import io.resys.thena.spi.DbState;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Data @Accessors(fluent = true)
public class AnyTagQuery implements TagQuery {
  private final DbState state;
  private final String projectName; //repoId
  private String tagName;
  
  @Override
  public Multi<Tag> findAll() {
    RepoAssert.notEmpty(projectName, () -> "projectName can't be empty!");

    return state.toGitState().query(projectName)
        .onItem().transformToMulti(f -> f.tags().name(tagName).find());
  }
  @Override
  public Uni<Optional<Tag>> get() {
    RepoAssert.notEmpty(projectName, () -> "projectName can't be empty!");
    RepoAssert.notEmpty(tagName, () -> "tagName can't be empty!");
    
    return state.toGitState().query(projectName)
        .onItem().transformToUni(f -> f.tags().name(tagName).getFirst())
        .onItem().transform(tag -> Optional.ofNullable(tag));
  }
  @Override
  public Uni<Optional<Tag>> delete() {
    RepoAssert.notEmpty(projectName, () -> "projectName can't be empty!");
    RepoAssert.notEmpty(tagName, () -> "tagName can't be empty!");
    
    return state.toGitState().query(projectName)
    .onItem().transformToUni(query -> 
      query.tags().name(tagName).getFirst().onItem().transformToUni(tag -> {
        if(tag == null) {
          return Uni.createFrom().item(Optional.empty());
        }
        return query.tags().delete().onItem().transform(delete -> {
          if(delete.getDeletedCount() > 0) {
            return Optional.of(tag);
          }
          return Optional.empty();
        });
      })
      
    );
  }
}
