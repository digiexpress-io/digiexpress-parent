package io.thestencil.client.api;

/*-
 * #%L
 * stencil-client-api
 * %%
 * Copyright (C) 2021 - 2022 Copyright 2021 ReSys OÜ
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

import javax.annotation.Nullable;

import org.immutables.value.Value;

@Value.Immutable
public interface Markdowns {
  List<Markdown> getValues();
  List<ImageResource> getImages();
  List<LinkResource> getLinks();
  List<String> getLocales();

  @Value.Immutable
  interface LinkResource {
    String getId();
    String getType();
    String getPath();
    String getValue();
    List<String> getLocale();
    Boolean getWorkflow();
    Boolean getGlobal();
    @Nullable
    String getDesc();
  }
  
  @Value.Immutable
  interface ImageResource {
    String getPath();
    byte[] getValue();
  }
  
  @Value.Immutable
  interface Markdown {
    String getLocale();
    String getPath();
    String getValue();
    List<Heading> getHeadings();
    List<ImageTag> getImages();
  }
  
  @Value.Immutable
  interface Heading {
    Integer getOrder();
    Integer getLevel();
    String getName();
  }
  
  @Value.Immutable
  interface ImageTag {
    Integer getLine();
    String getTitle();
    String getAltText();
    String getPath();
  }
  
}
