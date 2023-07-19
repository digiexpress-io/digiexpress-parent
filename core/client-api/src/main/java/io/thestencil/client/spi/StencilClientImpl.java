package io.thestencil.client.spi;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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

import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.ImmutableImageResource;
import io.thestencil.client.api.ImmutableMarkdown;
import io.thestencil.client.api.ImmutableMarkdowns;
import io.thestencil.client.api.ImmutableStencilConfig;
import io.thestencil.client.api.Markdowns;
import io.thestencil.client.api.MigrationBuilder.Sites;
import io.thestencil.client.api.StencilClient;
import io.thestencil.client.api.StencilComposer.SiteState;
import io.thestencil.client.api.StencilStore;
import io.thestencil.client.spi.beans.SitesBean;
import io.thestencil.client.spi.staticontent.support.ParserAssert;
import io.thestencil.client.spi.staticontent.visitors.CSVLinksVisitor;
import io.thestencil.client.spi.staticontent.visitors.ImmutableLinkData;
import io.thestencil.client.spi.staticontent.visitors.ImmutableTopicData;
import io.thestencil.client.spi.staticontent.visitors.MarkdownException;
import io.thestencil.client.spi.staticontent.visitors.MarkdownVisitor;
import io.thestencil.client.spi.staticontent.visitors.SiteStateVisitor;
import io.thestencil.client.spi.staticontent.visitors.SiteVisitor;
import io.thestencil.client.spi.staticontent.visitors.SiteVisitor.LinkData;
import io.thestencil.client.spi.staticontent.visitors.SiteVisitor.TopicData;
import io.thestencil.client.spi.staticontent.visitors.SiteVisitorDefault;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StencilClientImpl implements StencilClient {
  private final StencilStore store;

  @Override
  public MarkdownBuilder markdown() {
    return new MarkdownBuilder() {
      private Markdowns jsonOfSiteState;
      private ImmutableMarkdowns.Builder fromFiles;
      
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
        try {
          final var site = store.getConfig().getObjectMapper().readValue(jsonOfSiteState, SiteState.class);
          this.jsonOfSiteState = new SiteStateVisitor(dev).visit(site);
        } catch (IOException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
        return this;
      }
      
      @Override
      public MarkdownBuilder json(SiteState jsonOfSiteState, boolean dev) {
        this.jsonOfSiteState = new SiteStateVisitor(dev).visit(jsonOfSiteState);
        return this;
      }
      
      @Override
      public Markdowns build() {
        ParserAssert.isTrue(jsonOfSiteState != null || fromFiles != null, () -> "json or md files must be provided!");
        ParserAssert.isTrue(jsonOfSiteState == null || fromFiles == null, () -> "json or md files both can't be provided!");
        
        if(fromFiles != null) {
          return fromFiles.build();
        }
        return this.jsonOfSiteState;
      }
    };
  }
  @Override
  public SitesBuilder sites() {
    return new SitesBuilder() {
      private final SiteVisitor visitor = new SiteVisitorDefault(obj -> writeAsString(obj, store.getConfig().getObjectMapper()));
      private String imageUrl;
      private Long created;
      private Markdowns markdowns;
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
      public SitesBuilder created(long created) {
        this.created = created;
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

        markdowns.getValues()
        .forEach(value -> topic(builder -> builder
        .path(value.getPath())
        .locale(value.getLocale())
        .headings(value.getHeadings())
        .images(value.getImages())
        .value(value.getValue())
        .build()));
      
        markdowns.getLinks()
        .forEach(link -> link.getLocale().forEach(locale -> link(builder -> builder
          .id(link.getId())
          .path(link.getPath())
          .locale(locale)
          .type(link.getType())
          .name(link.getDesc())
          .global(link.getGlobal())
          .value(link.getValue())
          .workflow(link.getType().equals(SiteStateVisitor.LINK_TYPE_WORKFLOW))
          .build()
        )));
        
        final var visited = visitor.visit(imageUrl);
        final var content = visited.getSites().stream().collect(
          Collectors.toMap(e -> e.getLocale(), e -> e)
        );
        return SitesBean.builder()
            .created(created)
            .sites(content)
            .build();
      }
    };
  }
  

  @Override
  public ClientRepoBuilder repo() {
    return new ClientRepoBuilder() {
    private String repoName;
    private String headName;
    @Override
    public ClientRepoBuilder repoName(String repoName) {
      this.repoName = repoName;
      return this;
    }
    @Override
    public ClientRepoBuilder headName(String headName) {
      this.headName = headName;
      return this;
    }
    @Override
    public Uni<StencilClient> create() {
      StencilAssert.notNull(repoName, () -> "repoName must be defined!");
      return store.repo().repoName(repoName).headName(headName).create()
          .onItem().transform(newConfig -> {
            return new StencilClientImpl(newConfig);
          });
    }
    @Override
    public StencilClient build() {
      StencilAssert.notNull(repoName, () -> "repoName must be defined!");
      final var newConfig = store.repo().repoName(repoName).headName(headName).build();
      return new StencilClientImpl(newConfig);
    }
  };
  }

  @Override
  public StencilStore getStore() {
    return store;
  }
  

  private static String writeAsString(Object anyObject, ObjectMapper objectMapper) {
    try {
      return objectMapper.writeValueAsString(anyObject);
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    private ImmutableStencilConfig.Builder config = ImmutableStencilConfig.builder();
    private boolean inmemory = false;
    public Builder inmemory() {
      inmemory = true;
      return this;
    }
    
    public Builder config(Consumer<ImmutableStencilConfig.Builder> config) {
      config.accept(this.config);
      return this;
    }
    public Builder defaultObjectMapper() {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new GuavaModule());
      objectMapper.registerModule(new JavaTimeModule());
      objectMapper.registerModule(new Jdk8Module());
      config.objectMapper(objectMapper);
      return this;
    }
    public StencilClientImpl build() {
      final var store = inmemory ? new StencilStoreInMemory(config) : new StencilStoreImpl(this.config.build());
      return new StencilClientImpl(store);
    }
  }
}
