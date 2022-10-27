package io.digiexpress.client.spi.builders;

import io.digiexpress.client.api.ImmutableComposerState;
import io.digiexpress.client.api.ServiceClient;
import io.digiexpress.client.api.ServiceComposer;
import io.digiexpress.client.api.ServiceComposer.ComposerState;
import io.digiexpress.client.api.ServiceComposer.CreateNewConfg;
import io.digiexpress.client.api.ServiceComposer.CreateNextProcess;
import io.digiexpress.client.api.ServiceComposer.CreateRevision;
import io.digiexpress.client.api.ServiceDocument.ProcessDocument;
import io.digiexpress.client.api.ServiceDocument.ProcessRevisionDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceConfigDocument;
import io.digiexpress.client.api.ServiceEnvir;
import io.digiexpress.client.api.ServiceStore.StoreState;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ComposerCreateBuilderImpl implements ServiceComposer.CreateBuilder {
  private final ServiceClient client;

  @Override
  public Uni<ComposerState> repo() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Uni<ProcessRevisionDocument> revision(CreateRevision init) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Uni<ProcessDocument> process(CreateNextProcess init) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Uni<ServiceConfigDocument> config(CreateNewConfg init) {
    return null;
//        client.getStore().query().get().onItem().transform(this::state)
//        .onItem().transformToUni(state -> client.getStore().create(new CreateEntityVisitor(state, asset, client).visit()))
//        .onItem().transformToUni(savedEntity -> client.store().query().get().onItem().transform(this::state));
  }

  
  private ComposerState state(StoreState source) {
    final var envir = client.envir();
    source.getRevs().values().forEach(v -> envir.addCommand().id(v.getId()).rev(v).build());
    source.getProcesses().values().forEach(v -> envir.addCommand().id(v.getId()).process(v).build());
    source.getReleases().values().forEach(v -> envir.addCommand().id(v.getId()).release(v).build());
    source.getConfigs().values().forEach(v -> envir.addCommand().id(v.getId()).config(v).build());
    
    final var nextState = ImmutableComposerState.builder();
    envir.build().getValues().values().forEach(v -> toComposer(nextState, v));
    return nextState.build(); 
  }
  
  
  public static void toComposer(ImmutableComposerState.Builder builder, ServiceEnvir.Program wrapper) {
    switch (wrapper.getSourceType()) {
    case PROCESS_DEF:

      break;
    case PROCESS_REV:
      break;
      
    case SERVICE_CONFIG:
      break;
      
    case SERVICE_RELEASE:
      break;
    default:
      break;
    }
  }
}
