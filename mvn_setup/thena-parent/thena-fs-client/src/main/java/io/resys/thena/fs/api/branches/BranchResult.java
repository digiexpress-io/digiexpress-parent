package io.resys.thena.fs.api.branches;

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

import java.util.List;

import org.immutables.value.Value;

import io.resys.thena.api.envelope.CommitResultStatus;
import io.resys.thena.api.envelope.Message;
import io.resys.thena.api.envelope.ThenaEnvelope;
import io.resys.thena.fs.entities.Ref;
import jakarta.annotation.Nullable;

/**
 * Result envelope containing the outcome of a branch operation.
 * Includes the created/updated branch reference and operation status.
 */
@Value.Immutable
public
interface BranchResult extends ThenaEnvelope {
  /**
   * @return the tenant identifier where the branch operation was performed
   */
  String getTenantId();
  
  /**
   * @return the created/updated branch reference, null if operation failed
   */
  @Nullable Ref getBranch();
  
  /**
   * @return the overall status of the branch operation
   */
  CommitResultStatus getStatus();
  
  /**
   * @return list of diagnostic messages from the branch operation
   */
  List<Message> getMessages();
}
