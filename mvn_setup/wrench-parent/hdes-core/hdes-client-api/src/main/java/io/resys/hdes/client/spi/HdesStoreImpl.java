package io.resys.hdes.client.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.hdes.client.api.HdesStore;
import io.resys.hdes.client.api.ImmutableStoreEntity;
import io.resys.hdes.client.api.ImmutableStoreExceptionMsg;
import io.resys.hdes.client.api.ImmutableStoreState;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.exceptions.StoreException;
import io.resys.hdes.client.spi.util.HdesAssert;
import io.resys.hdes.client.spi.util.Sha2;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.api.envelope.DocContainer.DocObject;
import io.resys.thena.api.envelope.Message;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.spi.DocStoreImpl;
import io.resys.thena.spi.ThenaDocConfig;
import io.resys.thena.structures.doc.actions.DocObjectsQueryImpl;
import io.resys.thena.support.OidUtils;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;



public class HdesStoreImpl extends DocStoreImpl<HdesStoreImpl> implements HdesStore {
  
  public HdesStoreImpl(ThenaDocConfig config, DocStoreFactory<HdesStoreImpl> factory) {
    super(config, factory);
  }
  public static Builder<HdesStoreImpl> builder() {
    final DocStoreFactory<HdesStoreImpl> factory = (config, delegate) -> new HdesStoreImpl(config, delegate);
    return new Builder<HdesStoreImpl>(factory);
  }
  @Override
  protected StructureType getRepoType() {
    return StructureType.doc;
  }
  @Override
  public String getTenantId() {
    return super.getConfig().getRepoId();
  }
  @Override
  public Uni<StoreEntity> delete(DeleteAstType deleteType) {
    final Uni<StoreEntity> query = getEntityState(deleteType.getId());
    return query.onItem().transformToUni(state -> delete(state));
  }
  @Override
  public Uni<StoreEntity> create(CreateStoreEntity newType) {
    final var gid = newType.getId() == null ? gid(newType.getBodyType()) : newType.getId();
    
    final var entity = ImmutableStoreEntity.builder()
        .id(gid)
        .hash("")
        .body(newType.getBody())
        .bodyType(newType.getBodyType())
        .build();
    
    if(newType.getId() == null) {
      return createEntity(entity);  
    }
    
    return get().onItem().transformToUni(currentState -> {
      cantHaveEntityWithId(newType.getId(), currentState);  
      return updateEntity(entity);  
    });
  }
  @Override
  public Uni<StoreEntity> update(UpdateStoreEntity updateType) {
    final Uni<StoreEntity> query = getEntityState(updateType.getId());
    return query.onItem().transformToUni(state -> {
      final StoreEntity entity = ImmutableStoreEntity.builder()
          .from(state)
          .id(updateType.getId())
          .bodyType(state.getBodyType())
          .body(updateType.getBody())
          .build();
      return updateEntity(entity);
    });
  }
  @Override
  public QueryBuilder assetQuery() {
    final var that = this;
    return new QueryBuilder() {
      @Override public Uni<StoreEntity> get(String id) { return that.getEntityState(id); }
      @Override public Uni<StoreState> get() { return that.get(); }
    };
  }
  
  @Override
  public Uni<List<StoreEntity>> batch(ImportStoreEntity batchType) {
    final List<String> ids = new ArrayList<>();
    
    return get().onItem().transformToUni(currentState -> {

      final var createBuilder = config.getClient()
          .doc(getTenantId())
          .commit()
          .createManyDocs()
          .commitMessage("Save batch with new: " + batchType.getCreate().size() + " and updated: " + batchType.getUpdate().size() + " entries")
          .commitAuthor(config.getAuthor().get());
      
      final var modifyBuilder = config.getClient()
          .doc(getTenantId())
          .commit()
          .modifyManyBranches()
          .commitMessage("Save batch with new: " + batchType.getCreate().size() + " and updated: " + batchType.getUpdate().size() + " entries")
          .commitAuthor(config.getAuthor().get());
      
      // create new docs
      for(final var toBeSaved : batchType.getCreate()) {
        final var id = toBeSaved.getId();
        if(id != null) {
          cantHaveEntityWithId(id, currentState);
        }

        final var gid = toBeSaved.getId() == null ? gid(toBeSaved.getBodyType()) : toBeSaved.getId();
        final var entity = (StoreEntity) ImmutableStoreEntity.builder()
            .id(gid)
            .hash("")
            .body(toBeSaved.getBody())
            .bodyType(toBeSaved.getBodyType())
            .build();
        createBuilder
          .item()
          .docType(toBeSaved.getBodyType().name())
          .docId(gid)
          .branchName(DocObjectsQueryImpl.BRANCH_MAIN)
          .branchContent(JsonObject.mapFrom(ImmutableStoreEntity.builder()
              .from(entity)
              .hash(Sha2.blob(JsonObject.mapFrom(entity).encode()))
              .build()))
          .next();
        ids.add(gid);
      }
      
      // update docs
      for(final var toBeSaved : batchType.getUpdate()) {
        final var id = toBeSaved.getId();
        HdesAssert.isTrue(
            currentState.getDecisions().containsKey(id) ||
            currentState.getFlows().containsKey(id) ||
            currentState.getServices().containsKey(id), 
            () -> "Entity not found with id: '" + id + "'!");
        
        final var entity = (StoreEntity) ImmutableStoreEntity.builder()
            .id(id)
            .hash("")
            .body(toBeSaved.getBody())
            .bodyType(toBeSaved.getBodyType())
            .build();
        
        modifyBuilder
          .item()
          .docId(id)
          .branchName(DocObjectsQueryImpl.BRANCH_MAIN)
          .merge((old) -> JsonObject.mapFrom(ImmutableStoreEntity.builder()
              .from(entity)
              .hash(Sha2.blob(JsonObject.mapFrom(entity).encode()))
              .build()))
          .next();
        
        ids.add(entity.getId());
      }    
      //            
      final var created = batchType.getCreate().isEmpty() ? Uni.createFrom().voidItem() : createBuilder.build()
      .onItem().transform(resp -> {
        if(resp.getStatus() != CommitResultStatus.OK) {
          throw new StoreException("SAVE_FAIL", null, convertMessages("failed to create assets", resp.getMessages()));
        }
        return resp;
      });
      final var modified = batchType.getUpdate().isEmpty() ? Uni.createFrom().voidItem() : modifyBuilder.build()
      .onItem().transform(resp -> {
        if(resp.getStatus() != CommitResultStatus.OK) {
          throw new StoreException("SAVE_FAIL", null, convertMessages("failed to update assets", resp.getMessages()));
        }
        return resp;
      });
      
      return Uni.combine().all().unis(created, modified).asTuple();
    }).onItem().transformToUni(tuple -> get().onItem().transform(state -> state.findAll(ids)));
  }
  
  
  private String gid(AstBodyType type) {
    return OidUtils.gen();
  }

  private Uni<StoreEntity> delete(StoreEntity toBeDeleted) {
    throw new RuntimeException("not implemented");
  }

  private Uni<StoreEntity> updateEntity(StoreEntity toBeSaved) {
    return config.getClient()
      .doc(getTenantId())
      .commit()
      .modifyOneBranch()
      .docId(toBeSaved.getId())
      .branchName(DocObjectsQueryImpl.BRANCH_MAIN)
      .commitMessage("Save type: '" + toBeSaved.getBodyType() + "', with id: '" + toBeSaved.getId() + "'")
      .commitAuthor(config.getAuthor().get())
      .merge(old -> JsonObject.mapFrom(ImmutableStoreEntity.builder()
          .from(toBeSaved)
          .hash(Sha2.blob(JsonObject.mapFrom(toBeSaved).encode()))
          .build()))
      .build()    
      .onItem().transform(commit -> {
        if(commit.getStatus() == CommitResultStatus.OK) {
          return toBeSaved;
        }
        throw new StoreException("SAVE_FAIL", toBeSaved, convertMessages("failed update branch", commit.getMessages()));
      });
  }
  
  private Uni<StoreEntity> createEntity(StoreEntity toBeSaved) {
    return  config.getClient()
      .doc(getTenantId())
      .commit()
      .createOneDoc()
      .docId(toBeSaved.getId())
      .externalId(toBeSaved.getId())
      .docType(toBeSaved.getBodyType().name())
      .branchName(DocObjectsQueryImpl.BRANCH_MAIN)
      .commitMessage("Save type: '" + toBeSaved.getBodyType() + "', with id: '" + toBeSaved.getId() + "'")
      .commitAuthor(config.getAuthor().get())
      .branchContent(JsonObject.mapFrom(ImmutableStoreEntity.builder()
          .from(toBeSaved)
          .hash(Sha2.blob(JsonObject.mapFrom(toBeSaved).encode()))
          .build()))
      .build()    
      .onItem().transform(commit -> {
        if(commit.getStatus() == CommitResultStatus.OK) {
          return toBeSaved;
        }
        throw new StoreException("SAVE_FAIL", toBeSaved, convertMessages("failed update branch", commit.getMessages()));
      });
  }
  
  private Uni<StoreState> get() {
    return config.getClient().doc(config.getRepoId())
        .find().docQuery()
        .docType(AstBodyType.DT.name(), AstBodyType.FLOW.name(), AstBodyType.FLOW_TASK.name())
        .branchName(DocObjectsQueryImpl.BRANCH_MAIN)
        .findAll()
        .onItem().transform(state -> {
          if(state.getStatus() != QueryEnvelopeStatus.OK) {
            throw new StoreException("GET_REPO_STATE_FAIL", null, ImmutableStoreExceptionMsg.builder()
                .id(state.getRepo().getName())
                .value(state.getRepo().getId())
                .addAllArgs(state.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
                .build()); 
          }
          

          final var builder = ImmutableStoreState.builder();
          if(state.getObjects() == null) {
            return builder.build(); 
          }
          
          state.getObjects().accept((doc, docBranch, commit, commands, tree) -> {
            final var entity = docBranch.getValue().mapTo(ImmutableStoreEntity.class);
            switch(entity.getBodyType()) {
             case DT: builder.putDecisions(entity.getId(), entity); break;
             case FLOW_TASK: builder.putServices(entity.getId(), entity); break;
             case FLOW: builder.putFlows(entity.getId(), entity); break;
             default: throw new RuntimeException("Unknown type: " + entity.getBodyType() + "!");
            }
            return null;
          });
          return builder.build();
        });
  }

  private Uni<StoreEntity> getEntityState(String id) {
    return config.getClient().doc(config.getRepoId())
        .find().docQuery()
        .branchName(config.getHeadId())
        .get(id).onItem()
        .transform(state -> {
          if(state.getStatus() != QueryEnvelopeStatus.OK) {
            throw new StoreException("GET_FAIL", null, convertMessages1(state));
          }
          return  state.getObjects()
              .accept((doc, docBranch, commit, commands, tree) -> docBranch.getValue().mapTo(ImmutableStoreEntity.class))
              .iterator().next();
        });
  }
  
  
  private StoreExceptionMsg convertMessages(String id, List<Message> messages) {
    return ImmutableStoreExceptionMsg.builder()
        .id(id)
        .value("") //TODO
        .addAllArgs(messages.stream().map(message->message.getText()).collect(Collectors.toList()))
        .build();
  }

  private StoreExceptionMsg convertMessages1(QueryEnvelope<DocObject> state) {
    return ImmutableStoreExceptionMsg.builder()
        .id("STORE_STATE_ERROR").value("")
        .addAllArgs(state.getMessages().stream().map(message-> message.getText()).collect(Collectors.toList()))
        .build();
  }
  private void cantHaveEntityWithId(String id, StoreState currentState) {
    HdesAssert.isTrue(!currentState.getDecisions().containsKey(id), () -> "Entity of type 'decision' already exists with id: '" + id + "'!");
    HdesAssert.isTrue(!currentState.getFlows().containsKey(id), () -> "Entity of type 'flow' already exists with id: '" + id + "'!");
    HdesAssert.isTrue(!currentState.getServices().containsKey(id), () -> "Entity of type 'service' already exists with id: '" + id + "'!");
  }
}
