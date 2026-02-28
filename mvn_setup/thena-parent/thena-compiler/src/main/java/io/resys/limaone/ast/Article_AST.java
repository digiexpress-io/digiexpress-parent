package io.resys.limaone.ast;

import java.time.OffsetDateTime;

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
import java.util.Optional;

import org.immutables.value.Value;

import jakarta.annotation.Nullable;

@Value.Immutable
public interface Article_AST extends Simple_AST {
  String getTagName();
  List<Markdown> getValues();
  List<Image> getImages();
  List<Link> getLinks();
  List<String> getLocales();

  @Value.Immutable
  interface Link {
    String getId();
    String getType();
    String getPath();
    String getValue();
    List<String> getLocale();
    Boolean getWorkflow();
    Boolean getGlobal();
    Boolean getAnon();
    Boolean getAssignable();
    @Nullable OffsetDateTime getStartDate();
    @Nullable OffsetDateTime getEndDate();
    @Nullable String getDesc();
    // values for workflows
    @Nullable String getFormId();
    @Nullable String getFormName();
    @Nullable String getFormTag();
    @Nullable String getFlowName();
    
    
    default boolean isInPeriod(Optional<OffsetDateTime> now) {
      final var link = this;
      if(link.getStartDate() == null && link.getEndDate() == null) {
        return true;
      }
      
      if(now.isEmpty()) {
        return true;
      }
      
      final var target = now.get();
      if(link.getEndDate() != null && link.getEndDate().compareTo(target) < 0) {
        return false;
      }

      if(link.getStartDate() != null && link.getStartDate().compareTo(target) > 0) {
        return false;
      }
      return true;
    }
  }
  
  @Value.Immutable
  interface Image {
    String getPath();
    byte[] getValue();
  }
  
  @Value.Immutable
  interface Markdown {
    String getLocale();
    String getPath();
    String getValue();
    Boolean getAuth();
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
