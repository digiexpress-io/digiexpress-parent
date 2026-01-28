package io.digiexpress.tagomi.api.commands;

/*-
 * #%L
 * tagomi-client
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

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.digiexpress.tagomi.api.entities.TagomiContainer;
import io.digiexpress.tagomi.api.entities.TagomiContainer.LocaleAndLabel;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;



public interface TagomiUpdateCommands {
  
  Uni<TagomiContainer.Locale> locale(LocaleMutator changes);
  Uni<TagomiContainer.Template> template(TemplateMutator changes);
  Uni<List<TagomiContainer.Template>> templates(List<TemplateMutator> changes);
  
  Uni<TagomiContainer.Resource> resource(ResourceMutator changes);
  Uni<TagomiContainer.Service> service(ServiceMutator changes);

  
  @Value.Immutable
  @JsonSerialize(as = ImmutableLocaleMutator.class)
  @JsonDeserialize(as = ImmutableLocaleMutator.class)
  interface LocaleMutator {
    String getLocaleId(); 
    String getValue();
    Boolean getEnabled();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableTemplateMutator.class)
  @JsonDeserialize(as = ImmutableTemplateMutator.class)
  interface TemplateMutator {
    String getTemplateId();
    String getContent();
    @Nullable String getLocale();
    @Nullable List<String> getResourceIds(); // id-s to ResourceLink
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableResourceMutator.class)
  @JsonDeserialize(as = ImmutableResourceMutator.class)
  interface ResourceMutator {
    String getResourceId();
    @Nullable TagomiContainer.ResourceType getContentType();
    @Nullable String getResourceName();
    @Nullable byte[] getUploadBody(); // some static asset...
    @Nullable List<String> getTemplateIds();

  }
  @Value.Immutable
  @JsonSerialize(as = ImmutableServiceMutator.class)
  @JsonDeserialize(as = ImmutableServiceMutator.class)
  interface ServiceMutator {
    String getServiceId(); 
    String getServiceName();
    String getOrchestratorName();
  
    @Nullable List<LocaleAndLabel> getLabels();
  }
}
