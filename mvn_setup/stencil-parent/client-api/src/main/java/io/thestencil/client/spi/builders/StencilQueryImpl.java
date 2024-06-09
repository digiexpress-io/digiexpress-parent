package io.thestencil.client.spi.builders;

import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.api.actions.DocQueryActions.Branches;
import io.resys.thena.api.envelope.DocContainer.DocTenantObjects;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.ImmutableSiteState;
import io.thestencil.client.api.StencilClient.Article;
import io.thestencil.client.api.StencilClient.Entity;
import io.thestencil.client.api.StencilClient.EntityBody;
import io.thestencil.client.api.StencilClient.EntityType;
import io.thestencil.client.api.StencilClient.Link;
import io.thestencil.client.api.StencilClient.Locale;
import io.thestencil.client.api.StencilClient.Page;
import io.thestencil.client.api.StencilClient.Template;
import io.thestencil.client.api.StencilClient.Workflow;
import io.thestencil.client.api.StencilComposer.SiteContentType;
import io.thestencil.client.api.StencilComposer.SiteState;
import io.thestencil.client.api.StencilStore.StencilQuery;
import io.thestencil.client.spi.StencilDeserializer;
import io.thestencil.client.spi.StencilStoreImpl;
import io.thestencil.client.spi.exceptions.QueryException;
import io.thestencil.client.spi.exceptions.RefException;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class StencilQueryImpl implements StencilQuery {
  private final StencilStoreImpl store;
  private final StencilDeserializer deserializer;
  
  @Override
  public Uni<SiteState> head() {
    final var siteName = store.getConfig().getRepoId() + ":" + Branches.main.name();
    
    return store.getConfig().getClient()
      .doc(store.getConfig().getRepoId())
      .find().docQuery().branchMain()
      .docType(
          EntityType.LOCALE.name(), 
          EntityType.LINK.name(), 
          EntityType.ARTICLE.name(), 
          EntityType.WORKFLOW.name(), 
          EntityType.RELEASE.name(), 
          EntityType.PAGE.name(), 
          EntityType.TEMPLATE.name())
      .findAll()
      .onItem()
      .transform(state -> {
        if(state.getStatus() == QueryEnvelopeStatus.ERROR) {
          throw new RefException(siteName, state);
        }

        // Nothing present
        if(state.getObjects() == null) {
          return ImmutableSiteState.builder()
              .name(siteName)
              .contentType(SiteContentType.EMPTY)
              .build();
        }
        

        final var builder = mapTree(state.getObjects(), deserializer);
        return builder
            .name(siteName)
            .contentType(SiteContentType.OK)
            .build();
      });

  }
  
  @SuppressWarnings("unchecked")
  public static ImmutableSiteState.Builder mapTree(DocTenantObjects tenantObject, StencilDeserializer deserializer) {
    final var builder = ImmutableSiteState.builder();
    for(final var treeValue : tenantObject.getBranches().values()) {
      final var entity = deserializer.fromString(treeValue.getValue());
      final var id = entity.getId();
      
      switch (entity.getType()) {
      case ARTICLE:
        builder.putArticles(id, (Entity<Article>) entity);
        break;
      case LINK:
        builder.putLinks(id, (Entity<Link>) entity);
        break;
      case LOCALE:
        builder.putLocales(id, (Entity<Locale>) entity);
        break;
      case PAGE:
        builder.putPages(id, (Entity<Page>) entity);
        break;
      case RELEASE:
        break;
      case WORKFLOW:
        builder.putWorkflows(id, (Entity<Workflow>) entity);
        break;
      case TEMPLATE:
        builder.putTemplates(id, (Entity<Template>) entity);
        break;
      default: throw new RuntimeException("Don't know how to convert entity: " + entity.toString() + "!");
      }
    }
    return builder;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends EntityBody> Uni<List<Entity<T>>> head(List<String> ids, EntityType type) {

    
    return store.getConfig().getClient()
    .doc(store.getConfig().getRepoId())
    .find().docQuery().branchMain()
    .docType(
        EntityType.LOCALE.name(), 
        EntityType.LINK.name(), 
        EntityType.ARTICLE.name(), 
        EntityType.WORKFLOW.name(), 
        EntityType.RELEASE.name(), 
        EntityType.PAGE.name(), 
        EntityType.TEMPLATE.name())
    .findAll(ids).onItem()
    .transform(state -> {
      
      if(state.getStatus() != QueryEnvelopeStatus.OK) {
        throw new QueryException(String.join(",", ids), type, state);  
      }
      
      return state.getObjects().getBranches().values().stream()
        .map(blob -> (Entity<T>) deserializer.fromString(type, blob.getValue()))
        .collect(Collectors.toList());
      
    });
  }
}
