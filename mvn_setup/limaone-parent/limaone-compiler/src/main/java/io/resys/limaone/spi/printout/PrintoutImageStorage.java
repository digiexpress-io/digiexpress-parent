package io.resys.limaone.spi.printout;

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

import org.immutables.value.Value;

import io.resys.thena.api.envelope.Message;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

/**
 * Integration point for storing printout images
 */
public interface PrintoutImageStorage {
  Uni<ImageEnvelope> write(byte[] body);
  Uni<ImageEnvelope> read(String id);

  @Value.Immutable
  interface ImageEnvelope {
    OperationStatus getOperationStatus();
    List<Message> getOperationLogs();
    @Nullable Image getObject(); // Operation result
  }

  @Value.Immutable
  interface Image {
    String getId();
    byte[] getBody();
  }

  enum OperationStatus { OK, ERROR }
}
