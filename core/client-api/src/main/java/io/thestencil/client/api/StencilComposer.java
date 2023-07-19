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

import java.util.Map;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.thestencil.client.api.StencilClient.Article;
import io.thestencil.client.api.StencilClient.Entity;
import io.thestencil.client.api.StencilClient.Link;
import io.thestencil.client.api.StencilClient.Locale;
import io.thestencil.client.api.StencilClient.MarkdownBuilder;
import io.thestencil.client.api.StencilClient.Page;
import io.thestencil.client.api.StencilClient.Release;
import io.thestencil.client.api.StencilClient.SitesBuilder;
import io.thestencil.client.api.StencilClient.Template;
import io.thestencil.client.api.StencilClient.Workflow;
import io.thestencil.client.api.StencilStore.QueryBuilder;


public interface StencilComposer {
  CreateBuilder create();
  UpdateBuilder update();
  DeleteBuilder delete();
  MigrationBuilder migration();
  QueryBuilder query();
  MarkdownBuilder markdown();
  SitesBuilder sites();
  VersionBuilder version();
  

  @Value.Immutable
  @JsonSerialize(as = ImmutableSiteState.class)
  @JsonDeserialize(as = ImmutableSiteState.class)
  interface SiteState {
    String getName();
    @Nullable
    String getCommit();
    SiteContentType getContentType();
    Map<String, Entity<Release>> getReleases();
    Map<String, Entity<Locale>> getLocales();
    Map<String, Entity<Page>> getPages();
    Map<String, Entity<Link>> getLinks();
    Map<String, Entity<Article>> getArticles();
    Map<String, Entity<Workflow>> getWorkflows();
    Map<String, Entity<Template>> getTemplates();
  }
  
  enum SiteContentType {
    OK, ERRORS, NOT_CREATED, EMPTY, RELEASE
  }
}
