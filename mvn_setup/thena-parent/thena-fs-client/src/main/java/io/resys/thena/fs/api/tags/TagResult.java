package io.resys.thena.fs.api.tags;

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
import io.resys.thena.fs.entities.Tag;
import jakarta.annotation.Nullable;

/**
 * Result envelope containing the outcome of a commit operation.
 * Includes the created commit, operation status, and any diagnostic messages.
 */
@Value.Immutable
public
interface TagResult extends ThenaEnvelope {
  /**
   * @return the tenant identifier where the tag was applied
   */
  String getTenantId();
  
  /**
   * @return the created Tag object, null if tagging failed
   */
  @Nullable Tag getTag();
  
  /**
   * @return the overall status of the tag operation
   */
  CommitResultStatus getStatus();
  
  /**
   * @return list of diagnostic messages from the tag process
   */
  List<Message> getMessages();
}
