package io.dialob.client.spi;

import java.util.ArrayList;
import java.util.Arrays;

import io.dialob.api.form.FormPutResponse;
import io.dialob.api.form.FormValidationError;
import io.dialob.api.form.ImmutableFormPutResponse;
import io.dialob.client.api.DialobClient;
import io.dialob.client.api.DialobComposer;
import io.dialob.client.api.DialobDocument.FormDocument;
import io.dialob.client.spi.composer.CreateFormDocumentVisitor;
import io.dialob.client.spi.composer.GeComposerStateVisitor;
import io.dialob.client.spi.composer.GetComposerDocumentState;
import io.dialob.client.spi.composer.UpdateFormDocumentVisitor;
import io.dialob.client.spi.store.DialobDocumentStore;
import io.dialob.client.spi.support.DialobAssert;
import io.dialob.program.DialobFormValidator;
import io.dialob.program.FormValidatorExecutor;
import io.dialob.program.ValueSetValidator;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DialobComposerImpl implements DialobComposer {
  private final DialobClient client;  
  private final DialobDocumentStore store;

  @Override
  public DialobComposerImpl withTenantId(String tenatId) {
    return new DialobComposerImpl(client, store.withTenantId(tenatId));
  }
  @Override
  public Uni<ComposerState> get() {
    return store.getConfig().accept(new GeComposerStateVisitor());
  }
  @Override
  public Uni<ComposerDocumentState> get(String idOrName) {
    return get(idOrName, null);
  }
  @Override
  public Uni<ComposerDocumentState> get(String idOrName, String version) {
    return store.getConfig().accept(new GeComposerStateVisitor())
        .onItem().transform(state -> new GetComposerDocumentState(state, idOrName, version).get());
  }
  @Override
  public Uni<ComposerDocumentState> create(FormDocument asset) {
    return store.getConfig().accept(new GeComposerStateVisitor())
        .onItem().transformToUni(state -> store.getConfig().accept(new CreateFormDocumentVisitor(state, asset, client)))
        .onItem().transform(created -> created.iterator().next());
  }

  @Override
  public Uni<ComposerDocumentState> update(FormDocument asset) {
    DialobAssert.notNull(asset, () -> "asset can't be null!");
    DialobAssert.notNull(asset.getData().getId(), () -> "asset.value.id can't be null!");
    DialobAssert.notNull(asset.getData().getRev(), () -> "asset.value.rev can't be null!");
    
    final var visitor = new UpdateFormDocumentVisitor(asset, store);
    
    return visitor.start()
        .onItem().transform(visitor::visitEnvelope)
        .onItem().transform(updated -> {
      // flush cache
      client.getConfig().getCache().flush(updated.getForm().getId());
    
      // get the next state
      return updated;
    });
  }

  public Uni<FormPutResponse> validate(FormDocument asset) {
    final var validator = getValidator();
    final var errors = new ArrayList<FormValidationError>();
    final var updatedForm = asset.getData();
    final var includeForm = false;
    
    final ImmutableFormPutResponse.Builder putResponse = ImmutableFormPutResponse.builder().id(updatedForm.getId()).rev(updatedForm.getRev());
    
    errors.addAll(validator.validate(updatedForm));
    
    if (!errors.isEmpty()) {
      putResponse.ok(false);
      errors.forEach(putResponse::addErrors);
    } else {
      putResponse.ok(true);
    }
    if (includeForm) {
      putResponse.form(updatedForm);
    }

    // Response is still OK even if there are rule building errors as the document is still saved.
    return Uni.createFrom().item(putResponse.build());
  }


  @Override
  public Uni<FormDocument> apply(FormCommands asset) {
    throw new RuntimeException("not implemented");
  }
  @Override
  public Uni<ComposerState> delete(String id, String version) {
    throw new RuntimeException("not implemented");
  }
  @Override
  public Uni<ComposerDocumentState> copyAs(String id, String copyToName) {
    throw new RuntimeException("not implemented");    
  }
  private FormValidatorExecutor getValidator() {
    final FormValidatorExecutor validator = new FormValidatorExecutor(Arrays.asList(
        new DialobFormValidator(client.getConfig().getCompiler()),
        new ValueSetValidator()
        ));
    return validator;
  }
}
