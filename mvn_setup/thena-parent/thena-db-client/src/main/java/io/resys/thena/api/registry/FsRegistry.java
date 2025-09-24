package io.resys.thena.api.registry;

/*-
 * #%L
 * thena-db-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import io.resys.thena.api.registry.fs.FsCommitRegistry;
import io.resys.thena.api.registry.fs.FsCommitTreeRegistry;
import io.resys.thena.api.registry.fs.FsDirentAssignmentRegistry;
import io.resys.thena.api.registry.fs.FsDirentDataRegistry;
import io.resys.thena.api.registry.fs.FsDirentLabelRegistry;
import io.resys.thena.api.registry.fs.FsDirentLinkRegistry;
import io.resys.thena.api.registry.fs.FsDirentRegistry;
import io.resys.thena.api.registry.fs.FsDirentRemarkRegistry;

public interface FsRegistry {
  FsCommitRegistry commits();
  FsCommitTreeRegistry commitTrees();
  FsDirentAssignmentRegistry direntAssignments();
  FsDirentDataRegistry direntData();
  FsDirentLabelRegistry direntLabels();
  FsDirentLinkRegistry direntLinks();
  FsDirentRegistry dirents();
  FsDirentRemarkRegistry direntRemarks();
}
