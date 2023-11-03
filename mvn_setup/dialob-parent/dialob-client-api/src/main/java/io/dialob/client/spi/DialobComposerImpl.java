package io.dialob.client.spi;

import java.util.List;

import io.dialob.api.form.FormPutResponse;
import io.dialob.client.api.DialobClient;
import io.dialob.client.api.DialobComposer;
import io.dialob.client.api.DialobDocument.DocumentType;
import io.dialob.client.api.DialobDocument.FormDocument;
import io.dialob.client.api.DialobDocument.FormReleaseDocument;
import io.dialob.client.api.DialobStore.StoreEntity;
import io.dialob.client.api.DialobStore.StoreState;
import io.dialob.client.api.ImmutableComposerReleaseState;
import io.dialob.client.api.ImmutableComposerState;
import io.dialob.client.api.ImmutableUpdateStoreEntity;
import io.dialob.client.spi.composer.ComposerEntityMapper;
import io.dialob.client.spi.composer.CopyAsEntityVisitor;
import io.dialob.client.spi.composer.CreateFormDocumentVisitor;
import io.dialob.client.spi.composer.CreateReleaseVisitor;
import io.dialob.client.spi.composer.DeleteEntityVisitor;
import io.dialob.client.spi.composer.GetComposerDocumentState;
import io.dialob.client.spi.composer.ImportReleaseVisitor;
import io.dialob.client.spi.support.DialobAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DialobComposerImpl implements DialobComposer {

  private final DialobClient client;
  
  @Override
  public Uni<ComposerState> get() {
    return client.store().query().get().onItem().transform(this::composerState);
  }
  @Override
  public Uni<ComposerDocumentState> get(String idOrName) {
    return get(idOrName, null);
  }
  @Override
  public Uni<ComposerDocumentState> get(String idOrName, String version) {
    return client.store().query().get().onItem().transform(this::composerState)
        .onItem().transform(state -> new GetComposerDocumentState(state, idOrName, version).get());
  }
  @Override
  public Uni<ComposerDocumentState> create(FormDocument asset) {
    return client.store().query().get().onItem().transform(this::composerState)
        .onItem().transformToUni(state -> client.store().batch(new CreateFormDocumentVisitor(state, asset, client).visit()))
        .onItem().transform(savedEntity -> this.documentState(savedEntity));
  }

  @Override
  public Uni<ComposerReleaseState> create(CreateComposerRelease asset) {
    return client.store().query().get().onItem().transform(this::composerState)
        .onItem().transform(state -> new CreateReleaseVisitor(client.getConfig(), state).visit(asset))
        .onItem().transformToUni(release -> client.store().create(release.getStoreEntity())
          .onItem().transform(saved ->
            ImmutableComposerReleaseState.builder()
              .from(release.getReleaseState())
              .id(saved.getId())
              .build()
          )
        );
  }

  @Override
  public Uni<ComposerDocumentState> update(FormDocument asset) {
    DialobAssert.notNull(asset, () -> "asset can't be null!");
    DialobAssert.notNull(asset.getData().getId(), () -> "asset.value.id can't be null!");
    DialobAssert.notNull(asset.getData().getRev(), () -> "asset.value.rev can't be null!");
    
    final var doc = asset.getData();
    final var update = ImmutableUpdateStoreEntity.builder()
        .id(doc.getId())
        .version(doc.getRev())
        .body(this.client.getConfig().getMapper().toStoreBody(asset))
        .bodyType(DocumentType.FORM)
        .build();
    
    return client.store().update(update) 
    .onItem().transformToUni((StoreEntity updated) -> {
      // flush cache
      client.getConfig().getCache().flush(updated.getId());
    
      // get the next state
      return get(updated.getId());
    });
  }

  @Override
  public Uni<ComposerDocumentState> update(UpdateFormRevisionEntry asset) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Uni<ComposerState> delete(String id, String version) {
    DialobAssert.notNull(id, () -> "id can't be null!");
    DialobAssert.notNull(version, () -> "version can't be null!");
    
    return client.store().query().get().onItem().transform(this::composerState)
      .onItem().transform(state -> new DeleteEntityVisitor(state, id, version).visit())
      .onItem().transformToUni(commands -> client.store().batch(commands))
      .onItem().transformToUni(deleted -> {
        // flush cache
        for(final var entity : deleted) {
          client.getConfig().getCache().flush(entity.getId());
        }
        
        return client.store().query().get().onItem().transform(this::composerState);
      });
  }

  @Override
  public Uni<ComposerDocumentState> copyAs(String id, String copyToName) {
    DialobAssert.notNull(id, () -> "id can't be null!");
    DialobAssert.notNull(copyToName, () -> "copyToName can't be null!");
    
    return client.store().query().get().onItem().transform(this::composerState)
        .onItem().transform((ComposerState state) -> new CopyAsEntityVisitor(state, id, client, copyToName).visit())
        .onItem().transformToUni(newEntity -> client.store().batch(newEntity))
        .onItem().transform(this::documentState);
  }

  @Override
  public Uni<ComposerState> importRelease(FormReleaseDocument asset) {
    DialobAssert.notNull(asset, () -> "asset can't be null!");
    return client.store().query().get().onItem().transform(this::composerState)
        .onItem().transform(state -> new ImportReleaseVisitor(state, asset, client).visit())
        .onItem().transformToUni(newEntity -> client.store().batch(newEntity))
        .onItem().transformToUni(savedEntity -> client.store().query().get().onItem().transform(this::composerState));
  }

  @Override
  public Uni<FormPutResponse> validate(FormDocument asset) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Uni<FormDocument> apply(FormCommands asset) {
    // TODO Auto-generated method stub
    return null;
  }

  private ComposerDocumentState documentState(StoreEntity newEntity) {
   return null; 
  }
  

  private ComposerDocumentState documentState(List<StoreEntity> newEntity) {
   return null; 
  }

  private ComposerState composerState(StoreState source) {
    return DialobComposerImpl.composerState(this.client, source);
  }
  
  public static ComposerState composerState(DialobClient client, StoreState source) {
    // create envir
    final var envir = ComposerEntityMapper.toEnvir(client.envir(), source).build();
    
    // map envir
    final var builder = ImmutableComposerState.builder();
    envir.getValues().values().forEach(v -> ComposerEntityMapper.toComposer(builder, v));
    final ComposerState result = builder.build();
    
    return result;
  }

}
