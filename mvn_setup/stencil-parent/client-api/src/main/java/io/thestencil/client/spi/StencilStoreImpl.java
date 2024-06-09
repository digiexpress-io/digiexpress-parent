package io.thestencil.client.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.spi.DocStoreImpl;
import io.resys.thena.spi.ThenaDocConfig;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.ImmutableEntityState;
import io.thestencil.client.api.StencilClient.Entity;
import io.thestencil.client.api.StencilClient.EntityBody;
import io.thestencil.client.api.StencilClient.EntityType;
import io.thestencil.client.api.StencilStore;
import io.thestencil.client.spi.builders.StencilQueryImpl;
import io.thestencil.client.spi.exceptions.DeleteException;
import io.thestencil.client.spi.exceptions.QueryException;
import io.thestencil.client.spi.exceptions.SaveException;
import io.vertx.core.json.JsonObject;


public class StencilStoreImpl extends DocStoreImpl<StencilStoreImpl> implements StencilStore {
  
  private final StencilDeserializer deserializer = new StencilDeserializer();
  
  public StencilStoreImpl(ThenaDocConfig config, DocStoreFactory<StencilStoreImpl> factory) {
    super(config, factory);
  }
  public static DocStoreImpl.Builder<StencilStoreImpl> builder() {
    final DocStoreFactory<StencilStoreImpl> factory = (config, delegate) -> new StencilStoreImpl(config, delegate);
    return new DocStoreImpl.Builder<StencilStoreImpl>(factory);
  }
  @Override
  public StencilQuery stencilQuery() {
    return new StencilQueryImpl(this, deserializer);
  }
  @Override
  public StoreTenantQuery<StencilStoreImpl> tenantQuery() {
    return super.query();
  }
  @Override
  protected StructureType getRepoType() {
    return StructureType.doc;
  }
  
  @Override
  public StencilStoreImpl withTenantId(String tenantId) {
    return super.withTenantId(tenantId);
  }
  @Override
  public <T extends EntityBody> Uni<Entity<T>> delete(Entity<T> toBeDeleted) {
    return config.getClient().doc(config.getRepoId()).commit()
        .deleteOneDoc()
        .commitMessage("delete type: '" + toBeDeleted.getType() + "', with id: '" + toBeDeleted.getId() + "'")
        .commitAuthor(config.getAuthor().get())
        .docId(toBeDeleted.getId())
        .build().onItem().transform(commit -> {
          if(commit.getStatus() == CommitResultStatus.OK) {
            return toBeDeleted;
          }
          throw new DeleteException(toBeDeleted, commit);
        });
  }
  
  @Override
  public <T extends EntityBody> Uni<EntityState<T>> get(String blobId, EntityType type) {
    return config.getClient().doc(config.getRepoId())
         .find().docQuery()
         .docType(type.name())
         .branchMain()
         .get(blobId).onItem().transform(state -> {
          if(state.getStatus() != QueryEnvelopeStatus.OK) {
            throw new QueryException(blobId, type, state);  
          }
          final Entity<T> start = state.getObjects().accept((DocBranch branch) -> deserializer.fromString(type, branch.getValue()));
          
          return ImmutableEntityState.<T>builder()
              .src(state)
              .entity(start)
              .build();
        });
  }
  
  @Override
  public <T extends EntityBody> Uni<Entity<T>> save(Entity<T> toBeSaved) {
    return config.getClient().doc(config.getRepoId()).commit()
      .modifyOneBranch()
      .branchMain()
      .docId(toBeSaved.getId())
      .commitMessage("update type: '" + toBeSaved.getType() + "', with id: '" + toBeSaved.getId() + "'")
      .commitAuthor(config.getAuthor().get())
      .replace(JsonObject.mapFrom(toBeSaved))
      .build().onItem().transform(commit -> {
        if(commit.getStatus() == CommitResultStatus.OK) {
          return toBeSaved;
        }
        throw new SaveException(toBeSaved, commit);
      });
  }
  
  @Override
  public <T extends EntityBody> Uni<Entity<T>> create(Entity<T> toBeSaved) {
    return config.getClient().doc(config.getRepoId()).commit()
      .createOneDoc()
      .commitMessage("create type: '" + toBeSaved.getType() + "', with id: '" + toBeSaved.getId() + "'")
      .commitAuthor(config.getAuthor().get())
      
      .docType(toBeSaved.getType().name())
      .docId(toBeSaved.getId())
      .branchMain()
      .branchContent(JsonObject.mapFrom(toBeSaved))
      
      .build().onItem().transform(commit -> {
        if(commit.getStatus() == CommitResultStatus.OK) {
          return toBeSaved;
        }
        throw new SaveException(toBeSaved, commit);
      });
  }

  @Override
  public Uni<List<Entity<?>>> saveAll(List<Entity<?>> entities) {
    final var commitBuilder = config.getClient()
        .doc(config.getRepoId()).commit()
        .modifyManyBranches()
        .commitAuthor(config.getAuthor().get());
    

    for(final var target : entities) {
      commitBuilder.item()
        .branchMain()
        .docId(target.getId())
        .replace(JsonObject.mapFrom(target))
        .next();
    }
    
    final Entity<?> first = entities.iterator().next();    
    return commitBuilder
        .commitMessage("update type: '" + first.getType() + "', with id: '" + first.getId() + "'")
        .build().onItem().transform(commit -> {
          if(commit.getStatus() == CommitResultStatus.OK) {
            return entities;
          }
          throw new SaveException(entities, commit);
        });
  }
  
  @Override
  public Uni<List<Entity<?>>> batch(BatchCommand batch) {
    if(batch.getToBeDeleted().isEmpty() && batch.getToBeDeleted().isEmpty() && batch.getToBeCreated().isEmpty()) {
      return Uni.createFrom().item(Collections.emptyList());
    }
    
    final List<Entity<?>> all = new ArrayList<Entity<?>>();
    final var commitBuilder = config.getClient().doc(config.getRepoId())
        .commit()
        .modifyManyBranches()
        .commitMessage("Saving batch")
        .commitAuthor(config.getAuthor().get());

    for(final var target : batch.getToBeDeleted()) {
      commitBuilder.item().docId(target.getId()).removeDoc().next();
      
      all.add((Entity<?>) target);
    }
    for(final var target : batch.getToBeSaved()) {      
      commitBuilder.item().docId(target.getId())
      .branchMain()
        .docId(target.getId())
        .replace(JsonObject.mapFrom(target))
      .next();
      all.add((Entity<?>) target);
    }
    
    for(final var target : batch.getToBeCreated()) {
      commitBuilder.item().docId(target.getId())
        .branchMain()
        .docId(target.getId())
        .replace(JsonObject.mapFrom(target))
        .docType(target.getType().name())
        .createDoc()
      .next();
      
      all.add((Entity<?>) target);
    }
    return commitBuilder
        .commitMessage("batch" + 
            " created: '" + batch.getToBeCreated().size() + "',"+
            " updated: '" + batch.getToBeSaved().size() + "',"+
            " deleted: '" + batch.getToBeDeleted().size() + "'")
        .commitAuthor(config.getAuthor().get())
        .build().onItem().transform(commit -> {
          if(commit.getStatus() == CommitResultStatus.OK) {
            return Collections.unmodifiableList(all);
          }
          throw new SaveException(all, commit);
        });
  }
}
