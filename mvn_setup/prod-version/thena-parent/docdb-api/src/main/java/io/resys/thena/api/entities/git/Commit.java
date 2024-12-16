package io.resys.thena.api.entities.git;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import java.time.LocalDateTime;
import java.util.Optional;

import org.immutables.value.Value;

import io.resys.thena.api.entities.git.GitEntity.IsGitObject;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;

@Value.Immutable
public
interface Commit extends IsGitObject, GitEntity, ThenaTable {
  String getAuthor();
  LocalDateTime getDateTime();
  String getMessage();
  
  // Parent commit id
  Optional<String> getParent();
  
  // This commit is merge commit, that points to a commit in different branch
  Optional<String> getMerge();
  
  // Tree id that describes list of (resource name - content) entries
  String getTree();
}
