package io.thestencil.client.spi.builders;

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

import java.util.stream.Collectors;

import io.thestencil.client.api.CreateBuilder.BatchSite;
import io.thestencil.client.api.CreateBuilder.CreateArticle;
import io.thestencil.client.api.CreateBuilder.CreateLink;
import io.thestencil.client.api.CreateBuilder.CreateLocale;
import io.thestencil.client.api.CreateBuilder.CreatePage;
import io.thestencil.client.api.CreateBuilder.CreateWorkflow;
import io.thestencil.client.api.ImmutableBatchCommand;
import io.thestencil.client.api.ImmutableSiteState;
import io.thestencil.client.api.StencilClient;
import io.thestencil.client.api.StencilClient.Article;
import io.thestencil.client.api.StencilClient.Entity;
import io.thestencil.client.api.StencilClient.Link;
import io.thestencil.client.api.StencilClient.Locale;
import io.thestencil.client.api.StencilClient.Page;
import io.thestencil.client.api.StencilClient.Workflow;
import io.thestencil.client.api.StencilComposer.SiteState;
import io.thestencil.client.api.StencilStore.BatchCommand;

public class BatchSiteCommandVisitor {
  private final StencilClient client;
  private final ImmutableSiteState.Builder next;
  
  public BatchSiteCommandVisitor(SiteState start, StencilClient client) {
    super();
    this.client = client;
    this.next = ImmutableSiteState.builder().from(start);
  }

  public BatchCommand visit(BatchSite command) {
    
    final var locales = command.getLocales().stream().map(this::visitLocale).collect(Collectors.toList());
    final var articles = command.getArticles().stream().map(this::visitArticle).collect(Collectors.toList());
    final var pages = command.getPages().stream().map(this::visitPage).collect(Collectors.toList());
    final var workflows = command.getWorkflows().stream().map(this::visitWorkflow).collect(Collectors.toList());
    final var links = command.getLinks().stream().map(this::visitLink).collect(Collectors.toList());
    
    return ImmutableBatchCommand.builder()
        .addAllToBeCreated(locales)
        .addAllToBeCreated(articles)
        .addAllToBeCreated(pages)
        .addAllToBeCreated(workflows)
        .addAllToBeCreated(links)
        .build();
  }
  
  private Entity<Locale> visitLocale(CreateLocale init) {
    final var created = CreateBuilderImpl.locale(init, next.build(), client);
    next.putLocales(created.getId(), created);
    return created;
  }
  private Entity<Article> visitArticle(CreateArticle init) {
    final var created = CreateBuilderImpl.article(init, next.build(), client);
    next.putArticles(created.getId(), created);
    return created;
  }
  private Entity<Page> visitPage(CreatePage init) {
    final var created = CreateBuilderImpl.page(init, next.build(), client);
    next.putPages(created.getId(), created);
    return created;
  }
  private Entity<Workflow> visitWorkflow(CreateWorkflow init) {
    final var created = CreateBuilderImpl.workflow(init, next.build(), client);
    next.putWorkflows(created.getId(), created);
    return created;
  }
  private Entity<Link> visitLink(CreateLink init) {
    final var created = CreateBuilderImpl.link(init, next.build(), client);
    next.putLinks(created.getId(), created);
    return created;
  }
}
