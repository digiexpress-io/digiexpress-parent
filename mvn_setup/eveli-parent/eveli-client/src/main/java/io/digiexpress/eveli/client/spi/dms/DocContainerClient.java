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

import java.io.InputStream;
import java.util.Map;

import io.digiexpress.eveli.client.spi.dms.DocContainer.Doc;
import io.smallrye.mutiny.Uni;

public interface DocContainerClient {
  DocContainerBuilder containerBuilder();
  
  interface DocContainerBuilder {
    DocContainerBuilder externalId(String externalId);
    
    DocContainerBuilder createdBy(String createdBy);
    DocContainerBuilder draftedBy(String draftedBy);
    DocContainerBuilder decidedBy(String decidedBy);
    DocContainerBuilder title(String userGivenId);
    DocContainerBuilder props(Map<String, String> porps);
    DocContainerBuilder addDocument(Doc newDocument);
    Uni<DocContainerEnvelope> build();
  }
  
  
  interface DocBuilder {
    DocBuilder externalId(String externalId);
    DocBuilder name(String name);
    DocBuilder body(InputStream body);
    DocBuilder bodyType(String bodyType);
    DocBuilder mimeType(String mimeType);
    Doc build();
  }
}
