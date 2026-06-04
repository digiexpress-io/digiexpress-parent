package io.resys.limaone.persistence;

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

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import io.resys.limaone.authoring.Authoring.WorldFsQuery;
import io.resys.limaone.authoring.Authoring.WorldIndexQuery;
import io.resys.limaone.authoring.Authoring.WorldQuery;
import io.resys.limaone.authoring.Authoring.WorldRefQuery;
import io.resys.limaone.model.Description;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.thena.fs.api.FileSystem;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

public interface ModelWorldDb {
  ModelWorldDb withBranchName(Optional<String> branchName);
  ModelWorldDb withTenant(Optional<String> tenantName);
   
  WorldFsQuery worldFsQuery();
  WorldRefQuery worldRefQuery();
  WorldQuery worldQuery();
  WorldBuilder worldBuilder();
  WorldIndexQuery worldIndexQuery();
  
  CreateModelWorldDb createModelWorldDb();
  FileSystem getUserFileSystem();
  String getBranchName();
  
  interface CreateModelWorldDb {
    Uni<Void> createDbIfNotPresent();
  }
  
  interface WorldBuilder {
    WorldBuilder author(String string);
    WorldBuilder createdAt(OffsetDateTime createdAt);
    WorldBuilder docs(BodyType ... type);
    WorldBuilder docsId(String ... objectId);
    WorldBuilder lockWithCommit(UUID commitId);
    <T> Uni<T> build(Function<NextWorld, T> mergeFunction); 
  }
  
  interface NextWorld {
    // model state at current commit
    ModelWorld getCurrentWorld();
    
    <T extends Model.Body> Model<T> newModel(String fileName, T body, @Nullable Description desc);
    <T extends Model.Body> Model<T> mergeModel(String id, String fileName, T body, @Nullable Description desc);
    <T extends Model.Body> Model<T> deleteModel(String id, T body);
  }


}
