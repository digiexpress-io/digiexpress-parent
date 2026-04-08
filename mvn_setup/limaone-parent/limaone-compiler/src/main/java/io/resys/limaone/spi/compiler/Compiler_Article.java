package io.resys.limaone.spi.compiler;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.ast.Article_AST.Link;
import io.resys.limaone.ast.Simple_AST;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.program.ArticleProgram.LocalizedSite;
import io.resys.limaone.program.ImmutableLocalizedSite;
import io.resys.limaone.program.Program.ProgramAssociation;
import io.resys.limaone.spi.compiler.article.ArticleProgramImpl;
import io.resys.limaone.spi.compiler.article.Deltas.TopicData;
import io.resys.limaone.spi.compiler.article.ImmutableTopicData;
import io.resys.limaone.spi.compiler.article.LocalizedSiteBuilder;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Compiler_Article implements CompilableUnit {

  private final AST_Parser parser;
  private final ModelWorld world;

  @Override
  public ArtifactLink compile(NewArtifact resolution) {
    final var articleAST = parser.parseArticles().world(world).parse();
   
    final var artifact = resolution.ast(articleAST).id(world.getName()).name(world.getName());
    
    // topics
    final Map<String, List<TopicData>> localeTopicData = new HashMap<>();  
    for(final var md : articleAST.getValues()) {
      final TopicData topic = ImmutableTopicData.builder()
          .auth(md.getAuth())
          .path(md.getPath())
          .locale(md.getLocale())
          .headings(md.getHeadings())
          .images(md.getImages())
          .value(md.getValue())
          .build();
      var topics = localeTopicData.get(topic.getLocale());
      if(topics == null) {
        topics = new ArrayList<>();
        localeTopicData.put(topic.getLocale(), topics);
      }
      topics.add(topic);
    }
    
    // links
    final Map<String, List<Link>> pathLinkData = new HashMap<>();
    for(final var src : articleAST.getLinks()) {
      var links = pathLinkData.get(src.getPath());
      if(links == null) {
        links = new ArrayList<>();
        pathLinkData.put(src.getPath(), links);
      }
      links.add(src); 
    }
    
    // built result
    final var sites_stage_1 = localeTopicData.keySet().stream().sorted()
      .map(locale -> visitLocale(locale, localeTopicData.get(locale), pathLinkData, resolution))
      .collect(Collectors.toList());
    
    final var allWkLinks = sites_stage_1.stream()
        .collect(Collectors.toMap(site -> site.getLocale(), site -> site.getLinks().values().stream()
            .filter(e -> Boolean.TRUE.equals(e.getWorkflow()))
            .toList()));
    
    final var site_stage_2 = sites_stage_1.stream().map(e -> e.withWorkflowsInOtherLocales(allWkLinks))
        .map(e -> {
          final LocalizedSite casting = e;
          return casting;
        })
        .toList();
    
    artifact.build();
    
    return new ArtifactLink() {
      @Override
      public Simple_AST getAst() {
        return articleAST;
      }
      
      @Override
      public RuntimeLink accept(Artifact artifact) {
        
        return (runtime) -> {
          final List<ModelError> errors = artifact.getErrors();
          final List<ProgramAssociation> assocs = artifact.getAssociations();
          final var program = new ArticleProgramImpl(runtime, world.getName(), articleAST, artifact.getProgramStatus(), errors, assocs, site_stage_2);
          return program;
        };
      }
    };
  }

  private ImmutableLocalizedSite visitLocale(String locale, List<TopicData> localeTopics, Map<String, List<Link>> pathLinkData, NewArtifact resolution) {
    final var builder = new LocalizedSiteBuilder(locale, pathLinkData, resolution);
    localeTopics.sort((e1, e2) -> e1.getFullPath().compareTo(e2.getFullPath()));
    localeTopics.forEach(builder::addTopic);
    return builder.build();
  }
}
