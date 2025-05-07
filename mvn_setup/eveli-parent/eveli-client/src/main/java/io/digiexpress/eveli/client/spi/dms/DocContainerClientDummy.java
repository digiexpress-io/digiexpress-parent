package io.digiexpress.eveli.client.spi.dms;

/*-
 * #%L
 * eveli-client
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

import java.util.ArrayList;
import java.util.List;

import io.digiexpress.eveli.client.spi.dms.DocContainer.Doc;
import io.smallrye.mutiny.Uni;

public class DocContainerClientDummy implements DocContainerClient {

  @Override
  public DocContainerBuilder containerBuilder() {
    return new DocContainerBuilder() {
      private final List<Doc> docsToMerge = new ArrayList<>();
      
      @Override
      public DocContainerBuilder title(String userGivenId) {
        return this;
      }
      @Override
      public DocContainerBuilder externalId(String externalId) {
        return this;
      }
      @Override
      public DocContainerBuilder draftedBy(String draftedBy) {
        return this;
      }
      @Override
      public DocContainerBuilder decidedBy(String decidedBy) {
        return this;
      }
      @Override
      public DocContainerBuilder createdBy(String createdBy) {
        return this;
      }
      @Override
      public Uni<DocContainerEnvelope> build() {
        return Uni.createFrom().item(ImmutableDocContainerEnvelope.builder()
            .status(DocContainerEnvelope.DocContainerEnvelopeStatus.OK)
            .objects(ImmutableDocContainer.builder().id("dummy-doc-container-id").build())
            .build());
      }
      @Override
      public DocContainerBuilder addDocument(Doc newDocument) {
        docsToMerge.add(newDocument);
        return this;
      }
    };
  }
}
