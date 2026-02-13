package io.resys.thena.fs.spi.snapshot;

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
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
public class MergeBranchImpl {

  private final Ref ref;
  private final Commit commit; 
  
  public MergeBranchResult close() {
    final var updatedRef = ImmutableRef.builder()
        .from(ref)
        .commitId(commit.getId())
        .build();
        
    return new MergeBranchResult(updatedRef);
  }
  
  @Value
  public static class MergeBranchResult {
    Ref branch;
  }
}
