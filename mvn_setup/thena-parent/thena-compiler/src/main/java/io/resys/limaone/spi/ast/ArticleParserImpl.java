package io.resys.limaone.spi.ast;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.hash.Hashing;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.ast.AST_Parser.ArticleParser;
import io.resys.limaone.ast.Article_AST;
import io.resys.limaone.ast.Article_AST.Link;
import io.resys.limaone.ast.Article_AST.Markdown;
import io.resys.limaone.ast.ImmutableArticle_AST;
import io.resys.limaone.ast.ImmutableHeaders_AST;
import io.resys.limaone.ast.ImmutableLink;
import io.resys.limaone.ast.ImmutableMarkdown;
import io.resys.limaone.model.Article;
import io.resys.limaone.model.ArticleLink;
import io.resys.limaone.model.ArticleWorkflow;
import io.resys.limaone.model.Locale;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.spi.ast.AST_ParserImpl.AST_ParserProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RequiredArgsConstructor
@Slf4j
public class ArticleParserImpl implements AST_Parser.ArticleParser {

  public static String LINK_TYPE_WORKFLOW = "workflow";

  //private final List<Model<Locale>> locales = new ArrayList<>();
  private final Map<String, Model<Locale>> enablesLocales = new HashMap<>();
  private final AST_ParserProps props;
  private final StringBuilder hashBuilder = new StringBuilder();
  private ModelWorld world;

  @Override
  public ArticleParser world(ModelWorld world) {
    this.world = world;
    return this;
  }  

  public Article_AST parse() {
    final var result = ImmutableArticle_AST.builder()
        .addAllLocales(visitLocales().stream().map(e -> e.getBody().getValue()).collect(Collectors.toList()));
    
    for(final var article : world.getArticles().values()) {
      result.addAllValues(visitArticle(article));
    }
    
    for(final var link : world.getArticleLinks().values()) {
      result.addAllLinks(visitLinks(link));
    }
    for(final var link : world.getArticleWorkflows().values()) {
      result.addAllLinks(visitWorkflows(link));
    }
    
    return result
        .name(world.getName())
        .bodyType(BodyType.ARTICLE)
        .hash(Hashing.murmur3_128().hashString(hashBuilder.toString(), StandardCharsets.UTF_8).toString())
        .headers(ImmutableHeaders_AST.builder().build())
        .build();
  }

  private List<Link> visitWorkflows(Model<ArticleWorkflow> link) {
    final List<Link> result = new ArrayList<>();
    
    if(Boolean.TRUE.equals(link.getBody().getDisabled())) {
      return result;
    }
    
    if(!props.isDev() && Boolean.TRUE.equals(link.getBody().getDevMode())){
      return result;
    }
    
    final var usedLocales = link.getBody().getLabels().stream()
        .map(label -> label.getLocale())
        .collect(Collectors.toList());

    if(this.enablesLocales.values().stream().filter(l -> usedLocales.contains(l.getId())).findFirst().isEmpty()) {
      return result;
    }
    
    if(Boolean.TRUE.equals(link.getBody().getAssignable())) {
      for(final var label : link.getBody().getLabels()) {
        if(!enablesLocales.keySet().contains(label.getLocale())) {
          continue;
        }
      
        hashBuilder.append("workflow: ").append(link.getBodyHash());
        
        final var locale = enablesLocales.get(label.getLocale());
        final var resource = ImmutableLink.builder()
            .id(link.getId() + "-" + locale.getBody().getValue())
            .addLocale(locale.getBody().getValue())
            .desc(label.getLabelValue())
            .path("_") // reserve empty path
            .value(link.getBody().getValue())
            .startDate(link.getBody().getStartDate())
            .endDate(link.getBody().getEndDate())
            .anon(Boolean.TRUE.equals(link.getBody().getAnon()))
            .workflow(true)
            .global(false)
            .assignable(true)
            .flowName(link.getBody().getFlowName())
            .formName(link.getBody().getFormName())
            .formTag(link.getBody().getFormTag())
            .formId(link.getBody().getFormId())
            .type(LINK_TYPE_WORKFLOW)
            .build();
        result.add(resource);
      }
      return result;
    } else if(Boolean.FALSE.equals(link.getBody().getAssignable())) {
      return result;      
    }
    
    
    for(final var articleId : link.getBody().getArticles()) {
      final var article = world.getArticles().get(articleId);
      
      for(final var label : link.getBody().getLabels()) {
        if(!enablesLocales.keySet().contains(label.getLocale())) {
          continue;
        }
        
        hashBuilder.append("workflow: ").append(link.getBodyHash());
        final var locale = enablesLocales.get(label.getLocale());
        final var resource = ImmutableLink.builder()
            .id(link.getId() + "-" + locale.getBody().getValue())
            .addLocale(locale.getBody().getValue())
            .desc(label.getLabelValue())
            .path(visitArticlePath(article))
            .value(link.getBody().getValue())
            .startDate(link.getBody().getStartDate())
            .endDate(link.getBody().getEndDate())
            .anon(Boolean.TRUE.equals(link.getBody().getAnon()))
            .workflow(true)
            .global(false)
            .assignable(false)
            .flowName(link.getBody().getFlowName())
            .formName(link.getBody().getFormName())
            .formTag(link.getBody().getFormTag())
            .formId(link.getBody().getFormId())
            .type(LINK_TYPE_WORKFLOW)
            .build();
        result.add(resource);
      }
    }

    if(link.getBody().getArticles().isEmpty()) {
      for(Model<Article> article : world.getArticles().values()) {
        
        for(final var label : link.getBody().getLabels()) {
          if(!enablesLocales.keySet().contains(label.getLocale())) {
            continue;
          }
        
          hashBuilder.append("workflow: ").append(link.getBodyHash());
          final var locale = enablesLocales.get(label.getLocale());
          final var resource = ImmutableLink.builder()
              .id(link.getId() + "-" + locale.getBody().getValue())
              .addLocale(locale.getBody().getValue())
              .desc(label.getLabelValue())
              .path(visitArticlePath(article))
              .value(link.getBody().getValue())
              .startDate(link.getBody().getStartDate())
              .endDate(link.getBody().getEndDate())
              .anon(Boolean.TRUE.equals(link.getBody().getAnon()))
              .workflow(true).global(true).assignable(false)
              .flowName(link.getBody().getFlowName())
              .formName(link.getBody().getFormName())
              .formTag(link.getBody().getFormTag())
              .formId(link.getBody().getFormId())
              .type(LINK_TYPE_WORKFLOW)
              .build();
          result.add(resource);
        }
      }
    }
    
    return result;
  }
  
  private List<Link> visitLinks(Model<ArticleLink> link) {
    final List<Link> result = new ArrayList<>();

    if(!props.isDev() && Boolean.TRUE.equals(link.getBody().getDevMode())){
      return result;
    }
    
    final var usedLocales = link.getBody().getLabels().stream()
        .map(label -> label.getLocale())
        .collect(Collectors.toList());

    if(this.enablesLocales.values().stream().filter(l -> usedLocales.contains(l.getId())).findFirst().isEmpty()) {
      return result;
    }
    
    for(final var articleId : link.getBody().getArticles()) {
      final var article = world.getArticles().get(articleId);
      
      for(final var label : link.getBody().getLabels()) {
        if(!enablesLocales.keySet().contains(label.getLocale())) {
          continue;
        }
      
        hashBuilder.append("link: ").append(link.getBodyHash());
        final var locale = enablesLocales.get(label.getLocale());
        final var resource = ImmutableLink.builder()
            .id(link.getId() + "-" + locale.getBody().getValue())
            .addLocale(locale.getBody().getValue())
            .desc(label.getLabelValue())
            .path(visitArticlePath(article))
            .value(link.getBody().getValue())
            .workflow(false).anon(true).global(false).assignable(false)
            .type(link.getBody().getContentType())
            .build();
        result.add(resource);
        
      }
    }
    
    if(link.getBody().getArticles().isEmpty()) {
      for(Model<Article> article : world.getArticles().values()) {
        
        for(final var label : link.getBody().getLabels()) {
          if(!enablesLocales.keySet().contains(label.getLocale())) {
            continue;
          }
        
          hashBuilder.append("link: ").append(link.getBodyHash());
          final var locale = enablesLocales.get(label.getLocale());
          final var resource = ImmutableLink.builder()
              .id(link.getId() + "-" + locale.getBody().getValue())
              .addLocale(locale.getBody().getValue())
              .desc(label.getLabelValue())
              .path(visitArticlePath(article))
              .value(link.getBody().getValue())
              .workflow(false).anon(true).global(true).assignable(false)
              .type(link.getBody().getContentType())
              .build();
          result.add(resource);
        }
      }
    }
    
    return result;
  }
  
  private List<Markdown> visitArticle(Model<Article> article) {
    final String path = visitArticlePath(article);
    final List<Markdown> result = new ArrayList<>();

    if(!props.isDev() && Boolean.TRUE.equals(article.getBody().getDevMode())){
      return result;
    }

    for(final var page : world.getArticlePages().values()) {
      if(!page.getBody().getArticle().equals(article.getId())) {
        continue;
      }
      final var locale = enablesLocales.values().stream().filter(l -> page.getBody().getLocale().equals(l.getId())).findFirst();
      if(locale.isEmpty()) {
        continue;
      }
      if(!props.isDev() && Boolean.TRUE.equals(page.getBody().getDevMode())){
        continue;
      }
      
      final var content = page.getBody().getContent();
      final var ast = new MarkdownVisitor().visit(content);
      if(ast.getHeadings().stream().filter(Model -> Model.getLevel() == 1).findFirst().isEmpty()) {
        log.error("Failed to parse article '" + article.getBody().getName() + "', markdown must have atleast one h1(line starting with one # my super menu)");
      }
      
      hashBuilder.append("page: ").append(page.getBodyHash());

      result.add(ImmutableMarkdown.builder()
          .path(path)
          .auth(isAuth(article, null))
          .locale(locale.get().getBody().getValue())
          .value(content)
          .addAllHeadings(ast.getHeadings())
          .build());
    }
    
    return result;
  }
  
  private Boolean isAuth(Model<Article> src, List<String> visited)  {

    final var delegate = Optional.ofNullable(visited).orElse(new ArrayList<>());
    if(delegate.contains(src.getId())) {
      return false;
    }
    delegate.add(src.getId());
    
    if(Boolean.TRUE.equals(src.getBody().getAuthOnly())) {
      return true;
    }
    final var parentId = src.getBody().getParentId();
    if(parentId == null) {
      return false;
    }
    
    final var parent = this.world.getArticles().get(parentId);
    return isAuth(parent, delegate);
  }
  
  private String visitArticlePath(Model<Article> src) {

    final var visited = new ArrayList<String>();
    final StringBuilder path = new StringBuilder();
    Model<Article> article = src;
    do {
      
      if(path.length() > 0) {
        path.insert(0, "/");
      }
      path.insert(0, String.format("%03d", article.getBody().getOrder()) + "_" + article.getBody().getName());
      final var parentId = article.getBody().getParentId();
      if(visited.contains(parentId)) {
        log.error("Article broken, infinite loop near: '" + parentId + "'!");
        break;
      }
      visited.add(parentId);
      
      article = parentId == null ? null : world.getArticles().get(parentId);
      
      
    } while(article != null);

    return path.toString();
  }
  
  
  private Collection<Model<Locale>> visitLocales() {
    final var locales = this.world.getLocales().values().stream()
        .filter(l -> l.getBody().getEnabled())
        .map(locale -> {
          hashBuilder.append("locale: ").append(locale.getBodyHash());
          return locale;
        })
        .toList();
    
    this.enablesLocales.putAll(locales.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
    return locales;
  }

}
