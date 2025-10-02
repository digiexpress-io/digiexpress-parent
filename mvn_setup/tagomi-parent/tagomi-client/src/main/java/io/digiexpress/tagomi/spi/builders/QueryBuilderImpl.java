package io.digiexpress.tagomi.spi.builders;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.digiexpress.tagomi.api.TagomiStore;
import io.digiexpress.tagomi.api.entities.ImmutableTagomiContainer;
import io.digiexpress.tagomi.api.entities.TagomiContainer;
import io.digiexpress.tagomi.api.entities.TagomiContainer.IsTagomiObject;
import io.digiexpress.tagomi.api.entities.TagomiContainer.TagomiDocType;
import io.digiexpress.tagomi.api.entities.TagomiEntityContainer;
import io.digiexpress.tagomi.spi.TagomiStoreConfig;
import io.digiexpress.tagomi.spi.support.StoreException;
import io.digiexpress.tagomi.spi.support.StoreException.StoreExceptionMsg;
import io.resys.thena.api.entities.git.Blob;
import io.resys.thena.api.entities.git.Tree;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class QueryBuilderImpl implements TagomiStore.StateQuery {
  private final TagomiStoreConfig config;

  @Override
  public Uni<TagomiContainer> getState() {
    final var siteName = config.getTenantName() + ":" + config.getHeadName();
    return config.getClient().git(config.getTenantName()).tenants().get().onItem()
      .transformToUni(repo -> {
        if(repo == null) {
         return Uni.createFrom().item(ImmutableTagomiContainer.builder()
              .tagName(siteName)
              .build()); 
        }
      
        return config.getClient().git(config.getTenantName())
            .branch().branchQuery()
            .branchName(config.getHeadName())
            .docsIncluded()
            .get().onItem()
            .transform(state -> {
              if(state.getStatus() == QueryEnvelopeStatus.ERROR) {
                throw new StoreException("branch-error", null, StoreExceptionMsg.builder()
                    .id("thena-errors")
                    .value("failed to query from thena")
                    .messages(state.getMessages())
                    .build());
              }

              // Nothing present
              if(state.getObjects() == null) {
                return ImmutableTagomiContainer.builder()
                    .tagName(siteName)
                    .build();
              }
              
              final var commit = state.getObjects().getCommit();
              final var tree = state.getObjects().getTree();
              final var blobs = state.getObjects().getBlobs();
              final var builder = mapTree(tree, blobs, config);
              return builder
                  .commitId(commit.getId())
                  .tagName(siteName)
                  .commitAt(commit.getDateTime())
                  .build();
            });
      });
  }
  
  @Override
  public Uni<TagomiContainer> getStateByCommitId(String commitId) {
    return config.getClient()
        .git(config.getTenantName()).commit().commitQuery()
        .branchNameOrCommitOrTag(commitId)
        .docsIncluded()
        .get().onItem()
        .transform(state -> {
          if(state.getStatus() == QueryEnvelopeStatus.ERROR) {
            throw new StoreException("Can't find commit: '" + commitId + "'!", null);
          }
          
          final var tree = state.getObjects().getTree();
          final var blobs = state.getObjects().getBlobs();
          final var builder = mapTree(tree, blobs, config);
          
          return builder
              .tagName(config.getTenantName() + ":" + config.getHeadName() + ":" + commitId)
              .build();
        });
  }

  @Override
  public Uni<TagomiContainer> findAllStateObjectsById(List<String> ids, TagomiDocType type) {
    return config.getClient().git(config.getTenantName())
    .pull().pullQuery()
    .branchNameOrCommitOrTag(config.getHeadName())
    .docId(ids)
    .findAll().onItem()
    .transform(state -> {

      if(state.getStatus() != QueryEnvelopeStatus.OK) {
         throw new StoreException("branch-error", null, StoreExceptionMsg.builder()
             .id("thena-errors")
             .value("failed to query from thena")
             .messages(state.getMessages())
             .args(ids)
             .build());
      }
      
      final var builder = ImmutableTagomiContainer.builder();
      state.getObjects().getBlob().stream().forEach(blob -> {
        final var entity = config.getDeserializer().fromString(blob.getValue());
        mapAnyObject(entity, builder);
      });
      
      return builder
          .tagName(config.getTenantName() + ":" + config.getHeadName() + ":" + state.getObjects().getCommit().getId())
          .build();
      
    });
  }
  
  private static ImmutableTagomiContainer.Builder mapTree(Tree tree, Map<String, Blob> blobs, TagomiStoreConfig config) {
    final var builder = ImmutableTagomiContainer.builder();
    for(final var treeValue : tree.getValues().values()) {
      final var blob = blobs.get(treeValue.getBlob());
      final var entity = config.getDeserializer().fromString(blob.getValue());
      mapAnyObject(entity, builder);
    }
    return builder;
  }
  
  
  private static void mapAnyObject(IsTagomiObject entity, ImmutableTagomiContainer.Builder builder) {
    final var id = entity.getId();
    switch (entity.getDocType()) {
    case RESOURCE:
      builder.putResources(id, (TagomiContainer.Resource) entity);
      break;
    case LOCALE:
      builder.putLocales(id, (TagomiContainer.Locale) entity);
      break;
    case TAG:
      builder.putTags(id, (TagomiContainer.Tag) entity);
      break;
    case SERVICE:
      builder.putServices(id, (TagomiContainer.Service) entity);
      break;
    case TEMPLATE:
      builder.putTemplates(id, (TagomiContainer.Template) entity);
      break;
    default: throw new RuntimeException("Don't know how to convert entity: " + entity.toString() + "!");
    }
  }

  @Override
  public Uni<TagomiEntityContainer> getEntityState(String blobId, TagomiDocType type) {
    return config.getClient().git(config.getTenantName())
        .pull().pullQuery()
        .branchNameOrCommitOrTag(config.getHeadName())
        .docId(blobId)
        .get().onItem()
        .transform(state -> {
          if(state.getStatus() != QueryEnvelopeStatus.OK) {
            throw new StoreException(
                "Can't find object: '" + type + "' with id: '" + blobId + "'!", 
                null,
                StoreExceptionMsg.builder()
                  .id("failed-to-query-object-by-id")
                  .value("Failed to find target object by id and type!")
                  .messages(state.getMessages())
                  .args(Arrays.asList(blobId, type.name()))
                  .build()
            );
          }
          return config.getDeserializer().fromString(state.getObjects().getBlob().getValue());
        });
  }
}
