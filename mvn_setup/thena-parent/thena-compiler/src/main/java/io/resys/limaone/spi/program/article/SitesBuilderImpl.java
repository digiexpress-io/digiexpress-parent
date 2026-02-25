package io.resys.limaone.spi.program.article;

/*-
 * #%L
 * stencil-client-api
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

import java.util.function.Function;
import java.util.stream.Collectors;

import io.thestencil.client.api.ImmutableSites;
import io.thestencil.client.api.Markdowns;
import io.thestencil.client.api.MigrationBuilder.Sites;
import io.thestencil.client.api.StencilClient.SitesBuilder;
import io.thestencil.client.spi.staticontent.support.ParserAssert;
import io.thestencil.client.spi.staticontent.visitors.ImmutableLinkData;
import io.thestencil.client.spi.staticontent.visitors.ImmutableTopicData;
import io.thestencil.client.spi.staticontent.visitors.SiteStateVisitor;
import io.thestencil.client.spi.staticontent.visitors.SiteVisitor;
import io.thestencil.client.spi.staticontent.visitors.SiteVisitorDefault;
import io.thestencil.client.spi.staticontent.visitors.SiteVisitor.LinkData;
import io.thestencil.client.spi.staticontent.visitors.SiteVisitor.TopicData;

public class SitesBuilderImpl implements SitesBuilder {
  private final SiteVisitor visitor = new SiteVisitorDefault();
  private String imageUrl;
  private Long created;
  private Markdowns markdowns;
  private String tagName;
  private boolean auth = true;
  
  @Override
  public SitesBuilder source(Markdowns markdowns) {
    this.markdowns = markdowns;
    return this;
  }
  @Override
  public SitesBuilder imagePath(String imagePath) {
    this.imageUrl = imagePath;
    return this;
  }
  @Override
  public SitesBuilder tagName(String tagName) {
    this.tagName = tagName;
    return this;
  }
  @Override
  public SitesBuilder created(long created) {
    this.created = created;
    return this;
  }
  @Override
  public SitesBuilder auth(boolean auth) {
    this.auth = auth;
    return this;
  }

  private SitesBuilder topic(
      Function<ImmutableTopicData.Builder, TopicData> newTopic) {
    visitor.visitTopicData(newTopic.apply(ImmutableTopicData.builder()));
    return this;
  }
  private SitesBuilder link(
      Function<ImmutableLinkData.Builder, LinkData> newLink) {
    visitor.visitLinkData(newLink.apply(ImmutableLinkData.builder()));
    return this;
  }
  @Override
  public Sites build() {
    ParserAssert.notEmpty(imageUrl, () -> "imageUrl can't be empty!");
    ParserAssert.notNull(created, () -> "created can't be empty!");
    ParserAssert.notNull(markdowns, () -> "markdowns can't be empty!");
    ParserAssert.notNull(tagName, () -> "tagName can't be empty!");

    markdowns.getValues()
      .stream().filter(topic -> {
        boolean requiredAuth = Boolean.TRUE.equals(topic.getAuth());
        boolean isUserAuthenticated = this.auth;
        if(requiredAuth) {
          return isUserAuthenticated;  
        }
        return true;
      })
      .forEach(value -> topic(builder -> builder
      .auth(value.getAuth())
      .path(value.getPath())
      .locale(value.getLocale())
      .headings(value.getHeadings())
      .images(value.getImages())
      .value(value.getValue())
      .build()));
    
  
    markdowns.getLinks()
      .forEach(link -> link.getLocale()
        .forEach(locale -> link(builder -> builder
          .id(link.getId())
          .path(link.getPath())
          .locale(locale)
          .type(link.getType())
          .name(link.getDesc())
          .assignable(link.getAssignable())
          .anon(link.getAnon())
          .global(link.getGlobal())
          .value(link.getValue())
          .startDate(link.getStartDate())
          .endDate(link.getEndDate())
          .workflow(link.getType().equals(SiteStateVisitor.LINK_TYPE_WORKFLOW))
          .flowName(link.getFlowName())
          .formName(link.getFormName())
          .formTag(link.getFormTag())
          .formId(link.getFormId())
          .build()
    )));
    
    final var visited = visitor.visit(imageUrl);
    final var content = visited.getSites().stream().collect(
      Collectors.toMap(e -> e.getLocale(), e -> e)
    );
    return ImmutableSites.builder()
        .created(created)
        .sites(content)
        .tagName(tagName)
        .build();
  }
}
