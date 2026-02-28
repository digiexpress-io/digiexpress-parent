package io.resys.thena.fs.spi.snapshot;

import java.time.OffsetDateTime;

/*-
 * #%L
 * thena-fs-client
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

import io.resys.thena.fs.entities.Commit;
import io.resys.thena.fs.entities.ImmutableRef;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.support.OidUtils;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
public class NewBranchImpl {

  private final String branchName;
  private final Commit commit; 
  private final OffsetDateTime createdAt;
  
  public NewBranchResult close() {
    final var newRef = ImmutableRef.builder()
        .id(OidUtils.genUUID())
        .refName(branchName)
        .commitId(commit.getId())
        .refCreatedAt(createdAt)
        .build();
    return new NewBranchResult(newRef);
  }
  
  @Value
  public static class NewBranchResult {
    Ref branch;
  }
}
