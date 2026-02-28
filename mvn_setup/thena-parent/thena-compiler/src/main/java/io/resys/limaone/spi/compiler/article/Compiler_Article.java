package io.resys.limaone.spi.compiler.article;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.ast.Article_AST.Link;
import io.resys.limaone.ast.Simple_AST;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.program.ArticleProgram.LocalizedSite;
import io.resys.limaone.program.Program;
import io.resys.limaone.program.Program.ProgramAssociation;
import io.resys.limaone.program.Program.ProgramMessage;
import io.resys.limaone.spi.compiler.CompilableUnit;
import io.resys.limaone.spi.compiler.article.Deltas.TopicData;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Compiler_Article implements CompilableUnit {

  private final AST_Parser parser;
  private final ModelWorld world;

  @Override
  public OpenProgram compile(NewArtifact resolution) {
    final var articleAST = parser.parseArticles().world(world).parse();
   
    resolution.ast(articleAST).id(world.getName()).name(world.getName()).build();
    
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
    final var sites = localeTopicData.keySet().stream().sorted()
      .map(locale -> visitLocale(locale, localeTopicData.get(locale), pathLinkData, resolution))
      .collect(Collectors.toList());
  
    return new OpenProgram() {
      @Override
      public String getId() {
        return articleAST.getId();
      }
      
      @Override
      public Simple_AST getAst() {
        return articleAST;
      }
      
      @Override
      public Program close(Artifact artifact) {
        final List<ProgramMessage> errors = artifact.getErrors();
        final List<ProgramAssociation> assocs = artifact.getAssociations();
        final var program = new ImmutableArticleProgram(articleAST, artifact.getProgramStatus(), errors, assocs, sites);
        
        return program;
      }
    };
  }

  private LocalizedSite visitLocale(String locale, List<TopicData> localeTopics, Map<String, List<Link>> pathLinkData, NewArtifact resolution) {
    final var builder = new LocalizedSiteBuilder(locale, pathLinkData, resolution);
    localeTopics.sort((e1, e2) -> e1.getFullPath().compareTo(e2.getFullPath()));
    localeTopics.forEach(builder::addTopic);
    return builder.build();
  }
}
