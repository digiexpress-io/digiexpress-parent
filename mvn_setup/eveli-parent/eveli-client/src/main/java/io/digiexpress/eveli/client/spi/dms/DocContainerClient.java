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

import io.digiexpress.eveli.client.api.TaskClient.Task;
import io.digiexpress.eveli.client.spi.dms.DocContainer.Doc;
import io.resys.thena.api.entities.grim.ThenaGrimMergeObject.MergeMission;
import io.smallrye.mutiny.Uni;

public interface DocContainerClient {
  CreateDoc createDoc();
  DocContainerTaskUpdater updateTask();
  
  interface DocContainerTaskUpdater {
    DocContainerTaskUpdater userId(String userId);
    DocContainerTaskUpdater doc(DocContainer doc);
    DocContainerTaskUpdater task(MergeMission task);
    void build();
  }
  
  
  interface CreateDoc {
    CreateDoc externalId(String externalId);
    CreateDoc task(Task task);
    CreateDoc createdBy(String createdBy);
    CreateDoc draftedBy(String draftedBy);
    CreateDoc decidedBy(String decidedBy);
    CreateDoc title(String userGivenId);
    CreateDoc props(Map<String, String> props);
    CreateDoc addDocument(Doc newDocument);
    
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
