package io.resys.thena.api.registry;

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

import io.resys.thena.api.registry.git.BlobRegistry;
import io.resys.thena.api.registry.git.BranchRegistry;
import io.resys.thena.api.registry.git.CommitRegistry;
import io.resys.thena.api.registry.git.TagRegistry;
import io.resys.thena.api.registry.git.TreeRegistry;
import io.resys.thena.api.registry.git.TreeValueRegistry;

public interface GitRegistry {
  BlobRegistry blobs();
  CommitRegistry commits();
  TreeValueRegistry treeValues();
  TreeRegistry trees();
  BranchRegistry branches();
  TagRegistry tags();
}
