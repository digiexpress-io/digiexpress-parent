package io.digiexpress.client.spi;

import java.util.List;
import java.util.stream.Collectors;

import io.digiexpress.client.api.ImmutableComposerDialob;
import io.digiexpress.client.api.ImmutableComposerHdes;
import io.digiexpress.client.api.ImmutableComposerMessage;
import io.digiexpress.client.api.ImmutableComposerStencil;
import io.digiexpress.client.api.ImmutableServiceComposerDefinitionState;
import io.digiexpress.client.api.ImmutableServiceComposerState;
import io.digiexpress.client.api.ServiceClient;
import io.digiexpress.client.api.ServiceComposer;
import io.digiexpress.client.api.ServiceComposerState;
import io.digiexpress.client.api.ServiceComposerState.ComposerDialob;
import io.digiexpress.client.api.ServiceComposerState.ComposerHdes;
import io.digiexpress.client.api.ServiceComposerState.ComposerStencil;
import io.digiexpress.client.api.ServiceComposerState.ServiceComposerDefinitionState;
import io.digiexpress.client.api.ServiceComposerState.SiteContentType;
import io.digiexpress.client.api.ServiceDocument.ServiceDefinitionDocument;
import io.digiexpress.client.api.ServiceStore.StoreState;
import io.digiexpress.client.spi.composer.ComposerCreateBuilderImpl;
import io.digiexpress.client.spi.store.StoreException;
import io.resys.hdes.client.api.ImmutableComposerState;
import io.resys.hdes.client.api.ImmutableStoreEntity;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.hdes.client.spi.composer.ComposerEntityMapper;
import io.resys.thena.docdb.api.models.Repo;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ServiceComposerImpl implements ServiceComposer {

  private final ServiceClient client;

  @Override
  public CreateBuilder create() {
    return new ComposerCreateBuilderImpl(client);
  }

  @Override
  public ServiceComposer.QueryBuilder query() {
    final var parent = this;
    return new ServiceComposer.QueryBuilder() {
      @Override public Uni<ServiceComposerState> release(String releaseId) { return null; }
      @Override public Uni<ServiceComposerState> head() { return client.getQuery().getRepos().onItem().transformToUni(parent::headState); }
      @Override public Uni<ServiceComposerDefinitionState> definition(String definitionId) { return client.getQuery().getServiceDef(definitionId).onItem().transformToUni(parent::defState); }
    };
  }

  private Uni<ServiceComposerDefinitionState> defState(ServiceDefinitionDocument definition) {
    final var dialob = dialobState(definition);
    final var hdes = hdesState(definition);
    final var stencil = stencilState(definition);
    return Uni.combine().all().unis(dialob, hdes, stencil).asTuple()
      .onItem().transform(tuple -> ImmutableServiceComposerDefinitionState.builder()
          .definition(definition)
          .dialob(tuple.getItem1())
          .hdes(tuple.getItem2())
          .stencil(tuple.getItem3())
          .build());
  }

  private Uni<ComposerStencil> stencilState(ServiceDefinitionDocument definition) {
    return client.getQuery().getStencil(definition.getStencil().getTagName())
        .onItem().transform(state -> ImmutableComposerStencil.builder().sites(state.getSites()).build());
  }
  
  private Uni<ComposerHdes> hdesState(ServiceDefinitionDocument definition) {
    return client.getQuery().getHdes(definition.getHdes().getTagName()).onItem()
        .transform((AstTag tag) -> {
          final var envir = client.getConfig().getHdes().envir();
          
          tag.getValues().forEach(value -> {
            final var body = ImmutableStoreEntity.builder()
                .id(value.getId())
                .hash(value.getHash())
                .bodyType(value.getBodyType())
                .body(value.getCommands())
                .build();
            
            switch (value.getBodyType()) {
            case FLOW: { envir.addCommand().id(value.getId()).flow(body).build(); break; }
            case DT: { envir.addCommand().id(value.getId()).decision(body).build(); break; }
            case FLOW_TASK: { envir.addCommand().id(value.getId()).service(body).build(); break; }
            default: return;
            }
          });
          
          final var stateBuilder = ImmutableComposerState.builder();
          envir.build().getValues().values().forEach(v -> ComposerEntityMapper.toComposer(stateBuilder, v));
          final var state = stateBuilder.build();
          return ImmutableComposerHdes.builder()
              .decisions(state.getDecisions())
              .services(state.getServices())
              .flows(state.getFlows())
              .build();
        });
    
  }  
  private Uni<ComposerDialob> dialobState(ServiceDefinitionDocument definition) {
    return Multi.createFrom().items(definition.getProcesses().stream()).onItem().transformToUni(process -> {
      final var formId = process.getFormId();
      return Uni.combine().all().unis(
          client.getQuery().getFormRev(formId).onFailure().recoverWithNull(), 
          client.getQuery().getForm(formId).onFailure().recoverWithNull()).asTuple();
    }).concatenate().collect().asList()
      .onItem().transform(formAndRev -> {
        final var usedRevs = formAndRev.stream().filter(rev -> rev.getItem1() != null)
            .collect(Collectors.toMap(rev -> rev.getItem1().getId(), rev -> rev.getItem1(), (a, b) -> b));
        final var usedForms = formAndRev.stream().filter(form -> form.getItem2() != null)
            .collect(Collectors.toMap(rev -> rev.getItem2().getId(), rev -> rev.getItem2(), (a, b) -> b));        
        return ImmutableComposerDialob.builder()
            .putAllRevs(usedRevs)
            .putAllForms(usedForms)
            .build(); 
      }); 
  }
  
  private Uni<ServiceComposerState> headState(List<Repo> repos) { 
    final var created = repos.stream()
        .filter(repo -> 
            repo.getName().equals(client.getConfig().getStore().getRepoName()) ||
            repo.getId().equals(client.getConfig().getStore().getRepoName()))
        .findFirst().isPresent();  
    
    if(created) {
      return client.getQuery().head()
          .onItem().transform(this::okState)
          .onFailure(StoreException.class).recoverWithItem(this::errorState);        
    }
    final var notCreated = ImmutableServiceComposerState.builder()
        .contentType(SiteContentType.NOT_CREATED)
        .name(client.getConfig().getStore().getRepoName())
        .build();
    return Uni.createFrom().item(notCreated);
  }

  private ServiceComposerState okState(StoreState store) {
    final var mapper = client.getConfig().getMapper();
    final var configs = store.getConfigs().values().stream()
        .collect(Collectors.toMap(e -> e.getId(), mapper::toConfig));
    final var revisions = store.getRevs().values().stream()
        .collect(Collectors.toMap(e -> e.getId(), mapper::toRev));
    final var definitions = store.getDefs().values().stream()
        .collect(Collectors.toMap(e -> e.getId(), mapper::toDef));
    
    
    return ImmutableServiceComposerState.builder()
      .name(client.getConfig().getStore().getRepoName())
      .contentType(SiteContentType.OK)
      .commit(store.getCommit())
      .commitMsg(store.getCommitMsg())
      .configs(configs)
      .revisions(revisions)
      .definitions(definitions)
      .build();
  }
  
  private ServiceComposerState errorState(Throwable e) {
    final StoreException ex = (StoreException) e;
    return ImmutableServiceComposerState.builder()
        .contentType(SiteContentType.ERRORS)
        .name(client.getConfig().getStore().getRepoName())
        .messages(ex.getMessages().stream().map(msg -> ImmutableComposerMessage.builder()
            .id(msg.getId())
            .value(msg.getValue())
            .args(msg.getArgs())
            .build())
            .collect(Collectors.toList()))
        .build();
  }
}
