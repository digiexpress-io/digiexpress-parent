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

import java.io.Serializable;
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.digiexpress.tagomi.api.entities.TagomiContainer;
import io.digiexpress.tagomi.api.entities.TagomiContainer.LocaleAndLabel;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;




public interface TagomiCreateCommands {
  Uni<TagomiContainer.Locale> locale(CreateLocale init);
  Uni<TagomiContainer.Template> template(CreateTemplate init);
  Uni<TagomiContainer.Resource> resource(CreateResource init);
  Uni<TagomiContainer.Service> service(CreateService init);  
  Uni<TagomiContainer.Tag> tag(CreateTag init);
  
  interface Command extends Serializable {}

  @Value.Immutable
  @JsonSerialize(as = ImmutableCreateLocale.class)
  @JsonDeserialize(as = ImmutableCreateLocale.class)
  interface CreateLocale extends Command {
    @Nullable String getId();
    String getLocaleCode();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableCreateTemplate.class)
  @JsonDeserialize(as = ImmutableCreateTemplate.class)
  interface CreateTemplate extends Command {
    @Nullable String getId();
    String getServiceId();
    String getLocale();
    @Nullable String getContent();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableCreateResource.class)
  @JsonDeserialize(as = ImmutableCreateResource.class)
  interface CreateResource extends Command {
    @Nullable String getId();
    String getResourceName();
    String getContentType();
    byte[] getUploadBody(); // some static asset...
    List<String> getTemplateIds();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableCreateService.class)
  @JsonDeserialize(as = ImmutableCreateService.class)
  interface CreateService extends Command {
    @Nullable String getId();
    List<LocaleAndLabel> getLabels();
    
    String getServiceName();
    String getOrchestratorName();
  }

  
  @Value.Immutable
  @JsonSerialize(as = ImmutableCreateTag.class)
  @JsonDeserialize(as = ImmutableCreateTag.class)
  interface CreateTag extends Command {
    @Nullable String getId();
    String getTagName();
    String getNote();
    @Nullable String getCommitId();
  }
}
