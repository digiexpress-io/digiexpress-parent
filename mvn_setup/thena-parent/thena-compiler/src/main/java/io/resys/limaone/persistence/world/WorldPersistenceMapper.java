package io.resys.limaone.persistence.world;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.resys.limaone.model.Article;
import io.resys.limaone.model.ArticleLink;
import io.resys.limaone.model.ArticlePage;
import io.resys.limaone.model.ArticleTemplate;
import io.resys.limaone.model.ArticleWorkflow;
import io.resys.limaone.model.DecisionTable;
import io.resys.limaone.model.Deployment;
import io.resys.limaone.model.Flow;
import io.resys.limaone.model.FlowTask;
import io.resys.limaone.model.ImmutableDeployment;
import io.resys.limaone.model.ImmutableModel;
import io.resys.limaone.model.ImmutableModelWorld;
import io.resys.limaone.model.Locale;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.Body;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.thena.fs.entities.Blob;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.fs.entities.Tag;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class WorldPersistenceMapper {


  public static ImmutableModelWorld.Builder builder(Ref ref, List<Tag> tags) {
    final var builder = ImmutableModelWorld.builder()
        .refId(ref.getId())
        .commitId(ref.getCommitId())
        .name(ref.getRefName());
    
    ref.getTransitives().getTree().getTreeNodes()
      .stream()
      .map(ProxyBlob::new)
      .filter(p -> p.getBodyType() != null)
      .forEach(node ->  mapFrom(node, builder));
    
    for(final var tag : tags) {
      final var deployment = ImmutableDeployment.builder()
          .fromCommitId(tag.getCommitId())
          .fromRefId(tag.getRefId().orElse(""))
          .name(tag.getTagName())
          .external(false)
          .externalId(tag.getExternalId().orElse(null))
          .cockpitId(null)
          .createdBy(tag.getTagAuthor())
          .createdAt(tag.getTagCreatedAt())
          .description(tag.getTagDescription().orElse(""))
          .status(Deployment.BundleStatus.UNKNOWN)
          .build();

      final Model<Deployment> model = ImmutableModel.<Deployment>builder()
          .id(tag.getId())
          .bodyHash(tag.getCommitId())
          .bodyType(BodyType.DEPLOYMENT)
          .body(deployment)
          .build();
      builder.putDeployments(tag.getId(), model);
    }
    return builder;
  }
  
  
  
  public static ModelWorld mapFrom(Ref ref) {
    return mapFrom(ref, Collections.emptyList());
  }
  
  public static ModelWorld mapFrom(Ref ref, List<Tag> tags) {
    return builder(ref, tags).build();
  }
  
  public static void mapFrom(ProxyBlob node, ImmutableModelWorld.Builder builder) {
    switch(node.getBodyType()) {
      case ARTICLE: {
        final var p = node.mapTo(Article.class);
        builder.putArticles(p.getId(), p);
        return;
      }
      case LOCALE: {
        final var p = node.mapTo(Locale.class);
        builder.putLocales(p.getId(), p);
        return;
      }
      case ARTICLE_LINK: {
        final var p = node.mapTo(ArticleLink.class);
        builder.putArticleLinks(p.getId(), p);
        return;
      }
      case ARTICLE_WORKFLOW: {
        final var p = node.mapTo(ArticleWorkflow.class);
        builder.putArticleWorkflows(p.getId(), p);
        return;
      }
      case ARTICLE_PAGE: {
        final var p = node.mapTo(ArticlePage.class);
        builder.putArticlePages(p.getId(), p);
        return;
      }
      case ARTICLE_TEMPLATE: {
        final var p = node.mapTo(ArticleTemplate.class);
        builder.putArticleTemplates(p.getId(), p);
        return;
      }
      case FLOW: {
        final var p = node.mapTo(Flow.class);
        builder.putFlows(p.getId(), p);
        return;
      }
      case FLOW_TASK: {
        final var p = node.mapTo(FlowTask.class);
        builder.putFlowTasks(p.getId(), p);
        return;
      }
      case DECISION_TABLE: {
        final var p = node.mapTo(DecisionTable.class);
        builder.putDecisionTables(p.getId(), p);
        return;
      }
//      case DIALOB: {
//        final var p = node.mapTo(Dialob.class);
//        builder.putDialobs(p.getId(), p);
//        return;
//      }
//      case PRINTOUT: {
//        final var p = node.mapTo(Printout.class);
//        builder.putPrintouts(p.getId(), p);
//        return;
//      }
//      case PRINTOUT_PAGE: {
//        final var p = node.mapTo(PrintoutPage.class);
//        builder.putPrintoutPages(p.getId(), p);
//        return;
//      }
//      case PRINTOUT_SCRIPT: {
//        final var p = node.mapTo(PrintoutScript.class);
//        builder.putPrintoutScripts(p.getId(), p);
//        return;
//      }
//      case PRINTOUT_RESOURCE: {
//        final var p = node.mapTo(PrintoutResource.class);
//        builder.putPrintoutResources(p.getId(), p);
//        return;
//      }
      default: return;
    }
  }
  
  
  @Getter
  private static class ProxyBlob {
    private final Blob blob;
    private final Node node;
    private final BodyType bodyType;
    
    public ProxyBlob(Node node) {
      this.blob = node.getTransitives().getBlob();
      this.node = node;
      BodyType bodyType = null;
      try {
        bodyType = BodyType.valueOf(blob.getBlobType());
      } catch(Exception e) {
        log.trace("Blob bodyType: " + blob.getBlobType() + " not supported");
      }
      this.bodyType = bodyType;
    }
    
    public <T extends Body> Model<T> mapTo(Class<T> clazz) {
      Objects.requireNonNull(bodyType, () -> "Not supported mapping of object: " + JsonObject.mapFrom(node).encodePrettily());
      Objects.requireNonNull(blob.getBlobValue(), () -> "Object not loaded: " + JsonObject.mapFrom(node).encodePrettily());
      
      return ImmutableModel.<T>builder()
          .bodyType(bodyType)
          .bodyHash(node.getBlobId().get())
          .body(blob.getBlobValue().mapTo(clazz))
          .id(node.getObjectId())
          .build();
    } 
  }
}
