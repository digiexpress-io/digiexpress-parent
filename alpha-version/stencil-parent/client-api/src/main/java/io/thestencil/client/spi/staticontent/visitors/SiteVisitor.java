package io.thestencil.client.spi.staticontent.visitors;

/*-
 * #%L
 * stencil-static-content
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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
import java.util.Map;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.thestencil.client.api.Markdowns.Heading;
import io.thestencil.client.api.Markdowns.ImageTag;
import io.thestencil.client.api.MigrationBuilder.LocalizedSite;

public interface SiteVisitor {
  void visitTopicData(TopicData topic);
  void visitLinkData(LinkData link);
  void visitImageData(ImageData image);
  void visitTopicNameData(TopicNameData names);
  SiteVisitorOutput visit(String imageUrl);
  
  @Value.Immutable
  interface SiteVisitorOutput {
    List<Message> getMessage();
    List<LocalizedSite> getSites();
  }
  
  @Value.Immutable
  interface Message {
    String getText();
    @Nullable
    Object getObject();
  }

  @Value.Immutable
  interface TopicData {
    String getPath();
    String getLocale();
    String getValue();
    List<Heading> getHeadings();
    List<ImageTag> getImages();
  }

  @Value.Immutable
  interface TopicNameData {
    String getPath();
    Map<String, String> getLocale();
  }

  @Value.Immutable
  interface LinkData {
    String getId();
    String getPath();
    String getName();
    String getType();
    String getValue();
    String getLocale();
    Boolean getGlobal();
    Boolean getWorkflow();
  }

  @Value.Immutable
  interface ImageData {
    String getPath();
    byte[] getValue();
  }
}
