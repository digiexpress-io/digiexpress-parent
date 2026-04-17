package io.resys.limaone.persistence.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.resys.limaone.fs.ImmutableDirentBase;
import io.resys.limaone.fs.ImmutableWorldFs;
import io.resys.limaone.fs.WorldFs;
import io.resys.limaone.model.Article;
import io.resys.limaone.model.ArticlePage;
import io.resys.limaone.model.Locale;
import io.resys.limaone.model.Model.BodyType;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Ref;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class WorldFsFactory {
  private final Ref ref;
  private final ImmutableWorldFs.Builder world = ImmutableWorldFs.builder();
  private final List<Node> parseLater = new ArrayList<>();
  private final Map<String, Article> article_cache = new HashMap<>();
  private final Map<String, Locale> locale_cache = new HashMap<>();
  
  
  public WorldFs create() {
    final var nodes = ref.getTransitives().getTree().getTreeNodes();
    for(final var node : nodes) {
      final var bodyType = getBodyType(node);
      if(bodyType.isEmpty()) {
        continue;
      }
      
      final var dirent = visitFirstLoadNode(node, bodyType.get());
      if(dirent.isPresent()) {
        world.addDirents(dirent.get());
      } else {
        parseLater.add(node);
      }
    }
    
    for(final var later : parseLater) {
      final var bodyType = getBodyType(later).orElseThrow();
      final var dirent = visitSecondLoadNode(later, bodyType);
      world.addDirents(dirent);
      
    }    
    return world.build();
  }
  
  private ImmutableDirentBase visitSecondLoadNode(Node node, BodyType bodyType) {

    switch (bodyType) {
    case ARTICLE_PAGE: return createArticlePageDirent(node, bodyType);
    default: throw new IllegalArgumentException(bodyType + " is not recognized as valid node in second load!");
    }
  }
  
  private Optional<ImmutableDirentBase> visitFirstLoadNode(Node node, BodyType bodyType) {

    switch (bodyType) {
    case LOCALE: return Optional.of(createLocaleDirent(node, bodyType));
    case ARTICLE: return Optional.of(createArticleDirent(node, bodyType));
    case ARTICLE_PAGE: return Optional.empty();
    default: return Optional.of(createAnyDirent(node, bodyType));
    }
  }
  
  
  private ImmutableDirentBase createArticlePageDirent(Node node, BodyType bodyType) {
    final var blob = node.getTransitives().getBlob();
    final var page = blob.getBlobValue().mapTo(ArticlePage.class);
    final var articleId = page.getArticle();
    final var localeId = page.getLocale();
    final var article = getArticle(articleId);
    final var locale = getLocale(localeId);

    
    final var name = locale.getValue();
    final var dirent = ImmutableDirentBase.builder()
        .id(node.getObjectId())
        .fullPath(article.getName() + "/" + name)
        .name(name)
        .type(bodyType)
        .build();
      return dirent;
  }
  
  private ImmutableDirentBase createLocaleDirent(Node node, BodyType bodyType) {
    
    final var name = node.getNodeName();
    final var dirent = ImmutableDirentBase.builder()
        .id(node.getObjectId())
        .fullPath("locales/" + name)
        .name(name)
        .type(bodyType)
        .build();
    
    final var blob = node.getTransitives().getBlob();
    final var locale = blob.getBlobValue().mapTo(Locale.class);
    locale_cache.put(node.getObjectId(), locale);
    return dirent;
  }
  
  private ImmutableDirentBase createArticleDirent(Node node, BodyType bodyType) {
    final var blob = node.getTransitives().getBlob();
    final var dirent = ImmutableDirentBase.builder()
      .id(node.getObjectId())
      .fullPath(node.getFullPath())
      .name(node.getNodeName())
      .type(bodyType)
      .build();
    
    final var article = blob.getBlobValue().mapTo(Article.class);
    article_cache.put(node.getObjectId(), article);
    return dirent;
  }
  
  
  private ImmutableDirentBase createAnyDirent(Node node, BodyType bodyType) {
    final var dirent = ImmutableDirentBase.builder()
      .id(node.getObjectId())
      .fullPath(node.getFullPath())
      .name(node.getNodeName())
      .type(bodyType)
      .build();
    return dirent;
  }
  
  /**
   * resolve dirent type based on blob or other props
   */
  private Optional<BodyType> getBodyType(Node node) {
    if(node.getBlobId().isEmpty()) {
      return Optional.of(BodyType.FOLDER);
    }
    
    final var blob = node.getTransitives().getBlob();    
    try {
      final var type = BodyType.valueOf(blob.getBlobType());
      return Optional.of(type);
    } 
    catch(Exception e) {
      log.warn("Failed to get node type from blob: {}, message: {}", node.getNodeName(), e.getMessage());
      return Optional.empty();
    }
  }
  
  private Article getArticle(String articleId) {
    return article_cache.get(articleId); 
  }
  
  private Locale getLocale(String localeId) {
    return locale_cache.get(localeId); 
  }
}
