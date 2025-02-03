package io.thestencil.client.spi;

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

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

import io.thestencil.client.api.ImmutableImageResource;
import io.thestencil.client.api.ImmutableMarkdown;
import io.thestencil.client.api.ImmutableMarkdowns;
import io.thestencil.client.api.Markdowns;
import io.thestencil.client.api.StencilClient.MarkdownBuilder;
import io.thestencil.client.api.StencilComposer.SiteState;
import io.thestencil.client.spi.staticontent.support.ParserAssert;
import io.thestencil.client.spi.staticontent.visitors.CSVLinksVisitor;
import io.thestencil.client.spi.staticontent.visitors.MarkdownException;
import io.thestencil.client.spi.staticontent.visitors.MarkdownVisitor;
import io.thestencil.client.spi.staticontent.visitors.SiteStateVisitor;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class MarkdownBuilderImpl implements MarkdownBuilder {
  private Markdowns jsonOfSiteState;
  private ImmutableMarkdowns.Builder fromFiles;
  private LocalDateTime offset;
  
  
  @Override
  public MarkdownBuilder targetDate(LocalDateTime offset) {
    this.offset = offset;
    return this;
  }
  
  @Override
  public MarkdownBuilder md(String path, byte[] value) {
    if(fromFiles == null) {
      fromFiles = ImmutableMarkdowns.builder();
    }
    if (!path.toLowerCase().endsWith(".md")) {
      final var cleanName = path.toLowerCase();
      if(cleanName.equals("links.csv")) {
        fromFiles.addAllLinks(new CSVLinksVisitor(path).visit(value));
      } else if(cleanName.startsWith("images/")) {
        fromFiles.addImages(ImmutableImageResource.builder().path(path).value(value).build());
      }
      return this;
    }

    final var fragments = path.split("\\/");
    if (!(fragments.length == 2 || fragments.length == 3)) {
      throw new MarkdownException("Markdown: '" + path + "' must have [2..3] (level2/level2/en.md) levels but was: '"
          + fragments.length + "'!");
    }
    final var fileName = fragments[fragments.length - 1];
    if (fileName.length() != 5) {
      throw new MarkdownException(
          "Markdown: '" + path + "' must be name as <path>/<locale>.md but was: '" + path + "'!");
    }
    final var locale = fileName.substring(0, 2);
    
    try {
      final var content = new String(value, StandardCharsets.UTF_8);
      final String cleanPath;
      if (fragments.length == 2) {
        cleanPath = fragments[0];
      } else {
        cleanPath = fragments[0] + "/" + fragments[1];
      }

      final var ast = new MarkdownVisitor().visit(content);
      if(ast.getHeadings().stream().filter(entity -> entity.getLevel() == 1).findFirst().isEmpty()) {
        throw new MarkdownException("markdown must have atleast one h1(line starting with one # my super menu)");
      }
      
      fromFiles.addValues(ImmutableMarkdown.builder()
          .path(cleanPath)
          .locale(locale)
          .value(content)
          .addAllHeadings(ast.getHeadings())
          .addAllImages(ast.getImages())
          .build());
      
      
      return this;
    } catch (Exception e) {
      throw new MarkdownException("Failed to parse markdown: '" + path + "', error: " + e.getMessage(), e);
    }
  }
  
  @Override
  public MarkdownBuilder json(String jsonOfSiteState, boolean dev) {
    final var site = new JsonObject(jsonOfSiteState).mapTo(SiteState.class);
    this.jsonOfSiteState = new SiteStateVisitor(dev, Optional.ofNullable(offset)).visit(site);
    return this;
  }
  
  @Override
  public MarkdownBuilder json(SiteState jsonOfSiteState, boolean dev) {
    this.jsonOfSiteState = new SiteStateVisitor(dev, Optional.ofNullable(offset)).visit(jsonOfSiteState);
    return this;
  }
  
  @Override
  public Markdowns build() {
    ParserAssert.isTrue(jsonOfSiteState != null || fromFiles != null, () -> "json or md files must be provided!");
    ParserAssert.isTrue(jsonOfSiteState == null || fromFiles == null, () -> "json or md files both can't be provided!");
    
    if(fromFiles != null) {
      return fromFiles.tagName("md-files").build();
    }
    return this.jsonOfSiteState;
  }
}
